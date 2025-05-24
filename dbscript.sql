USE [master]
GO

/*******************************************************************************
   Drop database if it exists
********************************************************************************/
IF EXISTS (SELECT name FROM master.dbo.sysdatabases WHERE name = N'social_media')
BEGIN
	ALTER DATABASE [social_media] SET OFFLINE WITH ROLLBACK IMMEDIATE;
	ALTER DATABASE [social_media] SET ONLINE;
	DROP DATABASE [social_media];
END

GO

CREATE DATABASE [social_media]
GO

USE [social_media]
GO

/*******************************************************************************
	Drop tables if exists
*******************************************************************************/
DECLARE @sql nvarchar(MAX) 
SET @sql = N'' 

SELECT @sql = @sql + N'ALTER TABLE ' + QUOTENAME(KCU1.TABLE_SCHEMA) 
    + N'.' + QUOTENAME(KCU1.TABLE_NAME) 
    + N' DROP CONSTRAINT ' -- + QUOTENAME(rc.CONSTRAINT_SCHEMA)  + N'.'  -- not in MS-SQL
    + QUOTENAME(rc.CONSTRAINT_NAME) + N'; ' + CHAR(13) + CHAR(10) 
FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS AS RC 

INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS KCU1 
    ON KCU1.CONSTRAINT_CATALOG = RC.CONSTRAINT_CATALOG  
    AND KCU1.CONSTRAINT_SCHEMA = RC.CONSTRAINT_SCHEMA 
    AND KCU1.CONSTRAINT_NAME = RC.CONSTRAINT_NAME 

EXECUTE(@sql) 

GO
DECLARE @sql2 NVARCHAR(max)=''

SELECT @sql2 += ' Drop table ' + QUOTENAME(TABLE_SCHEMA) + '.'+ QUOTENAME(TABLE_NAME) + '; '
FROM   INFORMATION_SCHEMA.TABLES
WHERE  TABLE_TYPE = 'BASE TABLE'

Exec Sp_executesql @sql2 
GO


/*******************************************************************************
	Create table
*******************************************************************************/
CREATE TABLE [account]
(
  account_id INT IDENTITY(1,1),
  username VARCHAR(250) NOT NULL,
  password VARCHAR(250) NOT NULL,
  account_fullname VARCHAR(50) NOT NULL,
  account_email VARCHAR(50),
  account_phone VARCHAR(15),
  account_gender BIT, --0 'male', 1 'female'
  account_dob DATE,
  account_avatar VARCHAR(MAX),
  account_bio VARCHAR(250),
  account_status VARCHAR(10) NOT NULL DEFAULT 'active',
  CHECK (account_status IN ('active', 'inactive', 'banned', 'deleted')),
  account_role VARCHAR(10) NOT NULL DEFAULT 'customer',
  CHECK (account_role IN ('customer', 'admin')),
  PRIMARY KEY (account_id),
  UNIQUE (username, account_email)
);

CREATE TABLE post
(
  post_id INT IDENTITY(1,1),
  post_content VARCHAR(MAX),
  account_id INT,
  post_create_date DATETIME NOT NULL DEFAULT GETDATE(),
  post_last_update DATETIME,
  post_privacy VARCHAR(10) NOT NULL DEFAULT 'public',
  CHECK (post_privacy IN ('public', 'friend', 'private')),
  post_status VARCHAR(10) NOT NULL DEFAULT 'drafted',
  CHECK (post_status IN ('drafted', 'published', 'hidden', 'rejected', 'deleted')),
  PRIMARY KEY (post_id),
  FOREIGN KEY (account_id) REFERENCES account(account_id)
);

CREATE TABLE post_image
(
  post_image_id INT IDENTITY(1,1),
  post_image VARCHAR(MAX) NOT NULL,
  post_id INT NOT NULL,
  PRIMARY KEY (post_image_id),
  FOREIGN KEY (post_id) REFERENCES post(post_id)
);

CREATE TABLE hashtag
(
  hashtag_id INT IDENTITY(1,1),
  hashtag_name VARCHAR(50) NOT NULL,
  PRIMARY KEY (hashtag_id),
  UNIQUE(hashtag_name)
);

CREATE TABLE view_post
(
  account_id INT NOT NULL,
  post_id INT NOT NULL,
  PRIMARY KEY (account_id, post_id),
  FOREIGN KEY (account_id) REFERENCES account(account_id),
  FOREIGN KEY (post_id) REFERENCES post(post_id)
);

CREATE TABLE like_post
(
  account_id INT NOT NULL,
  post_id INT NOT NULL,
  PRIMARY KEY (account_id, post_id),
  FOREIGN KEY (account_id) REFERENCES account(account_id),
  FOREIGN KEY (post_id) REFERENCES post(post_id)
);

CREATE TABLE repost
(
  account_id INT NOT NULL,
  post_id INT NOT NULL,
  repost_create_date DATETIME NOT NULL DEFAULT GETDATE(),
  PRIMARY KEY (account_id, post_id),
  FOREIGN KEY (account_id) REFERENCES account(account_id),
  FOREIGN KEY (post_id) REFERENCES post(post_id)
);

CREATE TABLE report_post
(
  report_id INT IDENTITY(1,1),
  report_feedback VARCHAR(250) NOT NULL,
  account_id INT NOT NULL,
  post_id INT NOT NULL,
  report_create_date DATETIME NOT NULL DEFAULT GETDATE(),
  report_status BIT,--('0 accepted', '1 discarded')
  PRIMARY KEY (report_id),
  FOREIGN KEY (account_id) REFERENCES account(account_id),
  FOREIGN KEY (post_id) REFERENCES post(post_id)
);

CREATE TABLE report_account
(
  report_id INT IDENTITY(1,1),
  report_feedback VARCHAR(250) NOT NULL,
  account_id INT NOT NULL,
  reported_account_id INT NOT NULL,
  report_create_date DATETIME NOT NULL DEFAULT GETDATE(),
  report_status BIT,--('0 accepted', '1 discarded')
  PRIMARY KEY (report_id),
  FOREIGN KEY (account_id) REFERENCES account(account_id),
  FOREIGN KEY (reported_account_id) REFERENCES account(account_id)
);

CREATE TABLE post_mention
(
  mention_index INT NOT NULL,
  account_id INT NOT NULL,
  post_id INT NOT NULL,
  PRIMARY KEY (mention_index, post_id, account_id),
  FOREIGN KEY (account_id) REFERENCES account(account_id),
  FOREIGN KEY (post_id) REFERENCES post(post_id),
  UNIQUE (account_id, post_id)
);

CREATE TABLE tag_hashtag
(
  hashtag_index INT NOT NULL,
  post_id INT NOT NULL,
  hashtag_id INT NOT NULL,
  PRIMARY KEY (hashtag_index, post_id, hashtag_id),
  FOREIGN KEY (post_id) REFERENCES post(post_id),
  FOREIGN KEY (hashtag_id) REFERENCES hashtag(hashtag_id)
);

CREATE TABLE interact
(
  account_id_1 INT NOT NULL,
  account_id_2 INT NOT NULL,
  interact_status VARCHAR(10),
  CHECK (interact_status IN ('follow', 'friend', 'block')),
  FOREIGN KEY (account_id_1) REFERENCES account(account_id),
  FOREIGN KEY (account_id_2) REFERENCES account(account_id)
);

CREATE TABLE message
(
  message_content VARCHAR(MAX),
  message_create_date DATETIME NOT NULL DEFAULT GETDATE(),
  message_status VARCHAR(10) NOT NULL DEFAULT 'sended',
  CHECK (message_status IN ('sended', 'received', 'seen', 'deleted')),
  message_id INT IDENTITY(1,1),
  send_account_id INT NOT NULL,
  receive_account_id INT NOT NULL,
  PRIMARY KEY (message_id),
  FOREIGN KEY (send_account_id) REFERENCES account(account_id),
  FOREIGN KEY (receive_account_id) REFERENCES account(account_id)
);

CREATE TABLE friend_request
(
  friend_request_content VARCHAR(250),
  friend_request_date DATETIME NOT NULL DEFAULT GETDATE(),
  friend_request_status VARCHAR(10) NOT NULL DEFAULT 'sended',
  CHECK (friend_request_status IN ('sended', 'accepted', 'rejected', 'cancelled')),
  friend_request_id INT IDENTITY(1,1) NOT NULL,
  send_account_id INT NOT NULL,
  receive_account_id INT NOT NULL,
  PRIMARY KEY (friend_request_id),
  FOREIGN KEY (send_account_id) REFERENCES account(account_id),
  FOREIGN KEY (receive_account_id) REFERENCES account(account_id)
);

CREATE TABLE notification
(
  notification_id INT IDENTITY(1,1),
  notification_title VARCHAR(250) NOT NULL,
  notification_content VARCHAR(MAX) NOT NULL,
  notification_create_date DATETIME NOT NULL  DEFAULT GETDATE(),
  notification_last_update DATETIME,
  PRIMARY KEY (notification_id)
);

CREATE TABLE check_notification
(
  account_id INT NOT NULL,
  notification_id INT NOT NULL,
  PRIMARY KEY (account_id, notification_id),
  FOREIGN KEY (account_id) REFERENCES account(account_id),
  FOREIGN KEY (notification_id) REFERENCES notification(notification_id)
);

CREATE TABLE comment
(
  comment_id INT IDENTITY(1,1),
  comment_content VARCHAR(MAX) NOT NULL,
  comment_status BIT NOT NULL DEFAULT 0,--('active', 'deleted')
  comment_create_date DATETIME NOT NULL DEFAULT GETDATE(),
  comment_last_update DATETIME,
  account_id INT NOT NULL,
  post_id INT NOT NULL,
  reply_comment_id INT,
  PRIMARY KEY (comment_id),
  FOREIGN KEY (account_id) REFERENCES account(account_id),
  FOREIGN KEY (post_id) REFERENCES post(post_id)
);

ALTER TABLE comment
ADD FOREIGN KEY (reply_comment_id) REFERENCES comment(comment_id);

CREATE TABLE comment_mention
(
  mention_index INT NOT NULL,
  comment_id INT NOT NULL,
  account_id INT NOT NULL,
  PRIMARY KEY (mention_index, comment_id, account_id),
  FOREIGN KEY (comment_id) REFERENCES comment(comment_id),
  FOREIGN KEY (account_id) REFERENCES account(account_id),
  UNIQUE (comment_id)
);

CREATE TABLE like_comment
(
  account_id INT NOT NULL,
  comment_id INT NOT NULL,
  PRIMARY KEY (account_id, comment_id),
  FOREIGN KEY (account_id) REFERENCES account(account_id),
  FOREIGN KEY (comment_id) REFERENCES comment(comment_id)
);

CREATE TABLE report_comment
(
  report_id INT IDENTITY(1,1),
  report_feedback VARCHAR(250) NOT NULL,
  account_id INT NOT NULL,
  comment_id INT NOT NULL,
  report_create_date DATETIME NOT NULL DEFAULT GETDATE(),
  report_status BIT,--('0 accepted', '1 discarded')
  PRIMARY KEY (report_id),
  FOREIGN KEY (account_id) REFERENCES account(account_id),
  FOREIGN KEY (comment_id) REFERENCES comment(comment_id)
);

GO

/*******************************************************************************
	Insert Example Date
*******************************************************************************/

INSERT INTO account(username, password, account_fullname, account_email, account_phone, account_gender, account_dob, account_avatar, account_bio) VALUES
('account1', '1234561', 'Nguyen Van 1', 'nguyenvan1@gmail.com', '0123456781', 0, '2004-05-24', 'default.png', 'bio1'),
('account2', '1234562', 'Nguyen Van 2', 'nguyenvan2@gmail.com', '0123456782', 0, '2004-05-25', 'default.png', 'bio2'),
('account3', '1234563', 'Nguyen Van 3', 'nguyenvan3@gmail.com', '0123456783', 0, '2004-05-26', 'default.png', 'bio3'),
('account4', '1234564', 'Nguyen Van 4', 'nguyenvan4@gmail.com', '0123456784', 0, '2004-05-27', 'default.png', 'bio4'),
('account5', '1234565', 'Nguyen Van 5', 'nguyenvan5@gmail.com', '0123456785', 0, '2004-05-28', 'default.png', 'bio5'),
('account6', '1234566', 'Nguyen Van 6', 'nguyenvan6@gmail.com', '0123456786', 0, '2004-05-29', 'default.png', 'bio6'),
('account7', '1234567', 'Nguyen Van 7', 'nguyenvan7@gmail.com', '0123456787', 0, '2004-05-30', 'default.png', 'bio7'),
('account8', '1234568', 'Nguyen Van 8', 'nguyenvan8@gmail.com', '0123456788', 0, '2004-05-31', 'default.png', 'bio8'),
('account9', '1234569', 'Nguyen Van 9', 'nguyenvan9@gmail.com', '0123456789', 0, '2004-06-01', 'default.png', 'bio9'),
('account10', '12345610', 'Nguyen Van 10', 'nguyenvan10@gmail.com', '01234567810', 0, '2004-06-02', 'default.png', 'bio10'),
('account11', '12345611', 'Nguyen Van 11', 'nguyenvan11@gmail.com', '01234567811', 1, '2004-06-03', 'default.png', 'bio11'),
('account12', '12345612', 'Nguyen Van 12', 'nguyenvan12@gmail.com', '01234567812', 1, '2004-06-04', 'default.png', 'bio12'),
('account13', '12345613', 'Nguyen Van 13', 'nguyenvan13@gmail.com', '01234567813', 1, '2004-06-05', 'default.png', 'bio13'),
('account14', '12345614', 'Nguyen Van 14', 'nguyenvan14@gmail.com', '01234567814', 1, '2004-06-06', 'default.png', 'bio14'),
('account15', '12345615', 'Nguyen Van 15', 'nguyenvan15@gmail.com', '01234567815', 1, '2004-06-07', 'default.png', 'bio15'),
('account16', '12345616', 'Nguyen Van 16', 'nguyenvan16@gmail.com', '01234567816', 1, '2004-06-08', 'default.png', 'bio16'),
('account17', '12345617', 'Nguyen Van 17', 'nguyenvan17@gmail.com', '01234567817', 1, '2004-06-09', 'default.png', 'bio17'),
('account18', '12345618', 'Nguyen Van 18', 'nguyenvan18@gmail.com', '01234567818', 1, '2004-06-10', 'default.png', 'bio18'),
('account19', '12345619', 'Nguyen Van 19', 'nguyenvan19@gmail.com', '01234567819', 1, '2004-06-11', 'default.png', 'bio19'),
('account20', '12345620', 'Nguyen Van 20', 'nguyenvan20@gmail.com', '01234567820', 1, '2004-06-12', 'default.png', 'bio20');

INSERT INTO account(username, password, account_fullname, account_email, account_phone, account_gender, account_dob, account_avatar, account_bio, account_role) VALUES
('admin1', '123456', 'Nguyen Van A', 'nguyenvana@gmail.com', '012345678', 0, '2004-11-25', 'default.png', 'bio1', 'admin');

INSERT INTO post(post_content, account_id, post_create_date, post_status, post_privacy) VALUES
('<p>post 1</p>', 1, '2025-05-24', 'published', 'public'),
('<p>post 2</p>', 1, '2025-05-25', 'published', 'public'),
('<p>post 3</p>', 1, '2025-05-26', 'published', 'public'),
('<p>post 4</p>', 1, '2025-05-27', 'published', 'public'),
('<p>post 5</p>', 1, '2025-05-28', 'published', 'public'),
('<p>post 6</p>', 2, '2025-05-29', 'published', 'public'),
('<p>post 7</p>', 2, '2025-05-30', 'published', 'public'),
('<p>post 8</p>', 2, '2025-05-31', 'published', 'friend'),
('<p>post 9</p>', 2, '2025-06-01', 'published', 'friend'),
('<p>post 10</p>', 3, '2025-06-02', 'published', 'friend'),
('<p>post 11</p>', 3, '2025-06-03', 'published', 'friend'),
('<p>post 12</p>', 3, '2025-06-04', 'published', 'friend'),
('<p>post 13</p>', 3, '2025-06-05', 'published', 'friend'),
('<p>post 14</p>', 3, '2025-06-06', 'drafted', 'private'),
('<p>post 15</p>', 3, '2025-06-07', 'drafted', 'private'),
('<p>post 16</p>', 3, '2025-06-08', 'hidden', 'friend'),
('<p>post 17</p>', 3, '2025-06-09', 'hidden', 'friend'),
('<p>post 18</p>', 3, '2025-06-10', 'rejected', 'public'),
('<p>post 19</p>', 3, '2025-06-11', 'rejected', 'public'),
('<p>post 20</p>', 3, '2025-06-12', 'rejected', 'public');

INSERT INTO post_image(post_id, post_image) VALUES
(1, 'default.png'),
(1, 'default2.png'),
(2, 'default.png');

INSERT INTO hashtag(hashtag_name) VALUES
('fptu'),
('j4f'),
('it');

INSERT INTO tag_hashtag(hashtag_id, post_id, hashtag_index) VALUES
(1, 1, 1),
(2, 1, 9),
(3, 1, 4);

INSERT INTO post_mention(account_id, post_id, mention_index) VALUES
(2, 1, 2),
(3, 1, 9);

INSERT INTO like_post(post_id, account_id) VALUES (1, 1);
INSERT INTO like_post(post_id, account_id) VALUES (1, 2);
INSERT INTO like_post(post_id, account_id) VALUES (1, 3);
INSERT INTO like_post(post_id, account_id) VALUES (1, 4);
INSERT INTO like_post(post_id, account_id) VALUES (1, 5);
INSERT INTO like_post(post_id, account_id) VALUES (1, 6);
INSERT INTO like_post(post_id, account_id) VALUES (1, 7);
INSERT INTO like_post(post_id, account_id) VALUES (1, 8);
INSERT INTO like_post(post_id, account_id) VALUES (1, 9);
INSERT INTO like_post(post_id, account_id) VALUES (1, 10);
INSERT INTO like_post(post_id, account_id) VALUES (1, 11);
INSERT INTO like_post(post_id, account_id) VALUES (1, 12);
INSERT INTO like_post(post_id, account_id) VALUES (1, 13);
INSERT INTO like_post(post_id, account_id) VALUES (1, 14);
INSERT INTO like_post(post_id, account_id) VALUES (1, 15);
INSERT INTO like_post(post_id, account_id) VALUES (1, 16);
INSERT INTO like_post(post_id, account_id) VALUES (2, 6);
INSERT INTO like_post(post_id, account_id) VALUES (2, 7);
INSERT INTO like_post(post_id, account_id) VALUES (2, 8);
INSERT INTO like_post(post_id, account_id) VALUES (2, 9);
INSERT INTO like_post(post_id, account_id) VALUES (2, 10);
INSERT INTO like_post(post_id, account_id) VALUES (2, 11);
INSERT INTO like_post(post_id, account_id) VALUES (2, 12);
INSERT INTO like_post(post_id, account_id) VALUES (3, 10);
INSERT INTO like_post(post_id, account_id) VALUES (3, 11);
INSERT INTO like_post(post_id, account_id) VALUES (3, 12);
INSERT INTO like_post(post_id, account_id) VALUES (3, 13);
INSERT INTO like_post(post_id, account_id) VALUES (3, 14);
INSERT INTO like_post(post_id, account_id) VALUES (3, 15);
INSERT INTO like_post(post_id, account_id) VALUES (3, 16);
INSERT INTO like_post(post_id, account_id) VALUES (3, 17);
INSERT INTO like_post(post_id, account_id) VALUES (3, 18);
INSERT INTO like_post(post_id, account_id) VALUES (3, 19);
INSERT INTO like_post(post_id, account_id) VALUES (3, 20);

INSERT INTO repost(post_id, account_id) VALUES (1, 2);
INSERT INTO repost(post_id, account_id) VALUES (1, 3);
INSERT INTO repost(post_id, account_id) VALUES (1, 4);
INSERT INTO repost(post_id, account_id) VALUES (2, 3);
INSERT INTO repost(post_id, account_id) VALUES (2, 4); 

INSERT INTO report_post(report_feedback, account_id, post_id) VALUES ('feedback 1', 3, 14);
INSERT INTO report_post(report_feedback, account_id, post_id) VALUES ('feedback 2', 4, 14);
INSERT INTO report_post(report_feedback, account_id, post_id) VALUES ('feedback 3', 5, 15);
INSERT INTO report_post(report_feedback, account_id, post_id) VALUES ('feedback 4', 6, 15);

INSERT INTO report_account(report_feedback, account_id, reported_account_id) VALUES ('feedback account 1', 3, 14);
INSERT INTO report_account(report_feedback, account_id, reported_account_id) VALUES ('feedback account 2', 4, 14);
INSERT INTO report_account(report_feedback, account_id, reported_account_id) VALUES ('feedback account 3', 5, 15);
INSERT INTO report_account(report_feedback, account_id, reported_account_id) VALUES ('feedback account 4', 6, 15);

INSERT INTO interact(account_id_1, account_id_2, interact_status) VALUES (1, 2, 'friend');
INSERT INTO interact(account_id_1, account_id_2, interact_status) VALUES (1, 3, 'follow');
INSERT INTO interact(account_id_1, account_id_2, interact_status) VALUES (1, 4, 'follow');
INSERT INTO interact(account_id_1, account_id_2, interact_status) VALUES (2, 1, 'friend');
INSERT INTO interact(account_id_1, account_id_2, interact_status) VALUES (2, 3, 'follow');
INSERT INTO interact(account_id_1, account_id_2, interact_status) VALUES (20, 3, 'block');

INSERT INTO comment(comment_content, account_id, post_id) VALUES ('<p>comment 1</p>', 2, 1);
INSERT INTO comment(comment_content, account_id, post_id) VALUES ('<p>comment 2</p>', 3, 1);
INSERT INTO comment(comment_content, account_id, post_id) VALUES ('<p>comment 3</p>', 4, 1);
INSERT INTO comment(comment_content, account_id, post_id, reply_comment_id) VALUES ('<p>comment 4</p>', 5, 1, 1);
INSERT INTO comment(comment_content, account_id, post_id, reply_comment_id) VALUES ('<p>comment 5</p>', 6, 1, 1);
INSERT INTO comment(comment_content, account_id, post_id, reply_comment_id) VALUES ('<p>comment 6</p>', 7, 1, 1);
INSERT INTO comment(comment_content, account_id, post_id, reply_comment_id) VALUES ('<p>comment 7</p>', 8, 1, 2);
INSERT INTO comment(comment_content, account_id, post_id, reply_comment_id) VALUES ('<p>comment 8</p>', 9, 1, 2);

INSERT INTO comment_mention(account_id, comment_id, mention_index) VALUES
(1, 1, 1),
(2, 2, 1);

INSERT INTO notification(notification_title, notification_content) VALUES
('TEST', '<p> notification 1 </p>'),
('TEST2', '<p> notification 2 </p>');

INSERT INTO check_notification(account_id, notification_id) VALUES
(1, 1),
(1, 2),
(2, 1),
(3, 1),
(4, 1),
(5, 1),
(6, 1),
(7, 1),
(8, 1);

INSERT INTO friend_request(friend_request_content, send_account_id, receive_account_id, friend_request_status) VALUES
('would to be my friend', 1, 2, 'accepted'),
('I see your post is interesting', 3, 2, 'sended');

INSERT INTO message(message_content, send_account_id, receive_account_id, message_status) VALUES
('<p>Good day</p>', 1, 2, 'received'),
('<p>Good morning</p>', 2, 1, 'received');

INSERT INTO like_comment(account_id, comment_id) VALUES
(1, 1),
(2, 1),
(3, 1),
(4, 1),
(5, 1),
(5, 2),
(3, 2),
(4, 2);

INSERT INTO report_comment(report_feedback, account_id, comment_id) VALUES ('feedback comment 1', 3, 5);
INSERT INTO report_comment(report_feedback, account_id, comment_id) VALUES ('feedback comment 2', 4, 5);
INSERT INTO report_comment(report_feedback, account_id, comment_id) VALUES ('feedback comment 3', 5, 5);
INSERT INTO report_comment(report_feedback, account_id, comment_id) VALUES ('feedback comment 4', 6, 5);

GO