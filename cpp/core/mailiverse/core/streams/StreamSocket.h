/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_streams_StreamSocket_h__
#define __mailiverse_core_streams_StreamSocket_h__

#include <boost/asio.hpp>
#include "IO.h"
#include "mailiverse/utilities/SmartPtr.h"
#include <exception>

namespace mailiverse {
namespace core {
namespace streams {

class StreamSocket
{
protected:
	boost::asio::ip::tcp::socket socket;
	
public:
	StreamSocket (boost::asio::io_service &io_service, const std::string &host, const std::string &port) throws_ (Exception);
	virtual ~StreamSocket ();

	template<typename T>
	void read (T &v) throws_ (Exception)
	{
		try
		{
			boost::asio::read(socket, boost::asio::buffer((char *)&v, sizeof(v)));

#ifndef __BIG_ENDIAN__
			swap((char *)&v, sizeof(T));
#endif
		}
		catch (std::exception &e)
		{
			throw Exception(e.what());
		}
	}
	
	template<typename T>
	void write (const T &v) throws_ (Exception)
	{
		try
		{
			char buffer[sizeof(T)];
			memcpy (buffer, (char *)&v, sizeof (T));
		
#ifndef __BIG_ENDIAN__
			swap (buffer, sizeof (T));
#endif

			boost::asio::streambuf send;
			std::ostream stream(&send);
			stream.write(buffer, sizeof(v));
			
			boost::asio::write(socket, send);
		}
		catch (std::exception &e)
		{
			throw Exception(e.what());
		}
	}
	
	inline void write (const Packet &packet) throws_ (Exception)
	{
		try
		{
			int size = packet.size();
			write(size);

			boost::asio::streambuf request;
			std::ostream request_stream(&request);
			request_stream.write((char *)packet.data(), size);
			
			boost::asio::write(socket, request);
		}
		catch (std::exception &e)
		{	
			throw Exception(e.what());
		}
	}

	inline void read (Packet &packet) throws_ (Exception)
	{
		try
		{
			int size;
			read(size);
			std::vector<char> v;
			v.resize(size);
			
			boost::asio::read(socket, boost::asio::buffer(v));
			packet.assign(v.begin(), v.end());
		}
		catch (std::exception &e)
		{
			throw Exception(e.what());
		}
	}
} ;

DECLARE_SMARTPTR(StreamSocket);

} // namespace
} // namespace
} // namespace

#endif