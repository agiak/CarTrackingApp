-- Row level security for the five synced tables.
--
-- One policy set per operation, generated in a loop because they are identical:
-- a row belongs to exactly one user and only that user may touch it. No helper
-- function and no `security definer` is needed, unlike NetworthApp's
-- household membership check, because there is nothing to look up.
--
-- `(select auth.uid())` rather than a bare `auth.uid()`: wrapped in a subquery
-- Postgres evaluates it once for the whole statement instead of once per row,
-- which on a first upload of a few thousand rows is the difference between
-- fast and noticeable.

alter table public.cars enable row level security;
alter table public.trips enable row level security;
alter table public.fuel_refills enable row level security;
alter table public.expenses enable row level security;
alter table public.expense_categories enable row level security;

do $$
declare
    t text;
begin
    foreach t in array array['cars', 'trips', 'fuel_refills', 'expenses', 'expense_categories'] loop
        execute format(
            'create policy "Owner reads %1$s" on public.%1$I for select to authenticated
                 using ((select auth.uid()) = user_id)', t);
        execute format(
            'create policy "Owner adds %1$s" on public.%1$I for insert to authenticated
                 with check ((select auth.uid()) = user_id)', t);
        execute format(
            'create policy "Owner changes %1$s" on public.%1$I for update to authenticated
                 using ((select auth.uid()) = user_id)
                 with check ((select auth.uid()) = user_id)', t);
        execute format(
            'create policy "Owner deletes %1$s" on public.%1$I for delete to authenticated
                 using ((select auth.uid()) = user_id)', t);
        execute format('revoke all on public.%I from anon', t);
    end loop;
end;
$$;
