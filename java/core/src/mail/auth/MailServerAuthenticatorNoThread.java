/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.auth;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.callbacks.IoStop;
import core.callbacks.JSONDeserialize;
import core.callbacks.JSONSerialize;
import core.client.ClientCreateSession;
import core.client.ClientTestCreateSession;
import core.client.ClientUserSession;
import core.client.messages.Delete;
import core.client.messages.Get;
import core.client.messages.Put;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAFactory;
import core.crypt.KeyPairFromPassword;
import core.exceptions.PublicMessageException;
import core.io.IoChain;
import core.io.IoChainBase64;
import core.io.IoChainNewLinePackets;
import core.srp.SRPPackets;
import core.srp.client.SRPClientListener;
import core.srp.client.SRPClientUserSession;
import core.util.Environment;
import core.util.InternalResource;
import core.util.SimpleSerializer;

public class MailServerAuthenticatorNoThread 
{
	public static Callback testCreate_ (String user, IoChain sender)
	{
		return new CallbackDefault(user, sender) {
			public void onSuccess(Object... arguments) throws Exception {
				String user = V(0);
				IoChain sender = V(1);
				CryptorRSAAES cryptor = new CryptorRSAAES(CryptorRSAFactory.fromResources(null, InternalResource.getResourceAsStream(MailServerAuthenticatorNoThread.class, "truststore.jks")));

				new ClientTestCreateSession(
					cryptor, user, 
					createFinished_().setReturn(callback),
					new IoChainBase64(
						new IoChainNewLinePackets(
							sender
						)
					)
				).run();
			}
		};
	}
	
	public static Callback create_ (String user, KeyPairFromPassword keyPair, String token, IoChain sender) 
	{
		return new CallbackDefault(user, keyPair, token, sender) {
			public void onSuccess(Object... arguments) throws Exception {
				String user = V(0);
				KeyPairFromPassword keyPair = V(1);
				String token = V(2);
				IoChain sender = V(3);
				CryptorRSAAES cryptor = new CryptorRSAAES(CryptorRSAFactory.fromResources(null, InternalResource.getResourceAsStream(MailServerAuthenticatorNoThread.class, "truststore.jks")));

				new ClientCreateSession(
					cryptor, user, keyPair, SimpleSerializer.serialize(token), 
					createFinished_().setReturn(callback),
					new IoChainBase64(
						new IoChainNewLinePackets(
							sender
						)
					)
				).run();
			}
		};
	}
	
	public static Callback createFinished_ ()
	{
		return new IoStop()
			.addCallback(new JSONDeserialize())
			.addCallback(new CallbackDefault(){

				@Override
				public void onSuccess(Object... arguments) throws Exception {
					SRPPackets.PacketInit_ServerResponse response = (SRPPackets.PacketInit_ServerResponse)arguments[0];
					if (!response.succeeded)
						throw new PublicMessageException(response.reason);
					
					next(true);
				}
			});
	}

	public static Callback get_ (String user, KeyPairFromPassword keyPair, IoChain sender, SRPClientListener listener) throws Exception
	{
		return new CallbackDefault(user, keyPair, sender, listener) {
			public void onSuccess(Object... arguments) throws Exception {
				String user = V(0);
				KeyPairFromPassword keyPair = V(1);
				IoChain sender = V(2);
				SRPClientListener listener = V(3);
				
				new ClientUserSession(
					new Get(), 
					getFinished_().setReturn(callback),
					new SRPClientUserSession (user, keyPair,
						new IoChainBase64(
							new IoChainNewLinePackets(
								sender
							)
						),
						listener
					)
				).run();
			}
		};
	}
	
	public static Callback put_ (String user, KeyPairFromPassword keyPair, Environment environment, IoChain sender, SRPClientListener listener) 
	{
		return new JSONSerialize()
			.addCallback(new CallbackDefault(user, sender, keyPair, listener) {
				public void onSuccess(Object... arguments) throws Exception {
					byte[] block = (byte[])arguments[0];
					String user = (String)V(0);
					IoChain sender = (IoChain)V(1);
					KeyPairFromPassword keyPair = (KeyPairFromPassword)V(2);
					SRPClientListener listener = V(3);
					
					new ClientUserSession(
						new Put(block), 
						putFinished_().setReturn(callback),
						new SRPClientUserSession (user, keyPair,
							new IoChainBase64(
								new IoChainNewLinePackets(
									sender
								)
							),
							listener
						)
					).run();
				}
			});
	}
	
	public static Callback delete_ (String user, KeyPairFromPassword keyPair, IoChain sender, SRPClientListener listener) 
	{
		return 
			new CallbackDefault(user, sender, keyPair, listener) {
				public void onSuccess(Object... arguments) throws Exception {
					String user = (String)V(0);
					IoChain sender = (IoChain)V(1);
					KeyPairFromPassword keyPair = (KeyPairFromPassword)V(2);
					SRPClientListener listener = V(3);
					
					new ClientUserSession(
						new Delete(), 
						deleteFinished_().setReturn(callback),
						new SRPClientUserSession (user, keyPair,
							new IoChainBase64(
								new IoChainNewLinePackets(
									sender
								)
							),
							listener
						)
					).run();
				}
			};
	}
	
	public static Callback deleteFinished_ ()
	{
		return new IoStop();
	}
	
	public static Callback getFinished_ ()
	{
		return new IoStop().addCallback(new JSONDeserialize());
	}
	
	public static Callback putFinished_ ()
	{
		return new IoStop();
	}
	
}
