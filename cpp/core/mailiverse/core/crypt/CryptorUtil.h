/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_CryptorUtil_h__
#define __mailiverse_core_crypt_CryptorUtil_h__

#include <botan/botan.h>
#include "mailiverse/core/Block.h"

namespace mailiverse {
namespace core {
namespace crypt {

inline Block read_all_as_block(Botan::Pipe *pipe)
{
	auto msg = pipe->default_msg();
	Botan::SecureVector<Botan::byte> buffer(Botan::DEFAULT_BUFFERSIZE);
	
	Block b;
	b.reserve(pipe->remaining(msg));

	while(true)
	{
		size_t got = pipe->read(&buffer[0], buffer.size(), msg);
		if(got == 0)
			break;

		const char *begin = reinterpret_cast<const char*>(&buffer[0]);
		const char *end = begin + got;
		b.insert(b.end(), begin, end);
	}

	return b;
}

} // namespace
} // namespace
} // namespace

#endif
