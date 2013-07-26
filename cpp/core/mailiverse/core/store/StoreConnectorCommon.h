/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef Core_StoreConnectorCommon_h
#define Core_StoreConnectorCommon_h

#include "../connector/StoreConnector.h"

namespace mailiverse {
namespace core {
namespace store {

class StoreConnectorCommon : public connector::StoreConnector
{
public:
	typedef connector::FileInfo FileInfo;
	typedef connector::StoreConnector::VersionedBlock VersionedBlock;

protected:
	
	typedef std::map<std::string,std::string> Headers;
	typedef core::Block Content;
	typedef std::pair<Headers,Content> HeadersContent;

	Headers parseHeaders(const std::string &s);

	
};

} // namespace
} // namespace
} // namespace


#endif
