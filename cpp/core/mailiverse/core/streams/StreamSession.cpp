/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "StreamSession.h"
#include "mailiverse/utilities/Log.h"

using namespace mailiverse::core::streams;

StreamSession::StreamSession (StreamSession *_sender) :
	finished(false),
	sender(_sender)
{
	LogDebug(mailiverse::core::streams, "StreamSession");
	
	if (sender)
		sender->receiver = this;
}

StreamSession::~StreamSession ()
{
	LogDebug(mailiverse::core::streams, "~StreamSession");
}

void StreamSession::respondWith(const Packet &packet)
{
	getSender()->respondWith(packet);
}

void StreamSession::handlePacket (const Packet &packet) throws_ (Exception)
{
	getReciever()->handlePacket(packet);

}
void StreamSession::handleException (const Exception &e)
{
	getReciever()->handleException(e);
}

void StreamSession::start () throws_ (Exception)
{
	getReciever()->start();
}

void StreamSession::run () throws_ (Exception)
{
	getSender()->run();
}
