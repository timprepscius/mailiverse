/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_Constants_h__
#define __mailiverse_mail_manager_Constants_h__

#include "mailiverse/Types.h"
#include "../cache/ID.h"

namespace mailiverse {
namespace mail {
namespace manager {

struct Constants
{
	static const String
		SMTP_PASSWORD,
		CACHE_DIRECTORY,
		CACHE_PREFIX,
		MAIL_IN_JSON, MAIL_OUT_JSON,
		MAIL_IN_JSON_PREFIX, MAIL_OUT_JSON_PREFIX;

	static const cache::ID
		SETTINGS_KEY, MAIL_KEY, CONVERSATION_KEY, FOLDER_KEY;
						
	static const String
		INDEX_PREFIX, MAIL_PREFIX, CONVERSATION_PREFIX, FOLDER_PREFIX;
		
	static const String
		FOLDER_SYSTEM_ROOT,
		FOLDER_USER_ROOT,
		FOLDER_REPOSITORY,
	
		FOLDER_ALL,
		FOLDER_INBOX,
		FOLDER_SENT,
		FOLDER_SPAM,
		FOLDER_TRASH,
		FOLDER_DRAFTS;

	static const String CHECK_MAIL_LOCK_FILE_NAME;

	static const int 
		CHECK_MAIL_LOCK_TIME_SECONDS,
		CHECK_MAIL_LOCK_TIME_ALLOWED_BEFORE_RELOCK_SECONDS,
		
		NUM_ITEMS_TO_CHECK_IN_ONE_CYCLE;

};

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_manager_Constants_h__ */
