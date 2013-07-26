/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef CALLBACK_H_
#define CALLBACK_H_

#include <tr1/functional>
#include "mailiverse/Exception.h"
#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/utilities/Functions.h"

namespace mailiverse {
namespace core {
namespace util {

template<typename T=void>
class Callback
{
public:
	typedef utilities::Binder_1<T> Success;
	utilities::SmartPtr<Success> success;

	typedef utilities::Binder_1<Exception> Failure;
	utilities::SmartPtr<Failure> failure;

public:
	Callback () {}

	Callback(Success *_success, Failure *_failure=NULL) :
		success(_success),
		failure(_failure)
	{

	}

	void invoke(const T &v)
	{
		if (success)
			(*success)(v);
	}

	void invoke(const Exception &e)
	{
		if (failure)
			(*failure)(e);
	}
};

template<>
class Callback<void>
{
public:
	typedef utilities::Binder Success;
	utilities::SmartPtr<Success> success;

	typedef utilities::Binder_1<Exception> Failure;
	utilities::SmartPtr<Failure> failure;

public:
	Callback () {}

	Callback(Success *_success, Failure *_failure=NULL) :
		success(_success),
		failure(_failure)
	{}

	void invoke ()
	{
		if (success)
			(*success)();
	}

	void invoke (const Exception &e)
	{
		if (failure)
			(*failure)(e);
	}
};

class CallbackGeneric
{
public:
	typedef utilities::Binder_G Success;
	utilities::SmartPtr<Success> success;

	typedef utilities::Binder_1<Exception> Failure;
	utilities::SmartPtr<Failure> failure;

public:
	CallbackGeneric () {}

	CallbackGeneric(Success *_success, Failure *_failure=NULL) :
		success(_success),
		failure(_failure)
	{

	}

	void invoke(utilities::Argument *a=NULL)
	{
		if (success)
			(*success)(a);
	}

	void invoke(const Exception &e)
	{
		if (failure)
			(*failure)(e);
	}
};


} /* namespace util */
} /* namespace core */
} /* namespace mailiverse */

#endif /* CALLBACK_H_ */
