/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bc.util.encoders.Base64;

import core.util.LogOut;
import core.util.Streams;

import core.constants.ConstantsServer;
import mail.server.relay.LocalRelay;

/**
 * Servlet implementation class Send
 */
@WebServlet("/Send")
public class Send extends HttpServlet 
{
	LogOut log = new LogOut(Send.class);
	private static final long serialVersionUID = 1L;
	LocalRelay localRelay;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Send() throws Exception 
    {
        super();
        
        log.debug("send constructing");
        try
        {
			localRelay = new LocalRelay();
        }
        catch (Exception e)
        {
        	log.exception(e);
        	throw e;
        }
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
        log.debug("doPut");
		if (request.getContentLength() > ConstantsServer.MAXIMUM_MAIL_SIZE)
			throw new ServletException("Content size too large");
		
		byte[] bytes = Streams.readFullyBytes(request.getInputStream());
		if (bytes.length != request.getContentLength())
			throw new ServletException("Content size mismatch");

		doCors(response);
		
		try
		{
	        log.debug("localRelay");
			localRelay.onMail(Base64.decode(bytes));
			response.getOutputStream().println("Ok");
		}
		catch (Throwable e)
		{
			log.debug(e.toString());
			e.printStackTrace(System.out);
			throw new ServletException("Local mail relay failed: " + e.getMessage());
		}
	}

	@Override
	protected void doOptions (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doCors(response);
		super.doOptions(request, response);
	}
}
