SELECT
	device.notification_type,
	device.device_type,
	device.device_id,
	device.mark
FROM
	device
WHERE
	device.email = ?

