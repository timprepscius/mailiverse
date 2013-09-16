/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;


import app.service.JSClient;
import app.service.JSEventPropagator;
import app.service.JSHttpDelegate;
import app.service.JSRefill;
import app.service.JSResult;
import app.service.JSSignUp;
import app.service.JSStreamSessionWebSocket;

import mail.client.Client;
import mail.client.Events;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.constants.ConstantsClient;
import core.crypt.KeyPairFromPassword;
import core.srp.client.SRPClientListener;
import core.util.Environment;
import core.util.LogNull;
import core.util.LogOut;
import key.auth.KeyServerAuthenticatorNoThread;

@Export()
@SuppressWarnings("serial")
public class Main extends JSApplet implements Exportable, SRPClientListener
{
	static LogNull log = new LogNull (Main.class);

	static String VERSION_STRING = "M";
	
	public static Object delegate;
    JSEventPropagator eventPropagator;

    public Main ()
	{
    	eventPropagator = new JSEventPropagator();
    	eventPropagator.signal ("Loaded");
    	
    	eventPropagator.add(
    		"Loaded", null, 
	    	new CallbackDefault () {
				@Override
				public void onSuccess(Object... arguments) throws Exception
				{
					Object window = getWindow(this);
					JSInvoker.invoke(window, "onMailiverseServiceLoaded", null);
				}}
		);
	}
	
    /* (non-Javadoc)
	 * @see app.service.MainInterface#setDelegate(java.lang.Object)
	 */
    public void setDelegate (Object delegate)
    {
    	log.debug("setDelegate");
    	this.delegate = delegate;
    	eventPropagator.setDelegate(delegate);
    }
    
	/* (non-Javadoc)
	 * @see app.service.MainInterface#garbageCollect()
	 */
	public void garbageCollect ()
	{
		System.gc();
	}
	
    /* (non-Javadoc)
	 * @see app.service.MainInterface#dispatchEvents()
	 */
    public void dispatchEvents ()
    {
    	try
    	{
	    	eventPropagator.dispatchEvents();
    	}
    	catch (Exception e)
    	{
    		log.exception(e);
    	}
    }
    
    /* (non-Javadoc)
	 * @see app.service.MainInterface#getSignUp()
	 */
    public JSSignUp getSignUp ()
    {
    	return new JSSignUp(this);
    }
    
    /* (non-Javadoc)
	 * @see app.service.MainInterface#getRefill()
	 */
    public JSRefill getRefill ()
    {
    	return new JSRefill(this);
    }
    
    public JSDelete getDelete ()
    {
    	return new JSDelete(this);
    }
    
    //----------------------------------------------------------------------------------------
    
	/* (non-Javadoc)
	 * @see app.service.MainInterface#authenticate(java.lang.String, java.lang.String)
	 */
	public void authenticate (String user, String password)
	{
		authenticate_(user, password).invoke();
	}
	
	public Callback authenticate_ (String user, String password)
	{
		KeyPairFromPassword keyPair = new KeyPairFromPassword(password);
		CallbackChain chain = new CallbackChain();
		return chain
			.addCallback(keyPair.generate_())
			.addCallback(io_(user))
			.addCallback(start_(user))
			.addCallback(propagate_());
	}
	
	public Callback io_ (String user)
	{
		return new CallbackDefault(user) {
			public void onSuccess(Object... arguments) throws Exception {
				String user = V(0);
				KeyPairFromPassword keyPair = (KeyPairFromPassword)arguments[0];
			
				KeyServerAuthenticatorNoThread.get_(
					user, keyPair,
					new JSStreamSessionWebSocket(ConstantsClient.KEY_SERVER_WEBSOCKET, delegate),
					Main.this
				).setReturn(callback).invoke();
			}
		};
	}
	
	public void onSRPStep (String event)
	{
		eventPropagator.signal("onAuthenticationStep", event);
	}
	
	public Callback start_ (String user)
	{
		return new CallbackDefault(user) {
			
			@Override
			public void onSuccess(Object... arguments) throws Exception {
				String user = V(0);
				Environment env = (Environment)arguments[0];
				
				JSClient client = new JSClient(
					Client.start(
						env, user, 
						new JSHttpDelegate(delegate),
						eventPropagator
					)
				);
				
				callback.invoke(client);
			}
		};
	}
	
	public Callback propagate_()
	{
		return new Callback () {
			
			public void invoke(Object...arguments)
			{
				JSResult<Client> result = new JSResult<Client>();
				result.invoke(arguments[0]);
				eventPropagator.signal(Events.Login, result);
			}
		};
	}
}
