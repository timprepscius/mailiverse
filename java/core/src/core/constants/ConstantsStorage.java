package core.constants;

public class ConstantsStorage 
{
	public final static String 
		HANDLER_DROPBOX = "DB",
		HANDLER_S3 = "S3",
		HANDLER_MV = "MV";
	
	public final static String 
		IN = "In",
		OUT = "Out",
		JSON = "_Json",

		NEW = "Mail",
		NEW_IN = NEW + "/" + IN,
		NEW_OUT = NEW + "/" + OUT,
		
		NEW_IN_JSON = NEW_IN + JSON,
		NEW_OUT_JSON = NEW_OUT + JSON,
	
		CACHE = "Cache",
		CACHE_PREFIX = CACHE + "/";
	
	public static int LARGE_MESSAGE_SIZE = 20 * 1024;

	public static final int FLUSH_LOCK_TIME_SECONDS = 10;
	public static final int FLUSH_LOCK_TIME_ALLOWED_BEFORE_RELOCK_SECONDS = 5;
	
	public static final int MAIL_CHECK_LOCK_TIME_SECONDS = 120;
	public static final int MAIL_CHECK_LOCK_TIME_ALLOWED_BEFORE_RELOCK_SECONDS = 60;

}
