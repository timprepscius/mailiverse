package mail.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import core.constants.ConstantsMvStore;
import core.server.captcha.Captcha;
import core.util.LogOut;
import core.util.Pair;
import core.util.Strings;

import store.server.StoreServer;

/**
 * Servlet implementation class StoreEnable
 */
@WebServlet("/StoreEnable")
public class StoreEnable extends HttpServlet 
{
	static LogOut log = new LogOut(StoreEnable.class);
	private static final long serialVersionUID = 1L;
       
	StoreServer store;
	Captcha captcha;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public StoreEnable() throws Exception
    {
        super();

        try
        {
	        store = new StoreServer();
	        captcha = new Captcha();
	        captcha.ensureTables();
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
	        
	        log.debug("email",email, "captchaToken",captchaToken);
	        
	        captcha.useToken(captchaToken, Captcha.CreateBucket);
	        Pair<String,String> result = store.newUserWithKeyPair(email);
	        
	        ArrayList<String> keyValues = new ArrayList<String>();
	        keyValues.add(ConstantsMvStore.AccessKeyId + "!" + result.first);
	        keyValues.add(ConstantsMvStore.SecretKey + "!" + result.second);
	        
	        response.getWriter().write(Strings.concat(keyValues, "&"));
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
