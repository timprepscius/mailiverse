/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef CONVERSATION_H_
#define CONVERSATION_H_

#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/utilities/Log.h"
#include "Model.h"
#include "Record.h"
#include "Mail.h"
#include "Header.h"
#include "mailiverse/utilities/Algorithm.h"

namespace mailiverse {
namespace mail {
namespace model {

class Conversation : public Model
{
	DECLARE_ITEM(Conversation);

protected:
	HeaderPtr header;
	RecordList items;

public:
	Conversation()	 
	{
		LogDebug(mailiverse::mail::model::Conversation, "construct");
		recomputeHeader();
	}
	
	virtual ~Conversation ()
	{
		LogDebug(mailiverse::mail::model::Conversation, "destruct");
	}
	
	void onLoaded () override
	{
		getLibrary()->onLoaded(this);
	}

	void recomputeHeader ()
	{
		header = new Header();
		header->setDictionary(new Dictionary());
		header->setAuthors(IdentityList());
		header->setRecipients(new Recipients());
		header->setTransportState(TransportState::NONE());

		for (auto &p : items)
		{
			MailPtr m = getLibrary()->getMail(p);
			if (m->isLoaded())
				accumulate(m);
		}
	}

	void accumulate(Mail *m)
	{
		Header *h = m->getHeader();

		if (h->getAuthor())
			header->getAuthors().insert(h->getAuthor());

		if (header->getDate() || h->getDate() > (header->getDate()))
		{
			header->setDate(h->getDate());
			header->setBrief(h->getBrief());
			header->setSubject(h->getSubjectExcludingReplyPrefix());
		}

		if (h->getRecipients())
			header->getRecipients()->add(h->getRecipients());

		header->getDictionary()->add(m);
		header->getTransportState()->mark(h->getTransportState());
		header->markState(TransportState::READ, false);
	}

	List<MailPtr> getItems ()
	{
		List<MailPtr> result;
		for (auto &p : items)
		{
			MailPtr m = getLibrary()->getMail(p);
			result.add(m);
		}

		return result;
	}

	int getNumItems ()
	{
		return items.size();
	}
	
	MailPtr getItem (int i)
	{
		return getLibrary()->getMail(items.get(i));
	}

	const RecordList &getItemIds ()
	{
		return items;
	}

	void setItemIds (const RecordList &items)
	{
		this->items = items;
	}

	void addItemId (const Record &record)
	{
		items.add(record);
		items.sort(utilities::ComparatorPairSecond<Record>());
	}

	void removeItemId (const cache::ID &id)
	{
		utilities::removeByFirst(items, id);
	}

	void addItem (Mail *mail)
	{
		addItemId (Record(mail->getID(), *mail->getHeader()->getDate()));
		accumulate (mail);

		markDirty();
	}

	void removeItem (Mail *mail)
	{
		removeItemId (mail->getID());
		recomputeHeader();

		markDirty();
	}

	void itemChanged (Mail *mail)
	{
		auto i = utilities::findByFirst(items, mail->getID());
		if (i!=items.end())
			i->second = *mail->getHeader()->getDate();

		items.sort(utilities::ComparatorPairSecond<Record>());
		recomputeHeader();

		markDirty();
	}

	Header *getHeader ()
	{
		return header;
	}

	void setHeader (Header *header)
	{
		this->header = header;
	}
	
	void unmarkState (const String &state)
	{
		markState(state, false);
	}
	
	void markState (const String &state, bool value=true)
	{
		if (getHeader()->hasState(state) != value)
		{
			getHeader()->markState(state, value);
			markDirty();
		}
	}
};

DECLARE_SMARTPTR(Conversation);
DECLARE_WEAKPTR(Conversation);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* CONVERSATION_H_ */
