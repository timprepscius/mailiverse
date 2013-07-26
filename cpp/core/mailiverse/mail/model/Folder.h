/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_Folder_h__
#define __mailiverse_mail_model_Folder_h__

#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/utilities/Log.h"
#include "FolderDefinition.h"
#include "Model.h"


namespace mailiverse {
namespace mail {
namespace model {

class Folder : public Model
{
protected:
	FolderDefinitionPtr folderDefinition;

public:
	Folder ()
	{
		LogDebug(mailiverse::mail::model::Folder, "construct");
	}
	
	virtual ~Folder () 
	{
		LogDebug(mailiverse::mail::model::Folder, "destruct");
	}

	FolderDefinition *getFolderDefinition()
	{
		return folderDefinition;
	}

	void setFolderDefinition(FolderDefinition *folderDefinition)
	{
		this->folderDefinition = folderDefinition;
	}

	const String &getName()
	{
		return folderDefinition->getName();
	}
	
	void onLoaded () override
	{
		getLibrary()->onLoaded(this);
	}


	virtual const RecordList &getConversationIds () = 0;
	virtual void setConversationIds(const RecordList &) = 0;
	virtual void addConversationId (const Record &) = 0;
	virtual int getNumConversations () = 0;
	virtual bool isFull () = 0;

	virtual Vector<ConversationPtr> getConversations (int from, int length) = 0;
	virtual bool hasConversation (Conversation *conversation) = 0;
	virtual void conversationAdded (Conversation *conversation) = 0;
	virtual void conversationDeleted (Conversation *conversation) = 0;
	virtual ConversationPtr getMatchingConversation (Header *header) = 0;
	virtual void removeConversations (const Set<cache::ID> &ids) = 0;
	

	void conversationChanged (Conversation *conversation)
	{
		conversationDeleted(conversation);
		conversationAdded(conversation);
	}
};

DECLARE_SMARTPTR(Folder);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* FOLDER_H_ */
