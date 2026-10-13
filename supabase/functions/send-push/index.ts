import { createClient } from "npm:@supabase/supabase-js@2.45.4";

// Sends one message to every registered device.
//
// It talks to FCM's HTTP v1 API, which is the only one still accepting new
// projects and which authenticates with a short-lived OAuth token rather than
// a server key — so the service account JSON lives here as a secret and the
// JWT exchange below is the price of admission.
//
// Messages are DATA-ONLY. A `notification` block would be drawn by the system
// while the app is backgrounded, which means the app never sees the message,
// never files it in its own notification history, and has no say over how it
// looks. The cost is that a force-stopped app misses it; for announcements,
// that is the right side of the trade.
//
// Required secrets:
//   PUSH_ADMIN_KEY            — the shared key callers must present
//   FIREBASE_SERVICE_ACCOUNT  — the whole service-account JSON, verbatim

const FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
const PAGE = 1000;
const CONCURRENCY = 20;

interface ServiceAccount {
  project_id: string;
  client_email: string;
  private_key: string;
}

function reply(status: number, body: Record<string, unknown>): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

/** Length-independent compare, so the key cannot be guessed a byte at a time. */
function secretsMatch(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i++) diff |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return diff === 0;
}

function base64url(bytes: Uint8Array): string {
  let binary = "";
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function pemToDer(pem: string): ArrayBuffer {
  const body = pem
    .replace("-----BEGIN PRIVATE KEY-----", "")
    .replace("-----END PRIVATE KEY-----", "")
    .replace(/\s+/g, "");
  const raw = atob(body);
  const out = new Uint8Array(raw.length);
  for (let i = 0; i < raw.length; i++) out[i] = raw.charCodeAt(i);
  return out.buffer;
}

/** Signs a JWT with the service account key and trades it for an access token. */
async function accessTokenFor(account: ServiceAccount): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  const encoder = new TextEncoder();
  const segment = (value: unknown) =>
    base64url(encoder.encode(JSON.stringify(value)));

  const unsigned = [
    segment({ alg: "RS256", typ: "JWT" }),
    segment({
      iss: account.client_email,
      scope: FCM_SCOPE,
      aud: "https://oauth2.googleapis.com/token",
      iat: now,
      exp: now + 3600,
    }),
  ].join(".");

  const key = await crypto.subtle.importKey(
    "pkcs8",
    pemToDer(account.private_key),
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"],
  );
  const signature = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    key,
    encoder.encode(unsigned),
  );
  const assertion = `${unsigned}.${base64url(new Uint8Array(signature))}`;

  const response = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion,
    }),
  });
  const payload = await response.json();
  if (!response.ok) {
    throw new Error(`OAuth exchange failed: ${JSON.stringify(payload)}`);
  }
  return payload.access_token as string;
}

type SendOutcome = "sent" | "stale" | "failed";

async function sendOne(
  token: string,
  projectId: string,
  bearer: string,
  data: Record<string, string>,
): Promise<SendOutcome> {
  const response = await fetch(
    `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${bearer}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: {
          token,
          data,
          // Without HIGH, a data-only message can sit until the device next
          // wakes up, which for an announcement may be hours.
          android: { priority: "HIGH" },
        },
      }),
    },
  );

  if (response.ok) return "sent";

  // 404 UNREGISTERED means the app was uninstalled; 400 usually means the
  // token is malformed. Both are permanent, so the row goes rather than
  // being retried on every future send.
  if (response.status === 404 || response.status === 400) return "stale";
  return "failed";
}

Deno.serve(async (req: Request) => {
  if (req.method !== "POST") {
    return reply(405, { error_code: "method_not_allowed", msg: "Use POST" });
  }

  const adminKey = Deno.env.get("PUSH_ADMIN_KEY");
  const rawAccount = Deno.env.get("FIREBASE_SERVICE_ACCOUNT");
  const url = Deno.env.get("SUPABASE_URL");
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!adminKey || !rawAccount || !url || !serviceKey) {
    return reply(500, {
      error_code: "unexpected_failure",
      msg: "Server is not configured",
    });
  }

  const presented = req.headers.get("x-admin-key") ?? "";
  if (!secretsMatch(presented, adminKey)) {
    return reply(401, { error_code: "unauthorized", msg: "Bad admin key" });
  }

  let body: Record<string, unknown>;
  try {
    body = await req.json();
  } catch {
    return reply(400, { error_code: "validation_failed", msg: "Invalid JSON" });
  }

  const title = String(body.title ?? "").trim();
  const message = String(body.body ?? "").trim();
  if (!title || title.length > 120) {
    return reply(400, {
      error_code: "validation_failed",
      msg: "title is required, 120 characters or fewer",
    });
  }
  if (message.length > 1000) {
    return reply(400, {
      error_code: "validation_failed",
      msg: "body must be 1000 characters or fewer",
    });
  }

  let account: ServiceAccount;
  try {
    account = JSON.parse(rawAccount);
  } catch {
    return reply(500, {
      error_code: "unexpected_failure",
      msg: "FIREBASE_SERVICE_ACCOUNT is not valid JSON",
    });
  }

  const admin = createClient(url, serviceKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });

  // Every registered device. PostgREST caps a response at 1000 rows, so this
  // pages rather than quietly notifying only the first thousand people.
  const tokens: string[] = [];
  for (let from = 0; ; from += PAGE) {
    const { data, error } = await admin
      .from("device_tokens")
      .select("token")
      .order("token", { ascending: true })
      .range(from, from + PAGE - 1);
    if (error) {
      return reply(500, { error_code: "unexpected_failure", msg: error.message });
    }
    tokens.push(...(data ?? []).map((row) => row.token as string));
    if (!data || data.length < PAGE) break;
  }

  if (tokens.length === 0) {
    return reply(200, { devices: 0, sent: 0, failed: 0, pruned: 0 });
  }

  let bearer: string;
  try {
    bearer = await accessTokenFor(account);
  } catch (error) {
    return reply(500, {
      error_code: "unexpected_failure",
      msg: error instanceof Error ? error.message : "OAuth failed",
    });
  }

  const payload: Record<string, string> = { title, body: message };

  let sent = 0;
  let failed = 0;
  const stale: string[] = [];

  // A fixed number of workers walking one shared queue: enough parallelism to
  // be quick, not so much that FCM starts rate limiting us.
  let next = 0;
  const workers = Array.from(
    { length: Math.min(CONCURRENCY, tokens.length) },
    async () => {
      while (true) {
        const index = next++;
        if (index >= tokens.length) return;
        const token = tokens[index];
        try {
          const outcome = await sendOne(token, account.project_id, bearer, payload);
          if (outcome === "sent") sent++;
          else if (outcome === "stale") stale.push(token);
          else failed++;
        } catch {
          failed++;
        }
      }
    },
  );
  await Promise.all(workers);

  if (stale.length > 0) {
    await admin.from("device_tokens").delete().in("token", stale);
  }

  return reply(200, {
    devices: tokens.length,
    sent,
    failed,
    pruned: stale.length,
  });
});
