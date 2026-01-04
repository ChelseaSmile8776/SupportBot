create table admin_groups (
  id bigserial primary key,
  chat_id bigint not null unique,
  title text,
  owner_telegram_user_id bigint,
  public_code varchar(32) not null unique,

  admin_chat_topic_thread_id int,
  admins_topic_thread_id int,
  stats_topic_thread_id int,

  pinned_admins_message_id int,
  pinned_stats_message_id int,

  created_at timestamptz not null default now()
);

create table user_profiles (
  id bigserial primary key,
  telegram_user_id bigint not null unique,
  username text,
  first_name text,

  active_admin_group_id bigint references admin_groups(id) on delete set null,

  last_menu_message_id int,

  pending_switch_admin_group_id bigint references admin_groups(id) on delete set null,
  pending_switch_until timestamptz
);

create table support_memberships (
  id bigserial primary key,
  user_profile_id bigint not null references user_profiles(id) on delete cascade,
  admin_group_id bigint not null references admin_groups(id) on delete cascade,
  created_at timestamptz not null default now(),
  last_used_at timestamptz not null default now(),
  unique(user_profile_id, admin_group_id)
);

create table group_admins (
  id bigserial primary key,
  admin_group_id bigint not null references admin_groups(id) on delete cascade,
  telegram_user_id bigint not null,
  role varchar(16) not null,
  rating_avg numeric(4,2),
  rating_count int not null default 0,
  unique(admin_group_id, telegram_user_id)
);

create table tickets (
  id bigserial primary key,
  admin_group_id bigint not null references admin_groups(id) on delete cascade,

  client_telegram_user_id bigint not null,
  assigned_admin_telegram_user_id bigint,

  status varchar(16) not null,
  category varchar(32) not null,

  forum_chat_id bigint not null,
  message_thread_id int,

  rating int,
  created_at timestamptz not null default now(),
  closed_at timestamptz
);

create index ix_tickets_group_status on tickets(admin_group_id, status);
create index ix_tickets_group_client on tickets(admin_group_id, client_telegram_user_id);
create index ix_tickets_group_thread on tickets(forum_chat_id, message_thread_id);