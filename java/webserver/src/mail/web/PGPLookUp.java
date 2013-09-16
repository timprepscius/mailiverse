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

import core.callback.CallbackSync;
import core.constants.ConstantsServer;
import core.util.HttpDelegate;
import core.util.HttpDelegateJava;
import core.util.Strings;

/**
 * Servlet implementation class Random
 */
@WebServlet("/PGPLookUp")
public class PGPLookUp extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PGPLookUp() 
    {
        super();
    }

    void doCors(HttpServletResponse response)
	{
        response.setHeader("Access-Control-Allow-Origin", ConstantsWeb.WEB_SERVER_URL);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
	}
    
    protected String getPublicKeyFromDb (String email) throws Exception
    {
    	return null;
    }
    
    protected String getPublicKeyId (String email) throws Exception
    {
    	// http://pgp.mit.edu:11371/pks/lookup?op=vindex&search=0x41FB71A4C0FF9D59&options=mr
    	// http://pgp.mit.edu:11371/pks/lookup?op=get&search=0xC0FF9D59&options=mr
    	// http://pgp.mit.edu:11371/pks/lookup?search=edwardsnowden%40nsawhistleblower.com&op=vindex
    	
    	HttpDelegate httpDelegate = new HttpDelegateJava();
    	CallbackSync result = new CallbackSync(
	    	httpDelegate.execute_(
	    		HttpDelegate.GET, 
	    		"http://pgp.mit.edu:11371/pks/lookup?op=vindex&options=mr&exact=on&search=" + email, 
	    		null, false, false)
	    );
    	
    	result.invoke();
    	String text = result.export(0);
    	
    	/*
    	info:1:2
    	pub:69B37AA9:1:4096:1339546547::
    	uid:Nathan Freitas <nathanfreitas@gmail.com>:1339547281::
    	uid:Nathan of Guardian <nathan@guardianproject.info>:1339547297::
    	uid:Nathan Freitas <nathan@freitas.net> <nathan@freitas.net>:1339546547::
    	pub:B374CBD2:17:3072:1276097766::r
    	uid:Nathan of Guardian <nathan@guardianproject.info>:1307704592::
    	uat::::
    	*/

    	String[] lines = Strings.splitLines(text);
    	
    	String info = null;
    	String pub = null;
    	String uid = null;
    	boolean found = false;
    	for (String line : lines)
    	{
    		if (line.startsWith("info:"))
    			info = line;
    		else
    		if (line.startsWith("pub:"))
    			pub = line;
    		else
    		if (line.startsWith("uid:"))
    			uid = line;
    		
    		if (uid!=null && uid.toLowerCase().contains(email) &&
    			pub!=null && !pub.endsWith(":r"))
    		{
    			found = true;
    			break;
    		}
    	}
    	
    	if (!found)
    		throw new Exception ("Could not find valid ID");
    	
		String[] pubParts = pub.split(":");
		return pubParts[1];
    	
    }
    
    protected String getPublicKey (String publicKeyId) throws Exception
    {
    	// http://pgp.mit.edu:11371/pks/lookup?op=get&search=0x69B37AA9&options=mr
    	
    	HttpDelegate httpDelegate = new HttpDelegateJava();
    	CallbackSync result = new CallbackSync(
	    	httpDelegate.execute_(
	    		HttpDelegate.GET, 
	    		"http://pgp.mit.edu:11371/pks/lookup?op=get&search=0x" + publicKeyId, 
	    		null, false, false)
	    );
    	
    	result.invoke();
    	String text = result.export(0);
    	
    	String beginBlock = "-----BEGIN PGP PUBLIC KEY BLOCK-----";
    	String endBlock = "-----END PGP PUBLIC KEY BLOCK-----";
    	
    	int blockBegin = text.indexOf(beginBlock);
    	int blockEnd = text.indexOf(endBlock);
    	
    	if (blockBegin == -1 || blockEnd ==-1)
    		throw new Exception ("Could not find block header or footer");
    	
    	String block = text.substring(blockBegin, blockEnd+endBlock.length()) + "\n";
    	
    	return block;

    	/*
		<html><head><title>Public Key Server -- Get ``0x69b37aa9 ''</title></head>
		<body><h1>Public Key Server -- Get ``0x69b37aa9 ''</h1>
		<pre>
		-----BEGIN PGP PUBLIC KEY BLOCK-----
		Version: SKS 1.1.0
		
		mQINBE/X27MBEACulyj4CfeOClOxZ2/tS6qnwjN8xQCVb7kWAS3yiMaXZ3oTcsay2njap1p4
		.
		.
		.
		pk13qRICEQk69M7L7zjpsVRwzFB2whFBZt6HwbYQeKbinv6aABMzllxTS0xxhw1SrY4dIuss
		MQgEEy/+
		=nMps
		-----END PGP PUBLIC KEY BLOCK-----
		</pre>
		</body></html>    	
		*/
    }

    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doCors(response);
		
		try
		{
	    	String email = request.getParameter("email");
	    	
	    	if (email.endsWith(ConstantsServer.AT_HOST))
	    	{
	    		String publicKey = getPublicKeyFromDb (email);
		    	response.getOutputStream().write(publicKey.getBytes());
	    	}
	    	else
	    	{
	    		String publicKeyId = getPublicKeyId(email);
	    		String publicKey = getPublicKey(publicKeyId);
		    	response.getOutputStream().write(publicKey.getBytes());
	    	}
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	@Override
	protected void doOptions (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doCors(response);
		super.doOptions(request, response);
	}
}
