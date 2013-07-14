/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.async;

import java.util.Date;
import java.util.List;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.callback.CallbackEmpty;
import core.callback.CallbackWithVariables;
import core.connector.FileInfo;

public abstract class AsyncStoreConnectorHelper implements AsyncStoreConnector 
{
	abstract public void list(String path, Callback callback);
	abstract public void createDirectory(String path, Callback callback);
	abstract public void get(String path, Callback callback);
	abstract public void put(String path, byte[] bytes, Callback callback); 
	abstract public void delete(String path, Callback callback);
//	abstract public void move(String from, String to, Callback callback); 

	public void ensureDirectories(String[] folders, Callback callback) 
	{
		CallbackChain chain = new CallbackChain();
		
		for (String path : folders)
		{
			chain.addCallback(new CallbackWithVariables(path) {

				@Override
				public void invoke(Object... arguments)
				{
					String path = V(0);
					createDirectory(path, callback);
				} 
				
			});
		}
		chain.setReturn(callback);

		chain.invoke();
	}
	
	@Override
	public Callback createDirectory_(String path) 
	{
		return new CallbackDefault(path) {
			public void onSuccess(Object... arguments) throws Exception {
				createDirectory((String)V(0), callback);
			}
		};
	}
	
	@Override
	public Callback list_(String path) 
	{
		return new CallbackDefault(path) {
			public void onSuccess(Object... arguments) throws Exception {
				list((String)V(0), callback);
			}
		};
	}
	
	@Override
	public Callback get_() 
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				get((String)arguments[0], callback);
			}
		};
	}
	
	public Callback get_(String path)
	{
		return 
			new CallbackDefault(path) {
				public void onSuccess(Object... arguments) throws Exception {
					get((String)V(0), callback);
				}
			};
	}
	
	@Override
	public Callback put_(String path) 
	{
		return new CallbackDefault(path) {
			public void onSuccess(Object... arguments) throws Exception {
				put((String)V(0), (byte[])arguments[0], callback);
			}
		};
	}
	
	public Callback put_(String path, byte[] bytes)
	{
		return 
			new CallbackDefault(path, bytes) {
				public void onSuccess(Object... arguments) throws Exception {
					put((String)V(0), (byte[])V(1), callback);
				}
			};
	}
	
	@Override
	public Callback move_(String from, String to) 
	{
		return new CallbackEmpty();
	}

	@Override
	public Callback delete_(String path) {
		return 
			new CallbackDefault(path) {
				public void onSuccess(Object... arguments) throws Exception {
					delete((String)V(0), callback);
				}
			};
	}

	@Override
	public Callback ensureDirectories_(String[] folders) 
	{
		return new CallbackDefault(new Object[] { folders }) {

			@Override
			public void onSuccess(Object... arguments) throws Exception {
				ensureDirectories((String[])V(0), callback);
			}
		};
	}
}
