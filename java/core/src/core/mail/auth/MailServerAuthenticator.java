/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.auth;

import core.client.ClientCreateSession;
import core.client.ClientTestCreateSession;
import core.client.ClientUserSession;
import core.client.messages.Delete;
import core.client.messages.Get;
import core.client.messages.Put;
import core.client.messages.Response;
import core.constants.ConstantsClient;
import core.constants.ConstantsServer;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAFactory;
import core.crypt.CryptorRSAJCE;
import core.exceptions.PublicMessageException;
import core.io.IoChain;
import core.io.IoChainBase64;
import core.io.IoChainNewLinePackets;
import core.io.IoChainSocket;
import core.io.IoChainThread;
import core.crypt.KeyPairFromPassword;
import core.srp.SRPPackets;
import core.srp.client.SRPClientUserSession;
import core.callback.CallbackWithVariables;
import core.callback.Callback;
import core.callbacks.IoStop;
import core.util.Environment;
import core.util.ExternalResource;
import core.util.InternalResource;
import core.util.JSONSerializer;
import core.util.SimpleSerializer;

public class MailServerAuthenticator 
{
	public static final int DEFAULT_PORT = ConstantsClient.MAIL_AUTH_PORT;
	public static final String DEFAULT_HOST = ConstantsClient.MAIL_AUTH_HOST;

	String host;
	int port;

	KeyPairFromPassword keyPair = null;
	Thread running = null;
	
	public MailServerAuthenticator (String host, int port)
	{
		this.host = host;
		this.port = port;
	}
	
	public MailServerAuthenticator ()
	{
		this(DEFAULT_HOST, DEFAULT_PORT);
	}
	
	public Thread testCreate (String user, Callback callback) throws Exception
	{
		CryptorRSAAES cryptor = new CryptorRSAAES(CryptorRSAFactory.fromResources(null, ExternalResource.getResourceAsStream(MailServerAuthenticator.class, "truststore.jks")));
		
		ClientTestCreateSession session = new ClientTestCreateSession(
			cryptor, user, 
			new CallbackWithVariables (callback) {
				public void invoke (Object... args)
				{
					testCreateFinished((Callback)V(0), args);
				}
			},
			new IoChainBase64(
				new IoChainNewLinePackets(
					new IoChainSocket(host, port)
				)
			)
		);
		
		running = new IoChainThread(session);
		running.start();
		
		return running;
	}
	
	public void testCreateFinished (Callback callback, Object[] args)
	{
		try
		{
			running = null;

			IoChain io = (IoChain)args[0];
			io.stop();
			
			Object arg = args[1];
			if (arg instanceof Exception)
				throw (Exception)arg;
		
			SRPPackets.PacketInit_ServerResponse response = SimpleSerializer.deserialize((byte[])arg);
			if (!response.succeeded)
				throw new PublicMessageException(response.reason);

			callback.invoke(true);
		}
		catch (Exception e)
		{
			callback.invoke(e);
		}
	}
	
	
	public Thread create (String user, String password, String token, Callback callback) throws Exception
	{
		keyPair = new KeyPairFromPassword (password);
		keyPair.generate();
		
		CryptorRSAAES cryptor = new CryptorRSAAES(CryptorRSAFactory.fromResources(null, ExternalResource.getResourceAsStream(MailServerAuthenticator.class, "truststore.jks")));
		
		ClientCreateSession session = new ClientCreateSession(
			cryptor, user, keyPair, SimpleSerializer.serialize(token), 
			new CallbackWithVariables (callback) {
				public void invoke (Object... args)
				{
					createFinished((Callback)V(0), args);
				}
			},
			new IoChainBase64(
				new IoChainNewLinePackets(
					new IoChainSocket(host, port)
				)
			)
		);
			
		running = new IoChainThread(session);
		running.start();
		
		return running;
	}
	
	public void createFinished (Callback callback, Object[] args)
	{
		try
		{
			running = null;

			IoChain io = (IoChain)args[0];
			io.stop();
			
			Object arg = args[1];
			if (arg instanceof Exception)
				throw (Exception)arg;
		
			SRPPackets.PacketInit_ServerResponse response = SimpleSerializer.deserialize((byte[])arg);
			if (!response.succeeded)
				throw new PublicMessageException(response.reason);
			
			callback.invoke(true);
		}
		catch (Exception e)
		{
			callback.invoke(e);
		}
	}
	
	public Thread delete (String user, String password, Callback callback) throws Exception
	{
		keyPair = new KeyPairFromPassword (password);
		keyPair.generate();
		
		ClientUserSession session = new ClientUserSession(
			new Delete(),  
			new IoStop().setReturn(callback),
			new SRPClientUserSession(
				user, keyPair,
				new IoChainBase64(
					new IoChainNewLinePackets(
						new IoChainSocket(host, port)
					)
				),
				null
			)
		);

		running = new IoChainThread(session);
		running.start();
		
		return running;
	}
	

	public Thread get (String user, String password, Callback callback) throws Exception
	{
		keyPair = new KeyPairFromPassword (password);
		keyPair.generate();
		
		ClientUserSession session = new ClientUserSession(
			new Get(),  
			new CallbackWithVariables (callback) {
				public void invoke (Object... args)
				{
					getOrPutFinished((Callback)V(0), args);
				}
			},
			new SRPClientUserSession(
				user, keyPair,
				new IoChainBase64(
					new IoChainNewLinePackets(
						new IoChainSocket(host, port)
					)
				),
				null
			)
		);

		running = new IoChainThread(session);
		running.start();
		
		return running;
	}
	
	public void getOrPutFinished (Callback callback, Object[] args)
	{
		try
		{
			running = null;
			
			IoChain io = (IoChain)args[0];
			io.stop();
			
			Object arg = args[1];
			if (arg instanceof Exception)
				throw (Exception)arg;
			
			Response response = (Response)arg;
			Environment e = JSONSerializer.deserialize(response.getBlock());
			callback.invoke(e);
		}
		catch (Exception e)
		{
			callback.invoke(e);
		}
	}
	
	public Thread put (String user, String password, Environment environment, Callback callback) throws Exception
	{
		keyPair = new KeyPairFromPassword (password);
		keyPair.generate();
		
		byte[] block = JSONSerializer.serialize(environment);
		
		ClientUserSession session = new ClientUserSession(
			new Put(block), 
			new CallbackWithVariables (callback) {
				public void invoke (Object... args)
				{
					getOrPutFinished((Callback)V(0), args);
				}
			},
			new SRPClientUserSession(
				user, keyPair,
				new IoChainBase64(
					new IoChainNewLinePackets(
						new IoChainSocket(host, port)
					)
				),
				null
			)
		);
		
		running = new IoChainThread(session);
		running.start();
		
		return running;
	}
}
