/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.exceptions.CryptoException;

public abstract class Cryptor 
{
	static final boolean TEST_ENCRYPTION = false;

	abstract public byte[] encrypt (byte[] bytes) throws CryptoException;
	abstract public byte[] decrypt (byte[] bytes) throws CryptoException;
	
	public Callback encrypt_()
	{
		return new CallbackDefault() {
			
			@Override
			public void onSuccess(Object... arguments) throws Exception {
				next(Cryptor.this.encrypt((byte[])arguments[0]));
			}
		};
	}

	public Callback decrypt_()
	{
		return new CallbackDefault() {
			
			@Override
			public void onSuccess(Object... arguments) throws Exception {
				next(Cryptor.this.decrypt((byte[])arguments[0]));
			}
		};
	}
}
