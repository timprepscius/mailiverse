package core.util;

import app.service.JSHttpDelegate;
import app.service.Main;

public class HttpDelegateFactory 
{
	static HttpDelegate create ()
	{
		return new JSHttpDelegate(Main.delegate);
	}
}
