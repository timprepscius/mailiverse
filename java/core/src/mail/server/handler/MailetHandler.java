/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.handler;

import java.io.InputStream;

import core.util.Environment;


public interface MailetHandler
{
	public void handleIn (String toAddress, Environment e, InputStream m) throws Exception;
	public void handleOut (String toAddress, Environment e, InputStream m) throws Exception;
}
