package com.jordanzimmerman;

import org.bc.crypto.digests.SHA256Digest;

public class Hash256i {

	byte[] hash(byte[] bytes)
	{
		SHA256Digest hash = new SHA256Digest();
		hash.update(bytes, 0, bytes.length);
		
		byte output[] = new byte[hash.getDigestSize()];
		hash.doFinal(output, 0);
		
		return output;
	}
	
}
