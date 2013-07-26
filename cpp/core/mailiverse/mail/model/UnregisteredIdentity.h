/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef UNREGISTEREDIDENTITY_H_
#define UNREGISTEREDIDENTITY_H_

#include "Identity.h"

namespace mailiverse {
namespace mail {
namespace model {

class UnregisteredIdentity : public Identity
{
public:
	UnregisteredIdentity(const std::string &full) :
		Identity(full) {}
		
	UnregisteredIdentity (const String &_name, const String &_email) :
		Identity(_name, _email) {}
	
	virtual ~UnregisteredIdentity() {}
};

DECLARE_SMARTPTR(UnregisteredIdentity);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* UNREGISTEREDIDENTITY_H_ */
