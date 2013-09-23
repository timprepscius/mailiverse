# this is really lame and I know it
# but I need to avoid the LIKE clause, until I have time to figure out
# where the function is for proper mysql escaping.

SELECT 
	k,
	mark,
	version,
	LENGTH(v) as size 
FROM 
	key_values 
WHERE 
	user_id = ? AND LOCATE(?,k)=1
