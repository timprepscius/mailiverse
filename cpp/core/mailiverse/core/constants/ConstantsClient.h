/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_constants_ConstantsClient_h__
#define __mailiverse_core_constants_ConstantsClient_h__

#include "mailiverse/Types.h"

namespace mailiverse {
namespace core {
namespace constants {

struct ConstantsClient 
{
	static const String	
		SERVER,
		WEB_SERVER_TOMCAT,
		
		KEY_AUTH_HOST,
		MAIL_AUTH_HOST, 
		
		AT_HOST;
		
		
	static const int
		KEY_AUTH_PORT,
		MAIL_AUTH_PORT;
} ;

} // namespace
} // namespace
} // namespace

#endif
