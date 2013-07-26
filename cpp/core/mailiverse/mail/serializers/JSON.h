/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_serializers_JSON_h__
#define __mailiverse_mail_serializers_JSON_h__

#include "../model/Lib.h"
#include "json/elements.h"
#include "mailiverse/utilities/Strings.h"

namespace mailiverse {
namespace mail {
namespace serializers {

class JSON
{
protected:
	const String VERSION;

public:
	JSON() {}
	virtual ~JSON() {}

	bool toBoolean (const json::Boolean &o)
	{
		return o;
	}
	
	json::Boolean toJSON(bool b)
	{
		return json::Boolean(b);
	}

	json::Number toJSON(int n)
	{
		return json::Number(json::Number::ValueType(n));
	}

	json::Number toJSON(long n)
	{
		return json::Number(json::Number::ValueType(n));
	}

	json::Number toJSON(long long n)
	{
		return json::Number(json::Number::ValueType(n));
	}

	long long toLong(const json::Number &o)
	{
		return o.as<long long>();
	}

	std::string toString_ (const json::String &s)
	{
		return json::String(s);
	}

	model::Identity *toIdentity (const json::String &o, model::Library *library)
	{
		model::IdentityPtr identity = new model::UnregisteredIdentity(toString_(o));
		return library->getAddressBook()->getIdentity(identity);
	}

	json::String toJSON (model::Identity *identity)
	{
		json::String o;
		o = toJSON(identity->getFull());

		return o;
	}

	model::IdentityList toIdentityList (const json::Array &a, model::Library *library)
	{
		model::IdentityList l;
		for (auto i = a.Begin(); i!=a.End(); ++i)
		{
			l.push_back(toIdentity(*i, library));
		}

		return l;
	}

	json::Array toJSON (const model::IdentityList &l)
	{
		json::Array a;
		for (auto &i: l)
			a.Insert(toJSON((model::Identity *)i));

		return a;
	}

	json::Array toJSON (const model::IdentitySet &l)
	{
		json::Array a;
		for (auto &i: l)
			a.Insert(toJSON((model::Identity *)i));

		return a;
	}

	json::String toJSON(String *s)
	{
		return json::String(*s);
	}

	json::String toJSON(const std::string &s)
	{
		return json::String(s);
	}

	String *toString(const json::String &s)
	{
		return new String(s);
	}

	model::Recipients *toRecipients(const json::Object &o, model::Library *library)
	{
		model::Recipients *r = new model::Recipients();
		r->setTo(toIdentityList(o["to"], library));
		r->setCc(toIdentityList(o["cc"], library));
		r->setBcc(toIdentityList(o["bcc"], library));
		r->setReplyTo(toIdentityList(o["replyTo"], library));

		return r;
	}

	json::Object toJSON (model::Recipients *r)
	{
		json::Object o;
		o["to"] = toJSON(r->getTo());
		o["cc"] = toJSON(r->getCc());
		o["bcc"] = toJSON(r->getBcc());
		o["replyTo"] = toJSON(r->getReplyTo());

		return o;
	}

	Date *toDate (const json::Number &d)
	{
		return new Date(toLong(d));
	}

	json::Number toJSON (const Date *d)
	{
		return toJSON(d->getTime());
	}
	
	model::Dictionary *toDictionary(const json::String &o)
	{
		model::Dictionary *d = new model::Dictionary();
		d->deserialize(o);
		return d;
	}

	json::String toJSON (model::Dictionary *d)
	{
		json::String o = d->serialize();
		return o;
	}

	model::Header *toHeader (const json::Object &h, model::Library *library, model::Model *item)
	{
		model::Header *header = new model::Header();

		if (h.Find("externalKey")!=h.End())
			header->setExternalKey(toString(h["externalKey"]));

		if (h.Find("originalKey")!=h.End())
			header->setOriginalKey(toString(h["originalKey"]));

		if (h.Find("uidl")!=h.End())
			header->setUIDL(toString(h["uidl"]));

		if (h.Find("author")!=h.End())
			header->setAuthor(toIdentity (h["author"], library));

		if (h.Find("authors")!=h.End())
			header->setAuthors(toIdentityList(h["authors"], library));

		if (h.Find("recipients")!=h.End())
			header->setRecipients(toRecipients(h["recipients"], library));

		if (h.Find("subject")!=h.End())
			header->setSubject(toString(h["subject"]));

		if (h.Find("date")!=h.End())
			header->setDate(toDate(h["date"]));

		if (h.Find("transportState")!=h.End())
			header->setTransportState(model::TransportState::fromString(toString_(h["transportState"])));

		if (h.Find("brief")!=h.End())
			header->setBrief(toString(h["brief"]));

		if (h.Find("dictionary")!=h.End())
			header->setDictionary(toDictionary(h["dictionary"]));

		return header;
	}

	json::Object toJSON (model::Header *h)
	{
		json::Object o;

		if (h->getExternalKey())
			o["externalKey"] = toJSON(h->getExternalKey());

		if (h->getOriginalKey())
			o["originalKey"] = toJSON(h->getOriginalKey());

		if (h->getUIDL())
			o["uidl"] = toJSON(h->getUIDL());

		if (h->getAuthor())
			o["author"] = toJSON(h->getAuthor());

		if (!h->getAuthors().empty())
			o["authors"] = toJSON(h->getAuthors());

		if (h->getRecipients())
			o["recipients"] = toJSON(h->getRecipients());

		if (h->getSubject())
			o["subject"] = toJSON(h->getSubject());

		if (h->getDate())
			o["date"] = toJSON(h->getDate());

		if (h->getTransportState())
			o["transportState"] = toJSON(h->getTransportState()->toString());

		if (h->getBrief())
			o["brief"] = toJSON(h->getBrief());

		if (h->getDictionary())
			o["dictionary"] = toJSON(h->getDictionary());

		return o;
	}

	model::Body *toBody (const json::UnknownElement &e)
	{
		const json::Object &o = e;
		model::Body *body = new model::Body();
		if (o.Find("text")!=o.End())
			body->setText(toString(o["text"]));
		if (o.Find("html")!=o.End())
			body->setHTML(toString(o["html"]));

		return body;
	}

	json::Object toJSON (model::Body *body)
	{
		json::Object o;
		if (body->hasText())
			o["text"] = toJSON(body->getText());
		if (body->hasHTML())
			o["html"] = toJSON(body->getHTML());

		return o;
	}

	void fromJSON (const json::Object &o, model::Mail *mail)
	{
		mail->setHeader(toHeader(o["header"], mail->getLibrary(), mail));
		mail->setBody(toBody(o["body"]));
		mail->setAttachments(toAttachments(o["attachments"]));
	}

	json::Object toJSON (model::Mail *mail)
	{
		json::Object o;
		o["version"] = toJSON(VERSION);
		o["header"] = toJSON(mail->getHeader());
		o["body"] = toJSON(mail->getBody());
		o["attachments"] = toJSON(mail->getAttachments());

		return o;
	}

	cache::ID toID (const json::String &u)
	{
		return cache::ID::deserialize(core::crypt::Base64::decode((std::string)u));
	}

	json::String toJSON (const cache::ID &k)
	{
		return core::crypt::Base64::encode(k.serialize());
	}

	model::Record toRecord(const json::Array &a)
	{
		return model::Record(toID(a[0]), Date(toLong(a[1])));
	}

	json::Array toJSON(const model::Record &r)
	{
		json::Array a;
		a.Insert(toJSON(r.first));
		a.Insert(toJSON(&r.second));
		return a;
	}

	json::Array toJSON(const model::RecordList &r)
	{
		json::Array a;
		for (auto &i : r)
			a.Insert(toJSON(i));

		return a;
	}

	json::Array toJSONNoDates(const model::RecordList &r)
	{
		json::Array a;
		for (auto &i : r)
			a.Insert(toJSON(i.first));

		return a;
	}

	model::RecordList toRecordList(const json::Array &m)
	{
		model::RecordList l;
		for (auto i=m.Begin(); i!=m.End(); ++i)
		{
			l.push_back(toRecord(*i));
		}

		return l;
	}

	model::RecordList toRecordListNoDates(const json::Array &m)
	{
		model::RecordList l;
		for (auto i=m.Begin(); i!=m.End(); ++i)
		{
			l.push_back(model::Record(toID(*i),Date::None));
		}

		return l;
	}

	void fromJSON (const json::Object &o, model::Conversation *c)
	{
		model::RecordList l;

		c->setHeader(toHeader(o["header"], c->getLibrary(), c));
		c->setItemIds(toRecordList(o["mail"]));
	}

	json::Object toJSON (model::Conversation *c)
	{
		json::Object o;
		o["version"] = toJSON(VERSION);

		o["header"] = toJSON(c->getHeader());
		o["mail"] = toJSON(c->getItemIds());

		return o;
	}

	model::FolderDefinition *toFolderDefinition(const json::Object &o, model::Library *library)
	{
		model::FolderDefinition *f = new model::FolderDefinition(toString_(o["name"]));

		if (o.Find("subject")!=o.End())
			f->setSubject(toString(o["subject"]));

		if (o.Find("author")!=o.End())
			f->setAuthor(toIdentity(o["author"], library));

		if (o.Find("recipient")!=o.End())
			f->setRecipient(toIdentity(o["recipient"], library));

		if (o.Find("stateDiffers")!=o.End() || o.Find("stateEquals")!=o.End())
		{
			model::TransportState *d=NULL,*e=NULL;
			if (o.Find("stateDiffers")!=o.End())
				d = model::TransportState::fromString(toString_(o["stateDiffers"]));

			if (o.Find("stateEquals")!=o.End())
				e = model::TransportState::fromString(toString_(o["stateEquals"]));

			f->setState(e, d);
		}
		
		if (o.Find("bayesianDictionary")!=o.End())
			f->setBayesianDictionary(toDictionary(o["bayesianDictionary"]));

		if (o.Find("autoBayesian")!=o.End())
			f->setAutoBayesian(toBoolean(o["autoBayesian"]));

		return f;
	}

	Pair<String,Date> toStringDate(const json::Array &a)
	{
		return Pair<String,Date>(toString_(a[0]), Date(toLong(a[1])));
	}

	json::Array toJSON_(const std::pair<String,Date> &r)
	{
		json::Array a;
		a.Insert(toJSON(r.first));
		a.Insert(toJSON(&r.second));
		return a;
	}

	json::Object toJSON(model::FolderDefinition *d)
	{
		json::Object o;
		o["name"] = toJSON(d->getName());

		if (d->getAuthor())
			o["author"] = toJSON(d->getAuthor());

		if (d->getSubject())
			o["subject"] = toJSON(d->getSubject());

		if (d->getRecipient())
			o["recipient"] = toJSON(d->getRecipient());

		if (d->getStateDiffers())
			o["stateDiffers"] = toJSON(d->getStateDiffers()->toString());

		if (d->getStateEquals())
			o["stateEquals"] = toJSON(d->getStateEquals()->toString());

		if (d->getBayesianDictionary())
			o["bayesianDictionary"] = toJSON(d->getBayesianDictionary());

		if (d->getAutoBayesian())
			o["autoBayesian"] = toJSON(d->getAutoBayesian());
			
		return o;
	}

	void fromJSON(const json::Object &v, model::Folder *f)
	{
		if (v.Find("definition")!=v.End())
			f->setFolderDefinition(toFolderDefinition(v["definition"], f->getLibrary()));

		if (dynamic_cast<model::FolderSet *>(f))
		{
			if (dynamic_cast<model::FolderMaster *>(f))
			{
				model::FolderMaster *fm = (model::FolderMaster *)f;

				{
					const json::Array &a = v["uidl"];
					List<Pair<String,Date> > l;
					for (auto i=a.Begin(); i!=a.End(); ++i)
						l.push_back(toStringDate(*i));

					fm->setUIDLHashes(l);
				}

				{
					const json::Array &a = v["externalKey"];

					List<Pair<String,Date> > l;
					for (auto i=a.Begin(); i!=a.End(); ++i)
						l.push_back(toStringDate(*i));

					fm->setExternalKeyHashes(l);
				}
			}

			model::FolderSet *fs = (model::FolderSet *)f;

			fs->setFolderIds(toRecordListNoDates(v["parts"]));
			fs->setNumConversations(toLong(v["numConversations"]));

			return;
		}

		f->setConversationIds (toRecordList(v["conversations"]));
	}

	json::Object toJSON(model::Folder *f)
	{
		json::Object v;
		v["version"] = json::String(VERSION);

		if (f->getFolderDefinition())
			v["definition"] = toJSON(f->getFolderDefinition());

		if (dynamic_cast<model::FolderSet *>(f))
		{
			if (dynamic_cast<model::FolderMaster *>(f))
			{
				model::FolderMaster *fm = (model::FolderMaster *)f;

				{
					json::Array a;
					for (auto &id : fm->getUIDLHashes())
						a.Insert(toJSON_(id));
					v["uidl"] = a;
				}

				{
					json::Array a;
					for (auto &id : fm->getExternalKeyHashes())
						a.Insert(toJSON_(id));
					v["externalKey"] = a;
				}
			}

			model::FolderSet *fs = (model::FolderSet *)f;
			v["parts"] = toJSONNoDates(fs->getFolderIds());
			v["numConversations"] = toJSON((long)fs->getNumConversations());

			return v;
		}

		v["conversations"] = toJSON(f->getConversationIds());

		return v;
	}

	model::Attachment *toAttachment(const json::Object &v)
	{
		return new model::Attachment(
			(v.Find("id")!=v.End()) ? toString(v["id"]) : NULL,
			(v.Find("disposition")!=v.End())  ? toString(v["disposition"]) : NULL,
			(v.Find("mime-type")!=v.End())  ? toString(v["mime-type"]) : NULL
		);
	}

	json::Object toJSON(model::Attachment *a)
	{
		json::Object o;
		if (a->getDisposition())
			o["disposition"] = toJSON(a->getDisposition());

		if (a->getMimeType())
			o["mime-type"] = toJSON(a->getMimeType());

		if (a->getId())
			o["id"] = toJSON(a->getId());

		return o;
	}
	model::Attachments *toAttachments(const json::Array &a)
	{
		model::Attachments *attachments = new model::Attachments ();
		for (auto i = a.Begin(); i!=a.End(); ++i)
		{
			attachments->addAttachment(
				toAttachment(*i)
			);
		}

		return attachments;
	}

	json::Array toJSON (model::Attachments *attachments)
	{
		json::Array a;
		for (auto &i : attachments->getList())
			a.Insert(toJSON((model::Attachment *)i));

		return a;
	}
	
	json::Object toJSON (model::Settings *settings)
	{
		json::Object o;
		
		for (auto &i : settings->getKV())
		{
			o[i.first] = toJSON(i.second);
		}
		
		return o;
	}

	void fromJSON (const json::Object &o, model::Settings *settings)
	{
		model::Settings::KV kv;
		for (auto i=o.Begin(); i!=o.End(); ++i)
			kv[i->name] = toString_(i->element);
		
		settings->setKV(kv);
	}

};


} // namespace
} // namespace
} // namespace

#endif 
