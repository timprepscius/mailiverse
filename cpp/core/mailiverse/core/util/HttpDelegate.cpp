/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "HttpDelegate.h"
#include <boost/asio.hpp>
#include "mailiverse/utilities/Log.h"

using namespace mailiverse::core::util;
using namespace mailiverse::core;
using namespace mailiverse::utilities;
using namespace mailiverse;
using namespace boost::asio::ip;

const String 
	HttpDelegate::GET = "GET", 
	HttpDelegate::PUT = "PUT", 
	HttpDelegate::POST = "POST", 
	HttpDelegate::DELETE = "DELETE";

HttpDelegate::Headers HttpDelegate::parseHeaders (const std::string &s)
{
	Headers headers;
	
	std::istringstream ss(s);
	while (!ss.eof())
	{
		const int bufSize = 4096;
		char buffer[bufSize];
		
		ss.getline(buffer, bufSize);
		int lineSize = ss.gcount();
		buffer[lineSize] = 0;
		std::string line(buffer);
		int colon = line.find(':');
		if (colon == -1)
			continue;
			
		std::string left = line.substr(0,colon);
		std::string right = line.substr(colon+1);
		right = trim(right);
		headers[left] = right;
	}
	
	return headers;
}

HttpDelegate::HeadersContent HttpDelegate::doExecute(
	const String &verb, 
	const String &url, 
	const Map<String,String> &headers, 
	const core::Block &data
)
{
	LogDebug (mailiverse::core::util::HttpDelegate, verb << " " << url << " " << toString(data));

	boost::asio::io_service io_service;

	int twoSlash = url.find("//");
	if (twoSlash == -1)
		throw Exception("no //");
	twoSlash+=2;
		
	int oneSlash = url.find("/", twoSlash);
	if (oneSlash == -1)
		oneSlash = url.length();
		
	std::string host = url.substr(twoSlash, oneSlash-twoSlash);
	std::string port = url.substr(0,url.find(':'));
	int colon = host.find(':');
	if (colon != -1)
	{
		port = host.substr(colon+1);
		host = host.substr(0,colon);
	}
	
	std::string path = "/";
	if (oneSlash < url.length())
		path = url.substr(oneSlash);
	
	LogDebug (mailiverse::core::util::HttpDelegate, verb << " " << host << " " << port << " " << path);

    tcp::resolver resolver(io_service);
    tcp::resolver::query query(host, port);
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

    buf << verb << " " << path << " HTTP/1.0\r\n";
    buf << "Host: " << host << "\r\n";
	
	if (!data.empty())
		buf << "Content-Length: " << data.size() << "\r\n";
		
	for (auto &i : headers)
		buf << i.first << ": " << i.second << "\r\n";

    buf << "Connection: close\r\n\r\n";
		
	request_stream << buf.str();

	if (!data.empty())
		request_stream.write((char *)data.data(), data.size());

    boost::asio::write(socket, request);
    boost::asio::streambuf response;
	
	std::ostringstream complete;
	while (boost::asio::read(socket, response, boost::asio::transfer_at_least(1), error)) 
		complete << &response;
		
	std::string completeString = complete.str();
	
	LogDebug (mailiverse::core::util::HttpDelegate::result, verb << " " << url << " ---> " << completeString);
	
	int endHeader = completeString.find("\r\n\r\n");
	if (endHeader != -1)
		return HeadersContent(parseHeaders(completeString.substr(0,endHeader)), completeString.substr(endHeader+4, -1));
	
	throw Exception("Could not parse results");
}


HttpDelegate::HeadersContent HttpDelegate::execute(
	const String &verb, 
	const String &url, 
	const Map<String,String> &headers, 
	const core::Block &data
)
{
	try
	{
		return doExecute(verb, url, headers, data);
	}
	catch (Exception &e)
	{
		throw e;
	}
	catch (boost::exception &e)
	{
		throw Exception("boost exception");
	}
	catch (std::exception &e)
	{
		throw Exception(e.what());
	}
}