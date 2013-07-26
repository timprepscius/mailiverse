/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_TrackingConnector_h__
#define __mailiverse_mail_manager_TrackingConnector_h__

#include <assert.h>
#include "Servent.h"
#include "Events.h"

namespace mailiverse {
namespace mail {
namespace manager {

class TrackingConnector : public Servent, public core::connector::StoreConnector
{
protected:
	core::connector::StoreConnectorPtr connector;

	int uploading;
	int downloading;

public:
	TrackingConnector(core::connector::StoreConnector *_connector) :
		connector(_connector),
		uploading(0),
		downloading(0)
	{}

	virtual ~TrackingConnector() {}

protected:
	void onUploadBegin()
	{
		uploading++;

		if (uploading == 1)
			getMaster()->getEventPropagator()->signal(Events::UploadBegin, NULL);
	}

	void onUploadEnd()
	{
		uploading--;
		assert(uploading >= 0);

		if (uploading == 0)
			getMaster()->getEventPropagator()->signal(Events::UploadEnd, NULL);
	}

	void onDownloadBegin()
	{
		downloading++;

		if (downloading == 1)
			getMaster()->getEventPropagator()->signal(Events::DownloadBegin, NULL);
	}

	void onDownloadEnd ()
	{
		downloading--;
		assert(downloading >= 0);

		if (downloading == 0)
			getMaster()->getEventPropagator()->signal(Events::DownloadEnd, NULL);
	}

public:

	virtual void open () throws_ (core::connector::ConnectorException)
	{
		connector->open();
	}

	virtual void close () throws_ (core::connector::ConnectorException)
	{
		connector->close();
	}

	virtual FileListing list (const std::string &path) throws_ (core::connector::ConnectorException)
	{
		FileListing result;
		try
		{
			onDownloadBegin();
			result = connector->list(path);
			onDownloadEnd();
		}
		catch (Exception &e)
		{
			onDownloadEnd();
			throw e;
		}
		return result;
	}

	virtual FileListing list (const std::string &path, const std::pair<core::connector::FileInfo::Date,core::connector::FileInfo::Date> &dateRange) throws_ (core::connector::ConnectorException)
	{
		FileListing result;
		try
		{
			onDownloadBegin();
			result = connector->list(path, dateRange);
			onDownloadEnd();
		}
		catch (Exception &e)
		{
			onDownloadEnd();
			throw e;
		}
		return result;
	}

	virtual void createDirectory (const std::string &path) throws_ (core::connector::ConnectorException)
	{
		try
		{
			onUploadBegin();
			connector->createDirectory(path);
			onUploadEnd();
		}
		catch (Exception &e)
		{
			onUploadEnd();
			throw e;
		}
	}

	virtual VersionedBlock get (const std::string &path) throws_ (core::connector::ConnectorException)
	{
		VersionedBlock result;
		try
		{
			onDownloadBegin();
			result = connector->get(path);
			onDownloadEnd();
		}
		catch (Exception &e)
		{
			onDownloadEnd();
			throw e;
		}

		return result;
	}

	virtual VersionedBlock get (const std::string &path, core::connector::FileInfo::Size size) throws_ (core::connector::ConnectorException)
	{
		VersionedBlock result;
		try
		{
			onDownloadBegin();
			result = connector->get(path, size);
			onDownloadEnd();
		}
		catch (Exception &e)
		{
			onDownloadEnd();
			throw e;
		}

		return result;
	}

	virtual core::connector::FileInfo::Version put (const std::string &path, const Block &contents) throws_ (core::connector::ConnectorException)
	{
		core::connector::FileInfo::Version result;
		try
		{
			onUploadBegin();
			result = connector->put(path, contents);
			onUploadEnd();
		}
		catch (Exception &e)
		{
			onUploadEnd();
			throw e;
		}

		return result;
	}

	virtual void move (const std::string &from, const std::string &to) throws_ (core::connector::ConnectorException)
	{
		try
		{
			onUploadBegin();
			connector->move(from, to);
			onUploadEnd();
		}
		catch (Exception &e)
		{
			onUploadEnd();
			throw e;
		}
	}

	virtual void remove (const std::string &path) throws_ (core::connector::ConnectorException)
	{
		try
		{
			onUploadBegin();
			connector->remove(path);
			onUploadEnd();
		}
		catch (Exception &e)
		{
			onUploadEnd();
			throw e;
		}
	}

	virtual bool ensureDirectories (const std::list<std::string> &folders)
	{
		bool result;
		try
		{
			onUploadBegin();
			result = connector->ensureDirectories(folders);
			onUploadEnd();
		}
		catch (Exception &e)
		{
			onUploadEnd();
			throw e;
		}
		return result;
	}

};

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_manager_TrackingConnector_h__ */
