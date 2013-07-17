package core.constants;

public class ConstantsClient
{
	public static final String 
		HOST = ConstantsClientPlatform.HOST, 
		TOMCAT_HOST = ConstantsClientPlatform.TOMCAT_HOST, 
		AUTH_HOST = ConstantsClientPlatform.AUTH_HOST, 
		WEB_HOST = ConstantsClientPlatform.WEB_HOST;
	
	public static final String ATHOST = "@" + HOST;
	public static final String WEB_SERVER_URL = "https://" + WEB_HOST;
	
	public static final String SERVER_TOMCAT = TOMCAT_HOST + "/Mailiverse/";
	public static final String WEB_SERVER_TOMCAT = "https://" + SERVER_TOMCAT;
	
	public static final String KEY_AUTH_HOST = AUTH_HOST;
	public static final int KEY_AUTH_PORT = 7000;
	
	public static final String MAIL_AUTH_HOST = AUTH_HOST;
	public static final int MAIL_AUTH_PORT = 7001;

	public static final String MAIL_SERVER_WEBSOCKET = "wss://" + SERVER_TOMCAT + "MailServer";
	public static final String KEY_SERVER_WEBSOCKET = "wss://" + SERVER_TOMCAT + "KeyServer";
	
	// Mailiverse
	public static final String DROPBOX_APPKEY = "YOUR_APPKEY";
	public static final String DROPBOX_APPSECRET = "YOUR_APPSECRET";
	
	public static final int MAXIMUM_MAIL_SIZE = 1024 * 1024 * 1;
	public static final String WEB_AUTHORIZED_URL = WEB_SERVER_URL + "/DropboxAuthorized.html";

	public static final String DROPBOX_AUTH_URL = 
		"https://www.dropbox.com/1/oauth/authorize" + 
			"?oauth_token=REQUEST_TOKEN_KEY" +
			"&oauth_callback=" + WEB_AUTHORIZED_URL + 
			"&locale=en";
}
