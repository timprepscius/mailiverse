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
import core.server.captcha.Captcha;
import core.util.ExternalResource;
import core.util.LogNull;
import core.util.LogOut;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

/**
 * Servlet implementation class CaptchaResponse
 */
@WebServlet("/CaptchaResponse")
public class CaptchaResponse extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    static LogOut log = new LogOut(CaptchaResponse.class);   
	Captcha captcha;
	
    /**
     * @throws ClassNotFoundException 
     * @throws IOException 
     * @throws SQLException 
     * @see HttpServlet#HttpServlet()
     */
    public CaptchaResponse() throws Exception 
    {
        super();

        Class.forName("com.mysql.jdbc.Driver");
        captcha = new Captcha();
        captcha.ensureTables();
    }

	void doCors(HttpServletResponse response)
	{
        response.setHeader("Access-Control-Allow-Origin", ConstantsWeb.WEB_SERVER_URL);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		log.debug("doGet");
		doPost(request, response);
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		log.debug("doPost");
		doCors(response);
		response.setContentType("application/json");
        
		try
		{
	        String remoteAddr = request.getRemoteAddr();
	        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
	        reCaptcha.setPrivateKey(ExternalResource.getTrimmedString(ConstantsServer.RECAPTCHA_PRIVATE_KEY));
	
	        String challenge = request.getParameter("recaptcha_challenge_field");
	        String uresponse = request.getParameter("recaptcha_response_field");
	        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
	
	        if (reCaptchaResponse.isValid()) 
	        {
	        	try
	        	{
		        	String token = captcha.captchaSucceeded();
		        	response.getWriter().write(("{'succeeded':true,'token':'" + token + "'}").replaceAll("'", "\""));
	        	}
	        	catch (Exception e)
	        	{
	        		response.getWriter().write("{'succeeded':false,'reason':'System error'}".replaceAll("'", "\""));
	        	}
	        } 
	        else 
	        {
	        	response.getWriter().write("{'succeeded':false,'reason':'User error'}".replaceAll("'", "\""));
	        }
		}
		catch (Exception e)
		{
    		response.getWriter().write("{'succeeded':false,'reason':'System error, captcha private key not set'}".replaceAll("'", "\""));
		}
	}
	
	@Override
	protected void doOptions (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		log.debug("doOptions");
		doCors(response);
        super.doOptions(request, response);
	}
}
