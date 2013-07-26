/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Hash.h"
#include "../streams/IO.h"
#include "mailiverse/utilities/Log.h"

#include <botan/botan.h>
#include "CryptorUtil.h"

using namespace mailiverse::core::crypt;
using namespace mailiverse;
using namespace mailiverse::core;

using namespace Botan;

Hash::Hash ()
{
	LogDebug(mailiverse::core::crypt, "Hash");
}

Hash::~Hash ()
{
	LogDebug(mailiverse::core::crypt, "~Hash");
}


HashSha256::HashSha256 ()
{
}


Block HashSha256::generate(const Block &b) const
{
	Botan::Pipe pipe(new Botan::Hash_Filter("SHA-256"));
	pipe.process_msg((const byte *)b.data(), b.size());
	
	return read_all_as_block(&pipe);
}

HashMd5::HashMd5 ()
{
}


Block HashMd5::generate(const Block &b) const
{
	Botan::Pipe pipe(new Botan::Hash_Filter("MD5"));
	pipe.process_msg((const byte *)b.data(), b.size());
	
	return toBlock(pipe.read_all_as_string());
}
