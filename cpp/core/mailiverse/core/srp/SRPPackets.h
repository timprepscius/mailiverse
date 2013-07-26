/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_srp_SRPPackets_h__
#define __mailiverse_core_srp_SRPPackets_h__

#include "../Types.h"
#include "mailiverse/utilities/SmartPtr.h"
#include <string>

namespace mailiverse {
namespace core {
namespace srp {

enum PacketType
{
	E_Packet1_ClientSendsHello,
	E_Packet2_ServerSendSalt,
	E_Packet3_ClientSendPublicKey,
	E_Packet4_ServerSendPublicKey,
	E_Packet5_ClientSendEvidence,
	E_Packet6_ServerSendEvidence,
	E_PacketInit_ClientPreAutheticationInitialization,
	E_PacketInit_ClientTestCreate,
	E_PacketInit_EncryptedPacket,
	E_PacketInit_ServerResponse,
	E_Packet_None = -1
} ;


struct SRPPacket
{
	PacketType type;

	SRPPacket(PacketType type);
	virtual ~SRPPacket();
} ;

DECLARE_SMARTPTR(SRPPacket);

namespace packets {

struct PacketInit_ClientTestCreate : public SRPPacket
{
	static const char *CLASS_NAME;

	PacketInit_ClientTestCreate() : 
		SRPPacket(E_PacketInit_ClientTestCreate)
	{}

	std::string version;
	std::string user;
} ;

struct PacketInit_ClientPreAutheticationInitialization : public SRPPacket
{
	static const char *CLASS_NAME;

	PacketInit_ClientPreAutheticationInitialization() : 
		SRPPacket(E_PacketInit_ClientPreAutheticationInitialization) 
	{}

	std::string user;
	Key v;
	Key s;
	Block extra;
	std::string version;
} ;

struct PacketInit_EncryptedPacket : public SRPPacket
{
	static const char *CLASS_NAME;

	PacketInit_EncryptedPacket() : 
		SRPPacket(E_PacketInit_EncryptedPacket) 
	{}

	Block encryptedBlock;
} ;

struct PacketInit_ServerResponse : public SRPPacket
{
	static const char *CLASS_NAME;

	PacketInit_ServerResponse() : 
		SRPPacket(E_PacketInit_ServerResponse) 
	{}

	bool succeeded;
	std::string reason;
} ;

struct Packet1_ClientSendsHello : public SRPPacket
{
	static const char *CLASS_NAME;

	Packet1_ClientSendsHello() : 
		SRPPacket(E_Packet1_ClientSendsHello) 
	{}

	std::string user;
	std::string version;
} ;

struct Packet2_ServerSendSalt : public SRPPacket
{
	static const char *CLASS_NAME;

	Packet2_ServerSendSalt() : 
		SRPPacket(E_Packet2_ServerSendSalt) 
	{}

	BigInteger salt;
	std::string version;
} ;

struct Packet3_ClientSendPublicKey : public SRPPacket
{
	static const char *CLASS_NAME;

	Packet3_ClientSendPublicKey() : 
		SRPPacket(E_Packet3_ClientSendPublicKey) 
	{}

	BigInteger publicKey;
} ;

struct Packet4_ServerSendPublicKey : public SRPPacket
{
	static const char *CLASS_NAME;

	Packet4_ServerSendPublicKey() : 
		SRPPacket(E_Packet4_ServerSendPublicKey) 
	{}

	BigInteger publicKey;
} ;

struct Packet5_ClientSendEvidence : public SRPPacket
{
	static const char *CLASS_NAME;

	Packet5_ClientSendEvidence() : 
		SRPPacket(E_Packet5_ClientSendEvidence) 
	{}

	BigInteger evidence;
} ;

struct Packet6_ServerSendEvidence : public SRPPacket
{
	static const char *CLASS_NAME;

	Packet6_ServerSendEvidence() : 
		SRPPacket(E_Packet6_ServerSendEvidence) 
	{}

	BigInteger evidence;
} ;

} // namespace
} // namespace
} // namespace
} // namespace

#endif
