/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_streams_StreamSession_h__
#define __mailiverse_core_streams_StreamSession_h__

#include "mailiverse/core/Types.h"
#include "mailiverse/utilities/SmartPtr.h"

namespace mailiverse {
namespace core {
namespace streams {

class StreamSession;

class StreamSession 
{
private:
	bool finished;
	utilities::SmartPtr<StreamSession> sender;
	StreamSession *receiver;
	
public:
	StreamSession (StreamSession *sender);
	virtual ~StreamSession ();
	
	StreamSession *getReciever ()
	{
		return receiver;
	}
	
	StreamSession *getSender ()
	{
		return sender;
	}
	
	virtual void handlePacket (const Packet &packet) throws_ (Exception);
	virtual void handleException (const Exception &e);
	virtual void respondWith(const Packet &packet);
	virtual void start () throws_ (Exception);
	virtual void run () throws_ (Exception);

	void setFinished ()
	{
		finished = true;
		if (sender)
			sender->setFinished();
	}

	bool isFinished ()
	{
		return finished;
	}
} ;

DECLARE_SMARTPTR(StreamSession);

} // namespace
} // namespace
} // namespace

#endif