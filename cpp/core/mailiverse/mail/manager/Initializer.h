/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef INITIALIZER_H_
#define INITIALIZER_H_

#include "Servent.h"
#include "mailiverse/utilities/SmartPtr.h"

namespace mailiverse {
namespace mail {
namespace manager {

class Initializer : public Servent
{
protected:
	bool ready;

protected:
	void onFirstRun();
	void onCacheInitialized ();
	void onCacheFailed ();
	void onFolderLoadComplete ();
	void onInitialized();
	void onSettingsLoaded();

public:
	Initializer();
	virtual ~Initializer();

	void start();

	bool isReady ();
};

DECLARE_SMARTPTR(Initializer);

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* INITIALIZER_H_ */
