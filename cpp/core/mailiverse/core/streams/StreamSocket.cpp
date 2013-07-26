/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "StreamSocket.h"
#include <istream>
#include "mailiverse/utilities/Log.h"

using namespace mailiverse::core::streams;
using namespace mailiverse;
using boost::asio::ip::tcp;
	
StreamSocket::StreamSocket (boost::asio::io_service &io_service, const std::string &host, const std::string &port) throws_ (Exception) :
	socket(io_service)
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

StreamSocket::~StreamSocket ()
{
	LogDebug(mailiverse::core::streams, "~StreamSocket");
}
