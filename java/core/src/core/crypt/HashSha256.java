/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import org.bc.crypto.digests.SHA256Digest;

public class HashSha256
{
	public HashSha256 ()
	{
		
	}
	
	public byte[] hash (byte[] bytes)
	{
		SHA256Digest digest = new SHA256Digest();
		
		byte[] out = new byte[digest.getDigestSize()];
		digest.update(bytes, 0, bytes.length);
		digest.doFinal(out, 0);
		return out;
	}
}
