SET @email = ?, @notification_type = ?, @device_type = ?, @device_id = ?;

DELETE FROM device WHERE device_type = @device_type AND device_id = @device_id;
REPLACE INTO device (email, device_type, device_id, notification_type, mark) VALUES(@email, @device_type, @device_id, @notification_type, now())
