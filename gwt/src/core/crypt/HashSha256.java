package core.crypt;

import core.util.Base64;

public class HashSha256 
{
	protected native String jsHash(String bytes64) /*-{
		return $wnd.mSupport.sha256_hash(bytes64);
	}-*/;
	
	public byte[] hash(byte[] bytes) 
	{
		return Base64.decode(jsHash(Base64.encode(bytes)));
	}
}
