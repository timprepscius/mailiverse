/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_Types_h
#define __mailiverse_mail_model_Types_h

#include "mailiverse/mail/cache/Type.h"

namespace mailiverse {
namespace mail {
namespace model {

namespace Types {

const cache::Type 
		Conversation = 0,
		Mail = 1,
		FolderRepository = 2,
		FolderPart = 3,
		FolderFilter = 4,
		FolderFilterSet = 5,
		FolderMaster = 6;

} ;

} // namespace
} // namespace
} // namespace

#endif
