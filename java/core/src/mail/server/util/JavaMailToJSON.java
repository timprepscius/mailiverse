/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import core.constants.ConstantsMailJson;
import core.util.Base64;
import core.util.LogNull;
import core.util.Pair;
import core.util.Streams;

public class JavaMailToJSON
{
	public static class MailDescription
	{
		public String author;
		public String subject;
		public String body;
	} ;
	
	static LogNull log = new LogNull(JavaMailToJSON.class);
	
	public JavaMailToJSON()
	{
		
	}
	
	public String portion(String s)
	{
		int end = s.indexOf('\n');
		if (end == -1)
			end = s.length();
		
		return s.substring(0, Math.min(end, 32));
	}
	
	public String encode(byte[] b)
	{
		return new String(Base64.encode(b));
	}
	
	public String encode(String s) throws UnsupportedEncodingException
	{
		return encode(s.getBytes("UTF-8"));
	}
	
	public JSONArray parseHeaders (Enumeration e) throws Exception
	{
		JSONArray headers = new JSONArray();
		while (e.hasMoreElements())
		{
			Header h = (Header)e.nextElement();
			JSONArray jh = new JSONArray();
			
			String k = MimeUtility.decodeText(h.getName());
			String v = MimeUtility.decodeText(h.getValue());
			log.debug("header",k,v);
			jh.put(k);
			jh.put(encode(v));
			
			headers.put(jh);
		}
		
		return headers;
	}
	
	public JSONObject parseDates (MimeMessage message) throws Exception
	{
		Date receivedDate = message.getReceivedDate();
		Date sentDate = message.getSentDate();
		
		JSONObject dates = new JSONObject();
		if (receivedDate!=null)
			dates.put(ConstantsMailJson.Received, "" + receivedDate.getTime());
		if (sentDate!=null)
			dates.put(ConstantsMailJson.Sent, "" + sentDate.getTime());

		return dates;
	}
	
	public JSONObject parseAddresses (MimeMessage message, MailDescription description) throws Exception
	{
		JSONObject ja = new JSONObject();
		
		ArrayList<Pair<String, Address[]>> ta = new ArrayList<Pair<String,Address[]>>();
		
		Message.RecipientType[] recipientTypes  = { Message.RecipientType.TO,  Message.RecipientType.CC, Message.RecipientType.BCC };
		for (Message.RecipientType r : recipientTypes)
		{
			try
			{
				Address[] as = message.getRecipients(r);
				ta.add(new Pair<String, Address[]>(r.toString().toLowerCase(), as));
			}
			catch (Exception e)
			{
				log.error("Failed to read addresses for " + r.toString());
			}
		}
		
		try
		{
			ta.add(new Pair<String, Address[]>(ConstantsMailJson.ReplyTo, message.getReplyTo()));
		}
		catch (Exception e)
		{
			log.error("Failed to read addresses for reply-to");
		}
		
		try
		{
			ta.add(new Pair<String, Address[]>(ConstantsMailJson.From,  message.getFrom() ));
		}
		catch (Exception e)
		{
			log.error("Failed to read addresses for from");
		}
		
		for (Pair<String, Address[]> tas : ta)
		{
			if (tas.second != null)
			{
				JSONArray jas = new JSONArray();
				for (Address a : tas.second)
				{
					InternetAddress ia = (InternetAddress)a;
					
					if (ia != null)
					{
						JSONObject jia = new JSONObject();
						String name = ia.getPersonal();
						String email = ia.getAddress();
						
						if (tas.first.equals(ConstantsMailJson.From))
							description.author = name;
						
						if (name != null)
							jia.put(ConstantsMailJson.Name, encode(MimeUtility.decodeText(name)));

						if (email != null)
							jia.put(ConstantsMailJson.Email, encode(MimeUtility.decodeText(email)));
						
						jas.put(jia);
					}
				}
			
				ja.put(tas.first, jas);
			}
		}
		
		return ja;
	}
	
	public JSONObject parseContent (Part message, MailDescription description) throws Exception
	{
		log.debug("parseContent");

		JSONObject c = new JSONObject();
		c.put(ConstantsMailJson.Headers, parseHeaders(message.getAllHeaders()));
		
		Object jValue = null;
		String jType = ConstantsMailJson.Unknown;
		
		Object content = message.getContent();
		if (content instanceof String)
		{
			String v = (String)content;
			log.debug("content", "string", portion(v));
			
			jType = ConstantsMailJson.String;
			jValue = v;
		}
		else
		if (content instanceof MimeMultipart)
		{
			log.debug("content", "multipart");
			
			MimeMultipart m = (MimeMultipart)content;
			
			JSONArray parts = new JSONArray();
			
			for (int i=0; i<m.getCount(); ++i)
			{
				BodyPart b = m.getBodyPart(i);
				parts.put(parseContent(b, description));
			}
			
			jType = ConstantsMailJson.MultiPart;
			jValue = parts;
		}
		else
		if (content instanceof InputStream)
		{
			String v = encode(Streams.readFullyBytes((InputStream)content));
			log.debug("inputstream", portion(v));
			
			jType = ConstantsMailJson.Bytes;
			jValue = v;
		}

		c.put(ConstantsMailJson.Class, jType);
		c.put(ConstantsMailJson.Value, jValue);
		
		return c;
	}
	
	public byte[] convert (String path, Date writeTime, byte[] bytes, MailDescription description) throws Exception
	{
		Session s = Session.getDefaultInstance(new Properties());
		MimeMessage message = new MimeMessage(s, new ByteArrayInputStream(bytes));

		if (message.getSubject()!=null)
		{
			String REPLY_PREFIX = "re:";
			
			description.subject = message.getSubject().trim();
			while (description.subject.toLowerCase().startsWith(REPLY_PREFIX))
				description.subject = description.subject.substring(REPLY_PREFIX.length()).trim();
		}
		
		JSONObject m = new JSONObject();
		m.put(ConstantsMailJson.Original, path);
		m.put(ConstantsMailJson.Addresses, parseAddresses(message, description));
		
		JSONObject dates = parseDates(message);
		dates.put(ConstantsMailJson.Written, "" + writeTime.getTime());

		m.put(ConstantsMailJson.Dates, dates);
		
		if (message.getMessageID()!=null)
			m.put(ConstantsMailJson.UIDL, message.getMessageID());
		
		m.put(ConstantsMailJson.Content, parseContent(message, description));
		
		return m.toString().getBytes("UTF-8");
	}
	
	public byte[] convertAttemptToShowException (String path, Date writeTime, byte[] bytes, MailDescription description) throws Exception
	{
		try
		{
			return convert(path, writeTime, bytes, description);
		}
		catch (Exception e)
		{
			description.author = "Unknown";
			description.subject = "Failed to parse message";
			description.body = "The mail server failed to parse the received mail";
			
			JSONObject m = new JSONObject();
			m.put(ConstantsMailJson.Original, path);
			m.put(ConstantsMailJson.Headers, new JSONArray("[ [\"subject\",\"The mail server failed to handle this message.\"] ]"));

			JSONObject dates = new JSONObject();
			dates.put(ConstantsMailJson.Written, "" + writeTime.getTime());
			m.put(ConstantsMailJson.Dates, dates);
			
			JSONObject c = new JSONObject();
			c.put(ConstantsMailJson.Class, ConstantsMailJson.String);
			c.put(ConstantsMailJson.Value, e.toString());
			c.put(ConstantsMailJson.Headers, new JSONArray("[ [\"Content-Type\",\"text/plain\"] ]"));
			m.put(ConstantsMailJson.Content, c);
			
			return m.toString().getBytes("UTF-8");
		}
	}
}
