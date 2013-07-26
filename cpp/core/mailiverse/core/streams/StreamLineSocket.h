/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_streams_StreamLineSocket_h__
#define __mailiverse_core_streams_StreamLineSocket_h__

#include <boost/asio.hpp>
#include <iostream>
#include "IO.h"
#include "mailiverse/utilities/SmartPtr.h"
#include <exception>

namespace mailiverse {
namespace core {
namespace streams {

class StreamLineSocket
{
protected:
	boost::asio::ip::tcp::socket socket;
	boost::asio::streambuf b;
	std::istream is;	
	
public:
	StreamLineSocket (boost::asio::io_service &io_service, const std::string &host, const std::string &port);
	virtual ~StreamLineSocket ();

	void write (const Packet &packet);
	void read (Packet &packet);
} ;

DECLARE_SMARTPTR(StreamLineSocket);

} // namespace
} // namespace
} // namespace

#endif
