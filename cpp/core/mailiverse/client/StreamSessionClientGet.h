/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_client_StreamSessionClientGet_h__
#define __mailiverse_client_StreamSessionClientGet_h__

#include "mailiverse/core/streams/StreamSession.h"

namespace mailiverse {
namespace client {

class StreamSessionClientGet : public core::streams::StreamSession
{
public:
	typedef core::streams::StreamSession Super;

	struct Delegate {
		virtual ~Delegate() {}
		virtual void onSuccess (const Block &packet) throws_ (Exception) = 0;
		virtual void onFailure (const Exception &exception) throws_ (Exception) = 0;
	} ;
	
	DECLARE_SMARTPTR(Delegate);

protected:
	DelegatePtr delegate;
	
public:
	StreamSessionClientGet (StreamSession *sender);
	virtual ~StreamSessionClientGet ();
	
	void setDelegate (Delegate *delegate);
	
	virtual void start () throws_ (Exception);	
	virtual void handlePacket (const Packet &packet) throws_ (Exception);
	virtual void handleException (const Exception &exception);
} ;

DECLARE_SMARTPTR(StreamSessionClientGet);

} // namespace
} // namespace

#endif
