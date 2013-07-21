/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package com.jordanzimmerman;

import core.crypt.HashSha256;

public class Hash256i 
{
	HashSha256 hash = new HashSha256();

	public byte[] hash(byte[] bytes)
	{
		return hash.hash(bytes);
	}
}
