/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Random.h"
#include <botan/botan.h>
#include <botan/auto_rng.h>

using namespace mailiverse::core::util;
using namespace mailiverse::core;
using namespace mailiverse;

Random::Random() :
	_rng(NULL)
{
}

void Random::init()
{
	if (!_rng)
		_rng = (void *)new Botan::AutoSeeded_RNG();
}

Random::~Random ()
{
	Botan::AutoSeeded_RNG *rng = (Botan::AutoSeeded_RNG *)_rng;
	_rng = NULL;

	delete rng;
}

unsigned char Random::nextByte()
{
	init();
	
	Botan::AutoSeeded_RNG *rng = (Botan::AutoSeeded_RNG *)_rng;
	return rng->next_byte();
}

long Random::nextLong()
{
	long r = 0L;
	for (int i=0; i<sizeof(long); ++i)
	{
		r |= nextByte() << (8 * i);
	}
	
	return r;
}

Block Random::nextBytes (int size)
{
	Block b;
	while (size-- > 0)
	{
		b.push_back(nextByte());
	}
	
	return b;
}