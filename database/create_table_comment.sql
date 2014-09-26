DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comment` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `content` varchar(1024) NOT NULL,
  `commenter_id` int(32) NOT NULL,
  `activity_id` int(32) NOT NULL,
  `predecessor_id` int(32) NOT NULL DEFAULT '-1',
  `generated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `parent_id` int(32) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`),
  KEY `comment_ibfk_1` (`commenter_id`),
  KEY `comment_ibfk_2` (`activity_id`),
  CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`commenter_id`) REFERENCES `user` (`id`),
  CONSTRAINT `comment_ibfk_2` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
