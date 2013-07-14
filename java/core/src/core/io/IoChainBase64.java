/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.io;

import java.io.IOException;

import core.util.Base64;

import core.util.LogNull;
import core.util.LogOut;

public class IoChainBase64 extends IoChain
{
	static LogNull log = new LogNull(IoChainBase64.class);
	
	public IoChainBase64(IoChain sender)
	{
		super(sender);
	}

	@Override
	public void send(byte[] packet) throws Exception 
	{
		byte[] encoded = Base64.encodeBytes(packet);
		// log.debug("send base64 check:", Strings.toString(Base64.decode(encoded)));

		super.send(encoded);
	}
	
	@Override
	protected void onReceive (byte[] packet) throws Exception
	{
		byte[] decoded = Base64.decodeBytes(packet);
		// log.debug("onReceive:",new String(packet));
		
		super.onReceive(decoded);
	}
}
