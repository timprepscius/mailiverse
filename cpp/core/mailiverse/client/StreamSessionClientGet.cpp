/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "StreamSessionClientGet.h"
#include "mailiverse/utilities/Json.h"
#include "mailiverse/utilities/Log.h"
#include "mailiverse/core/constants/ConstantsPushNotifications.h"

using namespace mailiverse::client;
using namespace mailiverse::core::constants;
using namespace mailiverse::core::streams;
using namespace mailiverse::utilities;

StreamSessionClientGet::StreamSessionClientGet (StreamSession *sender) :
	Super(sender),
	delegate(0)
{
	LogDebug(mailiverse::mail,"StreamSessionClientGet");
}

StreamSessionClientGet::~StreamSessionClientGet ()
{
	LogDebug(mailiverse::mail,"~StreamSessionClientGet");
}

void StreamSessionClientGet::setDelegate (Delegate *_delegate)
{
	delegate = _delegate;
}


void StreamSessionClientGet::handlePacket (const Packet &packet) throws_ (Exception)
{
	setFinished();
	delegate->onSuccess(packet);
}

void StreamSessionClientGet::handleException(const mailiverse::Exception &exception)
{
	setFinished();
	delegate->onFailure(exception);
}

void StreamSessionClientGet::start () throws_ (Exception)
{
	json::Object o;
	respondWith(toBlock(serializeJson("core.client.messages.Get",o)));
}
