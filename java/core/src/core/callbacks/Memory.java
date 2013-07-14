/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callbacks;

import java.util.HashMap;
import java.util.Map;

import core.callback.Callback;
import core.callback.CallbackDefault;

public class Memory
{
	public Map<String, Object> memory = new HashMap<String, Object>();
	
	@SuppressWarnings("unchecked")
	public <T> T get (String key)
	{
		return (T)memory.get(key);
	}
	
	public void put (String key, Object o)
	{
		memory.put(key, o);
	}
	
	public Callback store_(Object...keys)
	{
		return new CallbackDefault(keys) {
			@Override
			public void onSuccess(Object... arguments) throws Exception
			{
				for (int i=0; i<v.length; ++i)
				{
					memory.put((String)v[i], arguments[i]);
				}
				
				next(arguments);
			}
		};
	}
	
	public Callback restore_(Object...keys)
	{
		return new CallbackDefault(keys) {
			public void onSuccess(Object...arguments) {
				Object[] keys = v;
				Object[] args = new Object[keys.length];
				
				int j=0;
				for (Object i : keys)
				{
					if (i instanceof Integer)
						args[j++] = arguments[(Integer)i];
					else
						args[j++] = memory.get((String)i);
				}
				
				next(args);
			}
			
		};
	}

}
