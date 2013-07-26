/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_Events_h__
#define __mailiverse_mail_manager_Events_h__

#include "mailiverse/Types.h"

namespace mailiverse {
namespace mail {
namespace manager {

struct Events
{
	static const String
		Login,
		Initialize_Start,
		Initialize_FirstRun,
		Initialize_IndexedCacheLoadFailed,
		Initialize_IndexedCacheLoadComplete,
		Initialize_FolderLoadComplete,
		Initialized,
		NewMail,
		DeleteMail,
		LoadMail,
		LogNull,
		NewFolder,
		DeleteFolder,
		LoadFolder,
		NewConversation,
		ChangedConversation,
		DeleteConversation,
		InitiateLoadConversation,
		LoadConversation,
		FolderListing,
		SendSucceeded,
		SendFailed,
		CheckRequest,
		CheckBegin,
		CheckSuccess,
		CheckFailure,
		CheckEnd,
		CheckStep,
		UploadBegin,
		UploadEnd,
		DownloadBegin,
		DownloadEnd,
		OriginalLoaded,
		CacheDirty,
		CacheClean,
		CacheFailure,
		CacheBegin,
		CacheEnd,
		CacheSuccess,
		SettingsChanged,
		SettingsLoaded ;
};

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_manager_Events_h__ */
