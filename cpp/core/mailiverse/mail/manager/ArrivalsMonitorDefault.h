/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef ARRIVALSMONITORDEFAULT_H_
#define ARRIVALSMONITORDEFAULT_H_

#include "mailiverse/Types.h"
#include "ArrivalsMonitor.h"
#include <mailiverse/utilities/Monitor.h>
#include "mailiverse/core/crypt/Cryptor.h"
#include "mailiverse/core/connector/StoreConnector.h"
#include "Direction.h"
#include "mailiverse/utilities/Thread.h"

namespace mailiverse {
namespace mail {
namespace manager {

class ArrivalsMonitorDefault : public ArrivalsMonitor
{
protected:
	struct Listing {
		Direction::Enum direction;
		core::connector::FileInfo file;
	};

	struct ListingComparator {
		typedef Listing compare_type;
		bool operator ()(const Listing &lhs, const Listing &rhs) const {
			return lhs.file.date < rhs.file.date;
		}
	};

	Vector<Listing> getListing();

protected:
	bool finished;

	core::connector::StoreConnectorPtr store;
	utilities::EmptyMonitor action;
	core::connector::Lock checkMailLock;

	bool checking;
	void doCheck ();
	void onUpdateFinished();
	void onUpdateFailed (const Exception &e);

public:
	ArrivalsMonitorDefault(core::crypt::Cryptor *cryptor, core::connector::StoreConnector *connector);
	virtual ~ArrivalsMonitorDefault();
	
	void markFinished ();

	virtual void check() override;
	virtual bool isChecking () override;
};

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* ARRIVALSMONITORDEFAULT_H_ */
