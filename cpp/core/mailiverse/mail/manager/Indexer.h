/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_Indexer_h__
#define __mailiverse_mail_manager_Indexer_h__

#include "mailiverse/utilities/SmartPtr.h"
#include "Servent.h"
#include "../model/Folder.h"
#include "../model/FolderFilter.h"
#include "../model/FolderFilterSet.h"
#include "../model/FolderMaster.h"
#include "../model/FolderRepository.h"
#include "Direction.h"

namespace mailiverse {
namespace mail {
namespace manager {

class Indexer : public Servent
{
public:
	struct KnownFolderIds {
		static const cache::ID 
			RootCache,
			SystemFoldersId,
			RepositoryId,
			AllId,
			InboxId,
			SentId,
			DraftsId,
			SpamId,
			TrashId,
			UserFoldersId;
	};

protected:
	model::FolderMasterPtr systemFolders;
	model::FolderRepositoryPtr repository;
	model::FolderFilterPtr spam;
	model::FolderFilterSetPtr userFolders;

public:
	Indexer();
	virtual ~Indexer();

	void initialize ();
	void onFirstRun ();

	model::ConversationPtr addMail (model::Mail *mail);
	void markSpam (model::Conversation *conversation, bool value);
	
	void addFailure (model::Mail *mail);
	void addFailure (const String &externalKey, const Date &date);
	void addFailure (Direction::Enum direction, const String &path, const Date &date, const Exception &e);
	void addDuplicate (const String &externalKey, const Date &date);

	bool containsExternalKey (const String &externalKey);
	bool containsUIDL(const String &uidl);

	void addConversation (model::Conversation *conversation);
	void removeConversation (model::Conversation *conversation);
	void conversationChanged (model::Conversation *conversation);

	void replyMail (model::Conversation *conversation, model::Mail *mail);
	model::ConversationPtr newMail (model::Mail *mail);

	model::FolderPtr getSystemFolder (const String &folderName);
	List<model::FolderPtr> getSystemFolders ();
	List<model::FolderPtr> getUserFolders();
	
	model::Folder *getRepository ();
	model::Folder *getInbox ();
	model::FolderFilter *getSpam();
};

DECLARE_SMARTPTR(Indexer);

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_manager_Indexer_h__ */
