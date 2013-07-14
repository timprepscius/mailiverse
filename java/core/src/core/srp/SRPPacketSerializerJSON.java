/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.srp;

import java.math.BigInteger;

import core.util.Base64;
import org.json.JSONObject;

import core.util.CallSingle;
import core.util.JSONRegistry;
import core.util.Strings;


public class SRPPacketSerializerJSON
{
	static public void register () {}
	
	static {
		JSONRegistry.register (
			"core.srp.SRPPackets$Packet1_ClientSendsHello",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					
					SRPPackets.Packet1_ClientSendsHello p = (SRPPackets.Packet1_ClientSendsHello)v;
					object.put("user", p.user);
					object.put("version", p.version);
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					return new SRPPackets.Packet1_ClientSendsHello(
						v.get("user").toString(),
						v.get("version").toString()
					);
				}
			}
		);
		
		JSONRegistry.register (
			"core.srp.SRPPackets$Packet2_ServerSendSalt",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					
					SRPPackets.Packet2_ServerSendSalt p = (SRPPackets.Packet2_ServerSendSalt)v;
					object.put("salt", Base64.encode(p.salt));
					object.put("version", p.version);
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					return new SRPPackets.Packet2_ServerSendSalt(
						v.get("version").toString(),
						Base64.decode(v.get("salt").toString())
					);
				}
			}
		);

		JSONRegistry.register (
			"core.srp.SRPPackets$Packet3_ClientSendPublicKey",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					
					SRPPackets.Packet3_ClientSendPublicKey p = (SRPPackets.Packet3_ClientSendPublicKey)v;
					object.put("publicKey", new String(Base64.encode(p.publicKey)));
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					return new SRPPackets.Packet3_ClientSendPublicKey(
						Base64.decode(v.get("publicKey").toString())
					);
				}
			}
		);

		JSONRegistry.register (
			"core.srp.SRPPackets$Packet4_ServerSendPublicKey",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					
					SRPPackets.Packet4_ServerSendPublicKey p = (SRPPackets.Packet4_ServerSendPublicKey)v;
					object.put("publicKey", new String(Base64.encode(p.publicKey)));
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					return new SRPPackets.Packet4_ServerSendPublicKey(
						new BigInteger(Base64.decode(v.get("publicKey").toString()))
					);
				}
			}
		);
	
		JSONRegistry.register (
			"core.srp.SRPPackets$Packet5_ClientSendEvidence",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					
					SRPPackets.Packet5_ClientSendEvidence p = (SRPPackets.Packet5_ClientSendEvidence)v;
					object.put("evidence", new String(Base64.encode(p.evidence)));
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					return new SRPPackets.Packet5_ClientSendEvidence(
						Base64.decode(v.get("evidence").toString())
					);
				}
			}
		);

		JSONRegistry.register (
			"core.srp.SRPPackets$Packet6_ServerSendEvidenceAndPayload",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					
					SRPPackets.Packet6_ServerSendEvidenceAndPayload p = (SRPPackets.Packet6_ServerSendEvidenceAndPayload)v;
					object.put("evidence", new String(Base64.encode(p.evidence)));
						
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					return new SRPPackets.Packet6_ServerSendEvidenceAndPayload(
						new BigInteger(Base64.decode(v.get("evidence").toString()))
					);
				}
			}
		);

		JSONRegistry.register (
			"core.srp.SRPPackets$PacketInit_ClientTestCreate",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					
					SRPPackets.PacketInit_ClientTestCreate p = (SRPPackets.PacketInit_ClientTestCreate)v;
					object.put("user", p.user);
					object.put("version", p.version);
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					return new 
						SRPPackets.PacketInit_ClientTestCreate(
							v.getString("version"),
							v.getString("user")
						);
				}
			}
		);

		JSONRegistry.register (
			"core.srp.SRPPackets$PacketInit_EncryptedPacket",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					
					SRPPackets.PacketInit_EncryptedPacket p = (SRPPackets.PacketInit_EncryptedPacket)v;
					object.put("encryptedBlock", new String(Base64.encode(p.encryptedBlock)));
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					return new 
						SRPPackets.PacketInit_EncryptedPacket(
							Base64.decode(v.getString("encryptedBlock"))
						);
				}
			}
		);

	
		JSONRegistry.register (
			"core.srp.SRPPackets$PacketInit_ServerResponse",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					
					SRPPackets.PacketInit_ServerResponse p = (SRPPackets.PacketInit_ServerResponse)v;
					object.put("succeeded", p.succeeded);
					object.put("reason", p.reason);
					
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					return new 
						SRPPackets.PacketInit_ServerResponse(
							v.getBoolean("succeeded"),
							v.has("reason") ? v.getString("reason") : null
						);
				}
			}
		);

	
		JSONRegistry.register (
			"core.srp.SRPPackets$PacketInit_ClientPreAutheticationInitialization",
			new CallSingle<JSONObject,Object> () {
				@Override
				public JSONObject invoke(Object v) throws Exception
				{
					JSONObject object = new JSONObject();
					
					SRPPackets.PacketInit_ClientPreAutheticationInitialization p = (SRPPackets.PacketInit_ClientPreAutheticationInitialization)v;
					object.put("version", p.version);
					object.put("user", p.user);
					object.put("s", new String(Base64.encode(p.s)));	
					object.put("v", new String(Base64.encode(p.v)));	

					if (p.extra != null)
						object.put("extra", new String(Base64.encode(p.extra)));	
		
					return object;
				}
			},
			new CallSingle<Object,JSONObject> () {
				@Override
				public Object invoke(JSONObject v) throws Exception
				{
					SRPPackets.PacketInit_ClientPreAutheticationInitialization p =
						new SRPPackets.PacketInit_ClientPreAutheticationInitialization(
							v.getString("version"),
							v.getString("user"),
							Base64.decode(v.getString("v")),
							Base64.decode(v.getString("s"))
						);
					
					if (v.has("extra"))
						p.extra = Base64.decode(v.getString("extra"));
							
					return p;
				}
			}
		);
	}
}
