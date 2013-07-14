/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.srp;

import java.math.BigInteger;

import core.callback.Callback;
import core.callback.CallbackDefault;


public class SRPPackets
{
	public static void register () { }
	static { 
		SRPPacketSerializerJSON.register(); 
	}
	
	public static class PacketInit_ClientTestCreate
	{
		public String version;
		public String user;
		
		public PacketInit_ClientTestCreate(String version, String user)
		{
			this.version = version;
			this.user = user;
		}
	}

	public static class PacketInit_ClientPreAutheticationInitialization
	{
		public String user;
		public byte[] v;
		public byte[] s;
		public byte[] extra;
		public String version;
		
		public PacketInit_ClientPreAutheticationInitialization(String version, String user, byte[] v, byte[] s)
		{
			this.version = version;
			this.user = user;
			this.v = v;
			this.s = s;
			extra = null;
		}
	}
	
	public static class PacketInit_EncryptedPacket
	{
		public byte[] encryptedBlock;
		
		public PacketInit_EncryptedPacket (byte[] encryptedBlock)
		{
			this.encryptedBlock = encryptedBlock;
		}
		
		public static Callback wrap_()
		{
			return new CallbackDefault() {
				@Override
				public void onSuccess(Object... arguments) throws Exception {
					callback.invoke(new PacketInit_EncryptedPacket((byte[])(arguments[0])));
				}
			};
		}
		
		public static Callback unwrap_()
		{
			return new CallbackDefault() {
				@Override
				public void onSuccess(Object... arguments) throws Exception {
					PacketInit_EncryptedPacket packet = (PacketInit_EncryptedPacket)(arguments[0]);
					callback.invoke(packet.encryptedBlock);
				}
			};
		}
	}
	
	public static class PacketInit_ServerResponse
	{
		public boolean succeeded;
		public String reason;
		
		public PacketInit_ServerResponse(boolean succeeded, String reason)
		{
			this.succeeded = succeeded;
			this.reason = reason;
		}
	}

	public static class Packet1_ClientSendsHello
	{
		public String user;
		public String version;
		
		public Packet1_ClientSendsHello (String user, String version)
		{
			this.user = user;
			this.version = version;
		}
	}
	
	public static class Packet2_ServerSendSalt
	{
		public byte[] salt;
		public String version;
		
		public Packet2_ServerSendSalt (String version, byte[] salt)
		{
			this.salt = salt;
			this.version = version;
		}
		
		public byte[] getSalt ()
		{
			return salt;
		}
	}
	
	public static class Packet3_ClientSendPublicKey
	{
		public byte[] publicKey;

		public Packet3_ClientSendPublicKey (byte[] publicKey)
		{
			this.publicKey = publicKey;
		}

		public byte[] getPublicKey ()
		{
			return publicKey;
		}
	}
	
	public static class Packet4_ServerSendPublicKey
	{
		public byte[] publicKey;
		
		public Packet4_ServerSendPublicKey (BigInteger publicKey)
		{
			this.publicKey = publicKey.toByteArray();
		}

		public byte[] getPublicKey ()
		{
			return publicKey;
		}
	}
	
	public static class Packet5_ClientSendEvidence
	{
		public byte[] evidence;

		public Packet5_ClientSendEvidence (byte[] evidence)
		{
			this.evidence = evidence;
		}

		public byte[] getEvidence ()
		{
			return evidence;
		}
	}
	
	public static class Packet6_ServerSendEvidenceAndPayload
	{
		public byte[] evidence;

		public Packet6_ServerSendEvidenceAndPayload (BigInteger evidence)
		{
			this.evidence = evidence.toByteArray();
		}
		
		public byte[] getEvidence ()
		{
			return evidence;
		}
	}
}
