/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Constants.h"
#include "../model/ModelFactory.h"

using namespace mailiverse::mail::manager;
using namespace mailiverse::mail;
using namespace mailiverse;

const String
	Constants::SMTP_PASSWORD = "SMTP_PASSWORD",
	Constants::CACHE_DIRECTORY="Cache",
	Constants::CACHE_PREFIX="Cache/",
	Constants::MAIL_IN_JSON="Mail/In_Json",
	Constants::MAIL_OUT_JSON="Mail/Out_Json",
	Constants::MAIL_IN_JSON_PREFIX="Mail/In_Json/",
	Constants::MAIL_OUT_JSON_PREFIX="Mail/Out_Json/";

const cache::ID
	Constants::SETTINGS_KEY = cache::ID::fromLong(1),
	Constants::MAIL_KEY = cache::ID::fromLong(2),
	Constants::CONVERSATION_KEY = cache::ID::fromLong(3),
	Constants::FOLDER_KEY = cache::ID::fromLong(4);
	
/*
	Constants::SETTINGS_KEY = cache::ID::combine(INDEX_KEY, cache::ID::fromLong(1)),
	Constants::MAIL_KEY = cache::ID::combine(INDEX_KEY, cache::ID::fromLong(2)),
	Constants::CONVERSATION_KEY = cache::ID::combine(INDEX_KEY, cache::ID::fromLong(3)),
	Constants::FOLDER_KEY = cache::ID::combine(INDEX_KEY, cache::ID::fromLong(4));
*/

const String
	Constants::INDEX_PREFIX="I",
	Constants::MAIL_PREFIX="M",
	Constants::CONVERSATION_PREFIX="C",
	Constants::FOLDER_PREFIX = "F";

const String
	Constants::FOLDER_SYSTEM_ROOT = "system_root",
	Constants::FOLDER_USER_ROOT = "user_root",
	Constants::FOLDER_REPOSITORY = "Repository",

	Constants::FOLDER_ALL = "All",
	Constants::FOLDER_INBOX = "Inbox",
	Constants::FOLDER_SENT = "Sent",
	Constants::FOLDER_SPAM = "Spam",
	Constants::FOLDER_TRASH = "Trash",
	Constants::FOLDER_DRAFTS = "Drafts";

const String Constants::CHECK_MAIL_LOCK_FILE_NAME = "checkMail.lock";

const int 
	Constants::CHECK_MAIL_LOCK_TIME_SECONDS = 120,
	Constants::CHECK_MAIL_LOCK_TIME_ALLOWED_BEFORE_RELOCK_SECONDS = 60,
	
	Constants::NUM_ITEMS_TO_CHECK_IN_ONE_CYCLE = 15;
