/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;


public class JSONRegistry
{
	static Map<String, CallSingle<JSONObject,Object>> serializers = 
			new HashMap<String, CallSingle<JSONObject,Object>>();
		
	static Map<String, CallSingle<Object, JSONObject>> deserializers =
		new HashMap<String, CallSingle<Object,JSONObject>>();
			
	public static void register (
		String className, 
		CallSingle<JSONObject,Object> serializer, 
		CallSingle<Object, JSONObject> deserializer
	)
	{
		serializers.put(className, serializer);
		deserializers.put(className, deserializer);
	}
}
