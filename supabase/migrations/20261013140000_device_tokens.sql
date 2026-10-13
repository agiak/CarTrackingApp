-- The devices the server may push to.
--
-- The row is keyed on the Firebase token, not on the user, because the token
-- identifies an *installation*. One phone has one row whoever is signed into
-- it, so a second account signing in moves the row rather than adding a second
-- one — otherwise the previous owner would keep receiving notifications on a
-- device that is no longer theirs.
--
-- Nothing here is reachable from the client directly: the table has RLS on and
-- no policies at all, and every grant is revoked. Devices are registered
-- through the two security definer functions below. That is not belt and
-- braces, it is the only way the move above can be *safe*: a plain upsert
-- would need an UPDATE policy permissive enough to let any authenticated user
-- claim any token row, and claiming someone else's token means their phone
-- starts receiving notifications addressed to you.

create table public.device_tokens (
    token      text primary key check (length(token) between 1 and 4096),
    user_id    uuid not null references auth.users(id) on delete cascade,
    platform   text not null default 'android' check (platform in ('android', 'ios')),
    updated_at bigint not null default 0
);

-- Every send reads the whole table, but a user's own devices are looked up
-- when their account is deleted, and that is what cascades.
create index device_tokens_user_idx on public.device_tokens(user_id);

alter table public.device_tokens enable row level security;

revoke all on public.device_tokens from anon, authenticated;

-- ── registering ───────────────────────────────────────────────────────────
-- Knowing a token is proof of holding the device, which is what makes the
-- takeover on conflict legitimate here and illegitimate from a raw upsert.

create or replace function public.register_device_token(
    p_token text,
    p_platform text default 'android'
)
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
    if auth.uid() is null then
        raise exception 'not authenticated' using errcode = '28000';
    end if;
    if p_token is null or length(p_token) not between 1 and 4096 then
        raise exception 'invalid token' using errcode = '22023';
    end if;

    insert into public.device_tokens (token, user_id, platform, updated_at)
    values (
        p_token,
        auth.uid(),
        case when p_platform in ('android', 'ios') then p_platform else 'android' end,
        (extract(epoch from now()) * 1000)::bigint
    )
    on conflict (token) do update
        set user_id    = auth.uid(),
            platform   = excluded.platform,
            updated_at = excluded.updated_at;
end;
$$;

-- Scoped to the caller's own row on purpose: signing out should silence this
-- phone, never anybody else's.
create or replace function public.unregister_device_token(p_token text)
returns void
language sql
security definer
set search_path = ''
as $$
    delete from public.device_tokens
     where token = p_token
       and user_id = auth.uid();
$$;

revoke all on function public.register_device_token(text, text) from public, anon;
revoke all on function public.unregister_device_token(text) from public, anon;
grant execute on function public.register_device_token(text, text) to authenticated;
grant execute on function public.unregister_device_token(text) to authenticated;
