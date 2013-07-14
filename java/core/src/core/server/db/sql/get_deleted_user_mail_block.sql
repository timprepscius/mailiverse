SELECT
	deleted_mail_block.*
FROM
	deleted_user,
	deleted_mail_block
WHERE
	deleted_mail_block.user_id = deleted_user.id AND
	deleted_user.name = ?