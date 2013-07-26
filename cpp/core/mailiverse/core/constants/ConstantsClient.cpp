/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "ConstantsClient.h"

using namespace mailiverse::core::constants;
using namespace mailiverse;

const String
	ConstantsClient::AT_HOST = "@mailiverse.com";

#if defined(_DEBUG) && 0

const String
	ConstantsClient::SERVER = "red",
	ConstantsClient::MAIL_AUTH_HOST = "red",
	ConstantsClient::KEY_AUTH_HOST = "red",
	ConstantsClient::WEB_SERVER_TOMCAT = "http://red:8080/Mailiverse/";

const int
	ConstantsClient::KEY_AUTH_PORT = 7000,
	ConstantsClient::MAIL_AUTH_PORT = 7001;

#else

const String
	ConstantsClient::SERVER = "mail.mailiverse.com",
	ConstantsClient::MAIL_AUTH_HOST = "mail.mailiverse.com",
	ConstantsClient::KEY_AUTH_HOST = "mail.mailiverse.com",
	ConstantsClient::WEB_SERVER_TOMCAT = "https://mail.mailiverse.com/Mailiverse/";

const int
	ConstantsClient::KEY_AUTH_PORT = 7000,
	ConstantsClient::MAIL_AUTH_PORT = 7001;

#endif