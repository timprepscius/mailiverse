/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.ws;

import javax.servlet.annotation.WebServlet;
import core.constants.ConstantsServer;

/**
 * Servlet implementation class KeyServer
 */
@SuppressWarnings("serial")
@WebServlet("/KeyServer")
public class KeyServer extends MessageProxy
{

	public KeyServer() throws ClassNotFoundException
	{
		super(ConstantsServer.KEY_AUTH_HOST, ConstantsServer.KEY_AUTH_PORT);
	}
}
