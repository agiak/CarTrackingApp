-- Foundation for the Caribou backend: everything that is independent of the
-- app's own tables — the private schema, profiles, passwordless-confirmation,
-- temporary password resets and the keep-alive heartbeat.

-- ── private schema ────────────────────────────────────────────────────────
create schema if not exists private;
revoke all on schema private from public, anon;
grant usage on schema private to authenticated;

create or replace function public.set_updated_at() returns trigger
language plpgsql set search_path = '' as $$
begin new.updated_at := now(); return new; end; $$;

-- ── profiles ──────────────────────────────────────────────────────────────
create table public.profiles (
    user_id      uuid primary key references auth.users(id) on delete cascade,
    display_name text not null default '' check (length(display_name) <= 100),
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now()
);
alter table public.profiles enable row level security;

create policy "Users read their profile" on public.profiles
    for select to authenticated using (user_id = (select auth.uid()));
create policy "Users update their profile" on public.profiles
    for update to authenticated
    using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));

create trigger profiles_updated_at before update on public.profiles
    for each row execute function public.set_updated_at();

create or replace function public.handle_new_user() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
  insert into public.profiles (user_id, display_name)
  values (new.id, coalesce(new.raw_user_meta_data ->> 'display_name', ''))
  on conflict (user_id) do nothing;
  return new;
end; $$;
revoke execute on function public.handle_new_user() from public, anon, authenticated;

create trigger on_auth_user_created after insert on auth.users
    for each row execute function public.handle_new_user();

-- ── no email confirmation ─────────────────────────────────────────────────
-- Safety net behind the signup edge function, which already creates users
-- with email_confirm: true.
create or replace function private.auto_confirm_email() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
  if new.email is not null and new.email_confirmed_at is null then
    new.email_confirmed_at := now();
  end if;
  return new;
end; $$;
revoke all on function private.auto_confirm_email() from public, anon, authenticated;

create trigger auto_confirm_email before insert on auth.users
    for each row execute function private.auto_confirm_email();

-- ── temporary passwords ───────────────────────────────────────────────────
-- Only the password-reset edge function touches this, with the service role.
create table public.password_resets (
  user_id    uuid primary key references auth.users(id) on delete cascade,
  code_hash  text not null,            -- bcrypt; never the code in clear text
  expires_at timestamptz not null,
  attempts   int not null default 0,
  created_at timestamptz not null default now()
);
alter table public.password_resets enable row level security;
revoke all on public.password_resets from anon, authenticated;

-- New temporary code, at most one per minute. Returns the user and their
-- name, or nothing at all when no such account exists.
create or replace function public.request_password_reset(p_email text, p_code text)
returns table (user_id uuid, display_name text)
language plpgsql security definer set search_path = '' as $$
declare v_user uuid;
begin
  select u.id into v_user from auth.users u where lower(u.email) = lower(trim(p_email));
  if v_user is null then return; end if;
  if exists (
    select 1 from public.password_resets r
    where r.user_id = v_user and r.created_at > now() - interval '1 minute'
  ) then return; end if;
  insert into public.password_resets (user_id, code_hash, expires_at)
  values (v_user, extensions.crypt(p_code, extensions.gen_salt('bf')), now() + interval '1 hour')
  on conflict on constraint password_resets_pkey do update
    set code_hash = excluded.code_hash, expires_at = excluded.expires_at,
        attempts = 0, created_at = now();
  return query select v_user, (select p.display_name from public.profiles p where p.user_id = v_user);
end; $$;

-- Check a code: 5 attempts, one hour, single use.
create or replace function public.redeem_password_reset(p_email text, p_code text)
returns uuid language plpgsql security definer set search_path = '' as $$
declare v_user uuid; v_reset public.password_resets;
begin
  select u.id into v_user from auth.users u where lower(u.email) = lower(trim(p_email));
  if v_user is null then return null; end if;
  select * into v_reset from public.password_resets r where r.user_id = v_user for update;
  if not found or v_reset.expires_at < now() or v_reset.attempts >= 5 then return null; end if;
  if v_reset.code_hash = extensions.crypt(p_code, v_reset.code_hash) then
    delete from public.password_resets r where r.user_id = v_user;
    return v_user;
  end if;
  update public.password_resets r set attempts = r.attempts + 1 where r.user_id = v_user;
  return null;
end; $$;

revoke all on function public.request_password_reset(text, text) from public, anon, authenticated;
revoke all on function public.redeem_password_reset(text, text) from public, anon, authenticated;
grant execute on function public.request_password_reset(text, text) to service_role;
grant execute on function public.redeem_password_reset(text, text) to service_role;

-- ── keep-alive ────────────────────────────────────────────────────────────
-- A free project is paused after 7 days without activity; a daily GitHub
-- Action calls public.keep_alive() to stop that happening.
create table private.keep_alive (
  id smallint primary key default 1 check (id = 1),
  last_ping timestamptz not null default now(),
  pings bigint not null default 0
);
alter table private.keep_alive enable row level security;

create or replace function public.keep_alive() returns timestamptz
language sql security definer set search_path = '' as $$
  insert into private.keep_alive (id, last_ping, pings) values (1, now(), 1)
  on conflict (id) do update set last_ping = now(), pings = private.keep_alive.pings + 1
  returning last_ping;
$$;
revoke all on function public.keep_alive() from public, authenticated;
grant execute on function public.keep_alive() to anon;
