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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

