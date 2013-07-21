/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package core.util;

public class LogPlatform
{
	public native static void println(String s) /*-{
		$wnd.log(s);
	}-*/;
	
	public native static void printException(Object e) /*-{
		$wnd.logException(e);
	}-*/;

}
