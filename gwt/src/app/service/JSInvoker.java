/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;

import java.util.Collection;

import mail.client.model.Mail;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import core.util.LogNull;
import core.util.LogOut;

@Export
public class JSInvoker implements Exportable
{
	static LogNull log = new LogNull(JSInvoker.class);
	
	private static native JavaScriptObject _wrap(boolean o) /*-{
		return o;
	}-*/;
	
	private static native JavaScriptObject _wrap(int o) /*-{
		return o;
	}-*/;

	private static native JavaScriptObject _wrap(String o) /*-{
		return o;
	}-*/;
	
	private static JsArray<JavaScriptObject> _wrap(Object[] o)
	{
		JsArray<JavaScriptObject> a = JavaScriptObject.createArray().cast();
		for (Object i : o)
			a.push(wrap(i));
		
		return a;
	}
	
	static public JavaScriptObject wrap(Object o)
	{
		if (o instanceof Boolean)
			return _wrap((boolean)(Boolean)o);
		else
		if (o instanceof Integer)
			return _wrap((int)(Integer)o);
		else
		if (o instanceof String)
			return _wrap((String)o);
		else
		if (o instanceof Object[])
			return _wrap((Object[])o);
		else
		if (o instanceof Collection)
			return _wrap(((Collection<?>)o).toArray());
		else
		if (o instanceof Mail)
			return wrap(new MailI((Mail)o));

		try
		{
			return ExporterUtil.wrap(o);
		}
		catch (Throwable e)
		{
			log.debug(e);
			return (JavaScriptObject)o;
		}
	}
	
	static JavaScriptObject invoke (Object oo, String f, Object[] p) throws Exception
	{
		try
		{
			return doInvoke(oo, f, p);
		}
		catch (Exception e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			throw new Exception(t.toString());
		}
	}

	static JavaScriptObject doInvoke (Object oo, String f, Object[] p) throws Throwable
	{
		JavaScriptObject o = (JavaScriptObject)oo;

		if (p == null || p.length == 0)
			return invoke0(o,f);
		else
		if (p.length == 1)
			return invoke1(o,f,
				wrap(p[0])
		);
		else
		if (p.length == 2)
			return invoke2(o,f,
				wrap(p[0]), 
				wrap(p[1])
			);
		else
		if (p.length == 3)
			return invoke3(o,f,
				wrap(p[0]), 
				wrap(p[1]), 
				wrap(p[2])
			);
		else
		if (p.length == 4)
			return invoke4(o,f,
				wrap(p[0]), 
				wrap(p[1]), 
				wrap(p[2]), 
				wrap(p[3])
			);
		else
		if (p.length == 5)
			return invoke5(o,f,
				wrap(p[0]), 
				wrap(p[1]), 
				wrap(p[2]), 
				wrap(p[3]),
				wrap(p[4])
			);
		else
		if (p.length == 6)
			return invoke6(o,f,
				wrap(p[0]), 
				wrap(p[1]), 
				wrap(p[2]), 
				wrap(p[3]),
				wrap(p[4]),
				wrap(p[5])
			);
		else
		if (p.length == 7)
			return invoke7(o,f,
				wrap(p[0]), 
				wrap(p[1]), 
				wrap(p[2]), 
				wrap(p[3]),
				wrap(p[4]),
				wrap(p[5]),
				wrap(p[6])
			);
		else
		if (p.length == 8)
			return invoke8(o,f,
				wrap(p[0]), 
				wrap(p[1]), 
				wrap(p[2]), 
				wrap(p[3]),
				wrap(p[4]),
				wrap(p[5]),
				wrap(p[6]),
				wrap(p[7])
			);
		else
		if (p.length == 9)
			return invoke9(o,f,
				wrap(p[0]), 
				wrap(p[1]), 
				wrap(p[2]), 
				wrap(p[3]),
				wrap(p[4]),
				wrap(p[5]),
				wrap(p[6]),
				wrap(p[7]),
				wrap(p[8])
			);
		else
		if (p.length == 10)
			return invoke10(o,f,
				wrap(p[0]), 
				wrap(p[1]), 
				wrap(p[2]), 
				wrap(p[3]),
				wrap(p[4]),
				wrap(p[5]),
				wrap(p[6]),
				wrap(p[7]),
				wrap(p[8]),
				wrap(p[9])
			);
		
		return null;
	}

	static native JavaScriptObject invoke0 (JavaScriptObject o, String f) /*-{
		return o[f].call(o);
	}-*/;

	static native JavaScriptObject invoke1 (JavaScriptObject o, String f, 
			JavaScriptObject p0
	) /*-{
		return o[f].call(o, 
			p0
		);
	}-*/;

	static native JavaScriptObject invoke2 (JavaScriptObject o, String f, 
			JavaScriptObject p0, 
			JavaScriptObject p1
	) /*-{
		return o[f].call(o, 
			p0, 
			p1
		);
	}-*/;

	static native JavaScriptObject invoke3 (JavaScriptObject o, String f, 
			JavaScriptObject p0, 
			JavaScriptObject p1,  
			JavaScriptObject p2
	) /*-{
		return o[f].call(o,
			p0, 
			p1, 
			p2
		);
	}-*/;

	static native JavaScriptObject invoke4 (JavaScriptObject o, String f, 
			JavaScriptObject p0, 
			JavaScriptObject p1,  
			JavaScriptObject p2,  
			JavaScriptObject p3
	) /*-{
		return o[f].call(o, 
			p0, 
			p1, 
			p2, 
			p3
		);
	}-*/;

	static native JavaScriptObject invoke5 (JavaScriptObject o, String f, 
			JavaScriptObject p0, 
			JavaScriptObject p1,  
			JavaScriptObject p2,  
			JavaScriptObject p3,
			JavaScriptObject p4
	) /*-{
		return o[f].call(o, 
			p0, 
			p1, 
			p2, 
			p3,
			p4
		);
	}-*/;

	static native JavaScriptObject invoke6 (JavaScriptObject o, String f,
			JavaScriptObject p0, 
			JavaScriptObject p1,  
			JavaScriptObject p2,  
			JavaScriptObject p3,
			JavaScriptObject p4,
			JavaScriptObject p5
	) /*-{
		return o[f].call(o, 
			p0, 
			p1, 
			p2, 
			p3,
			p4,
			p5
		);
	}-*/;

	static native JavaScriptObject invoke7 (JavaScriptObject o, String f,
			JavaScriptObject p0, 
			JavaScriptObject p1,  
			JavaScriptObject p2,  
			JavaScriptObject p3,
			JavaScriptObject p4,
			JavaScriptObject p5,
			JavaScriptObject p6
	) /*-{
		return o[f].call(o, 
			p0, 
			p1, 
			p2, 
			p3,
			p4,
			p5,
			p6
		);
	}-*/;

	static native JavaScriptObject invoke8 (JavaScriptObject o, String f, 
			JavaScriptObject p0, 
			JavaScriptObject p1,  
			JavaScriptObject p2,  
			JavaScriptObject p3,
			JavaScriptObject p4,
			JavaScriptObject p5,
			JavaScriptObject p6,
			JavaScriptObject p7
	) /*-{
		return o[f].call(o, 
			p0, 
			p1, 
			p2, 
			p3,
			p4,
			p5,
			p6,
			p7
		);
	}-*/;

	static native JavaScriptObject invoke9 (JavaScriptObject o, String f, 
			JavaScriptObject p0, 
			JavaScriptObject p1,  
			JavaScriptObject p2,  
			JavaScriptObject p3,
			JavaScriptObject p4,
			JavaScriptObject p5,
			JavaScriptObject p6,
			JavaScriptObject p7,
			JavaScriptObject p8
	) /*-{
		return o[f].call(o, 
			p0, 
			p1, 
			p2, 
			p3,
			p4,
			p5,
			p6,
			p7,
			p8
		);
	}-*/;

	static native JavaScriptObject invoke10 (JavaScriptObject o, String f, 
			JavaScriptObject p0, 
			JavaScriptObject p1,  
			JavaScriptObject p2,  
			JavaScriptObject p3,
			JavaScriptObject p4,
			JavaScriptObject p5,
			JavaScriptObject p6,
			JavaScriptObject p7,
			JavaScriptObject p8,
			JavaScriptObject p9
	) /*-{
		return o[f].call(o, 
			p0, 
			p1, 
			p2, 
			p3,
			p4,
			p5,
			p6,
			p7,
			p8,
			p9
		);
	}-*/;

	public native static JavaScriptObject getMember(Object o, String s) /*-{
		return o[s];
	}-*/;
}
