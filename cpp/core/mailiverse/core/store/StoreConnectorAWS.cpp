/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "StoreConnectorAWS.h"
#include <boost/asio.hpp>
#include <botan/base64.h>
#include <botan/sha160.h>
#include <botan/hmac.h>
#include <botan/filters.h>
#include "mailiverse/core/streams/IO.h"
#include <boost/algorithm/string.hpp>
#include "mailiverse/utilities/Log.h"
#include "mailiverse/core/crypt/Hash.h"

#include <time.h>
#include <boost/date_time/gregorian/gregorian.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/date_time/local_time/local_time.hpp>
#include <boost/algorithm/string.hpp>

#include <pugixml/pugixml.hpp>
#include <algorithm>

#include "mailiverse/core/Types.h"

using namespace mailiverse::core::store;
using namespace mailiverse::core::connector;
using namespace mailiverse::core;
using namespace mailiverse::utilities;
using namespace mailiverse;

using namespace boost::asio::ip;

StoreConnectorAWSInfo::StoreConnectorAWSInfo (
	const std::string &_bucketName, 
	const std::string &_bucketRegion,
	const std::string &_accessId,
	const std::string &_secretKey
) : 
	bucketName(_bucketName),
	bucketRegion(_bucketRegion),
	accessId(_accessId),
	secretKey(_secretKey)
{
	LogDebug(mailiverse::core::store, "StoreConnectorAWSInfo");
}

StoreConnectorAWSInfo::~StoreConnectorAWSInfo ()
{
	LogDebug(mailiverse::core::store, "~StoreConnectorAWSInfo");
}

StoreConnectorAWS::StoreConnectorAWS (const StoreConnectorAWSInfo &_info) :
	info(_info)
{
	LogDebug(mailiverse::core::store, "StoreConnectorAWS");
}

StoreConnectorAWS::~StoreConnectorAWS()
{
	LogDebug(mailiverse::core::store, "~StoreConnectorAWS");
}

void StoreConnectorAWS::open () throws_ (ConnectorException)
{
}

void StoreConnectorAWS::close () throws_ (ConnectorException)
{
}

std::string StoreConnectorAWS::sign (const std::string &stringToBeSigned)
{
	std::string algorithmToUse = "HMAC(SHA-1)";
	
	Botan::SymmetricKey botanSymmetricKey = 
		Botan::SymmetricKey(
			reinterpret_cast<Botan::byte const *>(info.secretKey.data()), info.secretKey.size()
		);

	Botan::Pipe pipe(
		new Botan::Chain(
			new Botan::MAC_Filter(algorithmToUse, botanSymmetricKey), 
			new Botan::Base64_Encoder
		)
	);

	pipe.process_msg(stringToBeSigned);
	std::string result = pipe.read_all_as_string(0);

	return result;
}

std::string StoreConnectorAWS::createAuthorization(		
	const std::string &verb,
	const std::string &contentMd5,
	const std::string &contentType,
	const std::string &date,
	const std::string &canonicalizedAmzHeaders,
	const std::string &canonicalizedResource
)
{
	std::string message = 
		verb + "\n" 
		+ contentMd5 + "\n" 
		+ contentType + "\n" 
		+ date + "\n" 
		+ canonicalizedAmzHeaders + canonicalizedResource;

//	std::cout << "begin sign:++++\n" << message << "\nend sign:++++\n";

	return sign(message);
}

std::string StoreConnectorAWS::generateTimestamp ()
{
	boost::local_time::local_date_time t(boost::local_time::local_sec_clock::local_time(boost::local_time::time_zone_ptr()));
	boost::local_time::local_time_facet* lf(new boost::local_time::local_time_facet("%a, %d %b %Y %H:%M:%S GMT"));
	std::ostringstream oss;
	oss.imbue(std::locale(std::cout.getloc(), lf));
	oss << t;
	
//	return "Tue, 27 Mar 2007 19:36:42 +0000";
	return oss.str();
}

StoreConnectorCommon::HeadersContent StoreConnectorAWS::simpleRequest (
	const std::string &verb,
	const std::string &path, 
	const std::string &parameters,
	const Block &data
)
{
	try
	{
		std::string date = generateTimestamp();
		std::string auth = createAuthorization(verb, "", "", date, "", "/"+info.bucketName+path);

		boost::asio::io_service io_service;

		tcp::resolver resolver(io_service);
		tcp::resolver::query query(info.bucketName + "." + info.getBucketEndpoint(), "http");
		tcp::resolver::iterator endpoint_iterator = resolver.resolve(query);
		tcp::resolver::iterator end;

		tcp::socket socket(io_service);
		boost::system::error_code error = boost::asio::error::host_not_found;

		while (error && endpoint_iterator != end) 
		{
			socket.close();
			socket.connect(*endpoint_iterator++, error);
		}

		if (error) 
		{
			throw boost::system::system_error(error);
		}

		boost::asio::streambuf request;
		std::ostream request_stream(&request);
		std::ostringstream buf;

		buf << verb << " " << path << parameters << " HTTP/1.0\r\n";
		buf << "Host: " << info.bucketName << "." << info.getBucketEndpoint() << "\r\n";
		buf << "Date: " << date << "\r\n";
		buf << "Authorization: AWS " << info.accessId << ":" << auth << "\r\n";
		
		if (verb == "PUT")
			buf << "Content-Length: " << data.size() << "\r\n";
		
		buf << "Connection: close\r\n\r\n";
			
	//	std::cout << buf.str();
		request_stream << buf.str();

		if (verb == "PUT")
			request_stream.write((char *)data.data(), data.size());

		boost::asio::write(socket, request);
		boost::asio::streambuf response;
		
		std::ostringstream complete;
		while (boost::asio::read(socket, response, boost::asio::transfer_at_least(1), error)) 
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

StoreConnector::FileListing StoreConnectorAWS::list (const std::string &path) throws_ (ConnectorException)
{
	LogDebug(mailiverse::core::store::StoreConnectorAWS, this << " list " << path);

	try
	{
		std::pair<Headers,Content>
			simpleResult = simpleRequest("GET", "/", "?prefix=" + path + "&max-keys=10000");	

		std::string contents = toString(simpleResult.second);
		std::string s3DateString = toString(simpleResult.first["Date"]);
		
		Date s3Date = Date::fromTimeT(utilities::parseDate(s3DateString,"%a,%d %b %Y %H:%M:%S",false));
		Date::ValueType s3DateOffset = Date::now().getTime() - s3Date.getTime();
		LogDebug(mailiverse::core::store::StoreConnectorAWS, this << " calculated local clock is ahead " << s3DateOffset/1000 << " seconds");
	//	std::cerr << contents;

		pugi::xml_document doc;
		pugi::xml_parse_result result = doc.load(contents.c_str());
		pugi::xml_node listBucketResult = doc.child("ListBucketResult");
		
		FileListing results;

		for (pugi::xml_node c = listBucketResult.child("Contents"); c; c = c.next_sibling("Contents"))
		{
			std::string key = c.child_value("Key");
			std::string v = c.child_value("ETag");
	//		LogDebug(mailiverse::core::store::StoreConnectorAWS, this << " xml-ETag: " << v);
				
			Date date = Date::fromTimeT(utilities::parseDate(c.child_value("LastModified"),"%Y-%m-%dT%H:%M:%S",false));
				
			FileInfo fi(
				key,
				key.substr(path.size()), 
				fromString<int>(c.child_value("Size")),
				Date(date.getTime() + s3DateOffset),
				v
			);
			
			results.push_back(fi);
		}	
		return results;
	}
	catch (Exception &e)
	{
		throw ConnectorException(e.what());
	}
	catch (std::exception &e)
	{
		throw ConnectorException(e.what());
	}
	catch (...)
	{
		throw ConnectorException("Caught unknown exception");
	}
}

void StoreConnectorAWS::createDirectory (const std::string &path) throws_ (ConnectorException)
{
}

StoreConnectorCommon::VersionedBlock StoreConnectorAWS::get (const std::string &path) throws_ (ConnectorException)
{
	try
	{
		LogDebug(mailiverse::core::store::StoreConnectorAWS, this << " get " << path);

		HeadersContent results = simpleRequest("GET", "/" + path, "");
		if (results.first.find("ETag")==results.first.end())
			throw ConnectorException("Could not find Etag");
			
		return VersionedBlock(results.second, results.first["ETag"]);
	}
	catch (Exception &e)
	{
		throw ConnectorException(e.what());
	}
	catch (std::exception &e)
	{
		throw ConnectorException(e.what());
	}
	catch (...)
	{
		throw ConnectorException("Caught unknown exception");
	}
}

std::pair<Block,FileInfo::Version> StoreConnectorAWS::get (const std::string &path, long size) throws_ (ConnectorException)
{
	return get(path);
}

FileInfo::Version StoreConnectorAWS::put (const std::string &path, const Block &contents) throws_ (ConnectorException)
{
	LogDebug(mailiverse::core::store::StoreConnectorAWS, this << " put " << path);

	try
	{
		StoreConnectorCommon::HeadersContent results = simpleRequest("PUT", "/" + path, "", contents);
		if (results.first.find("ETag")==results.first.end())
			throw ConnectorException("Could not find Etag");
			
		return results.first["ETag"];
	}
	catch (Exception &e)
	{
		throw ConnectorException(e.what());
	}
	catch (std::exception &e)
	{
		throw ConnectorException(e.what());
	}
	catch (...)
	{
		throw ConnectorException("Caught unknown exception");
	}

}

void StoreConnectorAWS::move (const std::string &from, const std::string &to) throws_ (ConnectorException)
{
}

void StoreConnectorAWS::remove (const std::string &path) throws_ (ConnectorException)
{
	LogDebug(mailiverse::core::store::StoreConnectorAWS, this << " remove " << path);

}

bool StoreConnectorAWS::ensureDirectories (const std::list<std::string> &folders)
{
	return true;
}
