package mail.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import store.server.ConstantsMvServer;
import store.server.StoreServer;
import core.util.HttpDelegate;
import core.util.LogOut;
import core.util.Streams;

/**
 * Servlet implementation class StorePut
 */
@WebServlet("/StorePut")
public class StorePut extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	static LogOut log = new LogOut(StorePut.class);
	StoreServer store;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public StorePut() throws Exception
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
	
			byte[] content = Streams.readFullyBytes(request.getInputStream());
			String resource = request.getParameter(ConstantsMvServer.PARAMETER_RESOURCE);
			String keyId = StoreWebUtils.verifyUser(action, request, resource, content);
			
			String version = store.putKeyValue(keyId, resource, content);
			response.setHeader("Version", version);
    	}
    	catch (Exception e)
    	{
    		log.exception(e);
    		throw new ServletException(e);
    	}
    }
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doRequest(HttpDelegate.PUT, request, response);
	}

	@Override
	protected void doOptions (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doCors(response);
		super.doOptions(request, response);
	}
}
