/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.dropbox;

import core.constants.ConstantsDropbox;
import core.util.Environment;


public class ClientInfoDropbox 
{
	private String userPrefix;
	private String appKey;
	private String appSecret;
	private String tokenKey;
	private String tokenSecret;

	public ClientInfoDropbox (Environment e)
	{
		userPrefix = e.get(ConstantsDropbox.DropboxUserPrefix);
		appKey = e.checkGet(ConstantsDropbox.DropboxAppKey);
		appSecret = e.checkGet(ConstantsDropbox.DropboxAppSecret);
		tokenKey = e.checkGet(ConstantsDropbox.DropboxTokenKey);
		tokenSecret = e.checkGet(ConstantsDropbox.DropboxTokenSecret);
	}
	
	public String getUserPrefix ()
	{
		return userPrefix;
	}

	public String getAppKey ()
	{
		return appKey;
	}
	
	public String getAppSecret ()
	{
		return appSecret;
	}

	public String getTokenKey ()
	{
		return tokenKey;
	}

	public String getTokenSecret ()
	{
		return tokenSecret;
	}
}
