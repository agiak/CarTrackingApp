import { createClient } from "npm:@supabase/supabase-js@2.45.4";

// "I forgot my password", in two halves.
//
//   request → mint a temporary password, store only its bcrypt hash, mail the
//             plain one to the address
//   redeem  → check the temporary password and make it the real one, flagged
//             so the app forces a change on the next sign-in
//
// Neither half ever says whether an email address has an account. The request
// half always answers ok, and the redeem half gives the same "invalid" answer
// for a wrong code, an expired code and an address nobody registered — so
// this endpoint cannot be used to find out who has an account.

// No 0/O/1/l/I: the code is read off a phone screen and typed into another.
const ALPHABET = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
const CODE_LENGTH = 10;
const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const APP_NAME = "Caribou";

function reply(status: number, body: Record<string, unknown>): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

/**
 * Rejection sampling rather than `% ALPHABET.length`: the modulo would make
 * the first few letters of the alphabet slightly likelier, which is exactly
 * the sort of bias that makes a short code easier to guess.
 */
function generateCode(): string {
  const max = Math.floor(256 / ALPHABET.length) * ALPHABET.length;
  const out: string[] = [];
  const buffer = new Uint8Array(CODE_LENGTH * 2);
  while (out.length < CODE_LENGTH) {
    crypto.getRandomValues(buffer);
    for (const byte of buffer) {
      if (byte < max) {
        out.push(ALPHABET[byte % ALPHABET.length]);
        if (out.length === CODE_LENGTH) break;
      }
    }
  }
  return out.join("");
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function emailBody(code: string, name: string, greek: boolean) {
  const hello = name ? (greek ? `Γεια σου ${name},` : `Hi ${name},`) : (greek ? "Γεια σου," : "Hi,");
  const subject = greek
    ? `${APP_NAME}: προσωρινός κωδικός`
    : `${APP_NAME}: temporary password`;
  const lines = greek
    ? [
      hello,
      `Ο προσωρινός κωδικός σου για το ${APP_NAME} είναι:`,
      code,
      "Ισχύει για μία ώρα και μπορεί να χρησιμοποιηθεί μία μόνο φορά. Μόλις συνδεθείς θα σου ζητηθεί να ορίσεις νέο κωδικό.",
      "Ο παλιός σου κωδικός συνεχίζει να δουλεύει μέχρι να χρησιμοποιήσεις τον προσωρινό.",
      "Αν δεν ζήτησες εσύ αυτό το email, αγνόησέ το.",
    ]
    : [
      hello,
      `Your temporary password for ${APP_NAME} is:`,
      code,
      "It is valid for one hour and can be used once. After you sign in you will be asked to choose a new password.",
      "Your old password keeps working until the temporary one is used.",
      "If you did not ask for this email, you can ignore it.",
    ];

  const text = lines.join("\n\n");
  const html = `<!doctype html><html><body style="font-family:system-ui,-apple-system,Segoe UI,Roboto,sans-serif;font-size:15px;line-height:1.5;color:#1b1b1b">
<p>${escapeHtml(lines[0])}</p>
<p>${escapeHtml(lines[1])}</p>
<p style="font-size:26px;font-weight:700;letter-spacing:2px;font-family:ui-monospace,SFMono-Regular,Menlo,monospace">${escapeHtml(code)}</p>
<p>${escapeHtml(lines[3])}</p>
<p>${escapeHtml(lines[4])}</p>
<p style="color:#6b6b6b;font-size:13px">${escapeHtml(lines[5])}</p>
</body></html>`;

  return { subject, text, html };
}

async function sendEmail(to: string, code: string, name: string, greek: boolean): Promise<boolean> {
  const apiKey = Deno.env.get("BREVO_API_KEY");
  const sender = Deno.env.get("EMAIL_SENDER_ADDRESS");
  const senderName = Deno.env.get("EMAIL_SENDER_NAME") ?? APP_NAME;
  if (!apiKey || !sender) return false;

  const { subject, text, html } = emailBody(code, name, greek);
  const response = await fetch("https://api.brevo.com/v3/smtp/email", {
    method: "POST",
    headers: {
      "api-key": apiKey,
      "Content-Type": "application/json",
      "Accept": "application/json",
    },
    body: JSON.stringify({
      sender: { email: sender, name: senderName },
      to: [{ email: to }],
      subject,
      htmlContent: html,
      textContent: text,
    }),
  });
  return response.ok;
}

Deno.serve(async (req: Request) => {
  if (req.method !== "POST") {
    return reply(405, { error_code: "method_not_allowed", msg: "Use POST" });
  }

  let body: Record<string, unknown>;
  try {
    body = await req.json();
  } catch {
    return reply(400, { error_code: "validation_failed", msg: "Invalid JSON" });
  }

  const url = Deno.env.get("SUPABASE_URL");
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !serviceKey) {
    return reply(500, { error_code: "unexpected_failure", msg: "Server is not configured" });
  }
  const admin = createClient(url, serviceKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });

  const action = String(body.action ?? "");
  const email = String(body.email ?? "").trim().toLowerCase();
  if (!EMAIL.test(email)) {
    return reply(400, { error_code: "email_address_invalid", msg: "Invalid email address" });
  }

  // ── request ─────────────────────────────────────────────────────────────
  if (action === "request") {
    const greek = String(body.lang ?? "en").toLowerCase().startsWith("el");
    const code = generateCode();

    const { data, error } = await admin.rpc("request_password_reset", {
      p_email: email,
      p_code: code,
    });
    if (error) {
      return reply(500, { error_code: "unexpected_failure", msg: "Could not start the reset" });
    }

    // No rows means no such account, or one asked again within the minute.
    // Both answer ok, so neither is distinguishable from success.
    const row = Array.isArray(data) ? data[0] : data;
    if (!row) return reply(200, { ok: true });

    const sent = await sendEmail(email, code, String(row.display_name ?? ""), greek);
    if (!sent) {
      // The code is already stored, so this is the one case worth reporting:
      // staying silent would leave the user waiting for a mail that is never
      // coming.
      return reply(502, { error_code: "email_send_failed", msg: "Could not send the email" });
    }
    return reply(200, { ok: true });
  }

  // ── redeem ──────────────────────────────────────────────────────────────
  if (action === "redeem") {
    const password = String(body.password ?? "");
    if (!password) {
      return reply(400, { error_code: "invalid_credentials", msg: "Invalid code" });
    }

    const { data, error } = await admin.rpc("redeem_password_reset", {
      p_email: email,
      p_code: password,
    });
    if (error) {
      return reply(500, { error_code: "unexpected_failure", msg: "Could not check the code" });
    }

    const userId = typeof data === "string" ? data : null;
    if (!userId) {
      return reply(400, { error_code: "invalid_credentials", msg: "Invalid code" });
    }

    const { error: updateError } = await admin.auth.admin.updateUserById(userId, {
      password,
      user_metadata: { must_change_password: true },
    });
    if (updateError) {
      return reply(500, { error_code: "unexpected_failure", msg: "Could not set the password" });
    }
    return reply(200, { ok: true });
  }

  return reply(400, { error_code: "validation_failed", msg: "Unknown action" });
});
