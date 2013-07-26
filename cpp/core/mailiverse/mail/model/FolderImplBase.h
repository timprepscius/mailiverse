/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_FolderImplBase_h__
#define __mailiverse_mail_model_FolderImplBase_h__

#include "Folder.h"
#include "Library.h"
#include "mailiverse/utilities/Algorithm.h"

namespace mailiverse {
namespace mail {
namespace model {

class FolderImplBase : public Folder
{
	DECLARE_ITEM(FolderImplBase);

protected:
	static const int MAX_FOLDER_CONVERSATIONS = 1000;
	RecordList conversations;

public:
	FolderImplBase() {}

	virtual Vector<ConversationPtr> getConversations (int from, int length) override
	{
		Vector<ConversationPtr> result;

		for (int i=from; i<conversations.size() && result.size() < length; ++i)
		{
			ConversationPtr c =
				getLibrary()->getConversation(
					conversations.get(i)
				);

			result.add(c);
		}
		return result;
	}

	bool hasConversationId (const cache::ID &id)
	{
		return utilities::containsByFirst(conversations, id);
	}

	virtual bool hasConversation (Conversation *conversation) override
	{
		return hasConversationId(conversation->getID());
	}

	virtual void addConversationId (const Record &id)
	{
		conversations.add(id);
		conversations.sort(
			utilities::ComparatorReverse<
				utilities::ComparatorPairSecond<Record> 
			>()
		);
	}

	virtual void removeConversationId (const Record::first_type &id)
	{
		utilities::removeByFirst(conversations, id);
	}

	virtual const RecordList &getConversationIds() override
	{
		return conversations;
	}

	virtual void setConversationIds(const RecordList &conversations) override
	{
		this->conversations = conversations;
	}
	
	virtual int getNumConversations () override
	{
		return conversations.size();
	}

	virtual bool isFull () override
	{
		return conversations.size() > MAX_FOLDER_CONVERSATIONS;
	}

	virtual void removeConversations (const Set<cache::ID> &ids) override
	{
		for (auto &id : ids)
			removeConversationId(id);
		markDirty();
	}

	virtual void conversationAdded (Conversation *conversation) override
	{
		addConversationId(Record(conversation->getID(), *conversation->getHeader()->getDate()));
		markDirty();
	}

	virtual void conversationDeleted (Conversation *conversation) override
	{
		removeConversationId(conversation->getID());
		markDirty();
	}

	virtual ConversationPtr getMatchingConversation (Header *header) override
	{
		if (!header->getSubject())
			return NULL;

		StringPtr headerSubject = header->getSubjectExcludingReplyPrefix ();

		for (auto &id : conversations)
		{
			ConversationPtr conversation = getLibrary()->getConversation(id);

			if (conversation->isLoaded())
			{
				Header *compare = conversation->getHeader();
				StringPtr compareSubject = compare->getSubjectExcludingReplyPrefix();

				if (compareSubject)
				{
					if (utilities::toLowerCase(*compareSubject) == utilities::toLowerCase(*headerSubject))
					{
						return conversation;
					}
				}
			}
		}

		LogDebug (mailiverse::mail::model::FolderImplBase, "didn't find match for subject " << header->getSubject());
		return NULL;
	}
};

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* __mailiverse_mail_model_FolderImplBase_h__ */
