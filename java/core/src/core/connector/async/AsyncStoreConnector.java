/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.async;

import java.util.Date;

import core.callback.Callback;

public interface AsyncStoreConnector 
{	
	Callback list_ (String path);
	Callback createDirectory_ (String path);
	Callback ensureDirectories_ (String[] directories);
	
	Callback put_ (String path, byte[] bytes);
	Callback get_ (String path);
	
	Callback put_ (String path);
	Callback get_ ();
	
	Callback move_ (String from, String to);
	Callback delete_ (String path);
}
