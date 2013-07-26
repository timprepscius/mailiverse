/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "StoreConnectorFactory.h"
#include "StoreConnectorDropbox.h"
#include "StoreConnectorAWS.h"
#include "mailiverse/utilities/Log.h"

using namespace mailiverse::core::store;
using namespace mailiverse::core::connector;

StoreConnectorPtr StoreConnectorFactory::create (const Environment &e) throws_ (Exception)
{
	std::string handler = e.get("handler");
	if (handler == "DB")
	{
		return new StoreConnectorDropbox(
			StoreConnectorDropboxInfo (
				e.get("DB/DropboxUserPrefix"),
				e.get("DB/DropboxAppKey"),
				e.get("DB/DropboxAppSecret"),
				e.get("DB/DropboxTokenKey"),
				e.get("DB/DropboxTokenSecret")
			)
		);
	}
	else
	if (handler == "S3")
	{
		return new StoreConnectorAWS (
			StoreConnectorAWSInfo (
				e.get("S3/AWSBucketName"),
				e.getOrDefault("S3/AWSBucketRegion", ""),
				e.get("S3/AWSAccessKeyId"),
				e.get("S3/AWSSecretKey")
			)
		);
	}
	
	throw Exception("Unknown handler " + handler);
}
