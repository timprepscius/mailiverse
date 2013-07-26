/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse__mail_model_Item_h__
#define __mailiverse__mail_model_Item_h__

#include "../cache/Item.h"

namespace mailiverse {
namespace mail {
namespace model {

class Library;

class Model : public cache::Item
{
public:
	struct UserData 
	{
		virtual ~UserData () {}
	} ;
	
	DECLARE_SMARTPTR(UserData);
	typedef cache::Item Super;

protected:
	UserDataPtr userData;
	Library *library;

public:
	Model() : library(NULL) {}
	virtual ~Model() {}
	
	void setUserData (UserData *userData)
	{
		this->userData = userData;
	}
	
	template<typename T>
	T *getUserData ()
	{
		return dynamic_cast<T *>((UserData *)userData);
	}
	
	void setLibrary (Library *_library)
	{
		library = _library;
	}
	
	Library *getLibrary ()
	{
		return library;
	}
};

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* ITEM_H_ */
