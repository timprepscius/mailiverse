/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import java.util.ArrayList;

import java.util.Date;
import java.util.List;

/*
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;
*/

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.callbacks.Single;
import core.constants.ConstantsMailJson;
import core.crypt.CryptorAES;
import core.util.Base64;
import core.util.JSON_;
import core.util.JSON_.JSONException;
import core.util.LogNull;
import core.util.Strings;

import mail.client.cache.ID;
import mail.client.model.Attachment;
import mail.client.model.Attachments;
import mail.client.model.Body;
import mail.client.model.Dictionary;
import mail.client.model.Direction;
import mail.client.model.Header;
import mail.client.model.Identity;
import mail.client.model.Mail;
import mail.client.model.Recipients;
import mail.client.model.TransportState;
import mail.client.model.UnregisteredIdentity;


public class ArrivalsProcessor extends Servent<Master>
{
	static LogNull log = new LogNull(ArrivalsProcessor.class);
	
	@SuppressWarnings("serial")
	static class DuplicateMailException extends Exception {};
	
	
	public void processSuccess (Direction direction, String externalKey, Date date, byte[] inputStream) throws Exception
	{
		Indexer indexer = master.getIndexer();

		log.debug("processSuccess");
		try
		{
			Mail mail = processStream (direction, externalKey, date, inputStream);

			indexer.addMail(mail);
		}
		catch (DuplicateMailException e)
		{
			log.debug("Marking duplicate");
			indexer.addDuplicate(externalKey, date);
		}
		catch (Exception e)
		{
			throw e;
		}
	}
	
	public static String decode (String s)
	{
		return Strings.toString(Base64.decode(s));
	}
	
	public static String getFirstHeader(Object message, String s, String def) throws JSONException
	{
		Object a = JSON_.getArray(message, "headers");
		
		if (a != null)
		{
			s = s.toLowerCase();
			for (int i=0; i<JSON_.size(a); ++i)
			{
				Object h = JSON_.getArray(a, i);
				if (s.equals(JSON_.getString(h, 0).toLowerCase()))
					return decode(JSON_.getString(h, 1));
			}
		}
		
		return def;
	}
	
	public Callback setExternalKeys_(String externalKey, String originalKey)
	{
		return new CallbackDefault(externalKey,originalKey) {
			public void onSuccess(Object... arguments) throws Exception {
				log.debug("mail has finally loaded, setting external key");
				
				String externalKey = V(0);
				String originalKey = V(1);
				Mail mail = (Mail)arguments[0];
				
				if (mail.getHeader().getExternalKey()==null)
				{
					mail.getHeader().setOriginalKey(originalKey);
					mail.getHeader().setExternalKey(externalKey);
					mail.markDirty();
				}
			}
		};
	}
	
	Mail processStream (Direction direction, String externalKey, Date date, byte[] bytes) throws Exception
	{
		String string = Strings.toString(bytes);
		Object message = JSON_.parse(string);
		String originalKey = JSON_.getString(message, ConstantsMailJson.Original);
		Object content = JSON_.getObject(message,ConstantsMailJson.Content);
		// CryptorAES embeddedCryptor = null;
		
		/*
		// ok, if there are embedded cryptors we take a look at them and find ours
		if (JSON_.has(content, ConstantsMailJson.EmbeddedCryptors))
		{
			Object cryptors = JSON_.getArray(content, ConstantsMailJson.EmbeddedCryptors);
			for (int i=0; i<JSON_.size(cryptors); ++i)
			{
				try
				{
					String cryptor= JSON_.getString(cryptors, i);
					byte[] aesKey = getMaster().getCryptor().decrypt(Base64.decode(cryptor));
					embeddedCryptor = new CryptorAES(aesKey);
				}
				catch (Exception e)
				{
					log.debug("embedded cryptor " + i + " failed to decode");
				}
			}
		}
		*/
		
		String uidl = externalKey;
		
		if (JSON_.has(message, ConstantsMailJson.UIDL))
		{
			uidl = JSON_.getString(message, ConstantsMailJson.UIDL);
			if (master.getIndexer().containsUIDL(uidl))
			{
				log.debug("contains UIDL");
				if (direction == Direction.OUT)
				{
					log.debug("mail was sent");
					
					String id = uidl.replaceAll("<(.*)@.*>", "$1");
					log.debug("found embedded id", id);
					
					if (id != null)
					{
						CacheManager cacheManager = master.getCacheManager();
						
						Mail mail = cacheManager.getMail(ID.fromString(id));
						if (mail!= null)
						{
							log.debug("cache has the mail");
							mail.apply(setExternalKeys_(externalKey, originalKey));
						}
					}
				}
				
				throw new DuplicateMailException();
			}
		}
		
		String subject = getFirstHeader(content, "subject", "");
		
		Identity author = null;
		Recipients recipients = new Recipients ();

		if (JSON_.has(message, ConstantsMailJson.Addresses))
		{
			Object addresses = JSON_.getObject(message, ConstantsMailJson.Addresses);
			if (JSON_.has(addresses, ConstantsMailJson.From))
			{
				Object jAddresses = JSON_.getArray(addresses, ConstantsMailJson.From);
				if (JSON_.size(jAddresses) > 0)
				{
					Object jia = JSON_.getObject(jAddresses, 0);
					author = master.getAddressBook().getIdentity(
						new UnregisteredIdentity(
							JSON_.has(jia,ConstantsMailJson.Name) ?
								decode(JSON_.getString(jia,ConstantsMailJson.Name)) : null,
							JSON_.has(jia, ConstantsMailJson.Email) ?
								decode(JSON_.getString(jia, ConstantsMailJson.Email)) : null
						)
					);
				}
			}
		
			String[] buckets = { ConstantsMailJson.To, ConstantsMailJson.Cc, ConstantsMailJson.Bcc, ConstantsMailJson.ReplyTo };
			
			for (String bucket : buckets)
			{
				if (JSON_.has(addresses,bucket))
				{
					Object jAddresses = JSON_.getArray(addresses, bucket);
					for (int i=0; i<JSON_.size(jAddresses); ++i)
					{
						Object jia = JSON_.getObject(jAddresses, i);
						recipients.get(bucket).add(master.getAddressBook().getIdentity(
							new UnregisteredIdentity(
								JSON_.has(jia,ConstantsMailJson.Name) ?
										decode(JSON_.getString(jia,ConstantsMailJson.Name)) : null,
								JSON_.has(jia, ConstantsMailJson.Email) ?
									decode(JSON_.getString(jia, ConstantsMailJson.Email)) : null
							)
						));
					}
				}
			}
		}
		
		if (author == null)
			author = master.getAddressBook().getIdentity(new UnregisteredIdentity("<Unknown>"));

		Body body = new Body();
		Attachments attachments = new Attachments();
		
		List<Object> contents = new ArrayList<Object>();
		if (content != null)
			contents.add(content);
		
		while (contents.size() > 0)
		{
			Object c = contents.get(0);
			contents.remove(0);

			String clazz = JSON_.getString(c, ConstantsMailJson.Class);
			Object value = JSON_.has(c, ConstantsMailJson.Value) ? 
				JSON_.get(c, ConstantsMailJson.Value) : null;
			String contentType = getFirstHeader(c, "Content-Type", "text/plain");

			if (clazz.equals(ConstantsMailJson.String))
			{
				String valueString = JSON_.asString(value);
				
				/*
				if (contentType.startsWith("encrypted/block"))
				{
					String jsonBlock = Strings.toString(embeddedCryptor.decrypt(Base64.decode(valueString)));
					Object json = JSON_.parse(jsonBlock);
					
					subject = JSON_.has(json, "subject") ? JSON_.getString(json, "subject") : null;
					contents.add(JSON_.getObject(json, "content"));
				}
				else
				*/
				if (contentType.startsWith("text/html"))
				{
					if (body.getHTML() == null)
						body.setHTML(valueString);
				}
				else
				if (contentType.startsWith("text/plain"))
				{
					if (body.getText() == null)
						body.setText(valueString);
				}
			}
			else
			if (clazz.equals(ConstantsMailJson.MultiPart))
			{
				Object valueParts = (Object)value;
				for (int i=0; i<JSON_.size(valueParts); ++i)
				{
					Object valueContent = JSON_.getObject(valueParts,i);
					contents.add(valueContent);
				}
			}
			else
			if (clazz.equals(ConstantsMailJson.Bytes))
			{
				String contentDisposition = getFirstHeader(c, "Content-Disposition", "None");
				String contentId = getFirstHeader(c, "Content-Id", "None");
				
				String attachmentId = Attachment.getAttachmentId(contentDisposition, contentId);
				if (attachmentId != null)
				{
					attachments.addAttachment(
						new Attachment (attachmentId, contentDisposition, contentType)
					);
				}
			}
		}
		
		Date markDate = null;
		
		if (JSON_.has(message, ConstantsMailJson.Dates))
		{
			Object dates = JSON_.getObject(message, ConstantsMailJson.Dates);
			
			// use a date from somewhere
			markDate =
				JSON_.has(dates, ConstantsMailJson.Sent) ?
					new Date(Long.parseLong(JSON_.getString(dates, ConstantsMailJson.Sent))) : null;
			
			if (markDate == null)
				markDate = 
					JSON_.has(dates, ConstantsMailJson.Received) ?
							new Date(Long.parseLong(JSON_.getString(dates, ConstantsMailJson.Received))) : null;
							
			if (markDate == null)
				markDate = 
					JSON_.has(dates, ConstantsMailJson.Written) ?
						new Date(Long.parseLong(JSON_.getString(dates, ConstantsMailJson.Written))) : null;
		}
		
		if (markDate == null)
			markDate = date;

		// received or sent
		TransportState transportState = 
			direction == Direction.IN ?
				TransportState.fromList(TransportState.RECEIVED) :
					TransportState.fromList(TransportState.SENT);
				
		Header header = new Header(externalKey, originalKey, uidl, author, recipients, subject, markDate, transportState, body.calculateBrief());
		Mail mail = master.getCacheManager().newMail(header, body, attachments);
		mail.getHeader().setDictionary(new Dictionary (mail));
		
		if (!attachments.getList().isEmpty())
			mail.getHeader().markState("ATTACHMENT");
		
		return mail;
	}
		
	public boolean alreadyProcessed(String path)
	{
		Indexer indexer = master.getIndexer();
		return indexer.containsExternalKey(path);
	}

	public void processFailure(Direction direction, String path, Date date, Exception e)
	{
		log.error("processFailure ", direction, " ", path, " e:", e);

		Indexer indexer = master.getIndexer();
		indexer.addFailure(direction, path, date, e);
	}
}
