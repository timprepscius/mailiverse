/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.exceptions.CryptoException;

public class KeyPairFromPassword
{
	String password;
	public PBE[] pbe = { new PBE(), new PBE() };
	
	static final int CRYPTOR = 0, VERIFIER = 1;
	
	public KeyPairFromPassword (String password)
	{
		this.password = password;
	}		

	public Callback generate_()
	{
		CallbackChain chain = new CallbackChain();
		
		return chain
			.addCallback(
				pbe[CRYPTOR].generate_(
					password, 
					PBE.DEFAULT_SALT[CRYPTOR], 
					PBE.DEFAULT_ITERATIONS, 
					PBE.DEFAULT_KEYLENGTH
				)
			)
			.addCallback(
				pbe[VERIFIER].generate_(
					password, 
					PBE.DEFAULT_SALT[VERIFIER], 
					PBE.DEFAULT_ITERATIONS, 
					PBE.DEFAULT_KEYLENGTH
				)
			)
			.addCallback(
				new CallbackDefault() {
					public void onSuccess(Object... arguments) throws Exception {
						next(KeyPairFromPassword.this);
					}
				}
			);
	}
	
	public void generate() throws CryptoException
	{
		for (int i=0; i<2; ++i)
			pbe[i].generate(
				password, 
				PBE.DEFAULT_SALT[i], 
				PBE.DEFAULT_ITERATIONS, 
				PBE.DEFAULT_KEYLENGTH
			);
	}
	
	public byte[] getVerifier ()
	{
		return pbe[VERIFIER].key;
	}

	public PBE getCryptor ()
	{
		return pbe[CRYPTOR];
	}
}
