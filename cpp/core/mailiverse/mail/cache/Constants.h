/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_cache_Constants_h__
#define __mailiverse_mail_cache_Constants_h__

#include "mailiverse/Types.h"

namespace mailiverse {
namespace mail {
namespace cache {

struct Constants
{
	static const String CHECK_MAIL_LOCK_FILE_NAME;

	static const int 
		FLUSH_LOCK_TIME_SECONDS,
		FLUSH_LOCK_TIME_ALLOWED_BEFORE_RELOCK_SECONDS;
};

} // namespace
} // namespace
} // namespace

#endif