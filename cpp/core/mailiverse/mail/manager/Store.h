/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_Store_h__
#define __mailiverse_mail_manager_Store_h__

#include "mailiverse/core/connector/StoreConnectorEncrypted.h"
#include "mailiverse/core/connector/AsyncStoreConnectorSynth.h"
#include "../model/Original.h"
#include "Servent.h"

namespace mailiverse {
namespace mail {
namespace manager {

class Store : public Servent
{
protected:
	core::connector::AsyncStoreConnectorPtr connector;

public:
	Store(core::crypt::Cryptor *cryptor, core::connector::StoreConnector *connector)
	{
		this->connector = 
			new core::connector::AsyncStoreConnectorSynth(
				new core::connector::StoreConnectorEncrypted(cryptor, connector)
			);
	}

	model::Original *get(String path);

	core::connector::AsyncStoreConnector *getConnector()
	{
		return connector;
	}
};

DECLARE_SMARTPTR(Store);

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_manager_Store_h__ */
