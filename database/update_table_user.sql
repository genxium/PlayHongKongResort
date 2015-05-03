START TRANSACTION;
alter table `user` modify column gender varchar(16) not null default "";
alter table `user` add column age varchar(16) not null default "";
alter table `user` add column mood varchar(64) not null default "";
COMMIT;
