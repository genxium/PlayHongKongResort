-- MySQL dump 10.13  Distrib 5.6.27, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: hongkongresort
-- ------------------------------------------------------
-- Server version	5.6.27-0ubuntu0.14.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `player`
--

DROP TABLE IF EXISTS `player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) DEFAULT NULL,
  `display_name` varchar(32) DEFAULT NULL,
  `password` varchar(32) DEFAULT NULL,
  `email` varchar(32) DEFAULT NULL,
  `group_id` int(2) NOT NULL DEFAULT '0',
  `authentication_status` int(2) NOT NULL DEFAULT '0',
  `gender` varchar(16) NOT NULL DEFAULT '',
  `avatar` int(32) NOT NULL DEFAULT '0',
  `created_time` bigint(20) DEFAULT NULL,
  `verification_code` varchar(32) DEFAULT NULL,
  `unread_count` int(32) NOT NULL DEFAULT '0',
  `password_reset_code` varchar(32) DEFAULT NULL,
  `salt` varchar(16) DEFAULT NULL,
  `unassessed_count` int(11) NOT NULL DEFAULT '0',
  `age` varchar(16) NOT NULL DEFAULT '',
  `mood` varchar(64) NOT NULL DEFAULT '',
  `party` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `player`
--

LOCK TABLES `player` WRITE;
/*!40000 ALTER TABLE `player` DISABLE KEYS */;
INSERT INTO `player` VALUES (45,'genxium',NULL,NULL,NULL,3,0,'',194,NULL,NULL,0,NULL,NULL,0,'','',1),(46,'angryboy',NULL,NULL,NULL,1,0,'',0,NULL,'183c422b674903ea',0,NULL,NULL,0,'','',1),(47,'winless',NULL,NULL,NULL,1,0,'',0,NULL,'91fdbb213d4ac5e3',0,NULL,NULL,0,'','',1),(48,'qazwsx',NULL,NULL,'3123123@163.com',1,0,'',0,NULL,'bf2269936085ffdd',0,NULL,NULL,0,'','',1),(51,'genxium5555',NULL,'561d0861caf428562508acc20f4a8550','genxium@gmail.com',3,0,'malejn',0,NULL,NULL,0,NULL,'f6670f9acac01f12',0,'2892342','no mood lalala',0),(52,'RayC33',NULL,NULL,'saichanjiawei@gmail.com',1,0,'',0,NULL,'8616dbb4a04e6840',0,NULL,NULL,0,'','',1),(53,'wofuck',NULL,NULL,'m@roowe.net',1,0,'',0,NULL,'3ebe71e5217c2570',0,NULL,NULL,0,'','',1),(54,'hachicats',NULL,NULL,NULL,1,0,'',244,NULL,NULL,0,NULL,NULL,0,'','',1),(55,'genxium1988',NULL,'52633c22256370be439fceef08b77690','genxium@hotmail.com',3,0,'',0,NULL,'7b48c31f6f5c37cc',0,NULL,'409a56eff51c0443',0,'','',0);
/*!40000 ALTER TABLE `player` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-11-29 13:37:35
