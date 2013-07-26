/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_store_StoreConnectorFactory_h__
#define __mailiverse_core_store_StoreConnectorFactory_h__

#include "Environment.h"
#include "mailiverse/core/connector/StoreConnector.h"

namespace mailiverse {
namespace core {
namespace store {

class StoreConnectorFactory
{
public:
	static connector::StoreConnectorPtr create (const Environment &e) throws_ (Exception);
} ;

} // namespace
} // namespace
} // namespace

#endif