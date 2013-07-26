/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_FolderSimple_h__
#define __mailiverse_mail_model_FolderSimple_h__

#include "FolderSet.h"

namespace mailiverse {
namespace mail {
namespace model {

class FolderFilter : public FolderSet
{
public:
	typedef FolderSet Super;

public:
	FolderFilter() :
		FolderSet(Types::FolderPart)
	{}

	virtual bool matchesFilter (Conversation *conversation)
	{
		return true;
	}

	virtual void conversationAdded (Conversation *conversation) override
	{
		if (matchesFilter(conversation))
			Super::conversationAdded(conversation);
	}

	virtual void conversationDeleted (Conversation *conversation) override
	{
		if (hasConversation(conversation))
			Super::conversationDeleted(conversation);
	}

	void manuallyAdd (Conversation *conversation)
	{
		if (!hasConversation(conversation))
		{
			folderDefinition->conversationAdded(conversation);
			Super::conversationAdded(conversation);
		}
	}

	void manuallyRemove (Conversation *conversation)
	{
		if (hasConversation(conversation))
		{
			folderDefinition->conversationDeleted(conversation);
			Super::conversationDeleted(conversation);
		}
	}

};

DECLARE_SMARTPTR(FolderFilter);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* __mailiverse_mail_model_FolderSimple_h__ */
