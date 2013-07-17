/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

import mail.client.CacheManager;
import mail.client.Events;

import core.constants.ConstantsMailJson;
import core.crypt.CryptorAES;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAFactory;
import core.util.JSON_;
import core.util.LogNull;
import core.util.Pair;
import core.util.Strings;

public class Mail extends Model
{
	static LogNull log = new LogNull(Mail.class);

	static class SortByDateLatestFirst implements Comparator<Mail>  {

		@Override
		public int compare(Mail l, Mail r)
		{
			Date ld = l.getHeader().getDate(), rd = r.getHeader().getDate();
			return rd.compareTo(ld);
		}
	}
	
	static class SortByDateLatestLast implements Comparator<Mail>  {

		@Override
		public int compare(Mail l, Mail r)
		{
			Date ld = l.getHeader().getDate(), rd = r.getHeader().getDate();
			return ld.compareTo(rd);
		}
	}

	Header header;
	Body body;
	Attachments attachments;
	
	public Mail (CacheManager manager)
	{
		super(manager);
		
		getLoadCallbacks()
			.addCallback(manager.getMaster().getEventPropagator().signal_(Events.LoadMail, this));
	}
	
	public void reset ()
	{
		
	}
	
	public Header getHeader ()
	{
		return header;
	}
	
	public void setHeader (Header header)
	{
		this.header = header;
	}
		
	public Body getBody ()
	{
		return body;
	}
	
	public void setBody (Body body)
	{
		this.body = body;
	}
	
	public void setBody (String text, String html)
	{
		body.setText(text);
		body.setHTML(html);
		header.setBrief(body.calculateBrief());
	}
	
	public Identity[] calculateReplyTo ()
	{
		return header.calculateReplyTo(getManager().getMaster());
	}
	
	public Body calculateReply (String signature)
	{
		Body reply = new Body();
		
		String author = "" + getHeader().getAuthor();
		String date = "" + getHeader().getDate();
		
		String prefix = "On " + date + ", " + author + " wrote:";
		String html = 
			"<br/><br/>" + signature + "<br/><br/><div class='quote-prefix'>" + prefix + "</div>" + 
			"<blockquote class='cite' style='margin:0 0 0 .8ex;border-left:1px #ccc solid;padding-left:1ex'>" + 
					(body.hasHTML() ? body.getStrippedHTML() : ("<pre>" + body.getStrippedText() + "</pre>")) + "</blockquote>";
		
		reply.setText("\r\n\r\n" + signature + "\r\n\r\n" + prefix + "\r\n" + body.calculateReply());
		reply.setHTML(html);
		
		return reply;
	}
	
	public Attachments getAttachments ()
	{
		return attachments;
	}

	public void setAttachments(Attachments attachments)
	{
		this.attachments = attachments;
	}

	public boolean isPresendEncryptable() 
	{
		for (Identity i : getHeader().getRecipients().getAll())
		{
			if (!i.hasPublicKey())
				return false;
		}
		
		return true;
	}
	
	public Pair<String,String> presendEncrypt () throws Exception
	{
		byte[] aesKey = CryptorAES.newKey();
		CryptorAES aes = new CryptorAES(aesKey);
		
		List<String> cryptors = new ArrayList<String>();
		for (Identity i : getHeader().getRecipients().getAll())
		{
			CryptorRSAAES rsaaes = new CryptorRSAAES(CryptorRSAFactory.fromString(i.getPublicKey(), null));
			cryptors.add(Base64.encode(rsaaes.encrypt(aesKey)));
		}
		
		Object json = JSON_.newObject();
		JSON_.put(json, ConstantsMailJson.Class, JSON_.newString(ConstantsMailJson.MultiPart));
		Object multiPart = JSON_.newArray();
		
		
		if (getBody().hasText())
		{
			Object part = JSON_.newObject();
			Object headers = JSON_.newArray();
			Object mimeType = JSON_.newArray();
			JSON_.add(mimeType, JSON_.newString("Content-Type"));
			JSON_.add(mimeType, JSON_.newString("text/plain"));
			JSON_.add(headers, mimeType);			
			JSON_.put(part, ConstantsMailJson.Class, ConstantsMailJson.String);
			JSON_.put(part, ConstantsMailJson.Value, getBody().getText());
			JSON_.put(part, ConstantsMailJson.Headers, headers);
			
			JSON_.add(multiPart, part);
		}
		
		{
			Object part = JSON_.newObject();
			Object headers = JSON_.newArray();
			Object mimeType = JSON_.newArray();
			JSON_.add(mimeType, JSON_.newString("Content-Type"));
			JSON_.add(mimeType, JSON_.newString("text/html"));
			JSON_.add(headers, mimeType);			
			JSON_.put(part, ConstantsMailJson.Class, ConstantsMailJson.String);
			JSON_.put(part, ConstantsMailJson.Value, getBody().getHTML());
			JSON_.put(part, ConstantsMailJson.Headers, headers);

			JSON_.add(multiPart, part);
		}
		
		Object container = JSON_.newObject();
		JSON_.put(container, "subject", getHeader().getSubject());
		JSON_.put(container,  "content", multiPart);
		
		return new Pair<String,String>(
			Strings.concat(cryptors,","), 
			Base64.encode(aes.encrypt(Strings.toBytes(JSON_.asString(container))))
		);
	}
}
