/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "StoreConnectorInfo.h"
#include "mailiverse/utilities/Log.h"

using namespace mailiverse::core::connector;

StoreConnectorInfo::StoreConnectorInfo()
{
	LogDebug(mailiverse::core::connector, "StoreConnectorInfo");
}

StoreConnectorInfo::~StoreConnectorInfo()
{
	LogDebug(mailiverse::core::connector, "~StoreConnectorInfo");
}