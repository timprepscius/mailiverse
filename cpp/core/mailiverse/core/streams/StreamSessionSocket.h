/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_streams_StreamSessionSocket_h__
#define __mailiverse_core_streams_StreamSessionSocket_h__

#include "StreamSession.h"
#include "StreamSocket.h"
#include "mailiverse/utilities/Log.h"

namespace mailiverse {
namespace core {
namespace streams {

template<typename T>
class StreamSessionSocket : public StreamSession
{
public:
	typedef StreamSession Super;

protected:
	utilities::SmartPtr<T> socket;
	
public:
	StreamSessionSocket (T *_socket) :
		Super(0),
		socket(_socket)
	{
		LogDebug(mailiverse::core::streams, "StreamSessionSocket");
	}

	virtual ~StreamSessionSocket ()
	{
		LogDebug(mailiverse::core::streams, "~StreamSessionSocket");
	}	
	
	virtual void respondWith (const Packet &packet) throws_ (Exception)
	{
		socket->write(packet);
		LogDebug(mailiverse::core::streams::StreamSessionSocket, "SEND: " << utilities::toString(packet));
	}
	
	virtual void run () throws_ (Exception)
	{
		start();

		while (!isFinished())
		{
			try 
			{
				Packet packet;
				socket->read(packet);
				LogDebug(mailiverse::core::streams::StreamSessionSocket, "READ: " << utilities::toString(packet));
				
				handlePacket(packet);
			}
			catch (const Exception &e)
			{
				handleException(e);
			}
		}
	}

} ;

} // namespace
} // namespace
} // namespace

#endif
