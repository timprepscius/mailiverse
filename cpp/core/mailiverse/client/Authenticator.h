/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_Authenticator_h__
#define __mailiverse_mail_Authenticator_h__

#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/core/crypt/KeyPairFromPassword.h"
#include "StreamSessionClientGet.h"
#include "mailiverse/core/store/Environment.h"

namespace mailiverse {
namespace client {

class Authenticator : public StreamSessionClientGet::Delegate
{
public:
	struct Delegate 
	{
		virtual ~Delegate ();
		virtual void onStep (const std::string &) = 0;
		virtual void onSuccess (core::store::EnvironmentPtr environment) = 0;
		virtual void onFailure (const Exception &e) = 0;
	} ;

	DECLARE_SMARTPTR(Delegate);

protected:
	virtual void onSuccess (const Block &packet) throws_ (Exception);
	virtual void onFailure (const Exception &exception) throws_ (Exception);

	DelegatePtr delegate;
	core::crypt::KeyPairFromPasswordPtr keyPair;
	
public:
	Authenticator ();
	virtual ~Authenticator ();
	
	void setDelegate (Delegate *delegate);
	void authenticate (const String &user, const String &password);
} ;

DECLARE_SMARTPTR(Authenticator);

} // namespace
} // namespace

#endif
