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

import core.constants.ConstantsServer;
import core.server.log.Tail;


/**
 * Servlet implementation class LogTail
 */
@WebServlet("/LogTail")
public class LogTail extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	Tail tail = new Tail("/home/monitor/log/james-server.log");
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogTail() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    void doCors(HttpServletResponse response)
	{
        response.setHeader("Access-Control-Allow-Origin", ConstantsWeb.WEB_SERVER_URL);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doCors(response);
		try
		{
			response.getWriter().write(tail.getTail());
		}
		catch (IOException e)
		{
			response.getWriter().write("Caught an IO Exception, log file may being rotated.");
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
