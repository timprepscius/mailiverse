/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import org.bc.crypto.digests.MD5Digest;

public class HashMd5
{
	public HashMd5 ()
	{
		
	}
	
	public byte[] hash (byte[] bytes)
	{
		MD5Digest digest = new MD5Digest();

		byte[] out = new byte[digest.getDigestSize()];
		digest.update(bytes, 0, bytes.length);
		digest.doFinal(out, 0);
		return out;
	}
}
