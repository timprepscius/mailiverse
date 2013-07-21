/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package com.mailiverse.gwt.client;

import org.timepedia.exporter.client.ExporterUtil;

import mail.client.model.Original;

import app.service.Main;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Mailiverse_GWT implements EntryPoint {
	
	/**
	 * This is the entry point method.
	 */
	public native void propagateLoad (JavaScriptObject main) /*-{
		$wnd.onMailiverseBootstrapGWT(main);
	}-*/;
	
	
	public void onModuleLoad() 
	{
		ExporterUtil.exportAll();
		propagateLoad(ExporterUtil.wrap(new Main()));
	}
}
