-- =============================================================================
--  nayaMDCAT — Supabase access policies for the mobile app
-- =============================================================================
--  The Android app talks to Supabase with the PUBLIC "anon" key (it ships inside
--  every APK). With Row-Level Security (RLS) enabled, the anon role needs
--  explicit policies or every write is rejected — which shows up in the app as
--  "registration not saved to the database" and "image upload keeps buffering".
--
--  Login already works, which means anon SELECT is allowed but anon INSERT/UPDATE
--  and Storage uploads are not. Run this in:  Supabase Dashboard → SQL Editor.
--
--  ⚠️  SECURITY NOTE: granting the anon role blanket read/write access to the
--  users table is INSECURE — anyone who extracts the anon key from the APK can
--  read and modify all rows (including password fields). This matches the app's
--  current client-side auth design; see the "Hardening" note at the bottom.
-- =============================================================================


-- 1) USERS TABLE  — registration (INSERT), login (SELECT), profile edits (UPDATE)
-- -----------------------------------------------------------------------------
alter table public.users enable row level security;

drop policy if exists "app anon select users" on public.users;
drop policy if exists "app anon insert users" on public.users;
drop policy if exists "app anon update users" on public.users;

create policy "app anon select users" on public.users
  for select to anon using (true);
create policy "app anon insert users" on public.users
  for insert to anon with check (true);
create policy "app anon update users" on public.users
  for update to anon using (true) with check (true);


-- 2) OTHER APP TABLES  — the app reads AND writes these too. Apply the same
--    pattern to each one that exists in your project. Delete any that you do
--    not have. (Table names follow the app's REST calls.)
-- -----------------------------------------------------------------------------
do $$
declare
  t text;
  app_tables text[] := array[
    'subjects', 'topics', 'mcqs', 'test_results', 'user_progress',
    'notes', 'note_access_requests', 'posts', 'comments', 'post_likes',
    'post_shares', 'financial_transactions', 'push_notifications',
    'pending_mcqs', 'about_us', 'student_fees', 'app_settings',
    'announcements', 'reward_offers', 'reward_claims'
  ];
begin
  foreach t in array app_tables loop
    if exists (select 1 from information_schema.tables
               where table_schema = 'public' and table_name = t) then
      execute format('alter table public.%I enable row level security;', t);
      execute format('drop policy if exists "app anon all %1$s" on public.%1$I;', t);
      execute format(
        'create policy "app anon all %1$s" on public.%1$I '
        || 'for all to anon using (true) with check (true);', t);
    end if;
  end loop;
end $$;


-- 3) STORAGE  — the app uploads images to a PUBLIC bucket named "public-assets"
--    (profile pictures, notes, feed images, announcements, etc.).
-- -----------------------------------------------------------------------------
insert into storage.buckets (id, name, public)
values ('public-assets', 'public-assets', true)
on conflict (id) do update set public = true;

drop policy if exists "app anon read public-assets"   on storage.objects;
drop policy if exists "app anon upload public-assets"  on storage.objects;
drop policy if exists "app anon update public-assets"  on storage.objects;

create policy "app anon read public-assets" on storage.objects
  for select to anon using (bucket_id = 'public-assets');
create policy "app anon upload public-assets" on storage.objects
  for insert to anon with check (bucket_id = 'public-assets');
create policy "app anon update public-assets" on storage.objects
  for update to anon using (bucket_id = 'public-assets') with check (bucket_id = 'public-assets');


-- =============================================================================
--  Hardening (recommended, later): the app currently stores passwords in the
--  users table and authenticates on the client with the anon key. For a
--  production app, migrate to Supabase Auth (GoTrue) so credentials are never
--  exposed, then tighten these policies to `auth.uid()`-scoped rules. Ask and
--  I can help plan that migration.
-- =============================================================================
