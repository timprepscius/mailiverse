/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_FolderFilterSimple_h__
#define __mailiverse_mail_model_FolderFilterSimple_h__

#include "FolderFilter.h"

namespace mailiverse {
namespace mail {
namespace model {

class FolderFilterSimple : public FolderFilter
{
	DECLARE_ITEM(FolderFilterSimple);

public:
	FolderFilterSimple() {}

	virtual bool matchesFilter (Conversation *conversation) override
	{
		return folderDefinition->matchesFilter(conversation);
	}
};

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_model_FolderFilterSimple_h__ */
