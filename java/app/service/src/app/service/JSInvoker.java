package app.service;

import netscape.javascript.JSObject;

public class JSInvoker
{
	public static Object invoke(Object o, String f, Object[] args)
	{
		JSObject js = (JSObject)o;
		return js.call(f, args);
	}

	public static Object getMember(Object o, String member)
	{
		JSObject js = (JSObject)o;
		return js.getMember(member);
	}

	public static Object wrap(Object o)
	{
		return o;
	}
}
