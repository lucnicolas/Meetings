ALTER TABLE Guests DROP FOREIGN KEY FK_Guests_meetingId;
ALTER TABLE Guests DROP FOREIGN KEY FK_Guests_userId;

ALTER TABLE Participants DROP FOREIGN KEY FK_Participants_roomId;
ALTER TABLE Participants DROP FOREIGN KEY FK_Participants_meetingId;

DROP TABLE Meeting;
DROP TABLE User;
DROP TABLE Guests;
DROP TABLE Room;
