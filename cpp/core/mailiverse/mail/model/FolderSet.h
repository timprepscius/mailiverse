/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_Folderset_h__
#define __mailiverse_mail_model_Folderset_h__

#include <assert.h>
#include "mailiverse/utilities/Algorithm.h"
#include "Folder.h"
#include "FolderImplBase.h"
#include "Library.h"
#include "Types.h"

namespace mailiverse {
namespace mail {
namespace model {

class FolderSet : public Folder
{
	DECLARE_ITEM(FolderSet);

protected:
	RecordList parts;
	int numConversations;
	cache::Type childType;
	
	FolderPtr precachedFolder;

public:
	FolderSet(const cache::Type &childType)
	{
		this->numConversations = 0;
		this->childType = childType;
	}

	void preCacheMostRecentFolder ()
	{
		if (!parts.isEmpty())
			precachedFolder = getLibrary()->getFolder(childType, parts.get(0));
	}

	void addFolderIdFront (const Record &id)
	{
		parts.insert(parts.begin(), id);
	}

	void addFolderIdBack (const Record &id)
	{
		parts.push_back(id);
	}
	
	const RecordList &getFolderIds()
	{
		return parts;
	}

	void setFolderIds (const RecordList &parts)
	{
		this->parts = parts;
	}

	void addFolderFront (Folder *folder)
	{
		addFolderIdFront(Record(folder->getID(), Date::None));
		markDirty();
	}
	
	void addFolderBack (Folder *folder)
	{
		addFolderIdBack(Record(folder->getID(), Date::None));
		markDirty();
	}

	void removeFolder(Folder *folder) 
	{
		auto i = utilities::findByFirst(parts, folder->getID());
		if (i == parts.end())
			return;

		parts.erase(i);
		markDirty();

		// IMPLEMENT ME!!!
		assert(false);
/*
		if (folder.isLoaded())
		folder.markDeleted();
		else
		folder.getLoadCallbacks().addCallback(new Single(markDeleted_()));    
*/

	}

	virtual void onDeleting () override
	{
		List<FolderPtr> parts = getFolders();
		for(auto &part : parts)
			removeFolder(part);
	}

	List<FolderPtr> getFolders ()
	{
		List<FolderPtr> f;

		for (auto &id : parts)
			f.add(getLibrary()->getFolder(childType, id));

		return f;
	}

	virtual Vector<ConversationPtr> getConversations(int from, int length) override
	{
		int left = from;
		int right = from + length;
		int position = 0;
		
		Vector<ConversationPtr> result;

		for (auto &id : parts)
		{
			FolderPtr f = getLibrary()->getFolder(childType, id);

			if (f->isLoaded())
			{
				int numConversations = f->getNumConversations();
				if (left < numConversations + position)
				{
					int contribution = numConversations - (left-position);
					contribution = std::min(contribution, right-left);
					
					result.addAll(f->getConversations(left-position, contribution));
					left += contribution;
					position = left;
					
					LogDebug (mailiverse::mail::model::FolderSet::getConversationsNoFilter, "got " << contribution);
				}
				else 
				{
					LogDebug (mailiverse::mail::model::FolderSet::getConversationsNoFilter, "skipping " << numConversations);
					position += numConversations;
				}
				
				if (left >= right)
					break;
			}
			else
				break;
		}

		LogDebug (mailiverse::mail::model::FolderSet::getConversationsNoFilter, "result has " << result.size());
		return result;
	}

	virtual bool hasConversation(Conversation *conversation) override
	{
		for (auto &id : parts)
		{
			FolderPtr f = getLibrary()->getFolder(childType, id);

			if (f->isLoaded())
				if (f->hasConversation(conversation))
					return true;
		}

		return false;
	}

	virtual void conversationAdded(Conversation *conversation) override
	{
		FolderPtr first = !parts.empty() ? getLibrary()->getFolder(childType, parts.front()) : FolderPtr(NULL);

		if (parts.empty() || first->isFull() || !first->isLoaded())
		{
			precachedFolder = getLibrary()->newFolder(childType, new FolderDefinition(getID().toFileSystemSafe() + ":part"));
			addFolderIdFront(Record(precachedFolder->getID(), Date::None));
			first = precachedFolder;
		}

		first->conversationAdded(conversation);

		numConversations++;
		markDirty();
	}
	
	virtual void removeConversations (const Set<cache::ID> &ids) override
	{
		Set<cache::ID> removed;
		
		for (auto &id : parts)
		{
			FolderPtr f = getLibrary()->getFolder(childType, id);
			FolderImplBase *b = dynamic_cast<FolderImplBase *>((Folder *)f);
			if (b && b->isLoaded())
			{
				bool modified = false;
				for (auto &id : ids)
				{
					if (b->hasConversationId(id))
					{
						modified = true;
						b->removeConversationId(id);
						numConversations--;
						removed.add(id);
					}
				}
				
				if (modified)
					b->markDirty();
			}
			
			if (ids.size() == removed.size())
				break;
		}
		
		if (!removed.empty())
		{
			markDirty();
		}
	}

	virtual void conversationDeleted(Conversation *conversation) override
	{
		for (auto &id : parts)
		{
			FolderPtr f = getLibrary()->getFolder(childType, id);

			if (f->isLoaded())
			{
				if (f->hasConversation(conversation))
				{
					f->conversationDeleted(conversation);
					numConversations--;
					markDirty();

					break;
				}
			}
		}

	}

	virtual int getNumConversations () override
	{
		return numConversations;
	}

	void setNumConversations (int numConversations)
	{
		this->numConversations = numConversations;
	}

	virtual RecordList &getConversationIds() override
	{
		assert(false);
	}

	virtual void addConversationId(const Record &) override
	{
		assert(false);
	}

	virtual bool isFull() override
	{
		assert(false);
		return false;
	}

	virtual void setConversationIds(const RecordList &conversations) override
	{
		assert(false);
	}

	ConversationPtr getMatchingConversation (Header *header)
	{
		for (auto &id : parts)
		{
			FolderPtr f = getLibrary()->getFolder(childType, id);

			if (f->isLoaded())
			{
				ConversationPtr c = f->getMatchingConversation(header);
				if (c)
				{
					LogDebug(
						mailiverse::mail::model::FolderSet, 
						"getMatchingConversation for header " <<
							(header->getSubject() ? ((std::string)*header->getSubject()) : (std::string("NO SUBJECT"))) <<
							(c->getHeader()->getSubject() ? ((std::string)*c->getHeader()->getSubject()) : (std::string("NO SUBJECT")))
					);
					return c;
				}
			}
		}

		return NULL;
	}
};

DECLARE_SMARTPTR(FolderSet);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* __mailiverse_mail_model_Folderset_h__ */
