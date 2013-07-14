DELETE deleted_mail_block.* 
	FROM deleted_mail_block, deleted_user 
	WHERE deleted_user.name=? AND deleted_mail_block.user_id = deleted_user.id;
	
DELETE deleted_key_block.* 
	FROM deleted_key_block, deleted_user 
	WHERE deleted_user.name=? AND deleted_key_block.user_id = deleted_user.id;

DELETE FROM deleted_user WHERE deleted_user.name=?