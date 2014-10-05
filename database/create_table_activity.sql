DROP TABLE IF EXISTS `activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `title` varchar(32) NOT NULL,
  `content` mediumtext,
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `begin_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `application_deadline` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `capacity` int(32) NOT NULL DEFAULT '0',
  `status` int(3) NOT NULL DEFAULT '0',
  `host_id` int(32) NOT NULL,
  `num_applied` int(32) DEFAULT '0',
  `num_selected` int(32) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `status_index` (`status`),
  KEY `activity_ibfk_1` (`host_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
