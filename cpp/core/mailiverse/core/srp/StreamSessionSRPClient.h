/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_srp_StreamSessionSRPClient_h__
#define __mailiverse_core_srp_StreamSessionSRPClient_h__

#include "../streams/StreamSession.h"
#include "../crypt/KeyPairFromPassword.h"
#include "../srp/SRPClientSession.h"

namespace mailiverse {
namespace core {
namespace srp {

class StreamSessionSRPClient : public streams::StreamSession
{
public:
	typedef streams::StreamSession Super;
	
protected:
	std::string name;
	crypt::KeyPairFromPasswordPtr keys;
	int step;
	
	SRPClientSessionPtr srp;
	crypt::CryptorPtr cryptor;

public:
	StreamSessionSRPClient (streams::StreamSession *sender, const std::string &name, crypt::KeyPairFromPassword *keys);
	virtual ~StreamSessionSRPClient ();
	
	virtual void handlePacket (const Packet &packet) throws_ (Exception);
	virtual void respondWith (const Packet &packet);
	virtual void start () throws_ (Exception);
} ;

} // namespace
} // namespace
} // namespace

#endif
