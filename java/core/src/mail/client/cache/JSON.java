/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mail.client.Master;
import mail.client.Servent;
import mail.client.model.Attachment;
import mail.client.model.Attachments;
import mail.client.model.Body;
import mail.client.model.Conversation;
import mail.client.model.Dictionary;
import mail.client.model.Folder;
import mail.client.model.FolderDefinition;
import mail.client.model.FolderMaster;
import mail.client.model.FolderSet;
import mail.client.model.Header;
import mail.client.model.Identity;
import mail.client.model.Mail;
import mail.client.model.PublicKey;
import mail.client.model.PublicKeyRing;
import mail.client.model.Recipients;
import mail.client.model.Settings;
import mail.client.model.TransportState;
import mail.client.model.UnregisteredIdentity;

import core.util.Base64;

import core.util.JSON_;
import core.util.JSON_.JSONException;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Pair;
import core.util.Strings;

public class JSON extends Servent<Master>
{
	LogNull log = new LogNull(JSON.class);
	
	public Object toJSON (ID id)
	{
		return toJSON(id.serialize());
	}
	
	public ID toID (String json)
	{
		return ID.deserialize(toBytes(json));
	}
	
	public Identity toIdentity(String s)
	{
		return 
			master.getAddressBook().getIdentity(
				new UnregisteredIdentity(s)
			);
	}
	
	public Object toJSON (String s)
	{
		return JSON_.newString(s);
	}
	
	public Object toJSON (boolean b)
	{
		return JSON_.newBoolean(b);
	}
	
	public Object toJSON (int v)
	{
		return JSON_.newNumber(v);
	}
	
	public Object toJSON (Identity i)
	{
		return toJSON(i.toString());
	}
	
	public List<Identity> toIdentityList (Object a) throws JSONException
	{
		List<Identity> l = new ArrayList<Identity>();
		
		for (int i=0; i<JSON_.size(a); ++i)
		{
			l.add(toIdentity(JSON_.getString(a,i)));
		}
		
		return l;
	}

	public Object toJSON (Collection<Identity> l) throws JSONException
	{
		Object a = JSON_.newArray();
		for (Identity i: l)
			JSON_.add(a, toJSON(i));
		
		return a;
	}
	
	public Recipients toRecipients(Object o) throws JSONException
	{
		Recipients r = new Recipients();
		r.setTo(toIdentityList(JSON_.getArray(o, "to")));
		r.setCc(toIdentityList(JSON_.getArray(o, "cc")));
		r.setBcc(toIdentityList(JSON_.getArray(o, "bcc")));
		r.setReplyTo(toIdentityList(JSON_.getArray(o, "replyTo")));
		
		return r;
	}
	
	
	public Object toJSON (Recipients r) throws JSONException
	{
		Object o = JSON_.newObject();
		JSON_.put(o, "to", toJSON(r.getTo()));
		JSON_.put(o, "cc", toJSON(r.getCc()));
		JSON_.put(o, "bcc", toJSON(r.getBcc()));
		JSON_.put(o, "replyTo", toJSON(r.getReplyTo()));
		
		return o;
	}
	
	public Dictionary toDictionary(String s)
	{
		Dictionary d = new Dictionary();
		d.fromSerializableString(s);
		return d;
	}
	
	public Object toJSON (Dictionary d)
	{
		return toJSON(d.toSerializableString());
	}
	
	public Date toDate (long d)
	{
		return new Date(d);
	}
	
	public Object toJSON (Date d)
	{
		return JSON_.newNumber(d.getTime());
	}
	
	public Header toHeader (Object h) throws JSONException
	{
		Header header = new Header();
		
		if (JSON_.has(h, "externalKey"))
			header.setExternalKey(JSON_.getString(h, "externalKey"));
		
		if (JSON_.has(h, "originalKey"))
			header.setOriginalKey(JSON_.getString(h, "originalKey"));
		
		if (JSON_.has(h, "uidl"))
			header.setUIDL(JSON_.getString(h, "uidl"));
		
		if (JSON_.has(h, "author"))
			header.setAuthor(toIdentity (JSON_.getString(h, "author")));

		if (JSON_.has(h, "authors"))
			header.setAuthors(toIdentityList(JSON_.getArray(h, "authors")));
		
		if (JSON_.has(h, "recipients"))
			header.setRecipients(toRecipients(JSON_.getObject(h, "recipients")));
		
		if (JSON_.has(h, "subject"))
			header.setSubject(JSON_.getString(h, "subject"));
		
		if (JSON_.has(h, "date"))
			header.setDate(toDate(JSON_.getLong(h, "date")));
			
		if (JSON_.has(h, "transportState"))
			header.setTransportState(TransportState.fromString(JSON_.getString(h, "transportState")));
		
		if (JSON_.has(h, "brief"))
			header.setBrief(JSON_.getString(h, "brief"));
		
		if (JSON_.has(h, "dictionary"))
			header.setDictionary(toDictionary(JSON_.getString(h, "dictionary")));

		return header;
	}
	
	public Object toJSON (Header h) throws JSONException
	{
		Object o = JSON_.newObject();
		
		if (h.getExternalKey()!=null)
			JSON_.put(o, "externalKey", toJSON(h.getExternalKey()));
		
		if (h.getOriginalKey()!=null)
			JSON_.put(o, "originalKey", toJSON(h.getOriginalKey()));

		if (h.getUIDL()!=null)
			JSON_.put(o, "uidl", toJSON(h.getUIDL()));

		if (h.getAuthor() != null)
			JSON_.put(o, "author", toJSON(h.getAuthor()));
		
		if (h.getAuthors() != null)
			JSON_.put(o, "authors", toJSON(h.getAuthors()));

		if (h.getRecipients() != null)
			JSON_.put(o, "recipients", toJSON(h.getRecipients()));
		
		if (h.getSubject() != null)
			JSON_.put(o, "subject", toJSON(h.getSubject()));
		
		if (h.getDate() != null)
			JSON_.put(o, "date", toJSON(h.getDate()));
	
		if (h.getTransportState() != null)
			JSON_.put(o, "transportState", toJSON(h.getTransportState().toString()));

		if (h.getBrief() != null)
			JSON_.put(o, "brief", toJSON(h.getBrief()));
		
		if (h.getDictionary() != null)
			JSON_.put(o, "dictionary", toJSON(h.getDictionary()));
		
		return o;
	}

	public Body toBody (Object o) throws JSONException
	{
		Body body = new Body();
		
		if (JSON_.has(o, "text"))
			body.setText(JSON_.getString(o, "text"));
		
		if (JSON_.has(o, "html"))
			body.setHTML(JSON_.getString(o, "html"));
		
		return body;
	}
	
	public Object toJSON (Body b) throws JSONException
	{
		Object o = JSON_.newObject();
		if (b.hasText())
			JSON_.put(o, "text", toJSON(b.getText()));

		if (b.hasHTML())
			JSON_.put(o, "html", toJSON(b.getHTML()));
		
		return o;
	}

	public FolderDefinition toFolderDefinition(Object o) throws JSONException
	{
		FolderDefinition f = new FolderDefinition(JSON_.getString(o, "name"));
		
		if (JSON_.has(o, "subject"))
			f.setSubject(JSON_.getString(o, "subject"));
		
		if (JSON_.has(o, "author"))
			f.setAuthor(toIdentity(JSON_.getString(o, "author")));

		if (JSON_.has(o, "recipient"))
			f.setRecipient(toIdentity(JSON_.getString(o, "recipient")));
		
		if (JSON_.has(o, "stateDiffers") || JSON_.has(o, "stateEquals"))
		{
			TransportState d=null,e=null;
			if (JSON_.has(o, "stateDiffers"))
				d = TransportState.fromString(JSON_.getString(o, "stateDiffers"));
			
			if (JSON_.has(o, "stateEquals"))
				e = TransportState.fromString(JSON_.getString(o, "stateEquals"));
			
			f.setState(e, d);
		}
		
		if (JSON_.has(o,  "bayesianDictionary"))
			f.setBayesianDictionary(toDictionary(JSON_.getString(o, "bayesianDictionary")));
			
		if (JSON_.has(o, "autoBayesian"))
			f.setAutoBayesian(JSON_.getBoolean(o, "autoBayesian"));
		
		return f;
	}

	public Object toJSON(FolderDefinition d) throws JSONException
	{
		Object o = JSON_.newObject();
		JSON_.put(o, "name", toJSON(d.getName()));
		
		if (d.getAuthor()!=null)
			JSON_.put(o, "author", toJSON(d.getAuthor()));

		if (d.getSubject()!=null)
			JSON_.put(o, "subject", toJSON(d.getSubject()));

		if (d.getRecipient()!=null)
			JSON_.put(o, "recipient", toJSON(d.getRecipient()));
		
		if (d.getStateDiffers()!=null)
			JSON_.put(o, "stateDiffers", toJSON(d.getStateDiffers().toString()));
		
		if (d.getStateEquals()!=null)
			JSON_.put(o, "stateEquals", toJSON(d.getStateEquals().toString()));
		
		if (d.getBayesianDictionary()!=null)
			JSON_.put(o, "bayesianDictionary", toJSON(d.getBayesianDictionary()));
		
		if (d.getAutoBayesian())
			JSON_.put(o, "autoBayesian", toJSON(d.getAutoBayesian()));
		
		return o;
	}

	public Object toJSON(byte[] id)
	{
		return toJSON(Base64.encode(id));
	}

	public byte[] toBytes(String string)
	{
		return Base64.decode(string); 
	}

	public Attachments toAttachments(Object json) throws JSONException
	{
		Attachments attachments = new Attachments ();
		for (int i=0; i<JSON_.size(json); ++i)
		{
			Object v = JSON_.get(json, i);
			
			attachments.addAttachment(
				new Attachment(
					JSON_.has(v, "id") ? JSON_.getString(v, "id") : null,
					JSON_.has(v, "disposition") ? JSON_.getString(v, "disposition") : null,
					JSON_.has(v, "mime-type") ? JSON_.getString(v, "mime-type") : null
				)
			);
		}
		
		return attachments;
	}
	
	public Object toJSON (Attachments attachments) throws JSONException
	{
		Object a = JSON_.newArray();
		for (Attachment attachment : attachments.getList())
		{
			Object v = JSON_.newObject();
			JSON_.put(v, "id", toJSON(attachment.getId()));
			JSON_.put(v, "disposition", toJSON(attachment.getDisposition()));
			JSON_.put(v, "mime-type", toJSON(attachment.getMimeType()));
			
			JSON_.add(a, v);
		}
		
		return a;
	}
	
	public void fromJSON(Mail m, Object v) throws JSONException
	{
		log.debug("fromJSON mail",m.getId() ," ", m);
		
		String version = JSON_.getString(v, "version");
		
		m.setHeader	(
			toHeader(JSON_.getObject(v, "header"))
		);
		
		if (JSON_.has(v, "body"))
			m.setBody (
				toBody (JSON_.getObject(v, "body"))
			);

		if (JSON_.has(v, "attachments"))
			m.setAttachments (
				toAttachments (JSON_.getObject(v, "attachments"))
			);
	}
	
	public Object toJSON(Mail m) throws Exception
	{
		Object v = JSON_.newObject();
		JSON_.put(v, "version", toJSON("1.0"));
		
		JSON_.put(v, "header", toJSON(m.getHeader()));
		
		if (m.getBody() != null)
			JSON_.put(v, "body", toJSON(m.getBody()));
		
		if (m.getAttachments() != null)
			JSON_.put(v, "attachments", toJSON(m.getAttachments()));
		
		return v;
	}
	
	public void fromJSON(Conversation c, Object v) throws Exception
	{
		log.debug("fromJSON conversation", c.getId());

		String version = JSON_.getString(v, "version");
		c.setHeader(toHeader(JSON_.getObject(v, "header")));
		
		Object m = JSON_.getArray(v, "mail");
		for (int i=0; i<JSON_.size(m); ++i)
		{
			Object iNd = JSON_.getArray(m, i);
			c.addItemId(toID(JSON_.getString(iNd,0)), toDate(JSON_.getLong(iNd,1)));
		}
	}

	public Object toJSON(Conversation c) throws Exception
	{
		Object v = JSON_.newObject();
		JSON_.put(v, "version", toJSON("1.0"));
		
		JSON_.put(v, "header", toJSON(c.getHeader()));
		
		Object m = JSON_.newArray();
		for (Pair<ID,Date> p : c.getItemIds())
		{
			Object iNd = JSON_.newArray();
			JSON_.add(iNd, toJSON(p.first));
			JSON_.add(iNd, toJSON(p.second));
			JSON_.add(m, iNd);
		}

		JSON_.put(v, "mail", m);
		
		return v;
	}
	
	public void fromJSON(Folder f, Object v) throws Exception
	{
		String version = JSON_.getString(v, "version");
		
		if (JSON_.has(v, "definition"))
			f.setFolderDefinition(toFolderDefinition(JSON_.getObject(v, "definition")));

		if (f instanceof FolderSet)
		{
			if (f instanceof FolderMaster)
			{
				FolderMaster fm = (FolderMaster)f;
				
				{
					Object a = JSON_.getArray(v, "uidl");
					for (int i=0; i<JSON_.size(a); ++i)
					{
						Object iNd = JSON_.getArray(a, i);
						fm.addUIDLHash(JSON_.getString(iNd,0), toDate(JSON_.getLong(iNd, 1)));
					}
				}
				
				{
					Object a = JSON_.getArray(v, "externalKey");
					for (int i=0; i<JSON_.size(a); ++i)
					{
						Object iNd = JSON_.getArray(a, i);
						fm.addExternalKeyHash(JSON_.getString(iNd,0), toDate(JSON_.getLong(iNd,1)));
					}
				}
			}
			
			FolderSet fs = (FolderSet)f;
			
			Object a = JSON_.getArray(v, "parts");
			for (int i=JSON_.size(a)-1; i>=0; --i)  // reverse it
			{
				fs.addFolderId(toID(JSON_.getString(a, i)));
			}
			
			fs.setNumConversations(JSON_.getInt(v, "numConversations"));
			
			return;
		}
		
		Object a = JSON_.getArray(v, "conversations");
		for (int i=JSON_.size(a)-1; i>=0; --i)  // reverse it
		{
			Object iNd = JSON_.getArray(a, i);
			f.addConversationId(toID(JSON_.getString(iNd,0)), toDate(JSON_.getLong(iNd,1)));
		}
	}

	public Object toJSON(Folder f) throws Exception
	{
		Object v = JSON_.newObject();
		JSON_.put(v, "version", toJSON("1.0"));
		
		if (f.getFolderDefinition() != null)
			JSON_.put(v, "definition", toJSON(f.getFolderDefinition()));

		if (f instanceof FolderSet)
		{
			if (f instanceof FolderMaster)
			{
				FolderMaster fm = (FolderMaster)f;
				
				{
					Object a = JSON_.newArray();
					for (Map.Entry<String, Date> id : fm.getUIDLHashes().entrySet())
					{
						Object iNd = JSON_.newArray();
						JSON_.add(iNd, toJSON(id.getKey()));
						JSON_.add(iNd, toJSON(id.getValue()));
						JSON_.add(a, iNd);
					}
					JSON_.put(v, "uidl", a);
				}

				{
					Object a = JSON_.newArray();
					for (Map.Entry<String, Date> id : fm.getExternalKeyHashes().entrySet())
					{
						Object iNd = JSON_.newArray();
						JSON_.add(iNd, toJSON(id.getKey()));
						JSON_.add(iNd, toJSON(id.getValue()));
						JSON_.add(a, iNd);
					}
					JSON_.put(v, "externalKey", a);
				}
			}
			
			FolderSet fs = (FolderSet)f;
			Object a = JSON_.newArray();
			for (ID id : fs.getFolderIds())
				JSON_.add(a, toJSON(id));
			
			JSON_.put(v, "parts", a);
			JSON_.put(v, "numConversations", toJSON(fs.getNumConversations()));
			
			return v;
		}
		
		Object a = JSON_.newArray();
		for (Pair<ID,Date> p : f.getConversationIds())
		{
			Object iNd = JSON_.newArray();
			JSON_.add(iNd, toJSON(p.first));
			JSON_.add(iNd, toJSON(p.second));
			JSON_.add(a, iNd);
		}
		JSON_.put(v, "conversations", a);
		
		return v;
	}

	public void fromJSON(Settings item, Object o) throws JSONException 
	{
		String[] keys = JSON_.keys(o);
	
		Map<String,String> kv = new HashMap<String,String>();
		
		for (String key : keys)
			kv.put(key,  JSON_.getString(o,  key));
		
		item.setKV(kv);
	}
	
	public Object toJSON(Settings item) throws JSONException
	{
		Object o = JSON_.newObject();
		
		for (Entry<String, String> key : item.getKV().entrySet())
		{
			JSON_.put(o, key.getKey(), toJSON(key.getValue()));
		}
		
		return o;
	}
	
	public void fromJSON(PublicKey item, Object parse) throws JSONException 
	{
		item.setEmail(JSON_.getString(parse, "email"));
		item.setPublicKey(JSON_.getString(parse, "publicKey"));
	}
	
	public Object toJSON(PublicKey item) throws JSONException
	{
		Object o = JSON_.newObject();
		JSON_.put(o, "email", item.toString());
		JSON_.put(o, "publicKey", item.getPublicKey());
		
		return o;
	}	
	
	public void fromJSON(PublicKeyRing item, Object a) throws JSONException 
	{
		for (int i=0; i<JSON_.size(a); ++i)  // reverse it
		{
			Object iNs = JSON_.getObject(a, i);
			item.addPublicKeyFromCache(
				toID(JSON_.getString(iNs,"id")), 
				toIdentity(JSON_.getString(iNs, "identity"))
			);
		}
	}
	
	public Object toJSON(PublicKeyRing item) throws JSONException
	{
		Object a = JSON_.newArray();
		for (Pair<ID,Identity> iNs : item.getPublicKeys())
		{
			Object o = JSON_.newObject();
			JSON_.put(o, "id", toJSON(iNs.first));
			JSON_.put(o, "identity", toJSON(iNs.second));
		}
		
		return a;
	}	

}
