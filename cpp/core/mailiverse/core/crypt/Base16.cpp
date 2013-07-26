/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Base16.h"
#include <botan/botan.h>
#include "CryptorUtil.h"

namespace mailiverse {
namespace core {
namespace crypt {

std::string Base16::encode (const Block &block)
{
	Botan::Pipe pipe(new Botan::Hex_Encoder(Botan::Hex_Encoder::Lowercase));
	pipe.process_msg(block.data(), block.size());
	Botan::SecureVector<Botan::byte> sv;

	return pipe.read_all_as_string();
}

Block Base16::decode (const std::string &block)
{
	Botan::Pipe pipe(new Botan::Hex_Encoder(Botan::Hex_Encoder::Lowercase));
	pipe.process_msg((Botan::byte *)block.data(), block.size());

	return read_all_as_block(&pipe);
}

Block Base16::encodeBytes (const Block &block)
{
	Botan::Pipe pipe(new Botan::Hex_Encoder(Botan::Hex_Encoder::Lowercase));
	pipe.process_msg(block.data(), block.size());
	Botan::SecureVector<Botan::byte> sv;

	return read_all_as_block(&pipe);
}

Block Base16::decodeBytes (const Block &block)
{
	Botan::Pipe pipe(new Botan::Hex_Encoder(Botan::Hex_Encoder::Lowercase));
	pipe.process_msg((Botan::byte *)block.data(), block.size());

	return read_all_as_block(&pipe);
}

} /* namespace crypt */
} /* namespace core */
} /* namespace mailiverse */
