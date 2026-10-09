create table public.zink_sync (
    user_id uuid primary key references auth.users(id) on delete cascade,
    revision bigint not null default 1 check (revision > 0),
    payload text not null check (octet_length(payload) <= 12582912),
    device_id text not null,
    updated_at timestamptz not null default now()
);
alter table public.zink_sync enable row level security;
revoke all on public.zink_sync from anon;
grant select, insert, update on public.zink_sync to authenticated;
create policy zink_sync_select on public.zink_sync for select to authenticated
    using ((select auth.uid()) = user_id);
create policy zink_sync_insert on public.zink_sync for insert to authenticated
    with check ((select auth.uid()) = user_id);
create policy zink_sync_update on public.zink_sync for update to authenticated
    using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);

-- Compare-and-swap prevents two devices from overwriting an unseen snapshot.
-- SECURITY INVOKER keeps all access subject to the caller's RLS policies.
create function public.zink_sync_commit(expected_revision bigint, new_payload text, new_device_id text)
returns bigint language plpgsql security invoker set search_path = '' as $$
declare result_revision bigint;
begin
    if auth.uid() is null then
        raise exception 'Sign in to ZApps first' using errcode = '42501';
    end if;
    if expected_revision = 0 then
        insert into public.zink_sync(user_id, payload, device_id)
        values (auth.uid(), new_payload, new_device_id)
        on conflict (user_id) do nothing
        returning revision into result_revision;
    else
        update public.zink_sync
        set payload = new_payload, device_id = new_device_id,
            revision = revision + 1, updated_at = now()
        where user_id = auth.uid() and revision = expected_revision
        returning revision into result_revision;
    end if;
    return coalesce(result_revision, 0);
end;
$$;
revoke all on function public.zink_sync_commit(bigint, text, text) from public, anon;
grant execute on function public.zink_sync_commit(bigint, text, text) to authenticated;
notify pgrst, 'reload schema';
