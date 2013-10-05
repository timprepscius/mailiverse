/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import core.util.Base64;

import core.crypt.CryptorRSA;
import core.crypt.CryptorRSAJCE;
import core.server.mailextra.MailExtraDb;
import core.util.Http;
import core.util.LogOut;
import core.util.Pair;

/**
 * Servlet implementation class StripePayment
 */
@WebServlet("/StripePayment")
public class StripePayment extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static LogOut log = new LogOut (StripePayment.class);
       
	CryptorRSA cryptor;
	MailExtraDb payment;

	/**
     * @throws Exception 
	 * @see HttpServlet#HttpServlet()
     */
    public StripePayment() throws Exception 
    {
        super();
        
		cryptor = new CryptorRSAJCE(getClass().getResourceAsStream("keystore.jks"), null);
		
        Class.forName("com.mysql.jdbc.Driver");
		payment = new MailExtraDb();		
		payment.ensureTables();
    }

    void doCors(HttpServletResponse response)
	{
        response.setHeader("Access-Control-Allow-Origin", ConstantsWeb.WEB_SERVER_URL);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
	}
    
    protected Pair<byte[],Exception> doStripe (String email, String stripeTokenID, String amount) throws Exception
    {
    	String apiKey = "YOUR_API_KEY";
    	
		log.debug("doGet",stripeTokenID,amount);
		
		String url = "https://api.stripe.com/v1/charges";

		URLConnection urlConnection = new URL(url).openConnection();

		String userPassword = apiKey + ":" + "";
		String encodedUserPassword = Base64.encode(userPassword.getBytes("UTF-8"));
		urlConnection.setRequestProperty ("Authorization", "Basic " + encodedUserPassword);
		urlConnection.setRequestProperty("Accept", "*/*");

		HttpURLConnection httpCon = (HttpURLConnection) urlConnection;

		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");
		Http.writeParameters(urlConnection,
				"amount", amount + "00",
				"currency", "usd",
				"card", stripeTokenID,
				"description", "Mailiverse Service for " + amount + " months"
			);

		Pair<byte[], Exception> result = Http.readFully(httpCon);
		log.debug(new String(result.first), result.second);
		
		return result;
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
	        String stripeTokenID = request.getParameter("stripeTransactionID");
	        String amount = request.getParameter("amount");

	        Pair<byte[], Exception> result = doStripe(email, stripeTokenID, amount);
	        if (result.second == null)
	        	payment.addDaysTo(email, Integer.parseInt(amount) * 31);
	        
			// record invoice
			response.getOutputStream().write(result.first);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw new ServletException(Http.INTERNAL_ERROR);
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
