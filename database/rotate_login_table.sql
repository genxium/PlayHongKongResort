delete from `login` where `timestamp` <	(UNIX_TIMESTAMP()*1000 - 604800000);
