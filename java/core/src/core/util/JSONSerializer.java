/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;


// http://stackoverflow.com/questions/2213734/using-gson-library-in-gwt-client-code
public class JSONSerializer
{	
	static LogNull log = new LogNull(JSONSerializer.class);
	
    public static <T> byte[] serialize (T o) throws IOException
	{
		try
		{
			if (o == null)
	            return Strings.toBytes("null");
	    
		    String className = o.getClass().getName();

		    Object serialization = null;  
		    if (className.equals("java.lang.String"))
		    	serialization = o;
		    else
		    	serialization = serialize(className, o);

		    byte[] result = Strings.toBytes(className + "!" + serialization);
		    log.debug("serialize: ", Strings.toString(result));
		    return result;
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize (byte[] bytes) throws IOException
	{
        String s = Strings.toString(bytes);
	    log.debug("deserialize: ",s);
        
        if (s.equals("null"))
                return null;
        
        int notPosition = s.indexOf('!');
        if (notPosition == -1)
                return null;
        
        String className = s.substring(0, notPosition);
        String serialization = s.substring(notPosition+1);
        
        try
		{
        	if (className.equals("java.lang.String"))
        		return (T)serialization;
        	
			return (T)deserialize(className, new JSONObject(serialization));
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
	}

	private static JSONObject serialize (String className, Object o) throws Exception
	{
		CallSingle<JSONObject,Object> serializer = JSONRegistry.serializers.get(className);
		if (serializer == null)
			throw new ClassNotFoundException("Unknown class " + className);
		
		return serializer.invoke(o);
	}

	private static Object deserialize (String className, JSONObject o) throws Exception
	{
		CallSingle<Object,JSONObject> deserializer = JSONRegistry.deserializers.get(className);
		if (deserializer == null)
			throw new ClassNotFoundException("Unknown class " + className);
		
		return deserializer.invoke(o);
	}
	
	
	static {
		JSONRegistry.register (
			"java.util.HashMap",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();

					Map<String,String> p = (Map<String,String>)v;
					for (Entry<String,String> e : p.entrySet())
					{
						log.debug(e.getKey(), e.getValue());
						object.put(e.getKey(), e.getValue());
					}
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					Map<String,String> m = new HashMap<String,String>();
					
					@SuppressWarnings("unchecked")
					Iterator<String> i = (Iterator<String>)v.keys();
					while (i.hasNext())
					{
						String key = i.next();
						String value = v.getString(key);
						
						log.debug(key, value);
						m.put(key, value);
					}

					return m;
				}
			}
		);

		JSONRegistry.register (
			"core.util.Block",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					
					Block p = (Block)v;
					if (p.bytes == null)
						object.put("bytes", "null");
					else
						object.put("bytes", Base64.encode(p.bytes));
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					String bytes = v.get("bytes").toString();

					if (bytes.equals("null"))
						return new Block(null);
					
					return new Block(
						Base64.decode(bytes)
					);
				}
			}
		);
		
		JSONRegistry.register (
			"core.util.Environment",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					
					Environment p = (Environment)v;
					for (String k : p.keySet())
						object.put(k, p.get(k));
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					Environment e = new Environment();
					Iterator<?> i = v.keys();
					while (i.hasNext())
					{
						String k = (String)i.next();
						e.put(k, v.get(k).toString());
					}
					
					return e;
				}
			}
		);
	}

}