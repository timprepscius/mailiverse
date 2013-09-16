/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import core.callback.Callback;
import core.callback.CallbackDefault;

public abstract class HttpDelegate
{
	public static final String GET = "GET";
	public static final String PUT = "PUT";
	public static final String POST = "POST";
	public static final String DELETE = "DELETE";
	
	public abstract void execute (String action, String url, String[][] headers, boolean binaryInput, boolean binaryOutput, byte[] contents, Callback callback);
	
	public Callback execute_(String action, String url, String[][]headers,boolean binaryInput, boolean binaryOutput)
	{
		return new CallbackDefault(action, url, headers, binaryInput, binaryOutput) {
			public void onSuccess(Object... arguments) throws Exception {
				String action = V(0);
				String url = V(1);
				String[][] headers = V(2);
				boolean binaryInput = (Boolean)V(3);
				boolean binaryOutput = (Boolean)V(4);
				
				byte[] content = (arguments!=null && arguments.length > 0) ? (byte[])arguments[0] : null;
				
				execute(action, url, headers, binaryInput, binaryOutput, content, callback);
			}
		};
	}
}
