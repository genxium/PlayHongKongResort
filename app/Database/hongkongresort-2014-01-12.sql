-- MySQL dump 10.13  Distrib 5.6.14, for Win64 (x86_64)
--
-- Host: localhost    Database: hongkongresort
-- ------------------------------------------------------
-- Server version	5.6.14-log

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
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity` (
  `ActivityId` int(32) NOT NULL AUTO_INCREMENT,
  `ActivityTitle` varchar(32) NOT NULL,
  `ActivityContent` varchar(1024) NOT NULL,
  `ActivityCreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ActivityBeginDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ActivityEndDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ActivityCapacity` int(32) NOT NULL DEFAULT '0',
  `ActivityStatus` int(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ActivityId`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity`
--

LOCK TABLES `activity` WRITE;
/*!40000 ALTER TABLE `activity` DISABLE KEYS */;
INSERT INTO `activity` VALUES (2,'New Activity','Lalala','2014-01-11 18:31:02','2014-01-11 18:31:02','2014-01-11 18:34:43',0,3),(3,'Wulalalalala','Hulalalalalala','2014-01-12 09:04:32','2014-01-12 09:04:32','2014-01-12 09:06:00',0,3),(4,'event1','edf','2014-01-12 09:07:09','2014-01-12 09:07:09','2014-01-12 09:07:42',0,3),(5,'event2','ghi','2014-01-12 09:07:56','2014-01-12 09:07:56','2014-01-12 09:09:01',0,3),(6,'event9','uio','2014-01-12 09:12:37','2014-01-12 09:12:37','2014-01-12 09:13:30',0,3),(7,'event8','abcdef','2014-01-12 09:17:04','2014-01-12 09:17:04','2014-01-12 09:17:56',0,3);
/*!40000 ALTER TABLE `activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activityimagerelationtable`
--

DROP TABLE IF EXISTS `activityimagerelationtable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activityimagerelationtable` (
  `ActivityImageRelationTableId` int(32) NOT NULL AUTO_INCREMENT,
  `ActivityId` int(32) NOT NULL,
  `ImageId` int(32) NOT NULL,
  PRIMARY KEY (`ActivityImageRelationTableId`),
  KEY `FK_ActivityId` (`ActivityId`),
  KEY `FK_ImageId` (`ImageId`),
  CONSTRAINT `FK_ActivityId` FOREIGN KEY (`ActivityId`) REFERENCES `activity` (`ActivityId`),
  CONSTRAINT `FK_ImageId` FOREIGN KEY (`ImageId`) REFERENCES `image` (`ImageId`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activityimagerelationtable`
--

LOCK TABLES `activityimagerelationtable` WRITE;
/*!40000 ALTER TABLE `activityimagerelationtable` DISABLE KEYS */;
INSERT INTO `activityimagerelationtable` VALUES (3,2,3),(4,2,4),(5,3,5),(8,5,8),(10,6,10),(11,6,11),(12,6,12),(14,7,14);
/*!40000 ALTER TABLE `activityimagerelationtable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `image`
--

DROP TABLE IF EXISTS `image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `image` (
  `ImageId` int(32) NOT NULL AUTO_INCREMENT,
  `ImageAbsolutePath` varchar(128) NOT NULL,
  `ImageURL` varchar(64) NOT NULL,
  PRIMARY KEY (`ImageId`),
  UNIQUE KEY `ImageAbsolutePath` (`ImageAbsolutePath`),
  UNIQUE KEY `ImageURL` (`ImageURL`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `image`
--

LOCK TABLES `image` WRITE;
/*!40000 ALTER TABLE `image` DISABLE KEYS */;
INSERT INTO `image` VALUES (3,'/home/Wing/play-2.2.1/PlayHongKongResort/public/images/UID12_1389465283228_fox01.jpg','/assets/images/UID12_1389465283228_fox01.jpg'),(4,'/home/Wing/play-2.2.1/PlayHongKongResort/public/images/UID12_1389465283278_pikachu01.jpg','/assets/images/UID12_1389465283278_pikachu01.jpg'),(5,'C:UsersacerDownloadsplay-2.2.1HongKongResort06-01-2014/public/images/UID12_1389517560224_abp-043.jpg','/assets/images/UID12_1389517560224_abp-043.jpg'),(8,'C:UsersacerDownloadsplay-2.2.1HongKongResort06-01-2014/public/images/UID21_1389517741467_IPZ-230.jpg','/assets/images/UID21_1389517741467_IPZ-230.jpg'),(10,'C:UsersacerDownloadsplay-2.2.1HongKongResort06-01-2014/public/images/UID22_1389517981206_MILD879.jpg','/assets/images/UID22_1389517981206_MILD879.jpg'),(11,'C:UsersacerDownloadsplay-2.2.1HongKongResort06-01-2014/public/images/UID22_1389518010809_hbad239.jpg','/assets/images/UID22_1389518010809_hbad239.jpg'),(12,'C:UsersacerDownloadsplay-2.2.1HongKongResort06-01-2014/public/images/UID22_1389518010813_abp-043.jpg','/assets/images/UID22_1389518010813_abp-043.jpg'),(14,'C:UsersacerDownloadsplay-2.2.1HongKongResort06-01-2014/public/images/UID22_1389518276171_crc080.jpg','/assets/images/UID22_1389518276171_crc080.jpg');
/*!40000 ALTER TABLE `image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
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
  CONSTRAINT `FK_UserGroupId` FOREIGN KEY (`UserGroupId`) REFERENCES `usergroup` (`GroupId`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (9,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@hotmail.com',1,0,0,'2013-12-23 14:13:30',0),(10,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@126.com',1,0,0,'2013-12-23 14:45:19',0),(11,'kk','e10adc3949ba59abbe56e057f20f883e','kk@gmail.com',1,0,0,'2013-12-27 14:13:30',0),(12,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@gmail.com',1,0,0,'2013-12-28 04:21:15',0),(13,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@yahoo.com',1,0,0,'2013-12-29 08:16:26',0),(18,'kk','e10adc3949ba59abbe56e057f20f883e','kk@hotmail.com',1,0,0,'2014-01-03 08:01:30',0),(19,'sai','e10adc3949ba59abbe56e057f20f883e','sai@126.com',1,0,0,'2014-01-03 08:12:15',0),(20,'yukping','e10adc3949ba59abbe56e057f20f883e','yukping@gmail.com',1,0,0,'2014-01-03 16:42:09',0),(21,'admin','c33367701511b4f6020ec61ded352059','admin@hongkongresort.com',3,0,0,'2014-01-07 16:12:42',0),(22,'ray','fcea920f7412b5da7be0cf42b8c93759','ray@gmail.com',1,0,0,'2014-01-12 09:11:48',0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `useractivityrelation`
--

DROP TABLE IF EXISTS `useractivityrelation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `useractivityrelation` (
  `UserActivityRelationId` int(3) NOT NULL,
  `UserActivityRelationName` varchar(32) NOT NULL,
  PRIMARY KEY (`UserActivityRelationId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `useractivityrelation`
--

LOCK TABLES `useractivityrelation` WRITE;
/*!40000 ALTER TABLE `useractivityrelation` DISABLE KEYS */;
INSERT INTO `useractivityrelation` VALUES (0,'host'),(1,'applied'),(2,'selected'),(3,'present'),(4,'absent');
/*!40000 ALTER TABLE `useractivityrelation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `useractivityrelationtable`
--

DROP TABLE IF EXISTS `useractivityrelationtable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `useractivityrelationtable` (
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
  CONSTRAINT `UserActivityRelationTable_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `user` (`UserId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_2` FOREIGN KEY (`ActivityId`) REFERENCES `activity` (`ActivityId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_3` FOREIGN KEY (`UserActivityRelationId`) REFERENCES `useractivityrelation` (`UserActivityRelationId`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `useractivityrelationtable`
--

LOCK TABLES `useractivityrelationtable` WRITE;
/*!40000 ALTER TABLE `useractivityrelationtable` DISABLE KEYS */;
INSERT INTO `useractivityrelationtable` VALUES (2,12,2,0,'2014-01-11 18:31:02'),(3,12,3,0,'2014-01-12 09:04:32'),(4,21,4,0,'2014-01-12 09:07:09'),(5,21,5,0,'2014-01-12 09:07:56'),(6,11,5,1,'2014-01-12 09:09:56'),(7,11,3,1,'2014-01-12 09:10:00'),(8,11,4,1,'2014-01-12 09:10:02'),(9,11,2,1,'2014-01-12 09:10:03'),(10,22,3,1,'2014-01-12 09:12:08'),(11,22,6,0,'2014-01-12 09:12:37'),(12,22,7,0,'2014-01-12 09:17:04');
/*!40000 ALTER TABLE `useractivityrelationtable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usergroup`
--

DROP TABLE IF EXISTS `usergroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usergroup` (
  `GroupId` int(32) NOT NULL,
  `GroupName` varchar(32) NOT NULL,
  PRIMARY KEY (`GroupId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usergroup`
--

LOCK TABLES `usergroup` WRITE;
/*!40000 ALTER TABLE `usergroup` DISABLE KEYS */;
INSERT INTO `usergroup` VALUES (0,'visitor'),(1,'user'),(2,'manager'),(3,'admin');
/*!40000 ALTER TABLE `usergroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `userrelation`
--

DROP TABLE IF EXISTS `userrelation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userrelation` (
  `UserRelationId` int(2) NOT NULL,
  `UserRelationName` varchar(32) NOT NULL,
  PRIMARY KEY (`UserRelationId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `userrelation`
--

LOCK TABLES `userrelation` WRITE;
/*!40000 ALTER TABLE `userrelation` DISABLE KEYS */;
/*!40000 ALTER TABLE `userrelation` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-01-12 19:25:34
