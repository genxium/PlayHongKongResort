alter table activity add column priority int not null default 0;
alter table activity add column order_mask int not null default 0;
alter table activity add column filter_mask int not null default 0;
create index `created_time_index` on `activity`(`created_time`);
create index `accepted_time_index` on `activity`(`last_accepted_time`);
create index `priority_index` on `activity`(`priority`);
create index `order_mask_index` on `activity`(`order_mask`);

