/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.dropbox.sync;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import core.util.HttpDelegate;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Pair;
import core.util.Streams;

public class DropboxSignup
{
	static LogNull log = new LogNull (DropboxSignup.class);
	
	static public AccessTokenPair getDropboxRequestToken (AppKeyPair appKeyPair) throws Exception
	{
		log.debug("getDropboxUserToken");
		URL url = new URL(
			"https://api.dropbox.com/1/oauth/request_token" +
				"?oauth_consumer_key=" + appKeyPair.key +
				"&oauth_signature_method=PLAINTEXT" + 
				"&oauth_signature=" +  appKeyPair.secret + "%26" +
				"&oauth_nonce=\"" + (new Date()).getTime() + "\""
			);
		URLConnection c = url.openConnection();
		String response = Streams.readFullyString(c.getInputStream(), "UTF-8");

		Pair<String,String> token = parseAuthToken(response);
		return new AccessTokenPair(token.first, token.second);	
	}

	static public AccessTokenPair getDropboxAccessToken (AppKeyPair appKeyPair, AccessTokenPair accessToken) throws Exception
	{
		URL url = new URL(
			"https://api.dropbox.com/1/oauth/access_token" +
				"?oauth_consumer_key=" + appKeyPair.key +
				"&oauth_token=" + accessToken.key + "&" +
				"&oauth_signature_method=PLAINTEXT" + 
				"&oauth_signature=" +  appKeyPair.secret + "%26" + accessToken.secret + 
				"&oauth_nonce=\"" + (new Date()).getTime() + "\""
			);
		URLConnection c = url.openConnection();
		String response = Streams.readFullyString(c.getInputStream(), "UTF-8");

		Pair<String,String> token = parseAuthToken(response);
		return new AccessTokenPair(token.first, token.second);	
	}

	static Pair<String,String> parseAuthToken (String response) throws Exception
	{
		String userKey=null, userSecret=null;
		String[] parts = response.split("&");
		for (String part : parts)
		{
			String[] keyValue = part.split("=");
			String key = keyValue[0];
			String value = keyValue[1];
			
			if (key.equalsIgnoreCase("oauth_token_secret"))
				userSecret = value;
			else
			if (key.equalsIgnoreCase("oauth_token"))
				userKey = value;
		}
		
		if (userSecret == null || userKey == null)
			throw new Exception ("Could parse authToken");
		
		return new Pair<String,String>(userKey, userSecret);
	}
}
