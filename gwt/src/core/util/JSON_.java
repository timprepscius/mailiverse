/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package core.util;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class JSON_ 
{
	@SuppressWarnings("serial")
	public static class JSONException extends Exception {
		public JSONException(Exception e)
		{
			super(e);
		}
	};
	
	public static Object newString(String string)
	{
		return new JSONString(string);
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
		return new JSONNumber(v);
	}
	
	public static String asString(Object value) 
	{
		return ((JSONString)value).stringValue();
	}
	
	public static int size(Object a) throws JSONException
	{
		try
		{
			return ((JSONArray)a).size();
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
			JSONObject o = (JSONObject) _o;
			return o.keySet().toArray(new String[0]);
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
			ja.set(ja.size(), (JSONValue)v);
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
			((JSONObject)o).put(k, (JSONValue)v);
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
			return ((JSONObject)o).containsKey(k);
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
			return ((JSONString)get(a,i)).stringValue();
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
			return ((JSONString)get(o,k)).stringValue();
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
			return (int) ((JSONNumber)get(o,k)).doubleValue();
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
			return (long) ((JSONNumber)get(o,k)).doubleValue();
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
			return (long) ((JSONNumber)get(o,k)).doubleValue();
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
			return JSONParser.parseStrict(s);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}

	public static Object newBoolean(boolean loaded)
	{
		return JSONBoolean.getInstance(loaded);
	}

	public static Object getObject(Object o, int i) throws JSONException
	{
		try
		{
			return (JSONObject)get(o,i);
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}

	public static boolean getBoolean(Object o, String string) throws JSONException 
	{
		try
		{
			return ((JSONBoolean)get(o, string)).booleanValue();
		}
		catch (Exception e)
		{
			throw new JSONException(e);
		}
	}
}
