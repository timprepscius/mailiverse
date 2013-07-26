/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "CryptorNone.h"
#include <sstream>
#include "mailiverse/utilities/Log.h"

using namespace mailiverse::core::crypt;
using namespace mailiverse;

Block CryptorNone::encrypt (const Block::Atom *begin, const Block::Atom *end)
{
	return Block(begin, end);
}

Block CryptorNone::decrypt (const Block::Atom *begin, const Block::Atom *end)
{
	return Block(begin, end);
}