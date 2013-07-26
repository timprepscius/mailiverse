/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "InternalResource.h"
#include "../crypt/Base64.h"

using namespace mailiverse::core::util;
using namespace mailiverse::core::crypt;
using namespace mailiverse::core;
using namespace mailiverse;

Block InternalResource::get(const String &package, const String &subKey)
{
	String key = package + "." + subKey;
	
	if (package=="mailiverse::mail::manager::Mailer" && subKey == "truststore.jks")
	{
		return toBlock(
			"YOUR TRUST STORE"
		);
	}
	if (package=="mailiverse::mail::manager::Notifications" && subKey == "truststore.jks")
	{
		return toBlock (
			"YOUR TRUST STORE"
		);
	}
	
	return Block();
}