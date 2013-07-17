/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mail.server.storage.AWSStorageCreation;

import core.constants.ConstantsServer;
import core.server.captcha.Captcha;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Strings;

/**
 * Servlet implementation class AcquireBucket
 */
@WebServlet("/CreateBucket")
public class CreateBucket extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    static LogOut log = new LogOut(CreateBucket.class);   
	Captcha captcha;
    AWSStorageCreation storageCreation;
	
    /**
     * @throws Exception 
     * @throws IOException 
     * @throws SQLException 
     * @see HttpServlet#HttpServlet()
     */
    public CreateBucket() throws Throwable 
    {
        super();
        
        log.debug("Constructing");
        
        Class.forName("com.mysql.jdbc.Driver");
        captcha = new Captcha();
        captcha.ensureTables();
        
        try
        {
        	storageCreation = new AWSStorageCreation();
        }
        catch (Throwable t)
        {
        	t.printStackTrace();
        	throw t;
        }
        log.debug("Finished constructing");
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
	        String email = request.getParameter("email");
	        String captchaToken = request.getParameter("captcha");
	        String region = request.getParameter("region");
	        
	        log.debug("email",email, "captchaToken",captchaToken);
	        
	        captcha.useToken(captchaToken, Captcha.CreateBucket);
	        Map<String,String> result = storageCreation.create(email, region);
	        
	        ArrayList<String> keyValues = new ArrayList<String>();
	        for (Map.Entry<String, String> keyValue : result.entrySet())
	        	keyValues.add(keyValue.getKey() + "!" + keyValue.getValue());
	        
	        response.getWriter().write(Strings.concat(keyValues, "&"));
		}
		catch (Throwable e)
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
