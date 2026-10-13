import { createClient } from "npm:@supabase/supabase-js@2.45.4";

// Creates an account that is immediately usable: no confirmation mail, no
// "check your inbox" dead end. That needs admin rights, which is why this is
// an edge function and not a call the app makes for itself — the service-role
// key lives here and never ships inside the APK.

const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_PASSWORD = 6;
// Supabase hashes with bcrypt, which silently ignores anything past 72 bytes.
const MAX_PASSWORD = 72;

function reply(status: number, body: Record<string, unknown>): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
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

  const email = String(body.email ?? "").trim().toLowerCase();
  const password = String(body.password ?? "");
  const displayName = String(body.display_name ?? "").trim().slice(0, 100);

  if (email.length > 254 || !EMAIL.test(email)) {
    return reply(400, { error_code: "email_address_invalid", msg: "Invalid email address" });
  }
  if (password.length < MIN_PASSWORD || password.length > MAX_PASSWORD) {
    return reply(400, {
      error_code: "weak_password",
      msg: `Password must be ${MIN_PASSWORD}-${MAX_PASSWORD} characters`,
    });
  }

  const url = Deno.env.get("SUPABASE_URL");
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !serviceKey) {
    return reply(500, { error_code: "unexpected_failure", msg: "Server is not configured" });
  }

  const admin = createClient(url, serviceKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });

  const { data, error } = await admin.auth.admin.createUser({
    email,
    password,
    email_confirm: true,
    user_metadata: { display_name: displayName },
  });

  if (error) {
    // Pass Supabase's own code through so the app can tell "this email is
    // taken" from "that password is too weak" without parsing prose.
    const status = error.status && error.status >= 400 && error.status < 600 ? error.status : 500;
    return reply(status, { error_code: error.code ?? "unexpected_failure", msg: error.message });
  }

  return reply(200, { id: data.user?.id });
});
