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

import core.constants.ConstantsServer;
import core.server.mailextra.MailExtraDb;
import core.util.Streams;

/**
 * Servlet implementation class DaysLeft
 */
@WebServlet("/DaysLeft")
public class DaysLeft extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	MailExtraDb payment;
    /**
     * @throws IOException 
     * @throws SQLException 
     * @see HttpServlet#HttpServlet()
     */
    public DaysLeft() throws Exception 
    {
        super();
        
        Class.forName("com.mysql.jdbc.Driver");
        payment = new MailExtraDb();
        payment.ensureTables();
    }

    void doCors(HttpServletResponse response)
	{
        response.setHeader("Access-Control-Allow-Origin", ConstantsWeb.WEB_SERVER_URL);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Content-Size");
	}

    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doCors(response);

		try
		{
			byte[] bytes = Streams.readFullyBytes(request.getInputStream());
			String email = new String(bytes);
			response.getOutputStream().write(("" + payment.getDaysLeft(email)).getBytes("UTF-8"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

	@Override
	protected void doOptions (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doCors(response);
		super.doOptions(request, response);
	}

}
