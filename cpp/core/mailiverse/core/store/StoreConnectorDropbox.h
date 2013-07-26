/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_store_StoreConnectorDropbox_h__
#define __mailiverse_core_store_StoreConnectorDropbox_h__

#include "StoreConnectorCommon.h"
#include "mailiverse/core/connector/StoreConnectorInfo.h"

namespace mailiverse {
namespace core {
namespace store {

class StoreConnectorDropboxInfo : public connector::StoreConnectorInfo
{
public:
	StoreConnectorDropboxInfo (
		const std::string &userPrefix,
		const std::string &appKey,
		const std::string &appSecret,
		const std::string &userKey,
		const std::string &userSecret
	);
	
	virtual ~StoreConnectorDropboxInfo();
	
	const std::string userPrefix, appKey, appSecret, userKey, userSecret;
} ;

class StoreConnectorDropbox : public StoreConnectorCommon
{
protected:
	StoreConnectorDropboxInfo info;

	std::pair<Headers,Content> simpleRequest (
		const std::string &verb, 
		const std::string &host, 
		const std::string &url, 
		const Block &block=Block()
	);

protected:
	std::string getGlobalPath (const std::string &path);
	std::string getUserPath (const std::string &path);

public:
	StoreConnectorDropbox(const StoreConnectorDropboxInfo &info);
	virtual ~StoreConnectorDropbox ();
	

	virtual void open () throws_ (connector::ConnectorException);
	virtual void close () throws_ (connector::ConnectorException);
	
	virtual FileListing list (const std::string &path) throws_ (connector::ConnectorException);
	virtual void createDirectory (const std::string &path) throws_ (connector::ConnectorException);
	
	virtual VersionedBlock get (const std::string &path) throws_ (connector::ConnectorException);
	virtual VersionedBlock get (const std::string &path, connector::FileInfo::Size size) throws_ (connector::ConnectorException);

	virtual FileInfo::Version put (const std::string &path, const Block &contents) throws_ (connector::ConnectorException);
	virtual void move (const std::string &from, const std::string &to) throws_ (connector::ConnectorException);
	virtual void remove (const std::string &path) throws_ (connector::ConnectorException);
	
	virtual bool ensureDirectories (const std::list<std::string> &folders);
} ;

} // namespace
} // namespace
} // namespace

#endif
