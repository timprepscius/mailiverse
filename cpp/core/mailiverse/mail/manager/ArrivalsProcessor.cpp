/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "ArrivalsProcessor.h"
#include "Master.h"
#include "mailiverse/utilities/JSON_.h"
#include "mailiverse/core/constants/ConstantsMailJson.h"
#include "mailiverse/utilities/Strings.h"

using namespace mailiverse::mail::manager;
using namespace mailiverse::mail::model;
using namespace mailiverse::mail::cache;
using namespace mailiverse::core::constants;
using namespace mailiverse::utilities;
using namespace mailiverse::core::crypt;
using namespace mailiverse;

ArrivalsProcessor::ArrivalsProcessor()
{
}

ArrivalsProcessor::~ArrivalsProcessor()
{
}

void ArrivalsProcessor::processSuccess (Direction::Enum direction, const String &externalKey, const Date &date, const Block &inputStream)
{
	Indexer *indexer = master->getIndexer();

	try
	{
		model::MailPtr mail = processStream (direction, externalKey, date, inputStream);

		indexer->addMail(mail);
	}
	catch (DuplicateMailException &e)
	{
		indexer->addDuplicate(externalKey, date);
	}
}

// this is not the best implemetation, lots of heap, but oh well for now

String ArrivalsProcessor::decode (const String &s)
{
	return toString(Base64::decode(s));
}

String ArrivalsProcessor::getFirstHeader(json::Object *message, const String &_s, const String &def)
{
	if (message != NULL)
	{
		if (JSON_.has(*message, "headers"))
		{
			json::Array &a = JSON_.getArray(*message, "headers");
			
			String s = toLowerCase(_s);
			for (int i=0; i<JSON_.size(a); ++i)
			{
				json::Array &h = JSON_.getArray(a, i);
				if (s == toLowerCase(JSON_.getString(h, 0)))
					return decode(JSON_.getString(h, 1));
			}
		}
	}
	
	return def;
}

MailPtr ArrivalsProcessor::parse(Direction::Enum direction, const String &externalKey, const Date &date, const core::Block &bytes)
{
	String string = toString(bytes);
	json::Object message = JSON_.parse(string);
	StringPtr originalKey = JSON_.getStringPtr(message, ConstantsMailJson::Original);
	json::Object *content = 
		JSON_.has(message,ConstantsMailJson::Content) ?
			&JSON_.getObject(message,ConstantsMailJson::Content) : NULL;
			
	String uidl = externalKey;
	
	if (JSON_.has(message, ConstantsMailJson::UIDL))
	{
		uidl = JSON_.getString(message, ConstantsMailJson::UIDL);
		if (master->getIndexer()->containsUIDL(uidl))
		{
			LogDebug(mailiverse::mail::manager::Parser, "contains UIDL");
			if (direction == Direction::OUT)
			{
				LogDebug(mailiverse::mail::manager::Parser, "mail was sent");
				
				bool startsWithLessThan = uidl.find('<')==0;
				int atPos = uidl.find('@');
				
				if (startsWithLessThan && atPos !=-1)
				{
					String id = uidl.substr(1,atPos-1);
					LogDebug(mailiverse::mail::manager::Parser, "found embedded id " << id);
					
					CacheManager *cacheManager = master->getCacheManager();
					
					MailPtr mail = cacheManager->getMail(Record(ID::fromFileSystemSafe(id), Date::None));
					if (mail!= NULL)
					{
					/*
						LogDebug(mailiverse::mail::manager::Parser, "cache has the mail");
						if (mail->isLoaded())
							setExternalKeys_(externalKey, originalKey).invoke(mail);
						else
							mail.getLoadCallbacks().addCallback(
								new Single(setExternalKeys_(externalKey, originalKey))
							);
					*/
					}
				}
			}
			
			throw DuplicateMailException();
		}
	}
	
	String subject = getFirstHeader(content, "subject", "");
	
	Identity *author = NULL;
	Recipients *recipients = new Recipients ();

	if (JSON_.has(message, ConstantsMailJson::Addresses))
	{
		json::Object &addresses = JSON_.getObject(message, ConstantsMailJson::Addresses);
		if (JSON_.has(addresses, ConstantsMailJson::From))
		{
			json::Array &jAddresses = JSON_.getArray(addresses, ConstantsMailJson::From);
			if (JSON_.size(jAddresses) > 0)
			{
				json::Object &jia = JSON_.getObject(jAddresses, 0);
				author = master->getAddressBook()->getIdentity(
					new UnregisteredIdentity(
						JSON_.has(jia,ConstantsMailJson::Name) ?
							decode(JSON_.getString(jia,ConstantsMailJson::Name)) : "",
						JSON_.has(jia, ConstantsMailJson::Email) ?
							decode(JSON_.getString(jia, ConstantsMailJson::Email)) : ""
					)
				);
			}
		}
	
		String buckets[] = { ConstantsMailJson::To, ConstantsMailJson::Cc, ConstantsMailJson::Bcc, ConstantsMailJson::ReplyTo };
		
		for (String bucket : buckets)
		{
			if (JSON_.has(addresses,bucket))
			{
				json::Array &jAddresses = JSON_.getArray(addresses, bucket);
				for (int i=0; i<JSON_.size(jAddresses); ++i)
				{
					json::Object &jia = JSON_.getObject(jAddresses, i);
					recipients->get(bucket).add(master->getAddressBook()->getIdentity(
						new UnregisteredIdentity(
							JSON_.has(jia,ConstantsMailJson::Name) ?
								decode(JSON_.getString(jia,ConstantsMailJson::Name)) : "",
							JSON_.has(jia, ConstantsMailJson::Email) ?
								decode(JSON_.getString(jia, ConstantsMailJson::Email)) : ""
						)
					));
				}
			}
		}
	}
	
	if (author == NULL)
		author = master->getAddressBook()->getIdentity(new UnregisteredIdentity("<Unknown>"));

	Body *body = new Body();
	Attachments *attachments = new Attachments();
	
	List<json::Object *> contents;
	contents.add(content);
	
	while (!contents.isEmpty())
	{
		json::Object *c = contents.get(0);
		contents.pop_front();

		String clazz = JSON_.getString(*c, ConstantsMailJson::Class);
		json::String none;
		json::UnknownElement &value = 
			JSON_.has(*c, ConstantsMailJson::Value) ? 
				JSON_.getUnknown(*c, ConstantsMailJson::Value) : (json::UnknownElement &)none;
		String contentType = getFirstHeader(c, "Content-Type", "text/plain");
		
		if (clazz == ConstantsMailJson::String_)
		{
			String valueString = JSON_.asString(value);
			
			if (startsWith(contentType, "text/html"))
			{
				if (body->getHTML() == NULL)
					body->setHTML(new String(valueString));
			}
			else
			if (startsWith(contentType,"text/plain"))
			{
				if (body->getText() == NULL)
					body->setText(new String(valueString));
			}
		}
		else
		if (clazz == ConstantsMailJson::MultiPart)
		{
			json::Array &valueParts = value;
			for (int i=0; i<JSON_.size(valueParts); ++i)
			{
				json::Object &valueContent = JSON_.getObject(valueParts,i);
				contents.add(&valueContent);
			}
		}
		else
		if (clazz == ConstantsMailJson::Bytes)
		{
			String contentDisposition = getFirstHeader(c, "Content-Disposition", "None");
			String contentId = getFirstHeader(c, "Content-Id", "None");
			
			StringPtr attachmentId = Attachment::getAttachmentId(&contentDisposition, &contentId);
			if (attachmentId != NULL)
			{
				attachments->addAttachment(
					new Attachment (attachmentId, new String(contentDisposition), new String(contentType))
				);
			}
		}
	}
	
	DatePtr markDate = NULL;
	
	if (JSON_.has(message, ConstantsMailJson::Dates))
	{
		json::Object &dates = JSON_.getObject(message, ConstantsMailJson::Dates);
		
		// use a date from somewhere
		markDate =
			JSON_.has(dates, ConstantsMailJson::Sent) ?
				new Date(fromString<Date::ValueType>(JSON_.getString(dates, ConstantsMailJson::Sent))) : NULL;
		
		if (markDate == NULL)
			markDate = 
				JSON_.has(dates, ConstantsMailJson::Received) ?
						new Date(fromString<Date::ValueType>(JSON_.getString(dates, ConstantsMailJson::Received))) : NULL;
						
		if (markDate == NULL)
			markDate = 
				JSON_.has(dates, ConstantsMailJson::Written) ?
					new Date(fromString<Date::ValueType>(JSON_.getString(dates, ConstantsMailJson::Written))) : NULL;
	}
	
	if (markDate == NULL)
		markDate = new Date(date);

	// received or sent
	TransportStatePtr transportState = 
		direction == Direction::IN ?
			TransportState::fromString(TransportState::RECEIVED) :
			TransportState::fromString(TransportState::SENT);
			
	Header *header = 
		new Header(
			new String(externalKey), 
			originalKey, 
			new String(uidl), 
			author, 
			recipients, 
			new String(subject), 
			markDate, 
			transportState, 
			body->calculateBrief()
		);

	if (!attachments->getList().isEmpty())
		header->markState(TransportState::ATTACHMENT);
		
	MailPtr mail = master->getCacheManager()->newMail(header, body, attachments);
	mail->getHeader()->setDictionary(new Dictionary (mail));
	
	return mail;
}

MailPtr ArrivalsProcessor::processStream (Direction::Enum direction, const String &externalKey, const Date &date, const Block &block)
{
	MailPtr mail = parse(direction, externalKey, date, block);
	
	AddressBook *addressBook = getMaster()->getAddressBook();
	if (mail->getHeader()->getRecipients())
		mail->getHeader()->getRecipients()->registerRecipients(addressBook);
		
	if (mail->getHeader()->getAuthor())
		mail->getHeader()->setAuthor(addressBook->getIdentity(mail->getHeader()->getAuthor()));
		
	return mail;
}

bool ArrivalsProcessor::alreadyProcessed(const String &path)
{
	Indexer *indexer = master->getIndexer();
	return indexer->containsExternalKey(path);
}

void ArrivalsProcessor::processFailure(Direction::Enum direction, const String &path, const Date &date, const Exception &e)
{
	Indexer *indexer = master->getIndexer();
	indexer->addFailure(direction, path, date, e);
}

Pair<Date,Date> ArrivalsProcessor::getRequestDateRange ()
{
	return Pair<Date,Date>(Date(0), Date(-1));
}
