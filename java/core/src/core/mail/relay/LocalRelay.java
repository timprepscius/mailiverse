/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.relay;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import mail.core.MimeMessageRetainMessageId;
import mail.core.SMTPAuthenticator;

import core.constants.ConstantsServer;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAJCE;
import core.util.ExternalResource;
import core.util.JSONSerializer;
import core.util.LogNull;
import core.util.LogOut;

public class LocalRelay
{
	LogOut log = new LogOut(LocalRelay.class);
	CryptorRSAAES cryptor;
	
	public LocalRelay () throws Exception
	{
		cryptor = new CryptorRSAAES(new CryptorRSAJCE(ExternalResource.getResourceAsStream(getClass(), "keystore.jks"), null));
	}
	
	public void onMail (byte[] bytes) throws Exception
	{
		log.debug("LocalRelay.onMail");
		
		byte[] clear = cryptor.decrypt(bytes);
		log.debug("after decrypt");
		
		Map<String, String> map = JSONSerializer.deserialize(clear);
		for (Entry<String, String> e : map.entrySet())
			log.trace(e.getKey(), e.getValue());
		
		String user = (String)map.get("user");
		String password = (String)map.get("password");
		
        Properties props = new Properties();
        props.put("mail.smtp.user", user);
        props.put("mail.smtp.host", ConstantsServer.LOCAL_SMTP_HOST);
        props.put("mail.smtp.port", ConstantsServer.LOCAL_SMTP_PORT);
        props.put("mail.smtp.auth", "true");

        // props.put("mail.smtp.starttls.enable","true");        
        
        props.put("mail.smtp.socketFactory.port", ConstantsServer.LOCAL_SMTP_PORT);
        // props.put("mail.debug", "true");
		
        Authenticator auth = 
        	new SMTPAuthenticator(
        		user,
        		password
        	);
        
		log.debug("setting properties");		
        Session session = Session.getInstance(props, auth);
        MimeMessageRetainMessageId message = new MimeMessageRetainMessageId(session);
        
        String version = map.get("version");
        String from = map.get("from");
        String to = map.get("to");
        String cc = map.get("cc");
        String bcc = map.get("bcc");
        String subject = map.get("subject");
        String text = map.get("text");
        String html = map.get("html");
        String messageId = map.get("messageId");
        
        String[] tos = to.split(",");
        String[] ccs = cc.split(",");
        String[] bccs = bcc.split(",");
        
        for (String address : tos)
        	message.addRecipients(RecipientType.TO, address);
        
        for (String address : ccs)
        	message.addRecipients(RecipientType.CC, address);
        	
        for (String address : bccs)
        	message.addRecipients(RecipientType.BCC, address);

        message.setFrom(new InternetAddress(from));
		if (subject != null)
			message.setSubject(subject);
			
        message.setSentDate(new Date());
        
        MimeMultipart multiPart = new MimeMultipart("alternative");

		if (text != null)
		{
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setContent(text, "text/plain");
			multiPart.addBodyPart(textPart);
		}
        
		if (html != null)
		{
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(html, "text/html");
			multiPart.addBodyPart(htmlPart);
		}
        
        message.setContent(multiPart);
        message.setMessageID(messageId);
        
		log.debug("Sending mail");		
        Transport.send(message);
        log.debug("Done sending mail");
	}
}
