/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_FolderFilterSet_h__
#define __mailiverse_mail_model_FolderFilterSet_h__

#include "FolderSet.h"

namespace mailiverse {
namespace mail {
namespace model {

class FolderFilterSet : public FolderSet
{
public:
	FolderFilterSet() :
		FolderSet(Types::FolderFilter)
	{}

};

DECLARE_SMARTPTR(FolderFilterSet);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* __mailiverse_mail_model_FolderFilterSet_h__ */
