/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Base64.h"
#include <botan/botan.h>
#include "CryptorUtil.h"

namespace mailiverse {
namespace core {
namespace crypt {

std::string Base64::encode (const Block &block)
{
	Botan::Pipe pipe(new Botan::Base64_Encoder);
	pipe.process_msg(block.data(), block.size());
	Botan::SecureVector<Botan::byte> sv;

	return pipe.read_all_as_string();
}

Block Base64::decode (const std::string &block)
{
	Botan::Pipe pipe(new Botan::Base64_Decoder);
	pipe.process_msg((Botan::byte *)block.data(), block.size());

	return read_all_as_block(&pipe);
}

Block Base64::encodeBytes (const Block &block)
{
	Botan::Pipe pipe(new Botan::Base64_Encoder);
	pipe.process_msg(block.data(), block.size());
	Botan::SecureVector<Botan::byte> sv;

	return read_all_as_block(&pipe);
}

Block Base64::decodeBytes (const Block &block)
{
	Botan::Pipe pipe(new Botan::Base64_Decoder);
	pipe.process_msg((Botan::byte *)block.data(), block.size());

	return read_all_as_block(&pipe);
}

} /* namespace crypt */
} /* namespace core */
} /* namespace mailiverse */
