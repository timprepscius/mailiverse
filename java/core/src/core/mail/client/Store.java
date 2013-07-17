/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.callback.CallbackWithVariables;
import core.connector.async.AsyncStoreConnector;
import core.connector.async.AsyncStoreConnectorEncrypted;
import core.crypt.Cryptor;
import mail.client.model.Mail;
import mail.client.model.Original;

public class Store extends Servent<Master>
{
	AsyncStoreConnector connector;
	
	public Store(Cryptor cryptor, AsyncStoreConnector connector)
	{
		this.connector = new AsyncStoreConnectorEncrypted(cryptor, connector);
	}
	
	public Original getOriginal(String path)
	{
		Original original = new Original(path);
		connector.get_().addCallback(
			new CallbackWithVariables(original){
				@Override
				public void invoke(Object... arguments)
				{
					Original original = V(0);
					if (arguments[0] instanceof Exception)
						original.setException((Exception)arguments[0]);
					else
						original.setData((byte[])arguments[0]);
					
					master.getEventPropagator().signal(Events.OriginalLoaded, original);
				}
			}
		).invoke(path);
		
		return original;
	}

	public AsyncStoreConnector getConnector()
	{
		return connector;
	}

	public void loadAttachments(Mail mail) 
	{
		connector.get_().addCallback(
			new CallbackDefault(mail)
			{
				public void onSuccess(Object... arguments) throws Exception
				{
					Mail mail = V(0);
					mail.getAttachments().loadFrom((byte[])arguments[0]);
					master.getEventPropagator().signal(Events.LoadAttachments, mail);
				}
				
				public void onFailure(Exception e)
				{
					Mail mail = V(0);
					master.getEventPropagator().signal(Events.LoadAttachmentsFailed, mail);
				}
			}
		).invoke(mail.getHeader().getExternalKey());
		
	}
	
	public void deleteMail (Mail mail)
	{
		CallbackChain chain = new CallbackChain();
		
		if (mail.getHeader().getExternalKey() != null)
			chain.addCallback(connector.delete_(mail.getHeader().getExternalKey()));
			
		if (mail.getHeader().getOriginalKey() != null)
			chain.addCallback(connector.delete_(mail.getHeader().getOriginalKey()));
		
		chain.invoke();
	}
}
