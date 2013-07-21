/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;

import com.google.gwt.core.client.JavaScriptObject;

import core.util.Characters;
import core.util.LogNull;
import core.util.LogOut;

public class JSInvokerNo 
{
	static LogNull log = new LogNull(JSInvokerNo.class);
	
	static String className(Class<?> c)
	{
		String name = c.getName();

		int d = name.lastIndexOf('$');

		if (d != -1)
		{
			if (d+1 < name.length())
			{
				if (Characters.isNumber(name.charAt(d+1)))
					return className(c.getSuperclass());
			}
		}

		return name;
	}
	
	static String classOf(Object p)
	{
		if (p == null)
			return null;
		
		return className(p.getClass());
	}
	
	static JavaScriptObject invoke (Object oo, String f, Object[] p) 
	{
		JavaScriptObject o = (JavaScriptObject)oo;

		if (p == null || p.length == 0)
			return invoke0(o,f);
		else
		if (p.length == 1)
			return invoke1(o,f,classOf(p[0]), p[0]);
		else
		if (p.length == 2)
			return invoke2(o,f,
				classOf(p[0]), p[0], 
				classOf(p[1]), p[1]
			);
		else
		if (p.length == 3)
			return invoke3(o,f,
				classOf(p[0]), p[0], 
				classOf(p[1]), p[1], 
				classOf(p[2]), p[2]
			);
		else
		if (p.length == 4)
			return invoke4(o,f,
				classOf(p[0]), p[0], 
				classOf(p[1]), p[1], 
				classOf(p[2]), p[2], 
				classOf(p[3]), p[3]
			);
		else
		if (p.length == 5)
			return invoke5(o,f,
				classOf(p[0]), p[0], 
				classOf(p[1]), p[1], 
				classOf(p[2]), p[2], 
				classOf(p[3]), p[3],
				classOf(p[4]), p[4]
			);
		else
		if (p.length == 6)
			return invoke6(o,f,
				classOf(p[0]), p[0], 
				classOf(p[1]), p[1], 
				classOf(p[2]), p[2], 
				classOf(p[3]), p[3],
				classOf(p[4]), p[4],
				classOf(p[5]), p[5]
			);
		else
		if (p.length == 7)
			return invoke7(o,f,
				classOf(p[0]), p[0], 
				classOf(p[1]), p[1], 
				classOf(p[2]), p[2], 
				classOf(p[3]), p[3],
				classOf(p[4]), p[4],
				classOf(p[5]), p[5],
				classOf(p[6]), p[6]
			);
		else
		if (p.length == 8)
			return invoke8(o,f,
				classOf(p[0]), p[0], 
				classOf(p[1]), p[1], 
				classOf(p[2]), p[2], 
				classOf(p[3]), p[3],
				classOf(p[4]), p[4],
				classOf(p[5]), p[5],
				classOf(p[6]), p[6],
				classOf(p[7]), p[7]
			);
		else
		if (p.length == 9)
			return invoke9(o,f,
				classOf(p[0]), p[0], 
				classOf(p[1]), p[1], 
				classOf(p[2]), p[2], 
				classOf(p[3]), p[3],
				classOf(p[4]), p[4],
				classOf(p[5]), p[5],
				classOf(p[6]), p[6],
				classOf(p[7]), p[7],
				classOf(p[8]), p[8]
			);
		else
		if (p.length == 10)
			return invoke10(o,f,
				classOf(p[0]), p[0], 
				classOf(p[1]), p[1], 
				classOf(p[2]), p[2], 
				classOf(p[3]), p[3],
				classOf(p[4]), p[4],
				classOf(p[5]), p[5],
				classOf(p[6]), p[6],
				classOf(p[7]), p[7],
				classOf(p[8]), p[8],
				classOf(p[9]), p[9]
			);
		
		return null;
	}

	static native JavaScriptObject invoke0 (JavaScriptObject o, String f) /*-{
		return o[f].call(o);
	}-*/;

	static native JavaScriptObject invoke1 (JavaScriptObject o, String f, 
			String c0, Object p0
	) /*-{
		return o[f].call(o, 
			$wnd.bindExportable(p0, c0)
		);
	}-*/;

	static native JavaScriptObject invoke2 (JavaScriptObject o, String f, 
			String c0, Object p0, 
			String c1, Object p1
	) /*-{
		return o[f].call(o, 
			$wnd.bindExportable(p0, c0), 
			$wnd.bindExportable(p1, c1)
		);
	}-*/;

	static native JavaScriptObject invoke3 (JavaScriptObject o, String f, 
			String c0, Object p0, 
			String c1, Object p1,  
			String c2, Object p2
	) /*-{
		return o[f].call(o,
			$wnd.bindExportable(p0, c0), 
			$wnd.bindExportable(p1, c1), 
			$wnd.bindExportable(p2, c2)
		);
	}-*/;

	static native JavaScriptObject invoke4 (JavaScriptObject o, String f, 
			String c0, Object p0, 
			String c1, Object p1,  
			String c2, Object p2,  
			String c3, Object p3
	) /*-{
		return o[f].call(o, 
			$wnd.bindExportable(p0, c0), 
			$wnd.bindExportable(p1, c1), 
			$wnd.bindExportable(p2, c2), 
			$wnd.bindExportable(p3, c3)
		);
	}-*/;

	static native JavaScriptObject invoke5 (JavaScriptObject o, String f, 
			String c0, Object p0, 
			String c1, Object p1,  
			String c2, Object p2,  
			String c3, Object p3,
			String c4, Object p4
	) /*-{
		return o[f].call(o, 
			$wnd.bindExportable(p0, c0), 
			$wnd.bindExportable(p1, c1), 
			$wnd.bindExportable(p2, c2), 
			$wnd.bindExportable(p3, c3),
			$wnd.bindExportable(p4, c4)
		);
	}-*/;

	static native JavaScriptObject invoke6 (JavaScriptObject o, String f,
			String c0, Object p0, 
			String c1, Object p1,  
			String c2, Object p2,  
			String c3, Object p3,
			String c4, Object p4,
			String c5, Object p5
	) /*-{
		return o[f].call(o, 
			$wnd.bindExportable(p0, c0), 
			$wnd.bindExportable(p1, c1), 
			$wnd.bindExportable(p2, c2), 
			$wnd.bindExportable(p3, c3),
			$wnd.bindExportable(p4, c4),
			$wnd.bindExportable(p5, c5)
		);
	}-*/;

	static native JavaScriptObject invoke7 (JavaScriptObject o, String f,
			String c0, Object p0, 
			String c1, Object p1,  
			String c2, Object p2,  
			String c3, Object p3,
			String c4, Object p4,
			String c5, Object p5,
			String c6, Object p6
	) /*-{
		return o[f].call(o, 
			$wnd.bindExportable(p0, c0), 
			$wnd.bindExportable(p1, c1), 
			$wnd.bindExportable(p2, c2), 
			$wnd.bindExportable(p3, c3),
			$wnd.bindExportable(p4, c4),
			$wnd.bindExportable(p5, c5),
			$wnd.bindExportable(p6, c6)
		);
	}-*/;

	static native JavaScriptObject invoke8 (JavaScriptObject o, String f, 
			String c0, Object p0, 
			String c1, Object p1,  
			String c2, Object p2,  
			String c3, Object p3,
			String c4, Object p4,
			String c5, Object p5,
			String c6, Object p6,
			String c7, Object p7
	) /*-{
		return o[f].call(o, 
			$wnd.bindExportable(p0, c0), 
			$wnd.bindExportable(p1, c1), 
			$wnd.bindExportable(p2, c2), 
			$wnd.bindExportable(p3, c3),
			$wnd.bindExportable(p4, c4),
			$wnd.bindExportable(p5, c5),
			$wnd.bindExportable(p6, c6),
			$wnd.bindExportable(p7, c7)
		);
	}-*/;

	static native JavaScriptObject invoke9 (JavaScriptObject o, String f, 
			String c0, Object p0, 
			String c1, Object p1,  
			String c2, Object p2,  
			String c3, Object p3,
			String c4, Object p4,
			String c5, Object p5,
			String c6, Object p6,
			String c7, Object p7,
			String c8, Object p8
	) /*-{
		return o[f].call(o, 
			$wnd.bindExportable(p0, c0), 
			$wnd.bindExportable(p1, c1), 
			$wnd.bindExportable(p2, c2), 
			$wnd.bindExportable(p3, c3),
			$wnd.bindExportable(p4, c4),
			$wnd.bindExportable(p5, c5),
			$wnd.bindExportable(p6, c6),
			$wnd.bindExportable(p7, c7),
			$wnd.bindExportable(p8, c8)
		);
	}-*/;

	static native JavaScriptObject invoke10 (JavaScriptObject o, String f, 
			String c0, Object p0, 
			String c1, Object p1,  
			String c2, Object p2,  
			String c3, Object p3,
			String c4, Object p4,
			String c5, Object p5,
			String c6, Object p6,
			String c7, Object p7,
			String c8, Object p8,
			String c9, Object p9
	) /*-{
		return o[f].call(o, 
			$wnd.bindExportable(p0, c0), 
			$wnd.bindExportable(p1, c1), 
			$wnd.bindExportable(p2, c2), 
			$wnd.bindExportable(p3, c3),
			$wnd.bindExportable(p4, c4),
			$wnd.bindExportable(p5, c5),
			$wnd.bindExportable(p6, c6),
			$wnd.bindExportable(p7, c7),
			$wnd.bindExportable(p8, c8),
			$wnd.bindExportable(p9, c9)
		);
	}-*/;

	public native static JavaScriptObject getMember(Object o, String s) /*-{
		return o[s];
	}-*/;
}
