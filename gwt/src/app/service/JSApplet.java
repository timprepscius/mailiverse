/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;

import com.google.gwt.core.client.JavaScriptObject;

public class JSApplet {

//    public void paint( Graphics g ) {
//        g.drawString(VERSION_STRING,4,15);
//    }  

	static public native JavaScriptObject getWindow(Object o) /*-{
		return $wnd;
	}-*/;

}
