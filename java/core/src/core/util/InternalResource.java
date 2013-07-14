/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class InternalResource
{
	public static InputStream getResourceAsStream (Class<?> c, String p)
	{
		return c.getResourceAsStream(p);
	}
	
	public static native String getResource(String r) /*-{
		return $wnd.resource_acquire(r);
	}-*/;
	
	public static InputStream getResourceAsStream_NO (Class<?> c, String p)
	{
		return new ByteArrayInputStream(Base64.decode(getResource(c.getName() + "." + p)));
	}

}
