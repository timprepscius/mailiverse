/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_utilities_Streams_h__
#define __mailiverse_utilities_Streams_h__

namespace mailiverse {
namespace utilities {
 
template<typename T>
struct MemStreamBuf : public std::streambuf
{
    MemStreamBuf(const T* b, int s)
	{
        setg((char *)b, (char *)b, (char *)b + s);
	}

    MemStreamBuf(const T* b, const T *e)
    {
        setg((char *)b, (char *)b, (char *)e);
    }
};

} // namespace
} // namespace

#endif
