/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef __Utilities_SafeContainer_h__
#define __Utilities_SafeContainer_h__

#include "Exception.h"
#include "FilledArray.h"

namespace mailiverse {
namespace utilities {

/**
 * the only exception the safe iteration templates throw
 */
DECLARE_EXCEPTION (SafeIterationException);

template<class T> class SafeIteration;

/**
 * provides an iterator template which is tightly bound to a safe iteration class
 */
template<class container, class iterator_type, class reference_type, class microsoftfix>
class SafeIterator : public iterator_type
{
	friend class SafeIteration<container>;

	private:
		SafeIterator (const SafeIterator<container, iterator_type, reference_type, microsoftfix> &copy);
	
	protected:
		const SafeIteration<container> &parent;

		iterator_type safe;
		bool valid;

	protected:
		void invalidate ()
		{
			valid = false;
			iterator_type::operator ++();
		}

		void invalidate (iterator_type i)
		{
			iterator_type::operator =(i);
			valid = false;
		}

	public:
		SafeIterator (const SafeIteration<container> &Parent) :
			parent (Parent)
		{ 
			valid = false;
			parent.addSafeIterator (this);
		}

		virtual ~SafeIterator ()
		{
			parent.removeSafeIterator (this);
		}

		SafeIterator<container, iterator_type, reference_type, microsoftfix> &operator =(const iterator_type &i)
		{
			valid = true;

			iterator_type::operator =(i);

			return *this;
		}

		reference_type operator *() const
		{
			validate ();

			return (reference_type)iterator_type::operator *();
		}

		SafeIterator<container, iterator_type, reference_type, microsoftfix> &operator ++() 
		{
			if (valid)
			{
				iterator_type::operator ++();
			}
			else
			{
				valid = true;
			};

			return *this;
		}

		SafeIterator<container, iterator_type, reference_type, microsoftfix> &operator --() 
		{
			iterator_type::operator --();

			if (!valid)
			{
				valid = true;
			}

			return *this;
		}

		bool isValid () const
		{
			return valid;
		}

		void validate () const throws_ (SafeIterationException)
		{
			if (!valid)
				throw SafeIterationException ("invalid iterator dereferenced");
		}
} ;

/**
 * provides a safe iteration wrapper for various container classes within STL
 *
 * use:
 *		Utilities::SafeIteration< std::set<int> > safe_integer_set;
 *
 *		Utilities::SafeIteration< std::set<int> >::safe_iterator i(safe_integer_set);
 *		for (i=safe_integer_set.begin(); i!=safe_integer_set.end(); ++i)
 *		{
 *			cout << (*i);
 *		}
 */
template<typename T>
class SafeIteration : public T
{
	public:
		typedef typename T::iterator T_iterator;
		typedef typename T::const_iterator T_const_iterator;

		typedef typename T::reference T_reference;
		typedef typename T::const_reference T_const_reference;

		typedef SafeIterator<T, T_iterator, T_reference, char> safe_iterator;
		typedef SafeIterator<T, T_const_iterator, T_const_reference, int> safe_const_iterator;

	protected:
		mutable FilledArray<safe_iterator *> iterators;
		mutable FilledArray<safe_const_iterator *> const_iterators;

		void ensureAllIterators (T_const_reference k) 
		{
			ensureAllIterators (find(k));
		}

		template<class Z>
		void ensureIteratorSet (const T_iterator &I, Z &set)
		{
			for (typename Z::iterator i=set.begin(); i!=set.end(); ++i)
			{
				if ( (*(*i))!=T::end() )
				{
					if ( (*(*i)) == I )
					{
						(*i)->invalidate ();
					}
				}
			}
		}

		void ensureAllIterators (const T_iterator &I) 
		{
			ensureIteratorSet (I, iterators);
			ensureIteratorSet (I, const_iterators);
		}

		template<class Z>
		void clearIteratorSet (Z &set)
		{
			for (typename Z::iterator i=set.begin(); i!=set.end(); ++i)
			{
				if ( (*(*i))!= T::end() )
				{
					(*i)->invalidate (T::end());
				}
			}
		}

		void clearAllIterators () 
		{
			clearIteratorSet (iterators);
			clearIteratorSet (const_iterators);
		}

	public:
		SafeIteration () 
		{
		}

		SafeIteration (const SafeIteration &copy)
		{
			T::operator =(copy);
		}

		SafeIteration &operator =(const SafeIteration &copy)
		{
			return T::operator =(copy);
		}

		inline void erase (T_const_reference k) 
		{ 
			ensureAllIterators(k); 
			T::erase(k); 
		}

		inline void erase (const T_iterator &i) 
		{ 
			T_iterator I(i); 
			ensureAllIterators(i); 
			T::erase(I); 
		}

		inline void clear () 
		{ 
			clearAllIterators(); 
			T::clear(); 
		}

	public:
		inline void addSafeIterator (safe_iterator *i) const 
		{ 
			iterators.insert (i); 
		}

		inline void addSafeIterator (safe_const_iterator *i) const 
		{ 
			const_iterators.insert (i); 
		}

		inline void removeSafeIterator (safe_iterator *i) const
		{ 
			iterators.erase (i); 
		}

		inline void removeSafeIterator (safe_const_iterator *i) const 
		{ 
			const_iterators.erase (i); 
		}
} ;

} // namespace Utilities 
} // namespace

#endif
