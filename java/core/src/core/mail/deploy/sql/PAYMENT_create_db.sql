CREATE DATABASE `payment` DEFAULT CHARACTER SET 'utf8';

USE payment;

CREATE TABLE `expirations` (
  `email` varchar(255) NOT NULL,
  `expiration` date NOT NULL,
  PRIMARY KEY (`email`)
) ENGINE=InnoDB;

CREATE user 'payment'@'localhost' identified by '__PASSWORD__';
GRANT all on payment.* to 'payment'@'localhost';