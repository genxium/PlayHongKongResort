DROP TABLE IF EXISTS `login`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `login` (
  `user_id` int(32) NOT NULL,
  `token` varchar(32) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `token` (`token`),
  KEY `login_ibfk_1` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

