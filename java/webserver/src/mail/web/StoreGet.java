package mail.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import store.server.ConstantsMvServer;
import store.server.StoreServer;

import core.util.Base64;
import core.util.HttpDelegate;
import core.util.LogOut;
import core.util.Pair;
import core.util.Streams;

/**
 * Servlet implementation class StoreGet
 */
@WebServlet("/StoreGet")
public class StoreGet extends HttpServlet 
{
	static LogOut log = new LogOut(StoreGet.class);
	private static final long serialVersionUID = 1L;
       
	StoreServer store;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public StoreGet() throws Exception
    {
        super();
        try
        {
        	store = new StoreServer();
        }
        catch (Exception e)
        {
        	log.exception(e);
        	throw e;
        }
    }

    void doCors(HttpServletResponse response)
	{
        response.setHeader("Access-Control-Allow-Origin", ConstantsWeb.WEB_SERVER_URL);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
	}
    
    protected void doRequest (String action, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
    	try
    	{
			doCors(response);
	
			String resource = request.getParameter(ConstantsMvServer.PARAMETER_RESOURCE);
			String keyId = StoreWebUtils.verifyUser(action, request, resource, null);
			
			Pair<String, byte[]> versionAndData = store.getKeyValue(keyId, resource);
			response.setHeader("Version", versionAndData.first);
			response.getOutputStream().write(versionAndData.second);
    	}
    	catch (Exception e)
    	{
    		log.exception(e);
    		throw new ServletException(e);
    	}
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doRequest(HttpDelegate.GET, request, response);
	}

	@Override
	protected void doOptions (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doCors(response);
		super.doOptions(request, response);
	}
}
