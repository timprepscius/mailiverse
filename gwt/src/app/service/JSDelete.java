/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import mail.auth.MailServerAuthenticatorNoThread;
import mail.client.EventPropagator;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.callback.CallbackWithVariables;
import core.constants.ConstantsClient;
import core.crypt.KeyPairFromPassword;
import core.srp.client.SRPClientListener;
import core.util.LogNull;

@Export
public class JSDelete implements Exportable, SRPClientListener
{
	static LogNull log = new LogNull(JSDelete.class);
	
	Main main;
	
	JSDelete (Main main)
	{
		this.main = main;
	}
	
	static class DeleteInfo 
	{
		String name, password;
		KeyPairFromPassword keyPair;
		JSResult<Boolean> callback;
		
		public DeleteInfo(String name, String password, JSResult<Boolean> callback)
		{
			this.name = name;
			this.password = password;
			this.callback = callback;
		}
	}
	
    public void doDelete (
    	String name, String password,
    	Object callback
    )
    {
    	log.debug("delete", name, password);
    	
    	CallbackChain chain = new CallbackChain();
    	
    	DeleteInfo info = new DeleteInfo(name, password, new JSResult<Boolean>(callback));
    	
    	chain.addCallback(new CallbackDefault(info) {
			public void onSuccess(Object... arguments) throws Exception {
		    	log.debug("delete_step_genKeyPair");
		    	
		    	DeleteInfo info = (DeleteInfo)V(0);
				JSInvoker.invoke(info.callback.getCallback(), "progress", new Object[] { "Creating verification key pair." });
				info.keyPair = new KeyPairFromPassword(info.password);
				info.keyPair.generate_().addCallback(callback).invoke();
			}
		});

    	chain.addCallback(new CallbackDefault(info) {
			public void onSuccess(Object... arguments) throws Exception {
		    	log.debug("delete_step_doDelete");
		    	
				DeleteInfo info = (DeleteInfo)V(0);
				JSInvoker.invoke(info.callback.getCallback(), "progress", new Object[] { "Deleting account." });
				
				call(
					MailServerAuthenticatorNoThread.delete_(
						info.name, info.keyPair,
						new JSStreamSessionWebSocket(ConstantsClient.MAIL_SERVER_WEBSOCKET, main.delegate),
						JSDelete.this
					)
				);
			}
    	});
    	
    	chain.addCallback(new CallbackWithVariables(info) {
			@Override
			public void invoke(Object... arguments) {
				DeleteInfo info = (DeleteInfo)V(0);
				info.callback.invoke(arguments);
			}
    	});
    	
    	main.eventPropagator.signal(
    		EventPropagator.INVOKE, 
    		chain
    	);
    }
    
    public void onSRPStep (String event)
    {
    	main.eventPropagator.signal("onAuthenticationStep", event);
    }
}
