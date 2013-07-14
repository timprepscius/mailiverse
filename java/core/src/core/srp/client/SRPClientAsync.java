/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.srp.client;

import com.jordanzimmerman.SRPClientSession;
import com.jordanzimmerman.SRPFactory;

import core.util.Base64;
import core.callback.Callback;
import core.callback.CallbackDefault;

public class SRPClientAsync
{
	SRPClientSession key;
	
	public SRPClientAsync (byte[] password)
	{
		key = SRPFactory.getInstance().newClientSession(password);
	}
	
	public Callback setSalt_(byte[] bs)
	{
		return new CallbackDefault(bs)
		{
			public void onSuccess(Object... arguments) throws Exception {
				key.setSalt_s((byte[])V(0));
				callback.invoke();
			}
		};
	}
	
	public Callback setServerPublicKey_(byte[] publicKey)
	{
		return new CallbackDefault(publicKey)
		{
			public void onSuccess(Object... arguments) throws Exception {
				key.setServerPublicKey_B((byte[])V(0));
				callback.invoke();
			}
		};
	}

	public Callback validateServerEvidenceValue_M2_(byte[] evidence)
	{
		return new CallbackDefault(evidence)
		{
			public void onSuccess(Object... arguments) throws Exception {
				key.validateServerEvidenceValue_M2((byte[])V(0));
				callback.invoke();
			}
		};
	}
	
	public byte[] getSessionKey()
	{
		return key.getSessionKey_K();
	}
	
	public byte[] getPublicKey()
	{
		return key.getPublicKey_A_();
	}
	
	public byte[] getEvidenceValue()
	{
		return key.getEvidenceValue_M1_();
	}
}
