/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_store_StoreConnectorAWS_h__
#define __mailiverse_core_store_StoreConnectorAWS_h__

#include "../connector/StoreConnectorInfo.h"
#include "StoreConnectorCommon.h"

namespace mailiverse {
namespace core {
namespace store {

class StoreConnectorAWSInfo : public connector::StoreConnectorInfo
{
public:
	StoreConnectorAWSInfo (
		const std::string &bucketName, 
		const std::string &bucketRegion,
		const std::string &accessId,
		const std::string &secretKey
	);
	virtual ~StoreConnectorAWSInfo();
	
	const std::string bucketName, bucketRegion, accessId, secretKey;
	
	String getBucketEndpoint ()
	{
		if (bucketRegion == "")
			return "s3.amazonaws.com";
			
		return "s3-" + bucketRegion + ".amazonaws.com";
	}
} ;

class StoreConnectorAWS : public StoreConnectorCommon
{
protected:
	std::string sign (const std::string &);
	std::string generateTimestamp ();

	std::string createAuthorization (
		const std::string &verb,
		const std::string &contentMd5,
		const std::string &contentType,
		const std::string &date,
		const std::string &canonicalizedAmzHeaders,
		const std::string &canonicalizedResource
	);

	HeadersContent simpleRequest (
		const std::string &verb,
		const std::string &path,
		const std::string &parameters,
		const Block &content = Block()
	);

	StoreConnectorAWSInfo info;

public:
	StoreConnectorAWS (const StoreConnectorAWSInfo &info);
	virtual ~StoreConnectorAWS ();

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
