/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package key.auth;

import core.client.ClientUserSession;
import core.client.messages.Get;
import core.client.messages.Put;
import core.client.messages.Response;
import core.constants.ConstantsClient;
import core.constants.ConstantsServer;
import core.crypt.Cryptor;
import core.io.IoChain;
import core.io.IoChainBase64;
import core.io.IoChainNewLinePackets;
import core.io.IoChainSocket;
import core.io.IoChainThread;
import core.crypt.KeyPairFromPasswordCryptor;
import core.crypt.KeyPairFromPassword;
import core.srp.client.SRPClientListener;
import core.srp.client.SRPClientUserSession;
import core.callback.Callback;
import core.util.Environment;
import core.util.JSONSerializer;
import core.util.Streams;
import core.util.Zip;
import core.callback.CallbackDefault;
import core.callbacks.IoStop;

public class KeyServerAuthenticator 
{
	public static final int DEFAULT_PORT = ConstantsClient.KEY_AUTH_PORT;
	public static final String DEFAULT_HOST = ConstantsClient.KEY_AUTH_HOST;

	String host;
	int port;
	
	KeyPairFromPassword keyPair = null;
	Cryptor cryptor;
	Thread running = null;
	
	public KeyServerAuthenticator(String host, int port)
	{
		this.host = host;
		this.port = port;
	}

	public KeyServerAuthenticator ()
	{
		this(DEFAULT_HOST, DEFAULT_PORT);
	}

	public Thread get (String user, String password, Callback callback, SRPClientListener listener) throws Exception
	{
		keyPair = new KeyPairFromPassword (password);
		keyPair.generate();
		
		cryptor = new KeyPairFromPasswordCryptor (keyPair);
		
		ClientUserSession session = new ClientUserSession(
			new Get(),
			getFinished_().addCallback(callback),
			new SRPClientUserSession (
				user, keyPair,
				new IoChainBase64(
					new IoChainNewLinePackets(
						new IoChainSocket(host, port)
					)
				),
				listener
			)
		);
		
		running = new IoChainThread(session);
		running.start();
		
		return running;
	}
	
	public Callback getFinished_ ()
	{
		return new CallbackDefault () {
			@Override
			public void onSuccess(Object... args) throws Exception
			{
				running = null;
				
				IoChain io = (IoChain)args[0];
				io.stop();
				
				Object arg = args[1];
				if (arg instanceof Exception)
					throw (Exception)arg;
				
				byte[] block = cryptor.decrypt((byte[])arg);
				block = Zip.inflate(block);
				Environment e = JSONSerializer.deserialize(block);
				next(e);
			}
		};
	}
	
	public Thread put (String user, String password, Environment environment, Callback callback, SRPClientListener listener) throws Exception
	{
		keyPair = new KeyPairFromPassword (password);
		keyPair.generate();
		
		cryptor = new KeyPairFromPasswordCryptor (keyPair);

		byte[] block = cryptor.encrypt(Zip.deflate(JSONSerializer.serialize(environment)));

		ClientUserSession session = new ClientUserSession(
			new Put(block), 
			putFinished_().addCallback(callback),
			new SRPClientUserSession (user, keyPair,
				new IoChainBase64 (
					new IoChainNewLinePackets(
						new IoChainSocket(host, port)
					)
				),
				listener
			)
		);
		
		running = new IoChainThread(session);
		running.start();
		
		return running;
	}
	
	public Callback putFinished_ ()
	{
		return new IoStop();
	}
}

