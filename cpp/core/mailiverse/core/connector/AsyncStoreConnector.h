/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef ASYNCSTORECONNECTOR_H_
#define ASYNCSTORECONNECTOR_H_

#include "StoreConnector.h"
#include "mailiverse/core/util/Callback.h"
#include "../Block.h"

namespace mailiverse {
namespace core {
namespace connector {

class AsyncStoreConnector
{
public:
	virtual ~AsyncStoreConnector() {}
	
	typedef StoreConnector::VersionedBlock VersionedBlock;

	typedef StoreConnector::FileListing FileListing;
	virtual void list (const std::string &path, util::Callback<FileListing> callback) = 0;
	virtual void list (const std::string &path, const std::pair<FileInfo::Date,FileInfo::Date> &dateRange, util::Callback<FileListing> callback) throws_ (ConnectorException) = 0;

	virtual void createDirectory (const std::string &path, util::Callback<> callback) = 0;

	virtual void get (const std::string &path, util::Callback<VersionedBlock> callback) = 0;
	virtual void get (const std::string &path, FileInfo::Size size, util::Callback<VersionedBlock> callback) = 0;

	virtual void put (const std::string &path, const Block &contents, util::Callback<FileInfo::Version> callback) = 0;
	virtual void remove (const std::string &path, util::Callback<> callback) = 0;
};

DECLARE_SMARTPTR(AsyncStoreConnector);

} /* namespace connector */
} /* namespace core */
} /* namespace mailiverse */

#endif /* ASYNCSTORECONNECTOR_H_ */
