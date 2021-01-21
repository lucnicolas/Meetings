CREATE TABLE Meeting (id INTEGER AUTO_INCREMENT NOT NULL, duration INTEGER NOT NULL, start DATETIME NOT NULL, title VARCHAR(255) NOT NULL, PRIMARY KEY (id));
CREATE TABLE User (id INTEGER AUTO_INCREMENT NOT NULL, eMail VARCHAR(255), firstName VARCHAR(255), name VARCHAR(255) NOT NULL, password VARCHAR(255) NOT NULL, PRIMARY KEY (id));
CREATE TABLE Guests (meetingId INTEGER NOT NULL, userId INTEGER NOT NULL, PRIMARY KEY (meetingId, userId));

CREATE TABLE Room (id INTEGER AUTO_INCREMENT NOT NULL, name VARCHAR(255) NOT NULL, capacity INTEGER NOT NULL, PRIMARY KEY (id));
CREATE TABLE Participants (roomId INTEGER NOT NULL, meetingId INTEGER NOT NULL, PRIMARY KEY (roomId, meetingId));

ALTER TABLE Guests ADD CONSTRAINT FK_Guests_meetingId FOREIGN KEY (meetingId) REFERENCES Meeting (id);
ALTER TABLE Guests ADD CONSTRAINT FK_Guests_userId FOREIGN KEY (userId) REFERENCES User (id);

ALTER TABLE Participants ADD CONSTRAINT FK_Participants_roomId FOREIGN KEY (roomId) REFERENCES Room (id);
ALTER TABLE Participants ADD CONSTRAINT FK_Participants_meetingId FOREIGN KEY (meetingId) REFERENCES Meeting (id);
