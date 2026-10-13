import { createClient } from "npm:@supabase/supabase-js@2.45.4";

// Deletes the caller's own account, and with it every row they own — the
// `on delete cascade` on each table's user_id does the rest.
//
// The caller is identified from their access token rather than from anything
// in the body, so this cannot be pointed at somebody else's account.

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

  const token = (req.headers.get("Authorization") ?? "").replace(/^Bearer\s+/i, "").trim();
  if (!token) {
    return reply(401, { error_code: "no_authorization", msg: "Sign in first" });
  }

  const url = Deno.env.get("SUPABASE_URL");
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !serviceKey) {
    return reply(500, { error_code: "unexpected_failure", msg: "Server is not configured" });
  }

  const admin = createClient(url, serviceKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });

  // The function is deployed with --no-verify-jwt, so the gateway has not
  // checked anything: this is the only thing standing between a stray request
  // and a deleted account.
  const { data, error } = await admin.auth.getUser(token);
  if (error || !data.user) {
    return reply(401, { error_code: "bad_jwt", msg: "Session expired" });
  }

  const { error: deleteError } = await admin.auth.admin.deleteUser(data.user.id);
  if (deleteError) {
    return reply(500, { error_code: "unexpected_failure", msg: "Could not delete the account" });
  }

  return reply(200, { ok: true });
});
