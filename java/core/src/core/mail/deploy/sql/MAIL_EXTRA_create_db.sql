CREATE DATABASE `mail_extra` DEFAULT CHARACTER SET 'utf8';

USE mail_extra;

CREATE TABLE expirations LIKE payment.expirations;
INSERT INTO expirations SELECT * FROM payment.expirations;

CREATE USER 'mail_extra'@'localhost' identified by '__PASSWORD__';
CREATE USER 'mail_extra'@'192.168.1.109' identified by '__PASSWORD__';
GRANT all on mail_extra.* to 'mail_extra'@'localhost';
GRANT all on mail_extra.* to 'mail_extra'@'192.168.1.109';
