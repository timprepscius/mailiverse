/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.client.messages;

import org.json.JSONObject;

import core.util.Base64;
import core.util.CallSingle;
import core.util.JSONRegistry;


public class ClientMessagesSerializerJSON
{
	static public void register () {}
	
	static {
		JSONRegistry.register (
			"core.client.messages.Get",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					Get get = (Get)v;
					JSONObject object = new JSONObject();
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					return new Get();
				}
			}
		);
		
		JSONRegistry.register (
			"core.client.messages.Delete",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					return new Delete();
				}
			}
		);

		JSONRegistry.register (
			"core.client.messages.Put",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					Put set = (Put)v;
					JSONObject object = new JSONObject();
					object.put("block", Base64.encode(set.getBlock()));
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					return new Put(Base64.decode(v.getString("block")));
				}
			}
		);

		JSONRegistry.register (
			"core.client.messages.Response",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					Response response = (Response)v;
					JSONObject object = new JSONObject();
					if (response.getBlock() != null)
						object.put("block", Base64.encode(response.getBlock()));
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					if (v.has("block"))
						return new Response(Base64.decode(v.getString("block")));
					else
						return new Response();	
				}
			}
		);
	}
}
