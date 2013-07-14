REPLACE INTO deleted_mail_block 
	SELECT mail_block.*
	FROM mail_block, user 
	WHERE user.name=? AND mail_block.user_id = user.id;

REPLACE INTO deleted_key_block
	SELECT key_block.*
	FROM key_block, user
	WHERE user.name=? AND key_block.user_id = user.id;
	
REPLACE INTO deleted_user(version, id, name, v, s, mark)
	SELECT version, id, name, v, s, mark
	FROM user
	WHERE user.name=?;
	
DELETE mail_block.* 
	FROM mail_block, user 
	WHERE user.name=? AND mail_block.user_id = user.id;
	
DELETE key_block.* 
	FROM key_block, user 
	WHERE user.name=? AND key_block.user_id = user.id;

DELETE FROM user WHERE user.name=?