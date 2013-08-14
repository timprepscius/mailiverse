/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;

import java.io.IOException;

import mail.client.model.Attachments;
import mail.client.model.Body;
import mail.client.model.Header;
import mail.client.model.Identity;
import mail.client.model.Mail;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

@Export
public class MailI implements Exportable 
{
	Mail o;
	
	public MailI (Mail o)
	{
		this.o = o;
	}
	
	public Mail deref ()
	{
		return o;
	}
	
	public boolean equals(MailI rhs)
	{
		return o == rhs.o || o.equals(rhs.o);
	}
	
	public Header getHeader()
	{
		return o.getHeader();
	}

	public void setHeader(Header header)
	{
		o.setHeader(header);
	}

	public Body getBody()
	{
		return o.getBody();
	}

	public void setBody(Body body)
	{
		o.setBody(body);
	}

	public void setBody(String text, String html)
	{
		o.setBody(text, html);
	}

	public Identity[] calculateReplyTo()
	{
		return o.calculateReplyTo();
	}

	public Body calculateReply(String signature) throws IOException
	{
		return o.calculateReply(signature);
	}

	public Attachments getAttachments()
	{
		return o.getAttachments();
	}

	public void setAttachments(Attachments attachments)
	{
		o.setAttachments(attachments);
	}

	public boolean isLoaded()
	{
		return o.isLoaded();
	}

}