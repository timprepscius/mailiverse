/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_ArrivalsMonitor_h__
#define __mailiverse_mail_manager_ArrivalsMonitor_h__

#include "Servent.h"
#include "mailiverse/utilities/SmartPtr.h"

namespace mailiverse {
namespace mail {
namespace manager {

class ArrivalsMonitor : public Servent
{
protected:
	static const int NUM_PROCESSED_FLUSH = 10;

public:
	virtual ~ArrivalsMonitor() {}

	virtual void check() = 0;
	virtual bool isChecking() = 0;
};

DECLARE_SMARTPTR(ArrivalsMonitor);

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_manager_ArrivalsMonitor_h__ */
