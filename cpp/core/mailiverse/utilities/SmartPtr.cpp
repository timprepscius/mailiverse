/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "SmartPtr.h"

using namespace mailiverse::utilities;

SmartPtrBase::Items SmartPtrBase::items;
SmartPtrBase::Weaks SmartPtrBase::weaks;
Mutex SmartPtrBase::m;

