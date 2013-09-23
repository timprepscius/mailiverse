package core.util;

import app.service.JSHttpDelegate;
import app.service.Main;

public class HttpDelegateFactory 
{
	static public HttpDelegate create ()
	{
		return new JSHttpDelegate(Main.delegate);
	}
}
