/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef FUNCTIONS_H_
#define FUNCTIONS_H_

#include "SmartPtr.h"
#include "Log.h"

namespace mailiverse {
namespace utilities {

class Binder
{
public:
	Binder () 
	{
		LogDebug(mailiverse::utilities::Functions, "Binder " << this);
	}
	
	virtual ~Binder() 
	{
		LogDebug(mailiverse::utilities::Functions, "~Binder " << this);	
	}
	
	virtual void operator()() const = 0;
};

template <typename F>
class Binder0 : public Binder
{
public:
    Binder0(const F& _func)
        : func(_func) { }
    void operator()() const { func(); }

private:
    F func;
};

template <typename F, typename T1>
class Binder1 : public Binder
{
public:
    Binder1(const F& _func, const T1& _p1)
        : func(_func), p1(_p1) { }
    void operator()() const { func(p1); }

private:
    F func;
    T1 p1;
};

template <typename F, typename T1, typename T2>
class Binder2 : public Binder
{
public:
    Binder2(const F& _func, const T1& _p1, const T2& _p2)
        : func(_func), p1(_p1), p2(_p2) { }
    void operator()() const { func(p1, p2); }

private:
    F func;
    T1 p1;
    T2 p2;
};

template <typename F, typename T1, typename T2, typename T3>
class Binder3 : public Binder
{
public:
    Binder3(const F& _func, const T1& _p1, const T2& _p2, const T3& _p3)
        : func(_func), p1(_p1), p2(_p2), p3(_p3) { }
    void operator()() const { func(p1, p2, p3); }

private:
    F func;
    T1 p1;
    T2 p2;
    T3 p3;
};

template <typename F, typename T1, typename T2, typename T3, typename T4>
class Binder4 : public Binder
{
public:
    Binder4(const F& _func, const T1& _p1, const T2& _p2, const T3& _p3, const T4 &_p4)
        : func(_func), p1(_p1), p2(_p2), p3(_p3),p4(_p4) { }
    void operator()() const { func(p1, p2, p3,p4); }

private:
    F func;
    T1 p1;
    T2 p2;
    T3 p3;
    T4 p4;
};

//-----------------------------------------

template <typename C, typename F>
class BinderC0 : public Binder
{
public:
    BinderC0(C _c, const F& _func)
        : c(_c),func(_func) { }
    void operator()() const { ((*c).*func)(); }

private:
    C c;
    F func;
};

template <typename C, typename F, typename T1>
class BinderC1 : public Binder
{
public:
    BinderC1(C _c, const F& _func, const T1& _p1)
        : c(_c), func(_func), p1(_p1) { }
    void operator()() const { ((*c).*func)(p1); }

private:
    C c;
    F func;
    T1 p1;
};

template <typename C, typename F, typename T1, typename T2>
class BinderC2 : public Binder
{
public:
    BinderC2(C _c, const F& _func, const T1& _p1, const T2& _p2)
        : c(_c), func(_func), p1(_p1), p2(_p2) { }
    void operator()() const { ((*c).*func)(p1, p2); }

private:
    C c;
    F func;
    T1 p1;
    T2 p2;
};

template <typename C, typename F, typename T1, typename T2, typename T3>
class BinderC3 : public Binder
{
public:
    BinderC3(C _c, const F& _func, const T1& _p1, const T2& _p2, const T3& _p3)
        : c(_c), func(_func), p1(_p1), p2(_p2), p3(_p3) { }
    void operator()() const { ((*c).*func)(p1, p2, p3); }

private:
    C c;
    F func;
    T1 p1;
    T2 p2;
    T3 p3;
};

template <typename C, typename F, typename T1, typename T2, typename T3, typename T4>
class BinderC4 : public Binder
{
public:
    BinderC4(C _c, const F& _func, const T1& _p1, const T2& _p2, const T3& _p3, const T4 &_p4)
        : c(_c), func(_func), p1(_p1), p2(_p2), p3(_p3),p4(_p4) { }
    void operator()() const { ((*c).*func)(p1, p2, p3,p4); }

private:
    C c;
    F func;
    T1 p1;
    T2 p2;
    T3 p3;
    T4 p4;
};

//-----------------------------------------

template<typename A1>
class Binder_1
{
public:
	typedef A1 Arg;
	
	Binder_1()
	{
		LogDebug(mailiverse::utilities::Functions, "Binder_1 " << this);
	}
	virtual ~Binder_1() 
	{
		LogDebug(mailiverse::utilities::Functions, "~Binder_1 " << this);
	}
	virtual void operator()(const Arg &) const = 0;
};

template <typename F, typename A1>
class Binder0_1 : public Binder_1<A1>
{
public:
    Binder0_1(const F &_func)
        : func(_func) { }
    void operator()(const A1 &a1) const { func(a1); }

private:
    F func;
};


template <typename F, typename T1, typename A1>
class Binder1_1 : public Binder_1<A1>
{
public:
    Binder1_1(const F &_func, const T1& _p1)
        : func(_func), p1(_p1) { }
    void operator()(const A1 &a1) const { func(p1, a1); }

private:
    F func;
    T1 p1;
};

template <typename F, typename T1, typename T2, typename A1>
class Binder2_1 : public Binder_1<A1>
{
public:
    Binder2_1(const F& _func, const T1& _p1, const T2& _p2)
        : func(_func), p1(_p1), p2(_p2) { }
    void operator()(const A1 &a1) const { func(p1, p2, a1); }

private:
    F func;
    T1 p1;
    T2 p2;
};

template <typename F, typename T1, typename T2, typename T3, typename A1>
class Binder3_1 : public Binder_1<A1>
{
public:
    Binder3_1(const F& _func, const T1& _p1, const T2& _p2, const T3& _p3)
        : func(_func), p1(_p1), p2(_p2), p3(_p3) { }
    void operator()(const A1 &a1) const { func(p1, p2, p3, a1); }

private:
    F func;
    T1 p1;
    T2 p2;
    T3 p3;
};

//-------------------------------------------

template <typename C, typename F, typename A1>
class BinderC0_1 : public Binder_1<A1>
{
public:
    BinderC0_1(C _c, const F &_func)
        : c(_c), func(_func) { }
    void operator()(const A1 &a1) const { ((*c).*func)(a1); }

private:
    C c;
    F func;
};


template <typename C, typename F, typename T1, typename A1>
class BinderC1_1 : public Binder_1<A1>
{
public:
    BinderC1_1(C _c, const F &_func, const T1& _p1)
        : c(_c), func(_func), p1(_p1) { }
    void operator()(const A1 &a1) const { ((*c).*func)(p1, a1); }

private:
    C c;
    F func;
    T1 p1;
};

template <typename C, typename F, typename T1, typename T2, typename A1>
class BinderC2_1 : public Binder_1<A1>
{
public:
    BinderC2_1(C _c, const F& _func, const T1& _p1, const T2& _p2)
        : c(_c), func(_func), p1(_p1), p2(_p2) { }
    void operator()(const A1 &a1) const { ((*c).*func)(p1, p2, a1); }

private:
    C c;
    F func;
    T1 p1;
    T2 p2;
};

template <typename C, typename F, typename T1, typename T2, typename T3, typename A1>
class BinderC3_1 : public Binder_1<A1>
{
public:
    BinderC3_1(C _c, const F& _func, const T1& _p1, const T2& _p2, const T3& _p3)
        : c(_c), func(_func), p1(_p1), p2(_p2), p3(_p3) { }
    void operator()(const A1 &a1) const { ((*c).*func)(p1, p2, p3, a1); }

private:
    C c;
    F func;
    T1 p1;
    T2 p2;
    T3 p3;
};


//-----------------------------------------

struct Argument
{
	Argument () 
	{
		LogDebug(mailiverse::utilities::Functions, "Argument " << this);
	}
	
	virtual ~Argument () 
	{
		LogDebug(mailiverse::utilities::Functions, "~Argument " << this);
	}
	
	virtual void *operator()() const = 0;
} ;

DECLARE_SMARTPTR(Argument);

template<typename A>
class Argument_ : public Argument
{
protected:
	A a;
public:
	Argument_(const A &_a) : a(_a) {}

	virtual void *operator()() const override
	{
		return a;
	}
} ;

class Binder_G : public Binder_1<Argument *>
{
public:
	virtual ~Binder_G() {}
};

DECLARE_SMARTPTR(Binder_G);

template<typename A1>
class Binder_GI : public Binder_G
{
protected:
	SmartPtr<Binder_1<A1>> b;

public:
	Binder_GI(Binder_1<A1> *_b) : b(_b){ }

	virtual void operator()(const Arg &a) const override
	{
		A1 p = (A1)((*a)());
		(*b)(p);
	}
};

template<>
class Binder_GI<void> : public Binder_G
{
protected:
	SmartPtr<Binder> b;

public:
	Binder_GI(Binder *_b) : b(_b){ }

	virtual void operator()(const Arg &a) const override
	{
		(*b)();
	}
};

//------------------------------------------

template<typename F>
Binder0<F> bind (const F &f) { return Binder0<F>(f); }

template<typename F, typename T1>
Binder1<F,T1> bind (const F &f, const T1 &p1) { return Binder1<F,T1>(f,p1); }

template<typename F, typename T1, typename T2>
Binder2<F,T1,T2> bind (const F &f, const T1 &p1, const T2 &p2) { return Binder2<F,T1,T2>(f,p1,p2); }

template<typename F, typename T1, typename T2, typename T3>
Binder3<F,T1,T2,T3> bind (const F &f, const T1 &p1, const T2 &p2, const T3 &p3) { return Binder3<F,T1,T2,T3>(f,p1,p2,p3); }

template<typename F, typename T1, typename T2, typename T3, typename T4>
Binder4<F,T1,T2,T3,T4> bind (const F &f, const T1 &p1, const T2 &p2, const T3 &p3, const T4 &p4) { return Binder4<F,T1,T2,T3,T4>(f,p1,p2,p3,p4); }

template<typename A1, typename F>
Binder0_1<F,A1> bind_1 (const F &f) { return Binder0_1<F,A1>(f); }

template<typename A1, typename F, typename T1>
Binder1_1<F,T1,A1> bind_1 (const F &f, const T1 &p1) { return Binder1_1<F,T1,A1>(f,p1); }

template<typename A1, typename F, typename T1, typename T2>
Binder2_1<F,T1,T2,A1> bind_1 (const F &f, const T1 &p1, const T2 &p2) { return Binder2_1<F,T1,T2,A1>(f,p1,p2); }

template<typename A1, typename F, typename T1, typename T2, typename T3>
Binder3_1<F,T1,T2,T3,A1> bind_1 (const F &f, const T1 &p1, const T2 &p2, const T3 &p3) { return Binder3_1<F,T1,T2,T3,A1>(f,p1,p2,p3); }

//-----------------------------------------------

template<typename F>
Binder *newbind (const F &f) { return new Binder0<F>(f); }

template<typename F, typename T1>
Binder *newbind (const F &f, const T1 &p1) { return new Binder1<F,T1>(f,p1); }

template<typename F, typename T1, typename T2>
Binder *newbind (const F &f, const T1 &p1, const T2 &p2) { return new Binder2<F,T1,T2>(f,p1,p2); }

template<typename F, typename T1, typename T2, typename T3>
Binder *newbind (const F &f, const T1 &p1, const T2 &p2, const T3 &p3) { return new Binder3<F,T1,T2,T3>(f,p1,p2,p3); }

template<typename F, typename T1, typename T2, typename T3, typename T4>
Binder *newbind (const F &f, const T1 &p1, const T2 &p2, const T3 &p3, const T4 &p4) { return new Binder4<F,T1,T2,T3,T4>(f,p1,p2,p3,p4); }

//-----------------------------------------------

template<typename C, typename F>
Binder *newbindC (C c, const F &f) { return new BinderC0<C,F>(c, f); }

template<typename C, typename F, typename T1>
Binder *newbindC (C c, const F &f, const T1 &p1) { return new BinderC1<C,F,T1>(c,f,p1); }

template<typename C, typename F, typename T1, typename T2>
Binder *newbindC (C c, const F &f, const T1 &p1, const T2 &p2) { return new BinderC2<C, F,T1,T2>(c,f,p1,p2); }

template<typename C, typename F, typename T1, typename T2, typename T3>
Binder *newbindC (C c, const F &f, const T1 &p1, const T2 &p2, const T3 &p3) { return new BinderC3<C,F,T1,T2,T3>(c,f,p1,p2,p3); }

template<typename C, typename F, typename T1, typename T2, typename T3, typename T4>
Binder *newbindC (C c, const F &f, const T1 &p1, const T2 &p2, const T3 &p3, const T4 &p4) { return new BinderC4<C,F,T1,T2,T3,T4>(c,f,p1,p2,p3,p4); }
//--------------------

template<typename A1, typename F>
Binder_1<A1> *newbind_1 (const F &f) { return new Binder0_1<F,A1>(f); }

template<typename A1, typename F, typename T1>
Binder_1<A1> *newbind_1 (const F &f, const T1 &p1) { return new Binder1_1<F,T1,A1>(f,p1); }

template<typename A1, typename F, typename T1, typename T2>
Binder_1<A1> *newbind_1 (const F &f, const T1 &p1, const T2 &p2) { return new Binder2_1<F,T1,T2,A1>(f,p1,p2); }

template<typename A1, typename F, typename T1, typename T2, typename T3>
Binder_1<A1> *newbind_1 (const F &f, const T1 &p1, const T2 &p2, const T3 &p3) { return new Binder3_1<F,T1,T2,T3,A1>(f,p1,p2,p3); }

//----------------------------------------

template<typename A1, typename C, typename F>
Binder_1<A1> *newbindC_1 (C c, const F &f) { return new BinderC0_1<C, F,A1>(c,f); }

template<typename A1, typename C, typename F, typename T1>
Binder_1<A1> *newbindC_1 (C c, const F &f, const T1 &p1) { return new BinderC1_1<C,F,T1,A1>(c,f,p1); }

template<typename A1, typename C, typename F, typename T1, typename T2>
Binder_1<A1> *newbindC_1 (C c, const F &f, const T1 &p1, const T2 &p2) { return new BinderC2_1<C, F,T1,T2,A1>(c, f,p1,p2); }

template<typename A1, typename C, typename F, typename T1, typename T2, typename T3>
Binder_1<A1> *newbindC_1 (C c, const F &f, const T1 &p1, const T2 &p2, const T3 &p3) { return new BinderC3_1<C,F,T1,T2,T3,A1>(c, f,p1,p2,p3); }

//--------------------

template<typename A1, typename F>
Binder_G *newbind_G (const F &f) { return new Binder_GI<A1>(newbind_1<A1>(f)); }

template<typename A1, typename F, typename T1>
Binder_G *newbind_G (const F &f, const T1 &p1) { return new Binder_GI<A1>(newbind_1<A1>(f,p1)); }

template<typename A1, typename F, typename T1, typename T2>
Binder_G *newbind_G (const F &f, const T1 &p1, const T2 &p2) { return new Binder_GI<A1>(newbind_1<A1>(f,p1,p2)); }

template<typename A1, typename F, typename T1, typename T2, typename T3>
Binder_G *newbind_G (const F &f, const T1 &p1, const T2 &p2, const T3 &p3) { return new Binder_GI<A1>(newbind_1<A1>(f,p1,p2,p3)); }

//--------------------

template<typename C, typename F>
Binder_G *newbindC_GV (C c, const F &f) { return new Binder_GI<void>(newbindC<C>(c,f)); }

template<typename A1, typename C, typename F>
Binder_G *newbindC_G (C c, const F &f) { return new Binder_GI<A1>(newbindC_1<A1,C>(c,f)); }

template<typename A1, typename C, typename F, typename T1>
Binder_G *newbindC_G (C c, const F &f, const T1 &p1) { return new Binder_GI<A1>(newbindC_1<A1,C>(c,f,p1)); }

template<typename A1, typename C, typename F, typename T1, typename T2>
Binder_G *newbindC_G (C c, const F &f, const T1 &p1, const T2 &p2) { return new Binder_GI<A1>(newbindC_1<A1,C>(c,f,p1,p2)); }

template<typename A1, typename C, typename F, typename T1, typename T2, typename T3>
Binder_G *newbindC_G (C c, const F &f, const T1 &p1, const T2 &p2, const T3 &p3) { return new Binder_GI<A1>(newbindC_1<A1,C>(c,f,p1,p2,p3)); }

//--------------------

template<typename A1, typename V>
Argument *newArg (const V &a1) { return new Argument_<A1>(a1); }

} /* namespace utilities */
} /* namespace mailiverse */
#endif /* FUNCTIONS_H_ */
