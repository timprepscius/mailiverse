package app.service;

import java.applet.Applet;

import netscape.javascript.JSObject;

import core.callback.Callback;

public class JSApplet extends Applet
{

	public Object getWindow(Callback callback)
	{
		return JSObject.getWindow(this);
	}

}
