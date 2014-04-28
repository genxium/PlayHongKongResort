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
  `ActivityContent` mediumtext,
  `ActivityCreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ActivityBeginTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ActivityApplicationDeadline` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ActivityCapacity` int(32) NOT NULL DEFAULT '0',
  `ActivityStatus` int(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ActivityId`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Activity`
--

LOCK TABLES `Activity` WRITE;
/*!40000 ALTER TABLE `Activity` DISABLE KEYS */;
INSERT INTO `Activity` VALUES (1,'Test activity','','2014-04-28 18:00:02','2013-12-31 16:00:00','2013-12-31 16:00:00',0,3),(2,'Test Upload','','2014-04-28 18:31:35','2013-12-31 16:00:00','2013-12-31 16:00:00',0,1),(4,'Test Save and Submit','lalala','2014-04-28 18:38:46','2013-12-31 16:00:00','2013-12-31 16:00:00',0,1),(5,'Test save','','2014-04-28 18:52:34','2013-12-31 16:00:00','2013-12-31 16:00:00',0,0);
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
  `GeneratedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ActivityImageRelationTableId`),
  KEY `FK_ActivityId` (`ActivityId`),
  KEY `FK_ImageId` (`ImageId`),
  CONSTRAINT `FK_ActivityId` FOREIGN KEY (`ActivityId`) REFERENCES `Activity` (`ActivityId`),
  CONSTRAINT `FK_ImageId` FOREIGN KEY (`ImageId`) REFERENCES `Image` (`ImageId`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ActivityImageRelationTable`
--

LOCK TABLES `ActivityImageRelationTable` WRITE;
/*!40000 ALTER TABLE `ActivityImageRelationTable` DISABLE KEYS */;
INSERT INTO `ActivityImageRelationTable` VALUES (1,2,1,'2014-04-28 18:31:35'),(4,4,4,'2014-04-28 18:38:46'),(5,4,5,'2014-04-28 18:38:46'),(6,5,6,'2014-04-28 18:53:51');
/*!40000 ALTER TABLE `ActivityImageRelationTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CommentOnActivity`
--

DROP TABLE IF EXISTS `CommentOnActivity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CommentOnActivity` (
  `CommentAId` int(32) NOT NULL AUTO_INCREMENT,
  `CommentAContent` varchar(1024) NOT NULL,
  `CommenterId` int(32) NOT NULL,
  `ActivityId` int(32) NOT NULL,
  `PredecessorId` int(32) NOT NULL DEFAULT '-1',
  `CommentType` int(3) NOT NULL DEFAULT '0',
  `GeneratedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ParentID` int(32) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`CommentAId`),
  KEY `FK_CommenterId` (`CommenterId`),
  KEY `FK_ActivityId` (`ActivityId`),
  CONSTRAINT `CommentOnActivity_ibfk_1` FOREIGN KEY (`CommenterId`) REFERENCES `User` (`UserId`),
  CONSTRAINT `CommentOnActivity_ibfk_2` FOREIGN KEY (`ActivityId`) REFERENCES `Activity` (`ActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CommentOnActivity`
--

LOCK TABLES `CommentOnActivity` WRITE;
/*!40000 ALTER TABLE `CommentOnActivity` DISABLE KEYS */;
/*!40000 ALTER TABLE `CommentOnActivity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Image`
--

DROP TABLE IF EXISTS `Image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Image` (
  `ImageId` int(32) NOT NULL AUTO_INCREMENT,
  `ImageAbsolutePath` varchar(128) NOT NULL,
  `ImageURL` varchar(64) NOT NULL,
  PRIMARY KEY (`ImageId`),
  UNIQUE KEY `ImageAbsolutePath` (`ImageAbsolutePath`),
  UNIQUE KEY `ImageURL` (`ImageURL`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Image`
--

LOCK TABLES `Image` WRITE;
/*!40000 ALTER TABLE `Image` DISABLE KEYS */;
INSERT INTO `Image` VALUES (1,'/Users/user/play-2.2.2/PlayHongKongResort/public/images/UID1_1398709895104_pikachu03.png','/assets/images/UID1_1398709895104_pikachu03.png'),(4,'/Users/user/play-2.2.2/PlayHongKongResort/public/images/UID1_1398710326272_FlameDragon.png','/assets/images/UID1_1398710326272_FlameDragon.png'),(5,'/Users/user/play-2.2.2/PlayHongKongResort/public/images/UID1_1398710326276_MarvelousSeed.jpg','/assets/images/UID1_1398710326276_MarvelousSeed.jpg'),(6,'/Users/user/play-2.2.2/PlayHongKongResort/public/images/UID1_1398711231988_pikachu03.png','/assets/images/UID1_1398711231988_pikachu03.png');
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
  `DisplayName` varchar(32) DEFAULT NULL,
  `UserPassword` varchar(32) NOT NULL,
  `UserEmail` varchar(32) NOT NULL,
  `UserGroupId` int(2) NOT NULL DEFAULT '0',
  `UserAuthenticationStatus` int(2) NOT NULL DEFAULT '0',
  `UserGender` int(1) NOT NULL DEFAULT '0',
  `UserLastLoggedInTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UserAvatar` int(32) NOT NULL DEFAULT '0',
  `UserLastLoggedOutTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UserLastExitTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UserCreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Username` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`UserId`),
  UNIQUE KEY `UserEmail` (`UserEmail`),
  UNIQUE KEY `UserName` (`Username`),
  UNIQUE KEY `Username_2` (`Username`),
  KEY `UserGroupId` (`UserGroupId`),
  CONSTRAINT `FK_UserGroupId` FOREIGN KEY (`UserGroupId`) REFERENCES `UserGroup` (`GroupId`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `User`
--

LOCK TABLES `User` WRITE;
/*!40000 ALTER TABLE `User` DISABLE KEYS */;
INSERT INTO `User` VALUES (1,NULL,'e10adc3949ba59abbe56e057f20f883e','genxium@gmail.com',1,0,0,'2014-04-28 17:37:38',0,'2014-04-28 17:37:38','2014-04-28 17:37:38','2014-04-28 17:37:38','genxium'),(4,NULL,'e10adc3949ba59abbe56e057f20f883e','genxium@hotmail.com',1,0,0,'2014-04-28 17:59:35',0,'2014-04-28 17:59:35','2014-04-28 17:59:35','2014-04-28 17:59:35','genxium1988'),(5,NULL,'c33367701511b4f6020ec61ded352059','admin@hongkongresort.com',3,0,0,'2014-04-28 18:14:22',0,'2014-04-28 18:14:22','2014-04-28 18:14:22','2014-04-28 18:14:22','admin');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  `LastApplyingTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `LastAcceptedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `LastRejectedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserActivityRelationTableId`),
  UNIQUE KEY `UA_UNI_ID` (`UserId`,`ActivityId`),
  KEY `UserId` (`UserId`),
  KEY `ActivityId` (`ActivityId`),
  KEY `UserActivityRelationId` (`UserActivityRelationId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `User` (`UserId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_2` FOREIGN KEY (`ActivityId`) REFERENCES `Activity` (`ActivityId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_3` FOREIGN KEY (`UserActivityRelationId`) REFERENCES `UserActivityRelation` (`UserActivityRelationId`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserActivityRelationTable`
--

LOCK TABLES `UserActivityRelationTable` WRITE;
/*!40000 ALTER TABLE `UserActivityRelationTable` DISABLE KEYS */;
INSERT INTO `UserActivityRelationTable` VALUES (1,4,1,0,'2014-04-28 18:00:02','2014-04-28 18:00:02','2014-04-28 18:00:02','2014-04-28 18:00:02'),(2,1,2,0,'2014-04-28 18:31:35','2014-04-28 18:31:35','2014-04-28 18:31:35','2014-04-28 18:31:35'),(4,1,4,0,'2014-04-28 18:38:46','2014-04-28 18:38:46','2014-04-28 18:38:46','2014-04-28 18:38:46'),(5,1,5,0,'2014-04-28 18:52:34','2014-04-28 18:52:34','2014-04-28 18:52:34','2014-04-28 18:52:34');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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

-- Dump completed on 2014-04-29  2:55:48
