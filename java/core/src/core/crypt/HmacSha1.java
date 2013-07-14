/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import org.bc.crypto.digests.SHA1Digest;
import org.bc.crypto.macs.HMac;
import org.bc.crypto.params.KeyParameter;

public class HmacSha1
{
	HMac mac;
	byte[] key;
	
	public HmacSha1(byte[] key)
	{
		mac = new HMac(new SHA1Digest());
		mac.init(new KeyParameter(key));
	}

	public byte[] mac(byte[] bytes)
	{
		mac.update(bytes, 0, bytes.length);
		byte[] out = new byte[mac.getMacSize()];
		mac.doFinal(out, 0);
		
		return out;
	}

}
