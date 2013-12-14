CREATE TABLE UserGroup (
	GroupId	INT(20) NOT NULL PRIMARY KEY,
	GroupName VARCHAR(32) NOT NULL
);

CREATE TABLE User (
    UserId INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    UserName VARCHAR(32) NOT NULL,
    UserPassword VARCHAR(32) NOT NULL,
    UserEmail VARCHAR(32) NOT NULL,
    UserGroupId INT(20) NOT NULL,
    UserAuthenticationStatus INT(2) NOT NULL,
    UserGender INT(1) NOT NULL,
    UserLastLoggedInTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    FOREIGN KEY (UserGroupId) REFERENCES UserGroup(GroupId)
);

CREATE TABLE Activity (
	ActivityId INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
	ActivityName VARCHAR(32) NOT NULL,
	ActivityContent VARCHAR(1024) NOT NULL,
	ActivityCreatedTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	ActivityBeginDate DATETIME NOT NULL,
	ActivityEndDate DATETIME NOT NULL,
	ActivityCapacity INT(32) NOT NULL
);

CREATE TABLE UserRelation (
	UserRelationId INT(2) NOT NULL PRIMARY KEY,
	UserRelationName VARCHAR(32) NOT NULL
);

CREATE TABLE UserActivityRelation (
	UserActivityRelationId INT(3) NOT NULL PRIMARY KEY,
	UserActivityRelationName VARCHAR(32) NOT NULL
);

CREATE TABLE UserActivityRelationTable (
    UserActivityRelationTableId INT(20) NOT NULL PRIMARY KEY,
    UserId INT(20) NOT NULL,
    ActivityId INT(20) NOT NULL,
    UserActivityRelationId INT(3) NOT NULL,
    GeneratedTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserId) REFERENCES User(UserId),
    FOREIGN KEY (ActivityId) REFERENCES Activity(ActivityId),
    FOREIGN KEY (UserActivityRelationId) REFERENCES UserActivityRelation(UserActivityRelationId)
)
