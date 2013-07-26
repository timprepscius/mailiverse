/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "SRPPacketSerializer.h"
#include "SRPPackets.h"
#include "mailiverse/utilities/Json.h"
#include "mailiverse/utilities/Log.h"

#include <botan/botan.h>

using namespace mailiverse::core::srp;
using namespace mailiverse::core;
using namespace mailiverse;
using namespace mailiverse::utilities;


SRPPacketPtr SRPPacketSerializer::deserialize (const Packet &packet)
{
	return SRPPacketSerializerJSON::deserialize(packet);
}

Packet SRPPacketSerializer::serialize (SRPPacket *packet)
{
	return SRPPacketSerializerJSON::serialize(packet);
}

PacketType SRPPacketSerializerJSON::decipherTypeFor(const std::string &className)
{
	if (className == packets::PacketInit_ClientTestCreate::CLASS_NAME)
		return PacketType::E_PacketInit_ClientTestCreate;

	if (className == packets::PacketInit_ClientPreAutheticationInitialization::CLASS_NAME)
		return PacketType::E_PacketInit_ClientPreAutheticationInitialization;

	if (className == packets::PacketInit_EncryptedPacket::CLASS_NAME)
		return PacketType::E_PacketInit_EncryptedPacket;

	if (className == packets::PacketInit_ServerResponse::CLASS_NAME)
		return PacketType::E_PacketInit_ServerResponse;

	if (className == packets::Packet1_ClientSendsHello::CLASS_NAME)
		return PacketType::E_Packet1_ClientSendsHello;

	if (className == packets::Packet2_ServerSendSalt::CLASS_NAME)
		return PacketType::E_Packet2_ServerSendSalt;

	if (className == packets::Packet3_ClientSendPublicKey::CLASS_NAME)
		return PacketType::E_Packet3_ClientSendPublicKey;

	if (className == packets::Packet4_ServerSendPublicKey::CLASS_NAME)
		return PacketType::E_Packet4_ServerSendPublicKey;

	if (className == packets::Packet5_ClientSendEvidence::CLASS_NAME)
		return PacketType::E_Packet5_ClientSendEvidence;

	if (className == packets::Packet6_ServerSendEvidence::CLASS_NAME)
		return PacketType::E_Packet6_ServerSendEvidence;

	return PacketType::E_Packet_None;
}

SRPPacketPtr SRPPacketSerializerJSON::deserialize(const Packet &packet)
{
	JsonDeserialization o = deserializeJson(toString(packet));
	std::string &className = o.first;
	json::Object &object = o.second;

	if (o.first == "null")
		return NULL;

	PacketType type = decipherTypeFor (className);
	LogDebug(mailiverse::core::crypt::srp, "SRPPacketSerializerJSON::deserialize " << toString(object));

	SRPPacketPtr result;
	switch (type)
	{
		case PacketType::E_Packet1_ClientSendsHello:
		{
			packets::Packet1_ClientSendsHello *p;
			result = p = new packets::Packet1_ClientSendsHello();
			
			p->user = (json::String)object["user"];
			p->version = (json::String)object["version"];
		}
		break;
		case PacketType::E_Packet2_ServerSendSalt:
		{
			packets::Packet2_ServerSendSalt *p;
			result = p = new packets::Packet2_ServerSendSalt();
			
			p->salt = toBigInteger(toBlockFromBase64(toBlock((json::String)object["salt"])));
			p->version = (json::String)object["version"];
		}
		break;
		
		case PacketType::E_Packet3_ClientSendPublicKey:
		{
			packets::Packet3_ClientSendPublicKey *p;
			result = p = new packets::Packet3_ClientSendPublicKey();
			
			p->publicKey = toBigInteger(toBlockFromBase64(toBlock((json::String)object["publicKey"])));
		}
		break;

		case PacketType::E_Packet4_ServerSendPublicKey:
		{
			packets::Packet4_ServerSendPublicKey *p;
			result = p = new packets::Packet4_ServerSendPublicKey();
			
			p->publicKey = toBigInteger(toBlockFromBase64(toBlock((json::String)object["publicKey"])));
		}
		break;

		case PacketType::E_Packet5_ClientSendEvidence:
		{
			packets::Packet5_ClientSendEvidence *p;
			result = p = new packets::Packet5_ClientSendEvidence();
			
			p->evidence = toBigInteger(toBlockFromBase64(toBlock((json::String)object["evidence"])));
		}
		break;

		case PacketType::E_Packet6_ServerSendEvidence:
		{
			packets::Packet6_ServerSendEvidence *p;
			result = p = new packets::Packet6_ServerSendEvidence();
			
			p->evidence = toBigInteger(toBlockFromBase64(toBlock((json::String)object["evidence"])));
		}
		break;		
		
		default:
			throw new Exception("Unable to deserialize packet");
	}
	
	return result;
}

Packet SRPPacketSerializerJSON::serialize(SRPPacket *packet)
{
	std::ostringstream oss;
	
	switch (packet->type)
	{
		case PacketType::E_Packet1_ClientSendsHello:
		{
			packets::Packet1_ClientSendsHello *p = (packets::Packet1_ClientSendsHello*)packet;
			json::Object o;
			o["user"] = json::String(p->user);
			o["version"] = json::String(p->version);
			
			oss << p->CLASS_NAME << "!";
			oss << o;
		};
		break;

		case PacketType::E_Packet2_ServerSendSalt:
		{
			packets::Packet2_ServerSendSalt *p = (packets::Packet2_ServerSendSalt*)packet;
			json::Object o;
			
			o["salt"] = json::String(toString(toBlockBase64(toBlock(p->salt))));
			oss << p->CLASS_NAME << "!";
			oss << o;
		};
		break;

		case PacketType::E_Packet3_ClientSendPublicKey:
		{
			packets::Packet3_ClientSendPublicKey *p = (packets::Packet3_ClientSendPublicKey*)packet;
			json::Object o;
			
			o["publicKey"] = json::String(toString(toBlockBase64(toBlock(p->publicKey))));
			oss << p->CLASS_NAME << "!";
			oss << o;
		};
		break;

		case PacketType::E_Packet4_ServerSendPublicKey:
		{
			packets::Packet4_ServerSendPublicKey *p = (packets::Packet4_ServerSendPublicKey*)packet;
			json::Object o;
			
			o["publicKey"] = json::String(toString(toBlockBase64(toBlock(p->publicKey))));
			oss << p->CLASS_NAME << "!";
			oss << o;
		};
		break;

		case PacketType::E_Packet5_ClientSendEvidence:
		{
			packets::Packet5_ClientSendEvidence *p = (packets::Packet5_ClientSendEvidence*)packet;
			json::Object o;
			
			o["evidence"] = json::String(toString(toBlockBase64(toBlock(p->evidence))));
			oss << p->CLASS_NAME << "!";
			oss << o;
		};
		break;

		case PacketType::E_Packet6_ServerSendEvidence:
		{
			packets::Packet6_ServerSendEvidence *p = (packets::Packet6_ServerSendEvidence*)packet;
			json::Object o;
			
			o["evidence"] = json::String(toString(toBlockBase64(toBlock(p->evidence))));
			oss << p->CLASS_NAME << "!";
			oss << o;
		};
		break;
		
		default:
			throw new Exception("Unable to serialize packet");
	}
				
	Block result = toBlock(oss.str());
	
	return result;
}
