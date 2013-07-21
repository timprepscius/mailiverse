package core.crypt;

import core.util.Base64;
import core.util.Strings;

public class HashSha1 
{
	protected native String jsHash(String bytes64) /*-{
		$wnd.mSupport.sha1_hash(bytes64);
	}-*/;

	public byte[] hash(byte[] bytes) 
	{
		return Base64.decode(jsHash(Base64.encode(bytes)));
	}

}
