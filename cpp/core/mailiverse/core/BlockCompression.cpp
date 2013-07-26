/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "BlockCompression.h"
#include <zlib.h>
#include "BlockIO.h"

using namespace mailiverse::core;
using namespace mailiverse;

Block mailiverse::core::inflate (const Block &block) throws_ (Exception)
{
	BlockWriter writer;
	const int BUF_SIZE=1024;
	Bytef s_outbuf[BUF_SIZE];
	
	int infile_size = block.size();
	Bytef *s_inbuf = (Bytef *)block.data();
	
	uLong *crc = (uLong *)(s_inbuf + infile_size - sizeof(uLong));
	bool hasCrc = (infile_size > 6) && (*crc != 0L);
	
	if (!hasCrc)
	{
		infile_size -=6;
		s_inbuf += 2;
	}
	
	z_stream stream;
	memset(&stream, 0, sizeof(stream));
	stream.next_in = s_inbuf;
	stream.avail_in = infile_size;
	stream.next_out = s_outbuf;
	stream.avail_out = BUF_SIZE;	
	
	int initState = 0;
	if (hasCrc)
		initState = inflateInit(&stream);
	else
		initState = inflateInit2(&stream, -15);
		
	if (initState)
    {
		throw Exception("Inflate failed");
    }

    for ( ; ; )
    {
      int status;
      status = ::inflate(&stream, Z_SYNC_FLUSH);

      if ((status == Z_STREAM_END) || (!stream.avail_out))
      {
        uint n = BUF_SIZE - stream.avail_out;
		writer.write((char *)s_outbuf, n);
        stream.next_out = s_outbuf;
        stream.avail_out = BUF_SIZE;
      }

      if (status == Z_STREAM_END)
        break;
      else if (status != Z_OK)
      {
		throw Exception("Inflate failed");
      }
    }

    if (inflateEnd(&stream) != Z_OK)
    {
	  throw Exception("Inflate failed");
    }

  return writer.getBlock();
}


Block mailiverse::core::deflate (const Block &block) throws_ (Exception)
{
	BlockWriter writer;
	const int BUF_SIZE=1024;
	Bytef s_outbuf[BUF_SIZE];
	
	Bytef *s_inbuf = (Bytef *)block.data();
	int infile_size = block.size();
	int level = Z_BEST_COMPRESSION;
	
	z_stream stream;
	memset(&stream, 0, sizeof(stream));
	stream.next_in = s_inbuf;
	stream.avail_in = infile_size;
	stream.next_out = s_outbuf;
	stream.avail_out = BUF_SIZE;	

	// Compression.
    if (deflateInit(&stream, level) != Z_OK)
    {
	  throw Exception("Deflate failed");
    }

    for ( ; ; )
    {
      int status;
      status = ::deflate(&stream, stream.avail_in ? Z_NO_FLUSH : Z_FINISH);

      if ((status == Z_STREAM_END) || (!stream.avail_out))
      {
        // Output buffer is full, or compression is done, so write buffer to output file.
        uint n = BUF_SIZE - stream.avail_out;
		writer.write((char *)s_outbuf, n);
        stream.next_out = s_outbuf;
        stream.avail_out = BUF_SIZE;
      }

      if (status == Z_STREAM_END)
        break;
      else if (status != Z_OK)
      {
	    throw Exception("Deflate failed");
      }
    }

    if (deflateEnd(&stream) != Z_OK)
    {
	  throw Exception("Deflate failed");
    }
  
  return writer.getBlock();
}

