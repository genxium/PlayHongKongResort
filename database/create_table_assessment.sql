DROP TABLE IF EXISTS `assessment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `assessment` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `from` int(32) NOT NULL,
  `to` int(32) NOT NULL,
  `content` varchar(128) NOT NULL,
  `activity_id` int(32) NOT NULL,
  `generated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `assessment_ibfk_1` (`from`),
  KEY `assessment_ibfk_2` (`to`),
  KEY `assessment_ibfk_3` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

