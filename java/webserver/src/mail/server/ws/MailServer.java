/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.ws;

import javax.servlet.annotation.WebServlet;
import core.constants.ConstantsServer;

/**
 * Servlet implementation class MailServer
 */
@WebServlet("/MailServer")
public class MailServer extends MessageProxy {

	public MailServer() throws ClassNotFoundException
	{
		super(ConstantsServer.MAIL_AUTH_HOST, ConstantsServer.MAIL_AUTH_PORT);
	}
}
