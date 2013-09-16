/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import core.util.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import mail.client.model.Conversation;
import mail.client.model.Identity;
import mail.client.model.Mail;
import mail.client.model.Settings;
import mail.client.model.TransportState;
import mail.client.model.UnregisteredIdentity;
import core.constants.ConstantsClient;
import core.crypt.Cryptor;
import core.crypt.CryptorRSA;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAFactory;
import core.callback.Callback;
import core.callback.CallbackDefault;
import core.callbacks.JSONSerialize;
import core.util.Base64;
import core.util.FastRandom;
import core.util.HttpDelegate;
import core.util.InternalResource;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Pair;
import core.util.Strings;

public class Mailer extends Servent<Master>
{
	static LogNull log = new LogNull(Mailer.class);
	HttpDelegate httpDelegate;
	FastRandom fastRandom = new FastRandom();
	
	public Mailer (HttpDelegate httpDelegate)
	{
		this.httpDelegate = httpDelegate;
	}
	
	public void sendMail (String password, Conversation conversation, Mail mail)
	{
		try
		{
			log.debug("Mailer.sendMail");
			mail.getHeader().unmarkState(TransportState.DRAFT);
			mail.getHeader().markState(TransportState.SENDING);
			mail.getHeader().getRecipients().registerRecipients(master.getAddressBook());

			conversation.itemChanged(mail);
			master.getIndexer().conversationChanged(conversation);
	
			doSend(password, conversation, mail);
		}
		catch (Exception e)
		{
			onSendFailed(conversation, mail, e);
		}
	}
	
	protected void doSend(String password, Conversation conversation, Mail mail) throws Exception
	{
		log.debug("Mailer.doSend");
		Map<String,String> sendMap = new HashMap<String,String>();
		
		Identity identity = getMaster().getIdentity();		
		sendMap.put("user", identity.getEmail());
		sendMap.put("password", password);
		
		sendMap.put("from", identity.getFull());
		sendMap.put("to", Strings.concat(mail.getHeader().getRecipients().getTo(), ","));
		sendMap.put("cc", Strings.concat(mail.getHeader().getRecipients().getCc(), ","));
		sendMap.put("bcc", Strings.concat(mail.getHeader().getRecipients().getBcc(), ","));
		sendMap.put("replyTo", Strings.concat(mail.getHeader().getRecipients().getReplyTo(), ","));
		sendMap.put("publicKey", Base64.encode(((CryptorRSA)getMaster().getCryptor()).getPublicKey()));
		
		/*
		if (mail.isPresendEncryptable())
		{
			Pair<String,String> presendEncrypted = mail.presendEncrypt();
			
			sendMap.put("cryptors", presendEncrypted.first);
			sendMap.put("block", presendEncrypted.second);
		}
		else
		{
		*/
			sendMap.put("subject", mail.getHeader().getSubject());
			sendMap.put("text", mail.getBody().getText());
			sendMap.put("html", mail.getBody().getHTML());
		/*
		}
		*/
		
		sendMap.put("messageId", mail.getHeader().getUIDL());		
		log.debug("sendMap ", sendMap);
		
		Cryptor cryptor = new CryptorRSAAES(CryptorRSAFactory.fromResources(null, InternalResource.getResourceAsStream(getClass(), "send-truststore.jks")));
		
		new JSONSerialize()
			.addCallback(cryptor.encrypt_())
			.addCallback(Base64.encodeBytes_())
			.addCallback(httpDelegate.execute_(
				HttpDelegate.PUT, ConstantsClient.WEB_SERVER_TOMCAT + "Send?random="+ fastRandom.nextLong(), null, false, false
			))
			.addCallback(onFinish_(conversation, mail))
			.invoke(sendMap);
	}		

	public Callback onFinish_ (Conversation conversation, Mail mail)
	{
		return 
			new CallbackDefault(conversation, mail) {
				public void onSuccess(Object...arguments)
				{
					onSendSucceeded((Conversation)V(0), (Mail)V(1));
				}
				
				public void onFailure(Exception e)
				{
					onSendFailed((Conversation)V(0), (Mail)V(1), e);
				}
		};
	}
	
	protected void onSendSucceeded (Conversation conversation, Mail mail)
	{
		log.debug("Mailer.onSendSucceeded");
		
		mail.getHeader().unmarkState(TransportState.SENDING);
		mail.getHeader().markState(TransportState.SENT);

		conversation.itemChanged(mail);
		master.getIndexer().conversationChanged(conversation);
		master.getEventPropagator().signal(Events.SendSucceeded, mail);
	}
	
	protected void onSendFailed (Conversation conversation, Mail mail, Exception e)
	{
		log.debug("Mailer.onSendFailed");
		log.exception(e);
		
		mail.getHeader().unmarkState(TransportState.SENDING);
		mail.getHeader().markState(TransportState.DRAFT);
		
		conversation.itemChanged(mail);
		master.getIndexer().conversationChanged(conversation);
		master.getEventPropagator().signal(Events.SendFailed, mail);
	}

	public void lookUpPGPFor(Identity identity) 
	{
	}
}
