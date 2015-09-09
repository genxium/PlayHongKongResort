delete from `image` where `meta_type`=0 and `generated_time` <	(UNIX_TIMESTAMP()*1000 - 1800000);
