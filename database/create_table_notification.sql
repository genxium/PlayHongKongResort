DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `is_read` int(2) DEFAULT '0',
  `from` int(32) NOT NULL,
  `to` int(32) NOT NULL,
  `content` varchar(128) NOT NULL,
  `activity_id` int(32) NOT NULL,
  `comment_id` int(32) DEFAULT NULL,
  `assessment_id` int(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `notification_ibfk_1` (`from`),
  KEY `notification_ibfk_2` (`to`),
  KEY `notification_ibfk_3` (`activity_id`),
  KEY `notification_ibfk_4` (`comment_id`),
  KEY `notification_ibfk_5` (`assessment_id`),
  CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`from`) REFERENCES `user` (`id`),
  CONSTRAINT `notification_ibfk_2` FOREIGN KEY (`to`) REFERENCES `user` (`id`),
  CONSTRAINT `notification_ibfk_3` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`),
  CONSTRAINT `notification_ibfk_4` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`),
  CONSTRAINT `notification_ibfk_5` FOREIGN KEY (`assessment_id`) REFERENCES `assessment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

