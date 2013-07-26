/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "StoreConnectorDropbox.h"
#include <boost/asio.hpp>
#include <boost/asio/ssl.hpp>
#include "mailiverse/utilities/Strings.h"
#include "mailiverse/utilities/Log.h"

#include <botan/base64.h>
#include <botan/sha160.h>
#include <botan/hmac.h>
#include <botan/filters.h>
#include "../streams/IO.h"
#include <boost/algorithm/string.hpp>
#include <time.h>
#include <algorithm>

#include <time.h>
#include <boost/date_time/gregorian/gregorian.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/date_time/local_time/local_time.hpp>

#include <json/elements.h>
#include <json/reader.h>
#include <json/writer.h>

#include "pugixml/pugixml.hpp"

#include "mailiverse/core/Types.h"
#include "mailiverse/core/Block.h"

using namespace mailiverse::core::connector;
using namespace mailiverse::core::store;
using namespace mailiverse::core;
using namespace mailiverse::utilities;
using namespace mailiverse;
using namespace boost::asio::ip;

StoreConnectorDropboxInfo::StoreConnectorDropboxInfo (
	const std::string &_userPrefix,
	const std::string &_appKey,
	const std::string &_appSecret,
	const std::string &_userKey,
	const std::string &_userSecret
) :
	userPrefix(_userPrefix),
	appKey(_appKey),
	appSecret(_appSecret),
	userKey(_userKey),
	userSecret(_userSecret)
{
	LogDebug(mailiverse::core::store, "StoreConnectorDropboxInfo");
}

StoreConnectorDropboxInfo::~StoreConnectorDropboxInfo ()
{
	LogDebug(mailiverse::core::store, "~StoreConnectorDropboxInfo");
}

//---------------------------------------------------------------------

StoreConnectorCommon::HeadersContent StoreConnectorDropbox::simpleRequest (
	const std::string &verb, const std::string &host, const std::string &url, const Block &data
)
{
	try
	{
		LogDebug(mailiverse::core::store::StoreConnectorDropbox::simpleRequest, url);

		boost::asio::io_service io_service;
		boost::asio::ssl::context context(io_service, boost::asio::ssl::context::sslv3_client);
		boost::asio::ssl::stream<boost::asio::ip::tcp::socket> s(io_service, context);
		boost::system::error_code error = boost::asio::error::host_not_found;

		s.lowest_layer().connect( * boost::asio::ip::tcp::resolver(io_service)
			.resolve( boost::asio::ip::tcp::resolver::query( host, "https" )  ) );    

		s.handshake( boost::asio::ssl::stream_base::client );

		boost::asio::streambuf request;
		std::ostream request_stream(&request);
		std::ostringstream buf;

		buf << verb << " " << url << " HTTP/1.0\r\n";
		buf << "Host: " << host << "\r\n";
		
		if (verb == "PUT")
		{
			buf << "Content-Length: " << data.size() << "\r\n";
			buf << "Content-Type: " << "application/octet-stream" << "\r\n";
		}
		buf << "Connection: close\r\n\r\n";
		
	//	std::cerr << buf.str();
		request_stream << buf.str();
		
		if (verb == "PUT")
			request_stream.write((char *)data.data(), data.size());

		boost::asio::write(s, request);
		boost::asio::streambuf response;
		
		std::ostringstream complete;
		while (boost::asio::read(s, response, boost::asio::transfer_at_least(1), error)) 
			complete << &response;
			
		std::string completeString = complete.str();
		
		int endHeader = completeString.find("\r\n\r\n");
		if (endHeader != -1)
			return HeadersContent(parseHeaders(completeString.substr(0,endHeader)), completeString.substr(endHeader+4, -1));
		
		throw ConnectorException("Could not parse results");
	}
	catch (ConnectorException &e)
	{
		throw e;
	}
	catch (std::exception &e)
	{
		throw ConnectorException(e.what());
	}
	catch (...)
	{
		throw ConnectorException("Unknown error during network operation");
	}
}

//----------------------------------------------------------------

StoreConnectorDropbox::StoreConnectorDropbox (const StoreConnectorDropboxInfo &_info) :
	info(_info)
{
}

StoreConnectorDropbox::~StoreConnectorDropbox()
{
}

void StoreConnectorDropbox::open () throws_ (ConnectorException)
{
}

void StoreConnectorDropbox::close () throws_ (ConnectorException)
{
}

std::string StoreConnectorDropbox::getGlobalPath (const std::string &path)
{
	return "/" + info.userPrefix + "/" + path;
}

std::string StoreConnectorDropbox::getUserPath (const std::string &path)
{
	if (path.size() <= info.userPrefix.size()+2)
		return "";
		
	return path.substr(info.userPrefix.length() + 2);
}

StoreConnector::FileListing StoreConnectorDropbox::list (const std::string &path) throws_ (ConnectorException)
{
	LogDebug(mailiverse::core::store::StoreConnectorDropbox, this << " list " << path);

	try
	{
		const std::string API_METADATA_URL="https://api.dropbox.com/1/metadata/sandbox";
		const std::string HOST="api.dropbox.com";
		
		
		std::string timeString = toString(time(NULL));
		std::string randomString = toString(random());
		
		std::string url = 
			API_METADATA_URL +
			getGlobalPath(path) + "?" +
			"oauth_consumer_key=" + info.appKey + "&" +
			"oauth_token=" + info.userKey + "&" +
			"oauth_signature_method=PLAINTEXT" + "&" +
			"oauth_signature=" + info.appSecret + "%26" + info.userSecret + "&"
			"oauth_timestamp=" + timeString + "&" +
			"oauth_nonce=" + randomString + "&" +
			"include_deleted=false";
			
		HeadersContent simpleResults = simpleRequest("GET", HOST, url);
		std::string dbDateString = toString(simpleResults.first["Date"]);
		
		Date dbDate = Date::fromTimeT(utilities::parseDate(dbDateString,"%a,%d %b %Y %H:%M:%S",false));
		Date::ValueType dateOffset = Date::now().getTime() - dbDate.getTime();
		LogDebug(mailiverse::core::store::StoreConnectorDropbox, this << " calculated local clock is ahead " << dateOffset/1000 << " seconds");
		
		std::string content = toString(simpleResults.second);
		
		json::SimpleMemStream iss(content);
		json::Object o;
		json::Reader<json::SimpleMemStream>::Read (o, iss);
		
//		std::ostringstream oss;
//		json::Writer::Write (o, oss);
//		std::cerr << oss.str();

		FileListing results;
		
		if (o.Find("contents") != o.End())
		{
			const json::Array &l = o["contents"];
			for (json::Array::const_iterator i=l.Begin(); i!=l.End(); ++i)
			{
				const json::Object &f = *i;
				
				std::string filePath = getUserPath((json::String)f["path"]);
				int fileSize = ((const json::Number &)f["bytes"]).as<int>(); // fromString<int>((json::Number)f["bytes"]);
				FileInfo::Version fileVersion = (const json::String&)f["rev"]; // fromString<FileInfo::Version>((const json::Number&)f["revision"]);
				
				if (filePath.size() <= path.size())
					continue;
				
				
				std::string s = (json::String)f["modified"];

	/*			
				tm dtm;
				char *x = strptime(s.c_str(), "%a,%d %b %Y %H:%M:%S", &dtm);
				long fileDate = mktime(&dtm);
	*/
				
				Date fileDate = Date::fromTimeT(utilities::parseDate((json::String)f["modified"], "%a,%d %b %Y %H:%M:%S", true));
				
				results.push_back (
					FileInfo(
						filePath, 
						filePath.substr(path.size()), 
						fileSize, 
						Date(fileDate.getTime() + dateOffset), 
						fileVersion
					)
				);
			}
		}
		
		return results;
	}
	catch (std::exception &e)
	{
		throw ConnectorException(e.what());
	}
	catch (...)
	{
		throw ConnectorException("unknown exception");
	}
}

void StoreConnectorDropbox::createDirectory (const std::string &path) throws_ (ConnectorException)
{
	LogDebug(mailiverse::core::store::StoreConnectorDropbox, this << " createDirectory " << path);
}

StoreConnectorCommon::VersionedBlock StoreConnectorDropbox::get (const std::string &path) throws_ (ConnectorException)
{
	LogDebug(mailiverse::core::store::StoreConnectorDropbox, this << " get " << path);

	try
	{
		const std::string API_METADATA_URL="https://api-content.dropbox.com/1/files/sandbox";
		const std::string HOST="api-content.dropbox.com";
		
		std::string timeString = toString(time(NULL));
		std::string randomString = toString(random());
		
		std::string url = 
			API_METADATA_URL +
			getGlobalPath(path) + "?" +
			"oauth_consumer_key=" + info.appKey + "&" +
			"oauth_token=" + info.userKey + "&" +
			"oauth_signature_method=PLAINTEXT" + "&" +
			"oauth_signature=" + info.appSecret + "%26" + info.userSecret + "&"
			"oauth_timestamp=" + timeString + "&" +
			"oauth_nonce=" + randomString;	
			
		HeadersContent simpleResults = simpleRequest("GET", HOST, url);
		if (simpleResults.first.find("x-dropbox-metadata") == simpleResults.first.end())
			throw ConnectorException("Could not find x-dropbox-metadata");
			
		std::string content = simpleResults.first["x-dropbox-metadata"];
		json::SimpleMemStream iss(content);
		json::Object o;
		json::Reader<json::SimpleMemStream>::Read (o, iss);
			
		return VersionedBlock(
			simpleResults.second, (json::String &)o["rev"]
		);
	}
	catch (boost::exception &e)
	{
		throw ConnectorException("boost exception");
	}
	catch (std::exception &e)
	{
		throw ConnectorException(e.what());
	}
	catch (...)
	{
		throw ConnectorException("unknown exception");
	}
}

StoreConnectorCommon::VersionedBlock StoreConnectorDropbox::get (const std::string &path, long size) throws_ (ConnectorException)
{
	return get(path);
}

FileInfo::Version StoreConnectorDropbox::put (const std::string &path, const Block &contents) throws_ (ConnectorException)
{
	LogDebug(mailiverse::core::store::StoreConnectorDropbox, this << " put " << path << " " << contents.size());

	try
	{
		const std::string API_METADATA_URL="https://api-content.dropbox.com/1/files_put/sandbox";
		const std::string HOST="api-content.dropbox.com";
		
		std::string timeString = toString(time(NULL));
		std::string randomString = toString(random());
		
		std::string url = 
			API_METADATA_URL +
			getGlobalPath(path) + "?" +
			"oauth_consumer_key=" + info.appKey + "&" +
			"oauth_token=" + info.userKey + "&" +
			"oauth_signature_method=PLAINTEXT" + "&" +
			"oauth_signature=" + info.appSecret + "%26" + info.userSecret + "&"
			"oauth_timestamp=" + timeString + "&" +
			"oauth_nonce=" + randomString + "&" + 
			"overwrite=true";
			
		HeadersContent simpleResults = simpleRequest("PUT", HOST, url, contents);

		std::string response = toString(simpleResults.second);
		json::SimpleMemStream iss(response);
		json::Object o;
		json::Reader<json::SimpleMemStream>::Read (o, iss);

		return FileInfo::Version((json::String &)o["rev"]);
	}
	catch (std::exception &e)
	{
		throw ConnectorException(e.what());
	}
	catch (...)
	{
		throw ConnectorException("unknown exception");
	}
}

void StoreConnectorDropbox::move (const std::string &from, const std::string &to) throws_ (ConnectorException)
{

}

void StoreConnectorDropbox::remove (const std::string &path) throws_ (ConnectorException)
{
}

bool StoreConnectorDropbox::ensureDirectories (const std::list<std::string> &folders)
{
	return true;
}

