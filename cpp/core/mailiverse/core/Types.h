/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_base_Types_h__
#define __mailiverse_core_base_Types_h__

#include <assert.h>

#include <vector>
#include <string>
#include "mailiverse/Exception.h"
#include "mailiverse/utilities/Strings.h"
#include <boost/static_assert.hpp>

#include <botan/botan.h>
#include <botan/bigint.h>
#include <botan/numthry.h>
#include <cmath>
#include "Block.h"

namespace mailiverse {

typedef unsigned char Byte;
typedef core::Block Block;
typedef Block Key;
typedef Block IV;
typedef Block Salt;
typedef Block Packet;
typedef Botan::BigInt BigInteger;

const int BlockMaxReadSize = 32 * 1024 * 1024;

//----------------------

inline Block toBlockFromFilled (int size, int fill=0);
inline BigInteger toBigInteger (const Block &block);
inline BigInteger toBigIntegerDecimal(const std::string &s);

namespace utilities 
{

template<>
inline std::string toString (const Block &block);

};

inline std::string toStringDecimal(const BigInteger &i);

inline Block toBlock (const BigInteger &i);
inline Block toBlock (const std::string &str);
inline Block toBlock (std::istream &is);
inline Block toBlock (std::ostringstream &os);
inline Block toBlock (const char *begin, const char *end);

template<typename T>
inline Block toBlockFromBase64 (const T &block);
inline Block toBlockBase64 (const Block &block);

template<typename T>
Block toBlockFromInitializer (const T &v);

inline BigInteger modPow(const BigInteger &b, const BigInteger &e, const BigInteger &m)
{	
	if (e.is_negative())
	{
		BigInteger en = e;
		en.flip_sign();
		return Botan::power_mod(Botan::inverse_mod(b, m), en, m);
	}
	
	return Botan::power_mod(b, e, m);
}

//----------------------

inline Block toBlockFromFilled (int size, int fill)
{
	Block block;
	block.assign(size, fill);
	return block;
}

inline BigInteger toBigInteger (const Block &_b)
{
	Block b = _b;
	BigInteger result;

	bool isNegative = b[0] & 0x80;
	
	if (isNegative)
		for (unsigned int i=0; i<b.size(); ++i)
			b[i] = ~b[i];
	
	result = BigInteger::decode(b.data(), b.size());

	if (isNegative)
	{
		result.set_sign(BigInteger::Negative);
		result -= 1;
	}	
	return result;
}

inline Block toBlock (const BigInteger &_v)
{
/*
	http://docs.oracle.com/javase/1.4.2/docs/api/java/math/BigInteger.html#toByteArray()
	public byte[] toByteArray()
	
	Returns a byte array containing the two's-complement representation of this BigInteger.
	The byte array will be in big-endian byte-order: the most significant byte is in the zeroth element. 
	The array will contain the minimum number of bytes required to represent this BigInteger, 
	including at least one sign bit, which is (ceil((this.bitLength() + 1)/8)). (This representation is 
	compatible with the (byte[]) constructor.)
*/
	BigInteger v = _v;
	int totalBytes = std::ceil((v.bits() + 1.0)/8.0);
	assert (totalBytes >= v.encoded_size());
	bool isNegative = v.is_negative();
	
	Block b;
	b.resize(totalBytes,0);
	if (isNegative)
		v += 1;

	BigInteger::encode(b.data() + (totalBytes - v.encoded_size()), v);

	if (isNegative)
		for (unsigned int i=0; i<b.size(); ++i)
			b[i] = ~b[i];
	
	return b;
}

inline std::string toStringDecimal(const BigInteger &i)
{
	return utilities::toString(i);
}

inline BigInteger toBigIntegerDecimal(const std::string &s)
{
	return utilities::fromString<BigInteger>(s);
}

inline Block toBlock (const std::string &str)
{
	Block result(str);
	return result;
}

inline Block toBlock (std::istream &is)
{
	std::string s;
	s.assign ((std::istreambuf_iterator<char>(is)), std::istreambuf_iterator<char>());
	
	return toBlock(s);
}

inline Block toBlock (std::ostringstream &os)
{
	return toBlock(os.str());
}

template<typename T>
Block toBlockFromInitializer (const T &v)
{
	Byte *begin = (Byte*)&v;
	int size = sizeof(T);
	Byte *end = begin + size;
	
	Block result(begin, end);
		return result;
}

inline Block toBlock (const char *begin, const char *end)
{
	return Block((u8*)begin, (u8*)end);
}

template<typename T>
inline Block toBlockFromBase64 (const T &block)
{
	Botan::Pipe pipe(new Botan::Base64_Decoder);
	pipe.process_msg((Botan::byte *)block.data(), block.size());

	return toBlock(pipe.read_all_as_string());
}

inline Block toBlockBase64 (const Block &block)
{
	Botan::Pipe pipe(new Botan::Base64_Encoder);
	pipe.process_msg(block.data(), block.size());
	Botan::SecureVector<Botan::byte> sv;
	
	return toBlock(pipe.read_all_as_string());
}

} // namespace

#endif
