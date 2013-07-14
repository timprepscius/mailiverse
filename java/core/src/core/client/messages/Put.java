/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.client.messages;

public class Put extends Message
{
	byte[] block;
	
	public Put(byte[] block)
	{
		this.block = block;
	}
	
	public byte[] getBlock()
	{
		return block;
	}
}
