package core.util;

public class HttpDelegateFactory 
{
	static public HttpDelegate create ()
	{
		return new HttpDelegateJava();
	}
}
