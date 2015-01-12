alter table `image` add column `meta_id` int(32) NOT NULL;
alter table `image` add column `meta_type` int(32) NOT NULL;
alter table `image` add column `generated_time` bigint NOT NULL DEFAULT 0;
UPDATE `image` as t1 INNER JOIN `user` AS t2 ON t1.id=t2.avatar SET t1.meta_type=1;
UPDATE `image` as t1 INNER JOIN `activity_image_relation` AS t2 ON t1.id=t2.image_id SET t1.meta_id=t2.activity_id, t1.meta_type=2;
