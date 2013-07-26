/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include <iostream>

#include "AsyncActions.h"

using namespace mailiverse::mail::manager;
using namespace mailiverse::mail;
using namespace mailiverse;

void AsyncActions::possiblyFlush ()
{
	// scope the reader
	{
		ActMonitor::Reader reader(actMonitor);
		if (!reader->empty())
			return;
			
		if (!getMaster()->getCacheManager()->shouldFlush())
			return;
	}
	
	Super::flush();
}

void AsyncActions::enableNotifications (const String &_deviceId, core::util::Callback<> callback) 
{ 
	String deviceId = _deviceId;
	utilities::Binder *binder = 
		utilities::newbindC (
			this, 
			&Actions::doEnableNotifications, 
			deviceId
		);
		
	Act act(
		core::util::Callback<>(binder, NULL),
		callback
	);
	
	ActMonitor::Writer writer(actMonitor);
	writer->push_back(act);
	actMonitor.signal();
}
