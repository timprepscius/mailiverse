/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package key.auth;

import core.client.ClientUserSession;
import core.client.messages.Get;
import core.client.messages.Put;
import core.crypt.KeyPairFromPasswordCryptor;
import core.crypt.Cryptor;
import core.crypt.KeyPairFromPassword;
import core.io.IoChain;
import core.io.IoChainBase64;
import core.io.IoChainNewLinePackets;
import core.srp.client.SRPClientListener;
import core.srp.client.SRPClientUserSession;
import core.util.Environment;
import core.util.JSONSerializer;
import core.util.Zip;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.callbacks.*;

public class KeyServerAuthenticatorNoThread 
{
	public static Callback getFinished_ (Cryptor cryptor)
	{
		return new IoStop()
			.addCallback(cryptor.decrypt_())
			.addCallback(Zip.inflate_())
			.addCallback(new JSONDeserialize());
	}

	public static Callback putFinished_ ()
	{
		return new IoStop();
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
					getFinished_(new KeyPairFromPasswordCryptor(keyPair))
						.setReturn(callback),
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
		return 
			new JSONSerialize()
				.addCallback(Zip.deflate_())
				.addCallback(new KeyPairFromPasswordCryptor (keyPair).encrypt_())
				.addCallback(new CallbackDefault(user, keyPair, sender, listener) {
					public void onSuccess(Object... arguments) throws Exception {
						byte[] block = (byte[])arguments[0];
						String user = (String)V(0);
						KeyPairFromPassword keyPair = (KeyPairFromPassword)V(1);
						IoChain sender = (IoChain)V(2);
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
}
