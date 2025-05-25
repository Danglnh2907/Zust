USE social_media

--View posts in news feed
SELECT account.*, post.*, post_image.post_image, like_number, repost_number, comment_number, tag_hashtag.hashtag_id, tag_hashtag.hashtag_index, post_mention.account_id, post_mention.mention_index FROM post
JOIN account ON post.account_id = account.account_id
LEFT JOIN post_image ON post.post_id = post_image.post_id
LEFT JOIN tag_hashtag ON post.post_id = tag_hashtag.post_id
LEFT JOIN post_mention ON post_mention.post_id = post.post_id
LEFT JOIN 
(SELECT post_id, COUNT(*) AS like_number FROM like_post
GROUP BY post_id) 
AS like_post ON post.post_id = like_post.post_id
LEFT JOIN 
(SELECT post_id, COUNT(*) AS repost_number FROM repost
GROUP BY post_id)
AS repost ON post.post_id = repost.post_id
LEFT JOIN
(SELECT post_id, COUNT(*) AS comment_number FROM comment
GROUP BY post_id)
AS comment ON post.post_id = comment.post_id
WHERE post.post_status = 'published' AND NOT post.post_id IN (SELECT post_id FROM view_post WHERE account_id = 1)

--View post
SELECT account.*, post.*, post_image.post_image, like_number, repost_number, comment_number, tag_hashtag.hashtag_id, tag_hashtag.hashtag_index, post_mention.account_id, post_mention.mention_index FROM post
JOIN account ON post.account_id = account.account_id
LEFT JOIN post_image ON post.post_id = post_image.post_id
LEFT JOIN tag_hashtag ON post.post_id = tag_hashtag.post_id
LEFT JOIN post_mention ON post_mention.post_id = post.post_id
LEFT JOIN 
(SELECT post_id, COUNT(*) AS like_number FROM like_post
GROUP BY post_id) 
AS like_post ON post.post_id = like_post.post_id
LEFT JOIN 
(SELECT post_id, COUNT(*) AS repost_number FROM repost
GROUP BY post_id)
AS repost ON post.post_id = repost.post_id
LEFT JOIN
(SELECT post_id, COUNT(*) AS comment_number FROM comment
GROUP BY post_id)
AS comment ON post.post_id = comment.post_id
WHERE post.post_id = 1

--Register with form
INSERT INTO account(username, password, account_fullname, account_gender, account_dob, account_email, account_phone)
VALUES('test1', '123456', 'testname1', 1, NULL, NULL, NULL)

--Register with Google account


--Email verification


--Login with form
SELECT * FROM account
WHERE username = 'account1' AND password = '1234561'

--Login with Google account
SELECT * FROM account
WHERE account_email = 'nguyenvan1@gmail.com'

--Logout


--Edit profile
UPDATE account
SET account_fullname = 'name change', account_gender = 0, account_dob = '2005-1-12', account_email = 'mail change', account_phone = '00000001', account_bio = 'update bio', account_avatar = 'default.png', account_status = 'inactive'
WHERE account_id = 20

--Setting


--Lock account
UPDATE account
SET account_status = 'inactive'
WHERE account_id = 20

--View all friends
SELECT * FROM interact
JOIN account ON interact.account_id_2 = account.account_id
WHERE interact.account_id_1 = 1 AND interact.interact_status = 'friend' AND account.account_status = 'active'

--View social credit


--Create post
INSERT INTO post(account_id, post_content, post_status, post_privacy) VALUES
(20, '<p>new post</p>', 'published', 'public');

--View all posts
SELECT account.*, post.*, post_image.post_image, like_number, repost_number, comment_number, tag_hashtag.hashtag_id, tag_hashtag.hashtag_index, post_mention.account_id, post_mention.mention_index FROM post
JOIN account ON post.account_id = account.account_id
LEFT JOIN post_image ON post.post_id = post_image.post_id
LEFT JOIN tag_hashtag ON post.post_id = tag_hashtag.post_id
LEFT JOIN post_mention ON post_mention.post_id = post.post_id
LEFT JOIN 
(SELECT post_id, COUNT(*) AS like_number FROM like_post
GROUP BY post_id)
AS like_post ON post.post_id = like_post.post_id
LEFT JOIN 
(SELECT post_id, COUNT(*) AS repost_number FROM repost
GROUP BY post_id)
AS repost ON post.post_id = repost.post_id
LEFT JOIN
(SELECT post_id, COUNT(*) AS comment_number FROM comment
GROUP BY post_id)
AS comment ON post.post_id = comment.post_id
WHERE post.account_id = 1 AND NOT post.post_status = 'deleted'
ORDER BY post_create_date DESC

--Edit post
UPDATE post
SET post_content = '<p>change post</p>', post_status = 'hidden', post_privacy = 'friend', post_last_update = GETDATE()
WHERE post_id = 20

--Delete post
UPDATE post
SET post_status = 'deleted'
WHERE post_id = 20

--Create new comment
INSERT INTO comment(account_id, post_id, comment_content) VALUES
(1, 20, '<p>comment</p>')

--Edit comment
UPDATE comment
SET comment_content = '<p>change</p>', comment_last_update = GETDATE()
WHERE comment_id = 5

--Delete comment
UPDATE comment
SET comment_status = 1
WHERE comment_id = 5

--View comment

--View all comments
SELECT * FROM comment
WHERE comment_status = 0 AND post_id = 1

--Reply to comments
INSERT INTO comment(account_id, post_id, comment_content, reply_comment_id) VALUES
(1, 20, '<p>comment</p>', 5);

--Like comment
INSERT INTO like_comment(comment_id, account_id) VALUES
(5, 5);

--Tag user
INSERT INTO comment_mention(comment_id, account_id, mention_index) VALUES
(5, 1, 10);

--Report
INSERT INTO report_comment(comment_id, account_id, report_feedback) VALUES
(5, 1, 'feedback');

INSERT INTO report_account(reported_account_id, account_id, report_feedback) VALUES
(20, 1, 'feedback');

--Like post
INSERT INTO like_post(post_id, account_id) VALUES
(20, 1);

--Repost post
INSERT INTO repost(post_id, account_id) VALUES
(20, 1);

--Report post
INSERT INTO report_post(post_id, account_id, report_feedback) VALUES
(20, 1, 'feedback');

--View other profile
SELECT account.*, following_number, follower_number, friend_number FROM account
LEFT JOIN 
(SELECT account_id_1, COUNT(*) AS following_number FROM interact
JOIN account ON  account.account_id = interact.account_id_2
WHERE interact_status = 'follow' AND account.account_status = 'active'
GROUP BY account_id_1)
AS [following] ON account.account_id = following.account_id_1
LEFT JOIN 
(SELECT account_id_2, COUNT(*) AS follower_number FROM interact
JOIN account ON  account.account_id = interact.account_id_1
WHERE interact_status = 'follow' AND account.account_status = 'active'
GROUP BY account_id_2)
AS [follower] ON account.account_id = follower.account_id_2
LEFT JOIN 
(SELECT account_id_1, COUNT(*) AS friend_number FROM interact
JOIN account ON  account.account_id = interact.account_id_2
WHERE interact_status = 'friend' AND account.account_status = 'active'
GROUP BY account_id_1)
AS [friend] ON account.account_id = friend.account_id_1
WHERE account.account_id = 1

--Add friend
INSERT INTO friend_request(send_account_id, receive_account_id, friend_request_content) VALUES
(19, 20, 'I see you interesting');

UPDATE friend_request
SET friend_request_status = 'accepted'
WHERE send_account_id = 19 AND receive_account_id = 20

INSERT INTO interact(account_id_1, account_id_2, interact_status) VALUES
(19, 20, 'friend'),
(20, 19, 'friend');

--Unfriend
DELETE FROM interact
WHERE (account_id_1 = 19 AND account_id_2 = 20) OR (account_id_1 = 20 AND account_id_2 = 19)

--View list of friends
SELECT account.* FROM interact
JOIN account ON  account.account_id = interact.account_id_2
WHERE interact_status = 'friend' AND account.account_status = 'active' AND interact.account_id_1 = 1

--Send message
INSERT INTO message(send_account_id, receive_account_id, message_content) VALUES
(1, 2, '<p>new message</p>');

--View chat history
SELECT * FROM message
WHERE (send_account_id = 1 AND receive_account_id = 2) OR (send_account_id = 2 AND receive_account_id = 1)
ORDER BY message_create_date DESC

--Forget password


--Search user by username
SELECT * FROM account
WHERE account_fullname LIKE '%1%'

--Search posts by hashtags
SELECT * FROM hashtag
JOIN tag_hashtag ON hashtag.hashtag_id = tag_hashtag.hashtag_id
JOIN post ON tag_hashtag.post_id = post.post_id
JOIN account ON post.account_id = account.account_id
LEFT JOIN post_image ON post.post_id = post_image.post_id
LEFT JOIN 
(SELECT post_id, COUNT(*) AS like_number FROM like_post
GROUP BY post_id)
AS like_number ON post.post_id = like_number.post_id
LEFT JOIN 
(SELECT post_id, COUNT(*) AS repost_number FROM repost
GROUP BY post_id)
AS repost_number ON post.post_id = repost_number.post_id
LEFT JOIN
(SELECT post_id, COUNT(*) AS comment_number FROM comment
GROUP BY post_id)
AS comment_number ON post.post_id = comment_number.post_id
WHERE hashtag.hashtag_name = 'j4f'

--View all liked posts
SELECT account.*, post.*, post_image.post_image, like_number, repost_number, comment_number FROM post
JOIN account ON post.account_id = account.account_id
LEFT JOIN post_image ON post.post_id = post_image.post_id
LEFT JOIN 
(SELECT post_id, COUNT(*) AS like_number FROM like_post
GROUP BY post_id)
AS like_number ON post.post_id = like_number.post_id
LEFT JOIN 
(SELECT post_id, COUNT(*) AS repost_number FROM repost
GROUP BY post_id)
AS repost_number ON post.post_id = repost_number.post_id
LEFT JOIN
(SELECT post_id, COUNT(*) AS comment_number FROM comment
GROUP BY post_id)
AS comment_number ON post.post_id = comment_number.post_id
JOIN like_post ON like_post.post_id = post.post_id
WHERE post.post_status = 'published' AND like_post.account_id = 1
ORDER BY post_create_date DESC

--View all notifications
SELECT * FROM notification
ORDER BY notification_create_date DESC

--View notification
SELECT * FROM notification
WHERE notification_id = 1

--Mark notification as read
INSERT INTO check_notification(account_id, notification_id) VALUES
(9, 1);

--Mark all notifications as read


--Chat with chatbot


--View all reports
SELECT * FROM report_post
WHERE report_status IS NULL
ORDER BY report_create_date

SELECT * FROM report_account
WHERE report_status IS NULL
ORDER BY report_create_date

SELECT * FROM report_comment
WHERE report_status IS NULL
ORDER BY report_create_date


--Accept report
UPDATE report_post
SET report_status = 0
WHERE report_id = 1

UPDATE report_account
SET report_status = 0
WHERE report_id = 1

UPDATE report_comment
SET report_status = 0
WHERE report_id = 1

--Discard report
UPDATE report_post
SET report_status = 1
WHERE report_id = 1

UPDATE report_account
SET report_status = 1
WHERE report_id = 1

UPDATE report_comment
SET report_status = 1
WHERE report_id = 1

--Ban user
UPDATE account
SET account_status = 'banned'
WHERE account_id = 20

--Delete post
UPDATE post
SET post_status = 'rejected'
WHERE post_id = 20

--Delete comment
UPDATE comment
SET comment_status = 1
WHERE comment_id = 5

--Notify to all users
INSERT INTO notification(notification_title, notification_content) VALUES
('NEW NOTIFICATION', '<p>something</p>');

