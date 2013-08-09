package com.benith.mailiverse;

import android.os.AsyncTask;
import core.callback.Callback;
import core.util.HttpDelegate;
import core.util.HttpDelegateJava;

public class HttpDelegateAndroid extends HttpDelegate
{
	HttpDelegate delegate = new HttpDelegateJava();

	protected class Request
	{
		String action;
		String url;
		String[][] headers;
		boolean binaryInput, binaryOutput;
		byte[] contents;
		Callback callback;
	}
	
	@Override
	public void execute(String action, String url, String[][] headers,
			boolean binaryInput, boolean binaryOutput, byte[] contents,
			Callback callback) {
		Request request = new Request();
		request.action = action;
		request.url = url;
		request.headers = headers;
		request.binaryInput = binaryInput;
		request.binaryOutput = binaryOutput;
		request.contents = contents;
		request.callback = callback;
		
		new AsyncTask<Request, Void, Void>() {
			protected Void doInBackground(Request... params) {
				Request r = params[0];
				
				delegate.execute(r.action, r.url, r.headers, r.binaryInput, r.binaryOutput, r.contents, r.callback);
				
				return null;
			}
		}.execute(request);
	}
}
