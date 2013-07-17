/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.core;

import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class MimeMessageRetainMessageId extends MimeMessage
{
	String savedMessageId;
	
	public MimeMessageRetainMessageId (Session session, InputStream inputStream) throws MessagingException
	{
		super(session, inputStream);
		
		savedMessageId = getMessageID();
	}
	
	public MimeMessageRetainMessageId (Session session) throws MessagingException
	{
		super(session);
	}
	
	public void setMessageID (String savedMessageId)
	{
		this.savedMessageId = savedMessageId;
	}

	@Override
	protected void updateMessageID()
	{
		try
		{
			setHeader("Message-ID", savedMessageId);
		}
		catch (javax.mail.MessagingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
