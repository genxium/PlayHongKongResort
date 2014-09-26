DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) DEFAULT NULL,
  `display_name` varchar(32) DEFAULT NULL,
  `password` varchar(32) NOT NULL,
  `email` varchar(32) NOT NULL,
  `group_id` int(2) NOT NULL DEFAULT '0',
  `authentication_status` int(2) NOT NULL DEFAULT '0',
  `gender` int(1) NOT NULL DEFAULT '0',
  `avatar` int(32) NOT NULL DEFAULT '0',
  `last_logged_in_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_logged_out_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_exit_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `verification_code` varchar(32) DEFAULT NULL,
  `password_reset_code` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
