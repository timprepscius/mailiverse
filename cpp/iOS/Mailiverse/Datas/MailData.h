/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef Mailiverse_MailData_h
#define Mailiverse_MailData_h

#include "mailiverse/mail/model/Lib.h"
#include "MMUtilities.h"

struct MailData : public mailiverse::mail::model::Model::UserData
{
	mailiverse::mail::cache::ID key;
	mailiverse::mail::cache::Version version;
	bool dirty;
	
	NSString *brief, *text, *html, *author, *date, *shortAuthor, *recipients;
	
	enum DisplayMode { SHORT, FULL, NONE };
	DisplayMode displayMode;
	
	int height;
	
	bool read;
	int color;
	bool draft;
	bool trash;
		
	MailData (mailiverse::mail::model::Mail *m)
	{
		key = m->getID();
		height = 0 + 97;

		dirty = false;
		read = false;
		displayMode = SHORT;
		color = -1;
		draft = false;
		trash = false;
	}
	
	bool isLoaded ()
	{
		return version != mailiverse::mail::cache::Version::NONE;
	}
	
	static MailData *load (mailiverse::mail::model::Mail *m, mailiverse::mail::model::Conversation *c)
	{
		if (!m->getUserData<MailData>())
			m->setUserData(new MailData(m));
	
		MailData *data = m->getUserData<MailData>();
		data->refresh(m, c);
	
		return data;
	}
	
	void refresh(mailiverse::mail::model::Mail *m, mailiverse::mail::model::Conversation *c)
	{
		if (m->isLoaded() && c->isLoaded())
		{
			if (version != m->getLocalVersion())
			{
				dirty = true;
				version = m->getLocalVersion();

				color = c->getHeader()->getAuthors().getIndexOf(m->getHeader()->getAuthor());
				date = toNSString(m->getHeader()->getRelativeDate());

				if (m->getHeader()->getAuthor())
					author = toNSString(m->getHeader()->getAuthor()->getShortName());

				if (m->getHeader()->getRecipients())
					recipients = toNSString(m->getHeader()->getRecipients()->toShortList());

				if (m->getHeader()->getBrief())
					brief = toNSString(*m->getHeader()->getBrief());

				if (m->getBody()->hasText())
					text = toNSStringPtr(m->getBody()->getTextHTMLified());
					
				if (m->getBody()->hasHTML())
					html = toNSStringPtr(m->getBody()->getHTMLStripped());

				read = m->getHeader()->hasState("READ");
				draft = m->getHeader()->hasState("DRAFT");
				trash = m->getHeader()->hasState("TRASH");
			}
		}
		else
		{
			author = toNSString("Loading");
		}
	}
} ;

DECLARE_SMARTPTR(MailData);

#endif
