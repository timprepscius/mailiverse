/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_FolderDefinition_h__
#define __mailiverse_mail_model_FolderDefinition_h__

#include "mailiverse/Types.h"
#include "mailiverse/utilities/SmartPtr.h"
#include "Conversation.h"

namespace mailiverse {
namespace mail {
namespace model {

class FolderDefinition
{
protected:
	String name;
	IdentityPtr author;
	IdentityPtr recipient;
	StringPtr subject;
	TransportStatePtr stateEquals, stateDiffers;
	DictionaryPtr bayesianDictionary;
	bool autoBayesian;

public:
	FolderDefinition (const String &name)
	{
		autoBayesian = false;
		this->name = name;
	}

	const String &getName ()
	{
		return name;
	}

	FolderDefinition *setAuthor(Identity *author)
	{
		this->author = author;
		return this;
	}

	Identity *getAuthor ()
	{
		return author;
	}

	FolderDefinition *setRecipient(Identity *recipient)
	{
		this->recipient = recipient;
		return this;
	}

	Identity *getRecipient ()
	{
		return recipient;
	}

	FolderDefinition *setSubject(String *subject)
	{
		this->subject = subject;
		return this;
	}

	String *getSubject ()
	{
		return subject;
	}

	FolderDefinition *setState(TransportState *stateEquals, TransportState *stateDiffers)
	{
		this->stateEquals = stateEquals;
		this->stateDiffers = stateDiffers;
		return this;
	}

	TransportState *getStateEquals ()
	{
		return stateEquals;
	}

	TransportState *getStateDiffers ()
	{
		return stateDiffers;
	}
	
	bool matchesFilter (Conversation *conversation)
	{
		Header *h = conversation->getHeader();

		bool matches = true;
		if (matches && author)
		{
			matches = h->getAuthors().contains(author);
		}
		if (matches && recipient)
		{
			matches = h->getRecipients()->contains(recipient);
		}
		if (matches && subject)
		{
			matches = *h->getSubject() == *subject;
		}
		if (matches && (stateEquals || stateDiffers))
		{
			bool equalMatch = stateEquals ? (h->getTransportState()->hasOne(stateEquals)) : true;
			bool differMatch = stateDiffers ? (h->getTransportState()->hasNone(stateDiffers)) : true;

			matches = equalMatch && differMatch;
		}
		if (matches && bayesianDictionary && autoBayesian)
		{
			matches = bayesianMatches(conversation);
		}
		return matches;
	}
	
	Dictionary *getBayesianDictionary ()
	{
		return bayesianDictionary;
	}

	FolderDefinition *setBayesianDictionary(Dictionary *bayesianDictionary) 
	{
		this->bayesianDictionary = bayesianDictionary;
		return this;
	}

	FolderDefinition *setAutoBayesian (bool autoBayesian)
	{
		this->autoBayesian = autoBayesian;
		return this;
	}

	bool bayesianMatches (Conversation *conversation)
	{
		return bayesianDictionary->bayesianMatches(conversation->getHeader()->getDictionary());
	}
	
	bool getAutoBayesian ()
	{
		return autoBayesian;
	}

	void conversationAdded(Conversation *conversation) 
	{
		if (bayesianDictionary)
		{
			bayesianDictionary->add(conversation->getHeader()->getDictionary());
		}
	}

	void conversationDeleted(Conversation *conversation) 
	{
		if (bayesianDictionary != NULL)
		{
			bayesianDictionary->subtract(conversation->getHeader()->getDictionary());
		}
	}

};

DECLARE_SMARTPTR(FolderDefinition);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_model_FolderDefinition_h__ */
