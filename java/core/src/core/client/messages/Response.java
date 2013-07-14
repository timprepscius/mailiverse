/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.client.messages;

public class Response extends Message
{
	byte[] block;
	
	public Response ()
	{
		
	}
	
	public Response (byte[] block)
	{
		this.block = block;
	}

	public byte[] getBlock()
	{
		return block;
	}
}
