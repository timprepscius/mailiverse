/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#include "Manipulations.h"

using namespace mailiverse::mail::model;
using namespace mailiverse::mail;
using namespace mailiverse;

void Manipulations::doMarkState (Conversation *conversation, const String &state, bool value)
{
	conversation->markState(state, value);
}

void Manipulations::doMarkStateAndReindex (Conversation *conversation, const String &state, bool value)
{
	conversation->markState(state, value);
	conversation->getLibrary()->reindexConversation(conversation);
}

void Manipulations::doAddToUserFolders (Conversation *conversation, const Set<FolderFilterPtr> &folders)
{
	for (auto &i : folders)
	{
		assert(i->isLoaded());
		
		if (i->isLoaded())
			i->manuallyAdd(conversation);
	}
}

void Manipulations::doRemoveFromUserFolder (Conversation *conversation, FolderFilter *folder)
{
	assert(folder->isLoaded());
	
	if (folder->isLoaded())
		folder->manuallyRemove(conversation);
}

core::util::Callback<> Manipulations::markState(Conversation *conversation, const String &state, bool value)
{
	ConversationPtr conversationPtr = conversation;
	
	utilities::Binder *binder = 
		utilities::newbind (
			&Manipulations::doMarkState, 
			conversationPtr, state, value
		);

	return core::util::Callback<> (binder);
}

core::util::Callback<> Manipulations::markStateAndReindex(Conversation *conversation, const String &state, bool value)
{
	ConversationPtr conversationPtr = conversation;
	
	utilities::Binder *binder = 
		utilities::newbind (
			&Manipulations::doMarkStateAndReindex, 
			conversationPtr, state, value
		);

	return core::util::Callback<> (binder);
}

core::util::Callback<> Manipulations::addToUserFolders(Conversation *conversation, const Set<FolderFilterPtr> &_folders)
{
	Set<FolderFilterPtr> folders = _folders;
	ConversationPtr conversationPtr = conversation;
	
	utilities::Binder *binder = 
		utilities::newbind (
			&Manipulations::doAddToUserFolders, 
			conversationPtr, folders
		);

	return core::util::Callback<> (binder);
}

core::util::Callback<> Manipulations::removeFromUserFolder(Conversation *conversation, FolderFilter *folder)
{
	ConversationPtr conversationPtr = conversation;
	FolderFilterPtr folderPtr = folder;
	
	utilities::Binder *binder = 
		utilities::newbind (
			&Manipulations::doRemoveFromUserFolder, 
			conversationPtr, folderPtr
		);

	return core::util::Callback<> (binder);
}

