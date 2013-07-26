/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __Utilities_FilledArray_h__
#define __Utilities_FilledArray_h__

#include <assert.h>

namespace mailiverse {
namespace utilities {

/**
 * std list and vector and set are too slow for some reason, need fast allocation, with
 */
template<typename T, int N=32>
class FilledArray
{
	private:
		bool alloc;
		int allocsize;
		int length;
		T staticArray[N];
		T *array;

	public:
		typedef T *iterator;
		typedef const T *const_iterator;

		inline iterator begin() { return array; }
		inline iterator end () { return array + length; }

		inline const_iterator begin() const { return array; }
		inline const_iterator end () const { return array + length; }

		inline T &front () { return *array; }
		inline T &back () { return *(array + (length - 1)); }
	
	private:
		FilledArray (const FilledArray &);
		FilledArray &operator =(const FilledArray &);

	public:
		inline FilledArray () : 
			allocsize(N), 
			length(0), 
			array(staticArray),
			alloc(true)
		{ 
		}

		inline FilledArray (T *_array, int _allocsize, int _length=0, bool _alloc=false) : 
			array(_array), 
			allocsize(_allocsize),
			length(_length),
			alloc(_alloc)
		{
		}
		
		inline FilledArray (int _allocsize) :
			array (_allocsize ? new T[_allocsize] : NULL),
			alloc(true),
			length(0),
			allocsize (_allocsize)
		{
		}

		~FilledArray () 
		{ 
			if (alloc && array && (array!=staticArray))
				delete[] array; 
		}

		inline void insert (const T &element) 
		{
			if (length == allocsize)
			{
				if (!alloc)
				{
					assert(length < allocsize);
					return;
				}

				// if there was an allocsize then use that allocsize doubled, else use
				// some default first value, of N
				
				int newallocsize = allocsize ? allocsize << 1 : N;
				T *newarray = new T[newallocsize];
				
				if (array)
				{
					memcpy (newarray, array, sizeof(T) * allocsize);
					if (array != staticArray)
						delete[] array;
				}

				array = newarray;
				allocsize = newallocsize;
			}

			++length;
			back() = element;
		}

		inline void erase (iterator i)
		{
			assert (length > 0);

			// decrease length
			--length;

			if (length > 0)
				*i = *end();
		}

		inline void erase (const T &element)
		{
			assert (length > 0);

			// decrease length
			--length;

			// now iterator all except for what used to be the end element 
			for (register iterator i=begin(); i!=end(); ++i)
			{
				if (*i == element)
				{
					// the end element was the last element
					*i = *end();
					break;
				}
			}
		}
		
		inline iterator find (const T &element)
		{
			// now iterator all except for what used to be the end element 
			for (register iterator i=begin(); i!=end(); ++i)
			{
				if (*i == element)
				{
					// the end element was the last element
					return i;
				}
			}
			
			return end();
		}

		inline int size () const
		{
			return length;
		}

		inline void clear ()
		{
			length = 0;
		}
} ;

} // namespace Utilities 
} // namespace Utilities

#endif
