/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_connector_EncryptedStoreConnector_h__
#define __mailiverse_core_connector_EncryptedStoreConnector_h__

#include "ConnectorException.h"
#include "mailiverse/Exception.h"
#include "../crypt/Cryptor.h"
#include "../BlockCompression.h"
#include "StoreConnector.h"

namespace mailiverse {
namespace core {
namespace connector {

class StoreConnectorEncrypted : public StoreConnector
{
private:
	StoreConnectorPtr store;
	crypt::CryptorPtr cryptor;
	
public:
	StoreConnectorEncrypted (crypt::Cryptor *_cryptor, StoreConnector *_store) :
		store(_store),
		cryptor(_cryptor)
	{
	}
	
	void open() throws_ (ConnectorException)
	{
		store->open();
	}

	void close() throws_ (ConnectorException)
	{
		store->close();
	}

	FileListing list(const std::string &path) throws_ (ConnectorException)
	{
		return store->list(path);
	}

	FileListing listDirectory (const std::string &path, const std::pair<FileInfo::Date,FileInfo::Date> &dateRange) throws_ (ConnectorException)
	{
		return store->list(path, dateRange);
	}

	void createDirectory(const std::string &path) throws_ (ConnectorException)
	{
		store->createDirectory(path);
	}

	VersionedBlock get(const std::string &path) throws_ (ConnectorException)
	{
		try
		{
			VersionedBlock result = store->get(path);
			result.first = inflate(cryptor->decrypt(result.first));
			return result;
		}
		catch (const Exception &e)
		{
			throw ConnectorException(e.what());
		}
	}

	VersionedBlock get(const std::string &path, long size) throws_ (ConnectorException)
	{
		return get(path);
	}

	FileInfo::Version put(const std::string &path, const Block &contents) throws_ (ConnectorException)
	{
		FileInfo::Version result;
		try
		{
			result = store->put(path, cryptor->encrypt(deflate(contents)));
		}
		catch (Exception &e)
		{
			throw ConnectorException(e.what());
		}

		return result;
	}

	void move(const std::string &from, const std::string &to) throws_ (ConnectorException)
	{
		store->move(from, to);
	}

	void remove(const std::string &path) throws_ (ConnectorException)
	{
		store->remove(path);
	}

	virtual bool ensureDirectories (const std::list<std::string> &folders)
	{
		return store->ensureDirectories(folders);
	}
} ;

} // namespace
} // namespace
} // namespace

#endif

