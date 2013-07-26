/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_util_HttpDelegate_h__
#define __mailiverse_core_util_HttpDelegate_h__

#include "mailiverse/Types.h"
#include "../Block.h"
#include "../util/Callback.h"

namespace mailiverse {
namespace core {
namespace util {

class HttpDelegate
{
public:
	static const String GET, PUT, POST, DELETE;

	typedef std::map<std::string,std::string> Headers;
	typedef core::Block Content;
	typedef std::pair<Headers,Content> HeadersContent;

protected:

	Headers parseHeaders(const std::string &s);
	HeadersContent doExecute(
		const String &action, 
		const String &url, 
		const Map<String,String> &headers, 
		const core::Block &contents
	);

public:
	virtual ~HttpDelegate() {}

	HeadersContent execute(
		const String &action, 
		const String &url, 
		const Map<String,String> &headers, 
		const core::Block &contents
	);
};

DECLARE_SMARTPTR(HttpDelegate);

} /* namespace util */
} /* namespace core */
} /* namespace mailiverse */
#endif /* HTTPDELEGATE_H_ */
