/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_FolderRepository_h__
#define __mailiverse_mail_model_FolderRepository_h__

#include "FolderSet.h"

namespace mailiverse {
namespace mail {
namespace model {

class FolderRepository : public FolderSet
{
	DECLARE_ITEM(FolderRepository);
public:
	typedef FolderSet Super;
	
public:
	FolderRepository() :
		FolderSet(Types::FolderPart)
	{}
	
	void onLoaded() override
	{
		Super::onLoaded();
		preCacheMostRecentFolder();
	}
};

DECLARE_SMARTPTR(FolderRepository);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_model_FolderRepository_h__ */
