# send-push

Sends one announcement to every device that has an account signed in.

## What has to exist first

**1. `app/google-services.json`** — download it from the Firebase console
(Project settings → Your apps → Android) and put it at `app/google-services.json`.
It is a client config, not a secret: the same values ship inside every APK, and
the Supabase publishable key is already committed next to it. **Commit it** —
without it the `com.google.gms.google-services` plugin fails the build, which
includes the GitHub Actions build.

**2. Two edge-function secrets.**

`FIREBASE_SERVICE_ACCOUNT` is the whole service-account JSON, verbatim.
Firebase console → Project settings → Service accounts → *Generate new private
key*. This one **is** a secret — it can send push as your project, so it never
goes in the repo.

`PUSH_ADMIN_KEY` is whatever you choose; it is the only thing standing between
the internet and your users' notification tray, so generate it, do not invent
it:

```bash
openssl rand -base64 32
```

Set both:

```bash
supabase secrets set --project-ref gqinaapwmxkvizehseag \
  PUSH_ADMIN_KEY='<the key you just generated>'

supabase secrets set --project-ref gqinaapwmxkvizehseag \
  FIREBASE_SERVICE_ACCOUNT="$(cat ~/Downloads/<service-account>.json)"
```

Or paste them in the dashboard: Edge Functions → Secrets.

## Sending

Open **`tools/push-admin.html`** in a browser — double-click it, `file://` is
fine. It is one static page with no build and nothing running: a title, a body,
a preview of what the notification will look like, and three buttons. Paste the
admin key once and the browser remembers it.

The three buttons, in the order you should press them:

1. **Πόσες συσκευές;** — asks how many devices this would reach and sends
   nothing (`dry_run`). Also the cheapest way to prove the whole chain works.
2. **Δοκιμή σε εμένα** — sends only to the devices of one account, by email.
3. **Αποστολή σε όλους** — everyone. Asks for confirmation first, because this
   one cannot be taken back.

Or by hand:

```bash
curl -X POST \
  'https://gqinaapwmxkvizehseag.supabase.co/functions/v1/send-push' \
  -H 'Content-Type: application/json' \
  -H 'x-admin-key: <PUSH_ADMIN_KEY>' \
  -d '{"title":"Caribou 1.2 is out","body":"Trips now sync between phones."}'
```

```json
{ "audience": "all", "devices": 12, "sent": 11, "failed": 0, "pruned": 1 }
```

- `devices` — how many devices were in the audience
- `sent` — accepted by Firebase
- `pruned` — tokens Firebase rejected as dead (uninstalled app); the rows are
  deleted, so the count shrinks on its own rather than rotting
- `failed` — temporary failures, not retried; send again if it is not zero

| field | | |
|---|---|---|
| `title` | required | ≤120 chars |
| `body` | optional | ≤1000 chars |
| `only_email` | optional | send only to that account's devices |
| `dry_run` | optional | count the audience, send nothing |

## Things worth knowing before you rely on it

**The function is public.** `verify_jwt` is off, because the caller is you with
curl, not a signed-in user. `x-admin-key` is the whole of the authentication.
If it leaks, rotate it — one `supabase secrets set` and the old one is dead.

**Messages are data-only.** That is what lets the app draw them itself and file
them in its in-app notification history. The cost: a device whose app has been
*force-stopped* by the user will not receive them until the app is opened
again. Notifications sent from the Firebase console UI are the other kind and
will appear, but will not reach the history.

**One HTTP call per device.** FCM v1 has no multicast. Twenty go out at a time,
which is comfortable into the low thousands of devices within one invocation.
Past that, move to an FCM *topic*: every device subscribes on sign-in and one
API call reaches all of them — at the cost of no longer knowing who is on the
list or being able to target one person.

**The admin key sits in the browser, not in the app.** It is deliberately not
shipped anywhere near the APK: a key inside an installed app is a key anybody
with the APK can pull out, and this one opens every user's notification tray.
If you later want to send from the phone instead, that is a different design —
mark an account as admin server-side and have the function check the caller's
JWT, so there is no shared secret in a client at all.

**Nothing can read `device_tokens` from the client.** The table has RLS on,
zero policies, and no grants: devices register through
`register_device_token()` / `unregister_device_token()`, which are
`security definer`. That is deliberate rather than paranoid — a plain upsert
would need an UPDATE policy loose enough to let any signed-in user claim any
token row, and claiming someone else's token points their phone at your
notifications.
