/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "StreamLineSocket.h"
#include <istream>
#include "mailiverse/utilities/Log.h"

using namespace mailiverse::core::streams;
using namespace mailiverse;
using boost::asio::ip::tcp;
	
StreamLineSocket::StreamLineSocket (boost::asio::io_service &io_service, const std::string &host, const std::string &port) :
	socket(io_service),
	is(&b)
{
	LogDebug(mailiverse::core::streams, "StreamSocket");

	tcp::resolver::query query(host.c_str(), port.c_str());	

	tcp::resolver resolver(io_service);
    tcp::resolver::iterator endpoint_iterator = resolver.resolve(query);
    tcp::resolver::iterator end;

    boost::system::error_code error = boost::asio::error::host_not_found;
    while (error && endpoint_iterator != end)
    {
      socket.close();
      socket.connect(*endpoint_iterator++, error);
    }
    if (error)
		throw Exception("Connection closed.");
} 

StreamLineSocket::~StreamLineSocket ()
{
	LogDebug(mailiverse::core::streams, "~StreamSocket");
}

void StreamLineSocket::write (const Packet &packet)
{
	try
	{
		Block b = toBlockBase64(packet);
	
		LogDebug(mailiverse::core::streams, "StreamSocket::write " << utilities::toString(packet) << " -> " << utilities::toString(b));
		boost::asio::streambuf request;
		std::ostream request_stream(&request);
		request_stream.write((char *)b.data(), b.size());
		request_stream.write("\n", 1);
		
		boost::asio::write(socket, request);
	}
	catch (std::exception &e)
	{	
		throw Exception(e.what());
	}
}

void StreamLineSocket::read (Packet &packet)
{
	try
	{
		boost::asio::read_until(socket, b, '\n');
		std::string line;
		std::getline(is, line);
		packet = toBlockFromBase64(toBlock(line));

		LogDebug(mailiverse::core::streams, "StreamSocket::read " << line << " -> " << utilities::toString(packet));
		
	}
	catch (boost::exception &e)
	{
		throw Exception("boost exception");
	}
	catch (std::exception &e)
	{
		throw Exception(e.what());
	}
	catch (...)
	{
		throw Exception("Unknown error");
	}
}
