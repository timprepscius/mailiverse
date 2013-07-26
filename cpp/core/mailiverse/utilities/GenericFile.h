/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef __Utilities_GenericFile_h__
#define __Utilities_GenericFile_h__

namespace mailiverse {
namespace utilities {
namespace GenericFile {

class In
{
	public:
		virtual ~In () {};
		virtual long seek (long seek, int origin) = 0;
		virtual long tell () = 0;
		virtual unsigned long read (char *buffer, unsigned long size) = 0;
		virtual long size () = 0;
} ;

} // namespace
} // namespace utilities
} // namespace mailiverse

#endif
