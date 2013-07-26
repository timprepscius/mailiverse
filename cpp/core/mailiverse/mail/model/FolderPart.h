/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_FolderPart_h__
#define __mailiverse_mail_model_FolderPart_h__

#include "FolderImplBase.h"

namespace mailiverse {
namespace mail {
namespace model {

class FolderPart : public FolderImplBase
{
	DECLARE_ITEM(FolderPart);
public:
	FolderPart() {}
};

DECLARE_SMARTPTR(FolderPart);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_model_FolderPart_h__ */
