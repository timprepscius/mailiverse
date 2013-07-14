/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSON_ 
{
	@SuppressWarnings("serial")
	public static class JSONException extends Exception 
	{
		public JSONException(Exception e)
		{
			super(e);
		}
	};
		
	public static Object newString(String string)
	{
		return string;
	}
	
	public static Object newArray ()
	{
		return new JSONArray();
	}
	
	public static Object newObject ()
	{
		return new JSONObject();
	}
	
	public static Object newNumber (long v)
	{
		return v;
	}
	
	public static int size(Object a) throws JSONException
	{
		try
		{
			return ((JSONArray)a).length();
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}
	
	public static String[] keys(Object _o) throws JSONException
	{
		try
		{
			ArrayList<String> a = new ArrayList<String>();
			
			JSONObject o = (JSONObject)_o;
			Iterator<?> i = o.keys();
			while (i.hasNext())
			{
				Object _k = i.next();
				a.add((String)_k);
			}
			
			return a.toArray(new String[0]);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}
	
	public static Object get(Object a, int k) throws JSONException
	{
		try
		{
			return ((JSONArray)a).get(k);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}
	
	public static void add (Object a, Object v) throws JSONException
	{
		try
		{
			JSONArray ja = (JSONArray)a;
			ja.put(ja.length(), v);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}

	public static Object get(Object o, String k) throws JSONException
	{
		try
		{
			return ((JSONObject)o).get(k);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
			
	}
	
	public static void put (Object o, String k, Object v) throws JSONException
	{
		try
		{
			((JSONObject)o).put(k, v);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}
	
	public static boolean has (Object o, String k) throws JSONException
	{
		try
		{
			return ((JSONObject)o).has(k);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}
	
	//----------------------------------------------
	
	public static String getString (Object a, int i) throws JSONException
	{
		try
		{
			return ((String)get(a,i));
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}
	
	public static String getString (Object o, String k) throws JSONException
	{
		try
		{
			return ((String)get(o,k));
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}
	
	public static Object getArray (Object o, String k) throws JSONException
	{
		try
		{
			return get(o,k);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}
	
	public static Object getObject(Object o, String k) throws JSONException
	{
		try
		{
			return get(o,k);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}

	public static int getInt(Object o, String k) throws JSONException
	{
		try
		{
			return ((JSONObject)o).getInt(k);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}

	public static Object getArray(Object a, int i) throws JSONException
	{
		try
		{
			return get(a,i);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}

	public static long getLong(Object o, int k) throws JSONException
	{
		try
		{
			return ((JSONArray)o).getLong(k);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}

	public static long getLong(Object o, String k) throws JSONException
	{
		try
		{
			return ((JSONObject)o).getLong(k);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}
	
	public static boolean getBoolean(Object o, String k) throws JSONException
	{
		try
		{
			return ((JSONObject)o).getBoolean(k);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}

	public static Object parse (String s) throws JSONException
	{
		try
		{
			return new JSONObject(s);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}

	public static Object newBoolean(boolean loaded)
	{
		return loaded;
	}

	public static Object getObject(Object a, int i) throws JSONException
	{
		try
		{
			return ((JSONArray)a).get(i);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}

	public static String asString(Object value)
	{
		return (String)value;
	}
}
