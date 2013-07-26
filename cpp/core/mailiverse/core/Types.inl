/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

inline Block toBlockFromFilled (int size, int fill)
{
	Block block;
	block.assign(size, fill);
	return block;
}

inline BigInteger toBigInteger (const Block &block)
{
	BigInteger result = BigInteger::decode(block.data(), block.size());
	
	int lastBit = block.size() * 8;
	if (result.get_bit(lastBit))
	{
		result.clear_bit(lastBit);
		result.set_sign(Botan::BigInt::Negative);
	}
	
	return result;
}

inline Block toBlock (const BigInteger &i)
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
	int totalBytes = std::ceil((i.bits() + 1.0)/8.0);
	int botanBytes = i.encoded_size();
	assert (totalBytes >= botanBytes);

	Block b;
	b.resize(totalBytes,0);
	BigInteger::encode(b.data() + (totalBytes - botanBytes), i);
	if (i.is_negative())
	{
		b[0] |= 1 << 8;
	}
	
	return b;
}

inline std::string toString (const Block &block)
{
	std::string result((char *)block.data(), block.size());
	assert(toBlock(result)==block);
	
	return result;
}


inline std::string toStringDecimal(const BigInteger &i)
{
	Botan::SecureVector<Botan::byte> v = BigInteger::encode(i, BigInteger::Decimal);
	std::string result((char *)v.begin(), v.size());
	
	assert(toBigIntegerDecimal(result)==i);
	return result;
}

inline BigInteger toBigIntegerDecimal(const std::string &s)
{
	BigInteger result(s);
	assert(toStringDecimal(result)==s);
	
	return result;
}

inline Block toBlock (const std::string &str)
{
	Block result(str.begin(), str.end());

	assert(toString(result) == str);
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
	
	assert(memcmp(begin, result.data(), size)==0);
	return result;
}

inline Block toBlock (const char *begin, const char *end)
{
	return Block(begin, end);
}

inline Block toBlockFromBase64 (const Block &block)
{
	Botan::Pipe pipe(new Botan::Base64_Decoder);
	pipe.process_msg(block.data(), block.size());

	return toBlock(pipe.read_all_as_string());
}

inline Block toBlockBase64 (const Block &block)
{
	Botan::Pipe pipe(new Botan::Base64_Encoder);
	pipe.process_msg(block.data(), block.size());
	Botan::SecureVector<Botan::byte> sv;
	
	return toBlock(pipe.read_all_as_string());
}

