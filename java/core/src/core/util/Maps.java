/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.util.HashMap;
import java.util.Map;

import core.callback.Callback;
import core.callback.CallbackDefault;

public class Maps
{	
	static LogNull log = new LogNull(Maps.class);
	
	@SuppressWarnings("unchecked")
	public static <K, V> void applyToMapAA(Map<K, V> m, Object[][] items)
	{
		for (Object[] p : items)
		{
			m.put((K)p[0], (V)p[1]);
		}
	}

	@SuppressWarnings("unchecked")
	public static <K, V> void applyToMapA(Map<K, V> m, Object ... items)
	{
		int i;
		for (i=1; i<items.length; i+=2)
		{
			m.put((K)items[i-1], (V)items[i]);
		}
	}
	
	public static <K,V> Map<K,V> toMapAA(Object[][] items)
	{
		Map<K,V> m = new HashMap<K,V> ();
		applyToMapAA(m, items);
		
		return m;
	}
	
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> toMap(Object ... items)
	{
		Map<K,V> m = new HashMap<K,V> ();

		int i;
		for (i=1; i<items.length; i+=2)
		{
			m.put((K)items[i-1], (V)items[i]);
		}
		
		return m;
	}
	
	public static <K,X> Callback put_(Map<K,X> map, K k, X x)
	{
		return new CallbackDefault(map, k,x) {
			public void onSuccess(Object... arguments) throws Exception {
				Map<K,X> map = V(0);
				K k = V(1);
				X x = V(2);
				map.put(k,x);
				
				log.debug("put",map,k,x);
				next(arguments);
			}
		};
	}

	public static <K,X> Callback put_(Map<K,X> map, K k)
	{
		return new CallbackDefault(map, k) {
			public void onSuccess(Object... arguments) throws Exception {
				Map<K,X> map = V(0);
				K k = V(1);
				X x = (X)arguments[0];
				map.put(k,x);
				log.debug("put",map,k,x);
				next(arguments);
			}
		};
	}
}
