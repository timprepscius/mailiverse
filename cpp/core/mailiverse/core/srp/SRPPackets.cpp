/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "SRPPackets.h"

using namespace mailiverse::core::srp;
using namespace mailiverse::core::srp::packets;

SRPPacket::SRPPacket(PacketType _type) :
	type(_type)
{
}

SRPPacket::~SRPPacket()
{

}

const char *PacketInit_ClientTestCreate::CLASS_NAME = "core.srp.SRPPackets$PacketInit_ClientTestCreate";
const char *PacketInit_ClientPreAutheticationInitialization::CLASS_NAME = "core.srp.SRPPackets$PacketInit_ClientPreAutheticationInitialization";
const char *PacketInit_EncryptedPacket::CLASS_NAME = "core.srp.SRPPackets$PacketInit_EncryptedPacket";
const char *PacketInit_ServerResponse::CLASS_NAME = "core.srp.SRPPackets$PacketInit_ServerResponse";
const char *Packet1_ClientSendsHello::CLASS_NAME = "core.srp.SRPPackets$Packet1_ClientSendsHello";
const char *Packet2_ServerSendSalt::CLASS_NAME = "core.srp.SRPPackets$Packet2_ServerSendSalt";
const char *Packet3_ClientSendPublicKey::CLASS_NAME = "core.srp.SRPPackets$Packet3_ClientSendPublicKey";
const char *Packet4_ServerSendPublicKey::CLASS_NAME = "core.srp.SRPPackets$Packet4_ServerSendPublicKey";
const char *Packet5_ClientSendEvidence::CLASS_NAME = "core.srp.SRPPackets$Packet5_ClientSendEvidence";
const char *Packet6_ServerSendEvidence::CLASS_NAME = "core.srp.SRPPackets$Packet6_ServerSendEvidenceAndPayload";
