/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_connector_Versioning_h__
#define __mailiverse_core_connector_Versioning_h__

#include <string>

namespace mailiverse {
namespace core {
namespace connector {

public class Versioning
{
	const std::string
		LOGIN = "1.0",
		CONFIGURATION = "1.0",
		CLIENT = "0.1";
} ;

} // namespace
} // namespace
} // namespace

#endif

