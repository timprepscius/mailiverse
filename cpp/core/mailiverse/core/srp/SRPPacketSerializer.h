/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_srp_SRPPacketSerializer_h__
#define __mailiverse_core_srp_SRPPacketSerializer_h__

#include "mailiverse/core/Types.h"
#include <string>
#include "SRPPackets.h"

namespace mailiverse {
namespace core {
namespace srp {

class SRPPacketSerializer
{
public:
	static SRPPacketPtr deserialize (const Packet &packet);
	static Packet serialize (SRPPacket *);
} ;


class SRPPacketSerializerJSON
{
protected:
	static PacketType decipherTypeFor (const std::string &className);

public:
	static SRPPacketPtr deserialize (const Packet &packet);
	static Packet serialize (SRPPacket *);
} ;

} // namespace
} // namespace
} // namespace

#endif
