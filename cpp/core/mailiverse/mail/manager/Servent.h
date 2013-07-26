/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_Servent_h__
#define __mailiverse_mail_manager_Servent_h__

namespace mailiverse {
namespace mail {
namespace manager {

class Master;

class Servent
{
protected:
	Master *master;

public:
	Servent() :	master(0) {}

	virtual ~Servent() {}

	void setMaster (Master *_master)
	{
		master = _master;
	}

	Master *getMaster ()
	{
		return master;
	}
};

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_manager_Servent_h__ */
