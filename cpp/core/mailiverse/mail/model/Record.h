/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef RECORDPTR_H_
#define RECORDPTR_H_

#include "../cache/ID.h"
#include "mailiverse/Types.h"

namespace mailiverse {
namespace mail {
namespace model {

typedef std::pair<cache::ID,Date> Record;

typedef Vector<Record> RecordList;

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* RECORDPTR_H_ */
