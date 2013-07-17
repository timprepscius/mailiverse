/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.handler;

import java.io.IOException;

import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.WebAuthSession;

public class DropboxGetAccessTokenPair
{

	/**
	 * @param args
	 * @throws DropboxException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws DropboxException, IOException
	{
        // Make the user log in and authorize us.
        WebAuthSession was = new WebAuthSession(new AppKeyPair(args[0], args[1]), Session.AccessType.APP_FOLDER);

        // Make the user log in and authorize us.
        WebAuthSession.WebAuthInfo info = was.getAuthInfo();
        System.out.println("1. Go to: " + info.url);
        System.out.println("2. Allow access to this app.");
        System.out.println("3. Press ENTER.");

        while (System.in.read() != '\n') {}

        // This will fail if the user didn't visit the above URL and hit 'Allow'.
        String uid = was.retrieveWebAccessToken(info.requestTokenPair);
        AccessTokenPair accessToken = was.getAccessTokenPair();
        
        System.out.println("UID: " + uid);
        System.out.println("AccessToken: " + accessToken.toString());
	}

}
