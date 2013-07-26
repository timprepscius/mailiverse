/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Authenticator.h"
#include "mailiverse/core/streams/StreamSessionSocket.h"
#include "mailiverse/core/crypt/KeyPairFromPassword.h"
#include "mailiverse/core/srp/StreamSessionSRPClient.h"
#include "StreamSessionClientGet.h"
#include "mailiverse/utilities/Json.h"
#include "mailiverse/core/BlockCompression.h"
#include "mailiverse/utilities/Log.h"
#include "mailiverse/core/streams/StreamLineSocket.h"
#include "mailiverse/core/constants/ConstantsClient.h"

using namespace mailiverse::client;
using namespace mailiverse::core::crypt;
using namespace mailiverse::core::srp;
using namespace mailiverse::core::streams;
using namespace mailiverse::core::store;
using namespace mailiverse::utilities;
using namespace mailiverse::core::constants;
using namespace mailiverse::core;
using namespace mailiverse;

Authenticator::Delegate::~Delegate ()
{
	LogDebug(mailiverse::mail,"Authenticator::~Delegate");
}

Authenticator::Authenticator () :
	delegate(0)
{
	LogDebug(mailiverse::mail,"Authenticator");
}

Authenticator::~Authenticator ()
{
	LogDebug(mailiverse::mail,"~Authenticator");
}

void Authenticator::setDelegate (Delegate *_delegate)
{
	delegate = _delegate;
}

void Authenticator::authenticate (const String &user, const String &password)
{
	AuthenticatorPtr preventDeletion = this;
	
	try
	{
		boost::asio::io_service io;

		if (delegate)
			delegate->onStep("Generating keys");

		keyPair = new KeyPairFromPassword(password, true);

		if (delegate)
			delegate->onStep("Connecting to server");

		std::string host = ConstantsClient::KEY_AUTH_HOST;
		std::string port = toString(ConstantsClient::KEY_AUTH_PORT);
		
		StreamLineSocketPtr socket = new StreamLineSocket(io, host, port);
		StreamSessionPtr streamSessionSocket = new StreamSessionSocket<StreamLineSocket>(socket);
		
		if (delegate)
			delegate->onStep("Generating protocol");
		
		StreamSessionClientGetPtr client = new
			StreamSessionClientGet (
				new StreamSessionSRPClient (
					streamSessionSocket,
					user, keyPair
				)
			);
			
		if (delegate)
			delegate->onStep("Logging in");

		client->setDelegate(this);
		client->run();
	}
	catch (Exception &e)
	{
		if (delegate)
			delegate->onFailure(e);
	}
	catch (...)
	{
		if (delegate)
			delegate->onFailure(Exception("unknown exception"));
	}
}

void Authenticator::onSuccess (const Block &packet) throws_ (Exception)
{
	try
	{
		JsonDeserialization packetDeserialization = deserializeJson(toString(packet));
		 
		json::Object &ebo = packetDeserialization.second;
		Block eb = toBlockFromBase64((std::string)(json::String)ebo["block"]);
		Block b = inflate(keyPair->Cryptor::decrypt(eb));
		
		LogDebug(mailiverse::client::Authenticator, toString(b));
		JsonDeserialization blockDeserialization = deserializeJson(toString(b));
		json::Object &o = blockDeserialization.second;
		
		EnvironmentPtr e = new Environment();
		for (json::Object::iterator i=o.Begin(); i!=o.End(); ++i)
		{
			LogDebug(mailiverse::mail::Authenticator, i->name << ":" << (std::string)(json::String)i->element << std::endl);
			e->put(i->name, (json::String)i->element);
		}
		
		if (delegate)
			delegate->onStep("Retrieving mailbox");
		
		if (delegate)
			delegate->onSuccess(e);
	}
	catch (const Exception &e)
	{
		throw e;
	}
	catch (const std::exception &e)
	{
		throw Exception(e.what());
	}
	catch (...)
	{
		throw Exception ("Unknown exception");
	}
}

void Authenticator::onFailure (const Exception &exception) throws_ (Exception)
{
	LogDebug(mailiverse::client::Authenticator, exception.what());
	
	if (delegate)
		delegate->onFailure(exception);
}

