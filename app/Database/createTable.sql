CREATE TABLE User (
    userId INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(256) NOT NULL,
    password VARCHAR(256) NOT NULL,
    name VARCHAR(256) NOT NULL
) ENGINE=INNODB;

CREATE TABLE Post (
    postId INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    postTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=INNODB;

CREATE TABLE Image (
    imageId INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    url TEXT NOT NULL,
    uploadTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=INNODB;

CREATE TABLE UserPostRelation (
    userId INTEGER NOT NULL,
    postId INTEGER NOT NULL,
    relation SMALLINT NOT NULL,
    FOREIGN KEY (userId)
    REFERENCES User(userId),
    FOREIGN KEY (postId)
    REFERENCES Post(postId)
) ENGINE=INNODB;

CREATE TABLE UserImageRelation (
    userId INTEGER NOT NULL,
    imageId INTEGER NOT NULL,
    relation SMALLINT NOT NULL,
    FOREIGN KEY (userId)
    REFERENCES User(userId),
    FOREIGN KEY (imageId)
    REFERENCES Image(imageId)
) ENGINE=INNODB;

CREATE INDEX imageIdIndexUserImageRelation ON UserImageRelation(imageId);

CREATE TABLE PostImageRelation (
    postId INTEGER NOT NULL,
    imageId INTEGER NOT NULL,
    relation SMALLINT NOT NULL,
    FOREIGN KEY (postId)
    REFERENCES Post(postId),
    FOREIGN KEY (imageId)
    REFERENCES Image(imageId)
) ENGINE=INNODB;

CREATE INDEX imageIdIndexPostImageRelation ON PostImageRelation(imageId);

INSERT INTO Image(url) VALUES("defaultAvatar.png");
INSERT INTO Image(url) VALUES("defaultPost.png");

INSERT INTO Post(content) VALUES("The first post");
INSERT INTO Post(content) VALUES("The second post");