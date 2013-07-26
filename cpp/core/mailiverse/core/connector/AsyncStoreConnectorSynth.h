/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef ASYNCSTORECONNECTORSYNTH_H_
#define ASYNCSTORECONNECTORSYNTH_H_

#include "AsyncStoreConnector.h"
#include "mailiverse/utilities/Monitor.h"
#include "mailiverse/utilities/Thread.h"
#include "../util/Callback.h"
#include "../Block.h"

namespace mailiverse {
namespace core {
namespace connector {

class AsyncStoreConnectorSynth : public AsyncStoreConnector
{
protected:
	StoreConnectorPtr store;

	bool finished;
	typedef utilities::Monitor<std::list<util::Callback<> > > Actions;
	Actions actions;
	utilities::Thread thread;

	static void __thread__ (void *);

public:
	AsyncStoreConnectorSynth(StoreConnector *store);
	virtual ~AsyncStoreConnectorSynth();

	void list (const std::string &path, util::Callback<FileListing> callback);
	void list (const std::string &path, const std::pair<FileInfo::Date,FileInfo::Date> &dateRange, util::Callback<FileListing> callback) throws_ (ConnectorException);
	void createDirectory (const std::string &path, util::Callback<> callback);

	void get (const std::string &path, util::Callback<VersionedBlock> callback);
	void get (const std::string &path, connector::FileInfo::Size size, util::Callback<VersionedBlock> callback);

	void put (const std::string &path, const Block &contents, util::Callback<FileInfo::Version> callback);
	void remove (const std::string &path, util::Callback<> callback);

};

} /* namespace connector */
} /* namespace core */
} /* namespace mailiverse */

#endif /* ASYNCSTORECONNECTORSYNTH_H_ */
