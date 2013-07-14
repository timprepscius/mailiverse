/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.srp.server;

import java.math.BigInteger;

import core.client.messages.ClientMessagesSerializerJSON;
import core.crypt.CryptorRSAAES;
import core.exceptions.CryptoException;
import core.exceptions.PublicMessageException;
import core.io.IoChain;
import core.srp.SRPPackets;
import core.srp.SRPPackets.PacketInit_ClientTestCreate;
import core.util.LogOut;
import core.util.SimpleSerializer;
import core.util.Strings;
import core.util.Triple;


public class SRPServerUserSession extends IoChain
{
	static { SRPPackets.register(); ClientMessagesSerializerJSON.register(); }

	LogOut log = new LogOut(SRPServerUserSession.class);
	
	CryptorRSAAES cryptorRSA;
	boolean noMoreRoomForUsers = false;
	String userName;
	SRPServer srp = new SRPServer();
	int step = 0;
	SRPServerUserSessionDb db;
	
	public SRPServerUserSession (CryptorRSAAES cryptorRSA, SRPServerUserSessionDb db, IoChain session)
	{
		super(session);
		
		this.db = db;
		this.cryptorRSA = cryptorRSA;
	}
	
	public String getUserName ()
	{
		return userName;
	}

	@Override
	public void onReceive (byte[] bytes) throws Exception
	{
		try
		{
			if (step == 0)
			{
				Object object = SimpleSerializer.deserialize(bytes);
				if (object instanceof SRPPackets.PacketInit_EncryptedPacket)
				{
					step = -1;

					SRPPackets.PacketInit_EncryptedPacket
						block = (SRPPackets.PacketInit_EncryptedPacket)object;

					Object packet = SimpleSerializer.deserialize(cryptorRSA.decrypt(block.encryptedBlock));
					
					if (packet instanceof SRPPackets.PacketInit_ClientPreAutheticationInitialization)
					{
						step_init((SRPPackets.PacketInit_ClientPreAutheticationInitialization)packet);
						return;
					}
					else
					if (packet instanceof SRPPackets.PacketInit_ClientTestCreate)
					{
						step_testcreate((SRPPackets.PacketInit_ClientTestCreate)packet);
						return;
					}
				}
			}
				
			switch (step++)
			{
				case 0:
				{
					SRPPackets.Packet1_ClientSendsHello packet = SimpleSerializer.deserialize(bytes);
					this.userName = packet.user;
					db.rateLimitFailure(userName);
					
					Triple<String, BigInteger, BigInteger> vvs = db.getUserVVS(packet.user);
					sender.send(SimpleSerializer.serialize(
						srp.step1_getSalt_send(vvs.first, vvs.second, vvs.third)
					));
				} 
				break;
					
				case 1:
				{
					SRPPackets.Packet3_ClientSendPublicKey packet = SimpleSerializer.deserialize(bytes);
					sender.send(SimpleSerializer.serialize(srp.step2_receivePublicKey_generatePublicKey_send(packet)));
				}
				break;
					
				case 2:
				{
					SRPPackets.Packet5_ClientSendEvidence packet = SimpleSerializer.deserialize(bytes);
					SRPPackets.Packet6_ServerSendEvidenceAndPayload out = 
						srp.step3_validateClientEvidence_generateEvidence_send(packet);
				
					sender.send(SimpleSerializer.serialize(out));
					super.open();
				} 
				break;
					
				default:
					super.onReceive(srp.streamDecrypt(bytes));
					
			}
		}
		catch (CryptoException e)
		{
			db.markFailure(userName);
			throw e;
		}
		catch (PublicMessageException e)
		{
			db.markFailure(userName);
			throw e;
		}
		catch (Exception e)
		{
			db.markFailure(userName);
			throw new CryptoException (e);
		} 
	}
	
	public void step_testcreate(PacketInit_ClientTestCreate packet) throws Exception
	{
		try
		{
			db.testCreate (packet.version, packet.user);
			sender.send (SimpleSerializer.serialize(new SRPPackets.PacketInit_ServerResponse(true, null)));
		}
		catch (PublicMessageException e)
		{
			sender.send (SimpleSerializer.serialize(new SRPPackets.PacketInit_ServerResponse(false, e.message)));
		}
		catch (Exception e)
		{
			sender.send (SimpleSerializer.serialize(new SRPPackets.PacketInit_ServerResponse(false, null)));
		}
		stop();
	}

	public void step_init (SRPPackets.PacketInit_ClientPreAutheticationInitialization packet) throws Exception
	{
		try
		{
			db.createUser (packet.version, packet.user, new BigInteger(packet.v), new BigInteger(packet.s), packet.extra);
			sender.send (SimpleSerializer.serialize(new SRPPackets.PacketInit_ServerResponse(true, null)));
		}
		catch (PublicMessageException e)
		{
			sender.send (SimpleSerializer.serialize(new SRPPackets.PacketInit_ServerResponse(false, e.message)));
		}
		catch (Exception e)
		{
			sender.send (SimpleSerializer.serialize(new SRPPackets.PacketInit_ServerResponse(false, null)));
		}
		stop();
	}
	
	@Override
	public void send(byte[] packet) throws Exception 
	{
		sender.send(srp.streamEncrypt(packet));
	}

}
