/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "AsyncStoreConnectorSynth.h"
#include "mailiverse/utilities/Log.h"

using namespace mailiverse::core::connector;
using namespace mailiverse::core;
using namespace mailiverse;

namespace {

void _list_ (StoreConnectorPtr store, const std::string &path, const std::pair<FileInfo::Date, FileInfo::Date> &range, util::Callback<AsyncStoreConnectorSynth::FileListing> callback)
{
	try
	{
		if (range.first.getTime()!=0)
			callback.invoke(store->list(path, range));
		else
			callback.invoke(store->list(path));
	}
	catch (Exception &e)
	{
		callback.invoke(e);
	}
}

void _createDirectory_ (StoreConnectorPtr store, const std::string &path, util::Callback<> callback)
{
	try
	{
		store->createDirectory(path);
		callback.invoke();
	}
	catch (Exception &e)
	{
		callback.invoke(e);
	}
}

void _get_ (StoreConnectorPtr store, const std::string &path, util::Callback<std::pair<Block,FileInfo::Version>> callback)
{
	try
	{
		callback.invoke(store->get(path));
	}
	catch (Exception &e)
	{
		callback.invoke(e);
	}
}


void _remove_ (StoreConnectorPtr store, const std::string &path, util::Callback<> callback)
{
	try
	{
		store->remove(path);
		callback.invoke();
	}
	catch (Exception &e)
	{
		callback.invoke(e);
	}
}

void _put_ (StoreConnectorPtr store, const std::string &path, const Block &block, util::Callback<FileInfo::Version> callback)
{
	try
	{
		callback.invoke(store->put(path, block));
	}
	catch (Exception &e)
	{
		callback.invoke(e);
	}
}

} // namespace

void AsyncStoreConnectorSynth::__thread__ (void *_connector)
{
	AsyncStoreConnectorSynth *connector = (AsyncStoreConnectorSynth*)_connector;
	while (true)
	{
		connector->actions.wait();
		connector->actions.reset();
		if (connector->finished)
			return;

		while (true)
		{
			util::Callback<> c;
			{
				Actions::Writer w(connector->actions);
				if (w->empty())
					break;

				c = w->front();
				w->pop_front();
			}

			LogDebug(mailiverse::core::connector::AsyncStoreConnectorSynth, "processing item");
			
			try
			{
				c.invoke();
			}
			catch (Exception &e)
			{
				LogDebug(mailiverse::core::connector::AsyncStoreConnectorSynth, "caught exception " << e.what());
				c.invoke(e);
			}
			catch (...)
			{
				LogDebug(mailiverse::core::connector::AsyncStoreConnectorSynth, "caught unknown exception");
				c.invoke(Exception("unknown exception"));
			}
			
			LogDebug(mailiverse::core::connector::AsyncStoreConnectorSynth, "processing item finished");
		}
	}
}


AsyncStoreConnectorSynth::AsyncStoreConnectorSynth(StoreConnector *_store) :
	store(_store),
	finished(false)
{
	thread = utilities::create(&__thread__, this);
}

AsyncStoreConnectorSynth::~AsyncStoreConnectorSynth()
{
	finished = true;
	actions.signal();
	utilities::wait(thread);
}

void AsyncStoreConnectorSynth::list (const std::string &_path, util::Callback<FileListing> callback)
{
	Actions::Writer w(actions);

	std::string path = _path;
	std::pair<FileInfo::Date, FileInfo::Date> range(Date(0),Date(0));

	w->push_back (
		util::Callback<> (
			utilities::newbind(&_list_, store, path, range, callback),
			callback.failure
		)
	);
	actions.signal();
}

void AsyncStoreConnectorSynth::list (const std::string &_path, const std::pair<FileInfo::Date,FileInfo::Date> &_range, util::Callback<FileListing> callback)
{
	Actions::Writer w(actions);

	std::string path = _path;
	std::pair<FileInfo::Date, FileInfo::Date> range = _range;

	w->push_back (
		util::Callback<> (
			utilities::newbind(&_list_, store, path, range, callback),
			callback.failure
		)
	);
	actions.signal();
}

void AsyncStoreConnectorSynth::createDirectory (const std::string &_path, util::Callback<> callback)
{
	Actions::Writer w(actions);

	std::string path = _path;

	w->push_back (
		util::Callback<> (
			utilities::newbind(&_createDirectory_, store, path, callback),
			callback.failure
		)
	);
	actions.signal();
}

void AsyncStoreConnectorSynth::get (const std::string &_path, util::Callback<std::pair<Block,FileInfo::Version>> callback)
{
	Actions::Writer w(actions);

	std::string path = _path;

	w->push_back (
		util::Callback<> (
			utilities::newbind(&_get_, store, path, callback),
			callback.failure
		)
	);
	actions.signal();
}

void AsyncStoreConnectorSynth::get (const std::string &_path, long size, util::Callback<std::pair<Block,FileInfo::Version>> _callback)
{
	get(_path, _callback);
}

void AsyncStoreConnectorSynth::put (const std::string &_path, const Block &_block, util::Callback<FileInfo::Version> callback)
{
	Actions::Writer w(actions);

	std::string path = _path;
	Block block = _block;

	w->push_back (
		util::Callback<> (
			utilities::newbind(&_put_, store, path, block, callback),
			callback.failure
		)
	);
	actions.signal();
}

void AsyncStoreConnectorSynth::remove (const std::string &_path, util::Callback<> callback)
{
	Actions::Writer w(actions);

	std::string path = _path;

	w->push_back (
		util::Callback<> (
			utilities::newbind(&_remove_, store, path, callback),
			callback.failure
		)
	);
	actions.signal();
}
