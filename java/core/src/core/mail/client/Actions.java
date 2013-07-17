/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import java.util.Date;

import mail.client.model.Attachments;
import mail.client.model.Body;
import mail.client.model.Conversation;
import mail.client.model.Header;
import mail.client.model.Mail;
import mail.client.model.Recipients;
import mail.client.model.TransportState;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.constants.ConstantsSettings;
import core.constants.ConstantsEnvironmentKeys;
import core.util.LogNull;
import core.util.Pair;


public class Actions extends Servent<Master>
{
	static LogNull log = new LogNull(Actions.class);
	
	public Body calculateSignaturedReplyBody (Mail mail)
	{
		String signature = getMaster().getCacheManager().getSettings().get(ConstantsSettings.SIGNATURE, "");
		return mail.calculateReply(signature);
	}
	
	public Pair<Conversation,Mail> newMail () throws Exception
	{
		Mail mail = master.getCacheManager().newMail (
			new Header (
				null, // external key
				null, // original key
				null,
				master.getIdentity(),
				new Recipients(),
				null,
				new Date(),
				TransportState.fromList(TransportState.DRAFT),
				null
			),
			new Body (),
			new Attachments ()
		);
	
		Conversation conversation = master.getIndexer().newMail(mail);
		return new Pair<Conversation,Mail>(conversation,mail);
	}
	
	public void saveMail (Conversation conversation, Mail mail)
	{
		mail.getHeader().setDate(new Date());
		mail.markDirty();
		
		conversation.itemChanged(mail);
		master.getIndexer().conversationChanged(conversation);
	}
	
	public void deleteMail (Conversation conversation, Mail mail)
	{
		conversation.removeItem(mail);
		master.getCacheManager().deleteMail(mail);
		master.getStore().deleteMail(mail);
		
		if (conversation.getNumItems()==0)
		{
			master.getIndexer().removeConversation(conversation);
			master.getCacheManager().deleteConversation(conversation);
		}
		else
		{
			master.getIndexer().conversationChanged(conversation);
		}
	}
	
	public Callback deleteMail_ (Conversation conversation, Mail mail)
	{
		return new CallbackDefault(conversation, mail) 
		{
			public void onSuccess(Object... arguments) throws Exception 
			{
				Conversation conversation = V(0);
				Mail mail = V(1);
				deleteMail(conversation, mail);
				next(arguments);
			}
		};
	}
	
	public void deleteConversation (Conversation conversation)
	{
		Mail mails[] = conversation.getItems().toArray(new Mail[0]);
		
		for (Mail mail : mails)
			mail.apply(deleteMail_(conversation, mail));
	}
	
	public Mail replyToAll (Conversation conversation, Mail mail) throws Exception
	{
		return reply (
			new Recipients (mail.getHeader().calculateReplyAll(getMaster()), null, null, null),
			conversation, mail, calculateSignaturedReplyBody(mail)
		);
	}

	public Mail replyTo (Conversation conversation, Mail mail) throws Exception
	{
		return reply (
			new Recipients (mail.getHeader().calculateReplyTo(getMaster()), null, null, null),
			conversation, mail, calculateSignaturedReplyBody(mail)
		);
	}
	
	public Mail forward (Conversation conversation, Mail mail) throws Exception
	{
		return reply (
			new Recipients (null,null,null,null),
			conversation, mail, new Body(mail.getBody())
		);
	}
	
	public void sendMail (Conversation conversation, Mail mail)
	{
		log.debug("Actions.sendMail");
		
		Mailer sendMail = master.getMailer();
		
		sendMail.sendMail(
			master.getEnvironment().get(ConstantsEnvironmentKeys.SMTP_PASSWORD),
			conversation,
			mail
		);
	}
	
	public void reindexConversation (Conversation conversation)
	{
		master.getIndexer().conversationChanged(conversation);
	}

	public Mail reply (Recipients recipients, Conversation conversation, Mail mail, Body body) throws Exception
	{
		Mail reply = master.getCacheManager().newMail (
			new Header (
				null, // external key
				null, // original key
				null,
				master.getIdentity(),
				recipients,
				mail.getHeader().getSubject(),
				new Date(),
				TransportState.fromList(TransportState.DRAFT),
				null
			),
			body != null ? body : new Body(),
			new Attachments ()
		);
		
		conversation.addItem(reply);
		master.getIndexer().replyMail(conversation, reply);

		return reply;
	}
}
