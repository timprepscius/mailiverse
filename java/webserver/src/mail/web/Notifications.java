/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mail.server.push.PushDb;

import org.json.JSONException;
import org.json.JSONObject;

import core.constants.ConstantsPushNotifications;
import core.crypt.Cryptor;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAJCE;
import core.exceptions.CryptoException;
import core.server.mailextra.MailExtraDb;
import core.util.ExternalResource;
import core.util.JSON_;
import core.util.LogOut;
import core.util.Streams;
import core.util.Strings;

/**
 * Servlet implementation class Notifications
 */
@WebServlet("/Notifications")
public class Notifications extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	LogOut log = new LogOut(Notifications.class);
       
	PushDb pushDb;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Notifications() throws Exception
    {
        super();

        Class.forName("com.mysql.jdbc.Driver");
        pushDb = new PushDb();
        pushDb.ensureDb();
    }
    
    void doCors(HttpServletResponse response)
	{
        log.debug("send doCors");
        
		response.setHeader("Access-Control-Allow-Origin", ConstantsWeb.WEB_SERVER_URL);
		response.setHeader("Access-Control-Allow-Methods", "PUT, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Content-Size");
	}    

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		try
		{
			doCors(response);
			
			byte[] bytes = Streams.readFullyBytes(request.getInputStream());
			if (bytes.length != request.getContentLength())
				throw new ServletException("Content size mismatch");
	
			pushDb.handleEncryptedBlock(bytes);
			
			response.getOutputStream().println("Ok");
		}
		catch (Exception e)
		{
			log.exception(e);
			throw new ServletException(e);
		}
	}

}
