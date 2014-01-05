-- MySQL dump 10.13  Distrib 5.6.14, for osx10.7 (x86_64)
--
-- Host: localhost    Database: hongkongresort
-- ------------------------------------------------------
-- Server version	5.6.14

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
-- Table structure for table `Activity`
--

DROP TABLE IF EXISTS `Activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Activity` (
  `ActivityId` int(32) NOT NULL AUTO_INCREMENT,
  `ActivityTitle` varchar(32) NOT NULL,
  `ActivityContent` varchar(1024) NOT NULL,
  `ActivityCreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ActivityBeginDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ActivityEndDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ActivityCapacity` int(32) NOT NULL DEFAULT '0',
  `ActivityStatus` int(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ActivityId`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Activity`
--

LOCK TABLES `Activity` WRITE;
/*!40000 ALTER TABLE `Activity` DISABLE KEYS */;
INSERT INTO `Activity` VALUES (32,'Testing Edit Accepted Activities','lalala','2013-12-26 11:14:42','2013-12-26 11:14:42','2013-12-28 17:15:32',0,3),(33,'Title 33 updating','Another test case.','2013-12-26 11:54:21','2013-12-26 11:54:21','2013-12-26 16:59:45',0,3),(34,'Edit Test','Tom and Jerry','2013-12-26 11:55:58','2013-12-26 11:55:58','2013-12-28 13:18:27',0,3),(35,'uet','sfdfd','2013-12-26 12:08:17','2013-12-26 12:08:17','2013-12-27 13:50:12',0,3),(36,'Testing','lallalalla','2013-12-26 15:43:20','2013-12-26 15:43:20','2013-12-26 16:40:31',0,3),(38,'Form Title','Testing form refactoring.','2013-12-27 14:13:48','2013-12-27 14:13:48','2014-01-04 19:00:52',0,1),(46,'Lalala','Miao','2013-12-29 11:32:28','2013-12-29 11:32:28','2013-12-29 12:18:23',0,3),(48,'Testing New Activity','uuuuuppppppppp','2013-12-29 12:21:50','2013-12-29 12:21:50','2013-12-29 12:22:07',0,1),(50,'lalala','lululu','2013-12-30 17:30:55','2013-12-30 17:30:55','2013-12-30 17:31:03',0,1),(53,'','','2014-01-03 07:17:24','2014-01-03 07:17:24','2014-01-03 07:17:24',0,0),(54,'Lalala','wtf','2014-01-03 08:22:06','2014-01-03 08:22:06','2014-01-03 08:22:15',0,1),(70,'New Activity ','Testing testing','2014-01-03 09:58:36','2014-01-03 09:58:36','2014-01-03 09:58:50',0,0),(71,'Image','Images','2014-01-05 12:20:07','2014-01-05 12:20:07','2014-01-05 15:10:31',0,0),(72,'Test Cover','images images...','2014-01-05 16:17:45','2014-01-05 16:17:45','2014-01-05 16:18:19',0,0);
/*!40000 ALTER TABLE `Activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ActivityImageRelationTable`
--

DROP TABLE IF EXISTS `ActivityImageRelationTable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ActivityImageRelationTable` (
  `ActivityImageRelationTableId` int(32) NOT NULL AUTO_INCREMENT,
  `ActivityId` int(32) NOT NULL,
  `ImageId` int(32) NOT NULL,
  PRIMARY KEY (`ActivityImageRelationTableId`),
  KEY `FK_ActivityId` (`ActivityId`),
  KEY `FK_ImageId` (`ImageId`),
  CONSTRAINT `FK_ImageId` FOREIGN KEY (`ImageId`) REFERENCES `Image` (`ImageId`),
  CONSTRAINT `FK_ActivityId` FOREIGN KEY (`ActivityId`) REFERENCES `Activity` (`ActivityId`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ActivityImageRelationTable`
--

LOCK TABLES `ActivityImageRelationTable` WRITE;
/*!40000 ALTER TABLE `ActivityImageRelationTable` DISABLE KEYS */;
INSERT INTO `ActivityImageRelationTable` VALUES (1,71,15),(2,71,16),(3,71,17),(4,72,22),(5,72,23),(6,72,24);
/*!40000 ALTER TABLE `ActivityImageRelationTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Image`
--

DROP TABLE IF EXISTS `Image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Image` (
  `ImageId` int(32) NOT NULL AUTO_INCREMENT,
  `ImageAbsolutePath` varchar(512) NOT NULL,
  `ImageURL` varchar(512) NOT NULL,
  PRIMARY KEY (`ImageId`),
  UNIQUE KEY `ImageAbsolutePath` (`ImageAbsolutePath`),
  UNIQUE KEY `ImageURL` (`ImageURL`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Image`
--

LOCK TABLES `Image` WRITE;
/*!40000 ALTER TABLE `Image` DISABLE KEYS */;
INSERT INTO `Image` VALUES (1,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID10.2014-01-03 01:08:03.444.pikachu01.png','/assets/images/UID10.2014-01-03 01:08:03.444.pikachu01.png'),(2,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID19.2014-01-03 16:21:45.804.pikachu03.png','/assets/images/UID19.2014-01-03 16:21:45.804.pikachu03.png'),(8,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID20.2014-01-04 00:46:22.27.pikachu04.gif','/assets/images/UID20.2014-01-04 00:46:22.27.pikachu04.gif'),(15,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID11.2014-01-05 23:10:59.315.pikachu01.png','/assets/images/UID11.2014-01-05 23:10:59.315.pikachu01.png'),(16,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID11.2014-01-05 23:11:45.725.pikachu02.jpeg','/assets/images/UID11.2014-01-05 23:11:45.725.pikachu02.jpeg'),(17,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID11.2014-01-05 23:11:50.219.pikachu03.png','/assets/images/UID11.2014-01-05 23:11:50.219.pikachu03.png'),(18,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID12.1388937289458.FlameDragon.png','/assets/images/UID12.1388937289458.FlameDragon.png'),(19,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID9.1388937390230.pikachu01.png','/assets/images/UID9.1388937390230.pikachu01.png'),(20,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID11.1388937406801.pikachu03.png','/assets/images/UID11.1388937406801.pikachu03.png'),(21,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID19.1388937432545.pikachu04.gif','/assets/images/UID19.1388937432545.pikachu04.gif'),(22,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID19.1388938699157.FlameDragon.png','/assets/images/UID19.1388938699157.FlameDragon.png'),(23,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID19.1388938699160.MarvelousSeed.jpg','/assets/images/UID19.1388938699160.MarvelousSeed.jpg'),(24,'/Users/user/play-2.2.0/PlayHelloWorld/public/images/UID19.1388938699163.pikachu01.png','/assets/images/UID19.1388938699163.pikachu01.png');
/*!40000 ALTER TABLE `Image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User` (
  `UserId` int(32) NOT NULL AUTO_INCREMENT,
  `UserName` varchar(32) NOT NULL,
  `UserPassword` varchar(32) NOT NULL,
  `UserEmail` varchar(32) NOT NULL,
  `UserGroupId` int(2) NOT NULL DEFAULT '0',
  `UserAuthenticationStatus` int(2) NOT NULL DEFAULT '0',
  `UserGender` int(1) NOT NULL DEFAULT '0',
  `UserLastLoggedInTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UserAvatar` int(32) NOT NULL DEFAULT '0',
  PRIMARY KEY (`UserId`),
  UNIQUE KEY `UserEmail` (`UserEmail`),
  KEY `UserGroupId` (`UserGroupId`),
  CONSTRAINT `FK_UserGroupId` FOREIGN KEY (`UserGroupId`) REFERENCES `UserGroup` (`GroupId`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `User`
--

LOCK TABLES `User` WRITE;
/*!40000 ALTER TABLE `User` DISABLE KEYS */;
INSERT INTO `User` VALUES (9,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@hotmail.com',0,0,0,'2013-12-23 14:13:30',19),(10,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@126.com',0,0,0,'2013-12-23 14:45:19',1),(11,'kk','e10adc3949ba59abbe56e057f20f883e','kk@gmail.com',0,0,0,'2013-12-27 14:13:30',20),(12,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@gmail.com',0,0,0,'2013-12-28 04:21:15',18),(13,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@yahoo.com',0,0,0,'2013-12-29 08:16:26',0),(18,'kk','e10adc3949ba59abbe56e057f20f883e','kk@hotmail.com',0,0,0,'2014-01-03 08:01:30',0),(19,'sai','e10adc3949ba59abbe56e057f20f883e','sai@126.com',0,0,0,'2014-01-03 08:12:15',21),(20,'yukping','e10adc3949ba59abbe56e057f20f883e','yukping@gmail.com',0,0,0,'2014-01-03 16:42:09',8);
/*!40000 ALTER TABLE `User` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserActivityRelation`
--

DROP TABLE IF EXISTS `UserActivityRelation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserActivityRelation` (
  `UserActivityRelationId` int(3) NOT NULL,
  `UserActivityRelationName` varchar(32) NOT NULL,
  PRIMARY KEY (`UserActivityRelationId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserActivityRelation`
--

LOCK TABLES `UserActivityRelation` WRITE;
/*!40000 ALTER TABLE `UserActivityRelation` DISABLE KEYS */;
INSERT INTO `UserActivityRelation` VALUES (0,'host'),(1,'applied'),(2,'selected'),(3,'present'),(4,'absent');
/*!40000 ALTER TABLE `UserActivityRelation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserActivityRelationTable`
--

DROP TABLE IF EXISTS `UserActivityRelationTable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserActivityRelationTable` (
  `UserActivityRelationTableId` int(32) NOT NULL AUTO_INCREMENT,
  `UserId` int(32) NOT NULL,
  `ActivityId` int(32) NOT NULL,
  `UserActivityRelationId` int(3) NOT NULL,
  `GeneratedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserActivityRelationTableId`),
  UNIQUE KEY `UA_UNI_ID` (`UserId`,`ActivityId`),
  KEY `UserId` (`UserId`),
  KEY `ActivityId` (`ActivityId`),
  KEY `UserActivityRelationId` (`UserActivityRelationId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `User` (`UserId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_2` FOREIGN KEY (`ActivityId`) REFERENCES `Activity` (`ActivityId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_3` FOREIGN KEY (`UserActivityRelationId`) REFERENCES `UserActivityRelation` (`UserActivityRelationId`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserActivityRelationTable`
--

LOCK TABLES `UserActivityRelationTable` WRITE;
/*!40000 ALTER TABLE `UserActivityRelationTable` DISABLE KEYS */;
INSERT INTO `UserActivityRelationTable` VALUES (10,9,32,0,'2013-12-26 11:14:42'),(11,9,33,0,'2013-12-26 11:54:21'),(12,9,34,0,'2013-12-26 11:55:58'),(13,9,35,0,'2013-12-26 12:08:17'),(14,9,36,0,'2013-12-26 15:43:20'),(16,11,38,0,'2013-12-27 14:13:48'),(24,13,46,0,'2013-12-29 11:32:28'),(26,13,48,0,'2013-12-29 12:21:50'),(28,13,32,1,'2013-12-30 17:10:20'),(29,12,35,1,'2013-12-30 17:21:21'),(30,12,50,0,'2013-12-30 17:30:55'),(31,13,36,1,'2013-12-30 18:28:28'),(32,13,33,1,'2013-12-30 18:30:13'),(33,13,34,1,'2013-12-30 18:30:24'),(34,12,33,1,'2013-12-30 18:32:02'),(36,12,36,1,'2014-01-02 15:20:43'),(37,12,32,1,'2014-01-03 06:28:04'),(38,13,35,1,'2014-01-03 07:02:13'),(40,13,53,0,'2014-01-03 07:17:24'),(41,18,34,1,'2014-01-03 08:05:17'),(42,18,32,1,'2014-01-03 08:07:30'),(43,18,33,1,'2014-01-03 08:09:12'),(44,18,35,1,'2014-01-03 08:10:42'),(45,18,36,1,'2014-01-03 08:11:10'),(46,19,34,1,'2014-01-03 08:13:28'),(47,19,32,1,'2014-01-03 08:18:46'),(48,19,33,1,'2014-01-03 08:18:57'),(49,19,54,0,'2014-01-03 08:22:06'),(57,19,36,1,'2014-01-03 09:12:39'),(66,19,70,0,'2014-01-03 09:58:36'),(67,12,34,1,'2014-01-03 12:13:25'),(68,20,33,1,'2014-01-03 16:46:47'),(69,11,33,1,'2014-01-04 11:20:45'),(70,11,71,0,'2014-01-05 12:20:07'),(71,19,72,0,'2014-01-05 16:17:45');
/*!40000 ALTER TABLE `UserActivityRelationTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserGroup`
--

DROP TABLE IF EXISTS `UserGroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserGroup` (
  `GroupId` int(32) NOT NULL,
  `GroupName` varchar(32) NOT NULL,
  PRIMARY KEY (`GroupId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserGroup`
--

LOCK TABLES `UserGroup` WRITE;
/*!40000 ALTER TABLE `UserGroup` DISABLE KEYS */;
INSERT INTO `UserGroup` VALUES (0,'visitor'),(1,'user'),(2,'manager'),(3,'admin');
/*!40000 ALTER TABLE `UserGroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserRelation`
--

DROP TABLE IF EXISTS `UserRelation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserRelation` (
  `UserRelationId` int(2) NOT NULL,
  `UserRelationName` varchar(32) NOT NULL,
  PRIMARY KEY (`UserRelationId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserRelation`
--

LOCK TABLES `UserRelation` WRITE;
/*!40000 ALTER TABLE `UserRelation` DISABLE KEYS */;
/*!40000 ALTER TABLE `UserRelation` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-01-06  0:36:12
