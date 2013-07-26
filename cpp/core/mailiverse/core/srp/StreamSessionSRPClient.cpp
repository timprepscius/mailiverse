/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "StreamSessionSRPClient.h"
#include "SRPPackets.h"
#include "SRPPacketSerializer.h"
#include "SRPFactory.h"
#include "../crypt/CryptorAES.h"
#include "mailiverse/utilities/Log.h"

#include "../streams/IO.h"

using namespace mailiverse::core::srp;
using namespace mailiverse::core::crypt;
using namespace mailiverse::core;
using namespace mailiverse::utilities;

StreamSessionSRPClient::StreamSessionSRPClient (
	streams::StreamSession *_session,
	const std::string &_name, 
	crypt::KeyPairFromPassword *_keys 
) :
	Super(_session),
	name(_name),
	keys(_keys),
	step(0)
{
	srp = SRPFactory::getInstance()->newClientSession(keys->getVerifier().secretKey);
}


StreamSessionSRPClient::~StreamSessionSRPClient () 
{

}

void StreamSessionSRPClient::handlePacket (const Packet &packet) throws_ (Exception)
{
	switch (step++)
	{
		case 0:
		{
			SRPPacketPtr p = SRPPacketSerializer::deserialize(packet);
			packets::Packet2_ServerSendSalt *in = dynamic_cast<packets::Packet2_ServerSendSalt *>((SRPPacket *)p);
			if (!in)
				throw Exception("deserialization failed");
				
			srp->setSalt_s(in->salt);
			
			packets::Packet3_ClientSendPublicKey out;
			out.publicKey = srp->getPublicKey_A();
			
			Super::respondWith(SRPPacketSerializer::serialize(&out));
		}
		break;
		case 1:
		{
			SRPPacketPtr p = SRPPacketSerializer::deserialize(packet);
			packets::Packet4_ServerSendPublicKey *in = dynamic_cast<packets::Packet4_ServerSendPublicKey *>((SRPPacket *)p);
			if (!in)
				throw Exception("deserialization failed");
		
			srp->setServerPublicKey_B(in->publicKey);
			
			packets::Packet5_ClientSendEvidence out;
			out.evidence = srp->getEvidenceValue_M1();
			
			Super::respondWith(SRPPacketSerializer::serialize(&out));
		}
		break;
		case 2:
		{
			SRPPacketPtr p = SRPPacketSerializer::deserialize(packet);
			packets::Packet6_ServerSendEvidence *in = dynamic_cast<packets::Packet6_ServerSendEvidence *>((SRPPacket *)p);
			if (!in)
				throw Exception("deserialization failed");
				
			srp->validateServerEvidenceValue_M2(in->evidence);
			cryptor = new CryptorAES(srp->getSessionKey_K());
			
			if (getReciever())
				getReciever()->start();
		}
		break;
		
		default:
		{
			if (!cryptor)
				throw Exception("Cryptor uninitialized");
				
			getReciever()->handlePacket(cryptor->decrypt(packet));
		}
		break;
	}
}

void StreamSessionSRPClient::respondWith(const Packet &packet)
{
	if (!cryptor)
		throw Exception("Cryptor uninitialized");

	LogDebug(mailiverse::core::srp,"encrypting " << toString(packet));

	Super::respondWith(cryptor->encrypt(packet));
}

void StreamSessionSRPClient::start () throws_ (Exception)
{
	packets::Packet1_ClientSendsHello hello;
	hello.user = name;
	hello.version = "1.0";
	
	Super::respondWith(SRPPacketSerializer::serialize(&hello));
}

