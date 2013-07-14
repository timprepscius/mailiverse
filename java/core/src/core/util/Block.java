/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.Serializable;

public class Block implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public byte[] bytes;
	
	public Block (byte[] bytes)
	{
		this.bytes = bytes;
	}
}
