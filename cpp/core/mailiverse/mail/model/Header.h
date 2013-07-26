/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef HEADER_H_
#define HEADER_H_

#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/Types.h"
#include "Identity.h"
#include "Dictionary.h"
#include "TransportState.h"
#include "Recipients.h"
#include "Constants.h"
#include "Library.h"

namespace mailiverse {
namespace mail {
namespace model {

class Header
{
protected:
	StringPtr externalKey;
	StringPtr originalKey;
	IdentityPtr author;
	IdentitySet authors;
	StringPtr subject;
	DatePtr date;
	RecipientsPtr recipients;
	DictionaryPtr dictionary;
	TransportStatePtr state;
	StringPtr brief;
	StringPtr uidl;

public:
	Header() {}

	Header (
		String *externalKey, 
		String *originalKey,
		String *uidl, 
		Identity *author, 
		Recipients *recipients, 
		String *subject, 
		Date *date, 
		TransportState *state, 
		String *brief
	)
	{
		this->externalKey = externalKey;
		this->originalKey = originalKey;
		this->uidl = uidl;
		this->author = author;
		this->subject = subject;
		this->date = date;
		this->recipients = recipients;
		this->state = state;
		this->brief = brief;

		assert(date);
		assert(recipients);
		assert(state);
	}

	virtual ~Header() 
	{
	}

	String *getExternalKey ()
	{
		return externalKey;
	}
	
	void setExternalKey (String *externalKey)
	{
		this->externalKey = externalKey;
	}
	
	String *getOriginalKey ()
	{
		return originalKey;
	}
	
	void setOriginalKey (String *originalKey)
	{
		this->originalKey = originalKey;
	}

	Identity *getAuthor ()
	{
		return author;
	}

	IdentityList filterMe (const IdentityList &identities, Identity *me)
	{
		IdentityList filtered;
		for (auto &identity : identities)
		{
			if (identity != me)
			{
				filtered.push_back(identity);
			}
		}

		return filtered;
	}

	IdentityList calculateReplyTo (Library *library)
	{
		IdentityPtr me = library->getIdentity();

		IdentityList results;

		if (getRecipients())
		{
			results = filterMe(getRecipients()->getReplyTo(), me);
			if (!results.empty())
				return results;

			if (getAuthor() == me)
			{
				results = filterMe(getRecipients()->getAll(), me);
				if (!results.empty())
					return results;
			}
		}

		if (getAuthor())
			results.add(getAuthor());

		return results;
	}
	
	IdentityList calculateReplyAll (Library *library)
	{
		IdentityPtr me = library->getIdentity();
		IdentityList results;
		
		if (getRecipients())
			results.addAll(filterMe(getRecipients()->getAll(), me));

		IdentityPtr author = getAuthor();
		if (author && author != me && !results.contains(author))
			results.push_front(author);

		return results;
	}

	void setAuthor (Identity *author)
	{
		this->author = author;
	}

	IdentitySet &getAuthors ()
	{
		return authors;
	}

	void setAuthors (const IdentityList &authors)
	{
		this->authors.insert(authors.begin(), authors.end());
	}
	
	String getAuthorsShortList ()
	{
		List<String> shorts;
		for (auto &i : authors)
			shorts.add(i->getShortName());
			
		return utilities::join(shorts, ", ");
	}

	String *getSubject ()
	{
		return subject;
	}

	String *getSubjectExcludingReplyPrefix ()
	{
		if (!subject)
			return NULL;

		String subject = *this->subject;

		while (utilities::startsWith(utilities::toLowerCase(subject), utilities::toLowerCase(Constants::REPLY_PREFIX)))
		{
			subject = utilities::trim(subject.substr(Constants::REPLY_PREFIX.length()));
		}

		return new String(subject);
	}

	Dictionary *getDictionary ()
	{
		return dictionary;
	}

	void setDictionary (Dictionary *dictionary)
	{
		this->dictionary = dictionary;
	}

	Date *getDate ()
	{
		return date;
	}

	void setDate (Date *date)
	{
		this->date = date;
	}

	Recipients *getRecipients ()
	{
		return recipients;
	}

	void setRecipients (Recipients *recipients)
	{
		this->recipients = recipients;
	}

	void setSubject(String *subject)
	{
		this->subject = subject;
	}

	void setTransportState (TransportState *state)
	{
		this->state = state;
	}

	TransportState *getTransportState ()
	{
		return state;
	}

	bool hasState(const String &flag)
	{
		return state->has(flag);
	}
	
	void unmarkState (const String &flag)
	{
		markState(flag, false);
	}

	void markState (const String &flag, bool value=true)
	{
		state->mark(flag, value);
	}

	void markModification ()
	{
		this->date = new Date(Date::now());
	}

	String *getBrief()
	{
		return brief;
	}

	void setBrief(String *brief)
	{
		this->brief = brief;
	}

	String getRelativeDate ();

	String *getUIDL()
	{
		return uidl;
	}

	void setUIDL (String *uidl)
	{
		this->uidl = uidl;
	}
};

DECLARE_SMARTPTR(Header);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* HEADER_H_ */
