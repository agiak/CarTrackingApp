-- Looks up one account's devices by email.
--
-- This exists so an announcement can be rehearsed on a single phone before it
-- reaches everybody. Sending to every user is not undoable — a typo lands in
-- thousands of notification trays and stays there — so the send tool needs a
-- way to aim at one device first, and aiming needs this lookup.
--
-- It has to be a definer function rather than a join in the edge function:
-- auth.users is outside the schemas PostgREST exposes, so the service role
-- cannot reach it over the REST API at all.
--
-- Execute is revoked from everyone and granted back only to service_role, so
-- the only caller is an edge function. A signed-in user calling this would be
-- able to confirm whether a given email has the app installed.

create or replace function public.device_tokens_for_email(p_email text)
returns setof text
language sql
security definer
set search_path = ''
as $$
    select t.token
      from public.device_tokens t
      join auth.users u on u.id = t.user_id
     where lower(u.email) = lower(trim(coalesce(p_email, '')))
       and coalesce(trim(p_email), '') <> '';
$$;

revoke all on function public.device_tokens_for_email(text) from public, anon, authenticated;
grant execute on function public.device_tokens_for_email(text) to service_role;
