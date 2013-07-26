/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_Manipulations_h__
#define __mailiverse_mail_model_Manipulations_h__

#include "Conversation.h"
#include "FolderFilter.h"

namespace mailiverse {
namespace mail {
namespace model {

class Manipulations 
{
protected:
	static void doMarkState(Conversation *conversation, const String &state, bool value);
	static void doMarkStateAndReindex(Conversation *conversation, const String &state, bool value);

	static void doAddToUserFolders(Conversation *conversation, const Set<FolderFilterPtr> &folders);
	static void doRemoveFromUserFolder(Conversation *conversation, FolderFilter *folder);

public:
	static core::util::Callback<> markState(Conversation *conversation, const String &state, bool value);
	static core::util::Callback<> markStateAndReindex(Conversation *conversation, const String &state, bool value);
	
	static core::util::Callback<> addToUserFolders(Conversation *conversation, const Set<FolderFilterPtr> &folders);
	static core::util::Callback<> removeFromUserFolder(Conversation *conversation, FolderFilter *folder);

} ;

} // namespace
} // namespace
} // namespace

#endif
