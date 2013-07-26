/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef Mailiverse_ConversationData_h
#define Mailiverse_ConversationData_h

#include "mailiverse/mail/model/Lib.h"
#include "MMUtilities.h"

struct ConversationData : public mailiverse::mail::model::Model::UserData
{
	mailiverse::mail::cache::ID key;
	mailiverse::mail::cache::Version version;
	bool dirty;
	
	NSString *authors;
	NSString *date;
	NSString *subject;
	NSString *brief;
	bool read;
	bool selected;
	
	ConversationData (mailiverse::mail::model::Conversation *c)
	{
		key = c->getID();
		read = true;
		selected = false;
		dirty = false;
	}
	
	void setSelected (bool selected)
	{
		this->selected = selected;
	}

	bool isLoaded ()
	{
		return version != mailiverse::mail::cache::Version::NONE;
	}

	static ConversationData *load(mailiverse::mail::model::Conversation *c)
	{
		if (!c->getUserData<ConversationData>())
			c->setUserData(new ConversationData(c));
			
		ConversationData *data = c->getUserData<ConversationData>();
		data->refresh(c);
		
		return data;
	}
	
	void refresh (mailiverse::mail::model::Conversation *c)
	{
		if (c->isLoaded())
		{
			if (version != c->getLocalVersion())
			{
				dirty = true;
				version = c->getLocalVersion();
				date = toNSString(c->getHeader()->getRelativeDate());
				authors = toNSString(c->getHeader()->getAuthorsShortList());
				subject = toNSStringPtr(c->getHeader()->getSubject());
				brief = toNSStringPtr(c->getHeader()->getBrief());
				read = c->getHeader()->hasState("READ");
			}
		}
		else
		{
			authors = toNSString("Loading");
		}
	}
} ;

DECLARE_SMARTPTR(ConversationData);


#endif
