/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.web;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpConnection;
import org.json.JSONObject;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import core.constants.ConstantsServer;
import core.crypt.CryptorRSA;
import core.crypt.CryptorRSAJCE;
import core.util.ExternalResource;
import core.util.Http;
import core.util.HttpDelegate;
import core.util.HttpDelegateJava;
import core.util.LogOut;
import core.util.Pair;
import core.util.Streams;

/**
 * Servlet implementation class BitPayCreateInvoice
 */
@WebServlet("/BitPayCreateInvoice")
public class BitPayCreateInvoice extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	static LogOut log = new LogOut(BitPayCreateInvoice.class);
       
	static protected final String apiKey = "YOUR_API_KEY";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BitPayCreateInvoice() throws Exception
    {
        super();
    }

	void doCors(HttpServletResponse response)
	{
        response.setHeader("Access-Control-Allow-Origin", ConstantsWeb.WEB_SERVER_URL);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
	}

	public static Pair<byte[], Exception> doBitPay (String email, String price) throws Exception
	{
		log.debug("doGet",email,price);
			
		String url = "https://bitpay.com/api/invoice";
		String json = 
			"{ #price#: __PRICE__, #currency#: #USD# }"
				.replaceAll("#", "\"")
				.replaceAll("__PRICE__", price);
		
		log.debug(url,json);

		URLConnection urlConnection = new URL(url).openConnection();

		String userPassword = apiKey + ":" + "";
		String encodedUserPassword = Base64.encode(userPassword.getBytes("UTF-8"));
		urlConnection.setRequestProperty ("Authorization", "Basic " + encodedUserPassword);
		urlConnection.setRequestProperty("Accept", "*/*");
		urlConnection.setRequestProperty("User-Agent", "curl/7.25.0 (x86_64-apple-darwin11.4.0) libcurl/7.25.0 OpenSSL/1.0.1c zlib/1.2.7 libidn/1.25");

		HttpURLConnection httpCon = (HttpURLConnection) urlConnection;
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("POST");

		httpCon.setRequestProperty("Content-Type", "application/json");
		BufferedOutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
		os.write(json.getBytes("UTF-8"));
		os.close();

		Pair<byte[], Exception> result = Http.readFully((HttpURLConnection)urlConnection);
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
			String price = request.getParameter("price");
			
			Pair<byte[], Exception> result = doBitPay(email, price);
			if (result.second != null)
			{
				log.debug(result.first, result.second);
				throw result.second;
			}
			
			response.getOutputStream().write(result.first);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
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
