/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_Environment_h__
#define __mailiverse_mail_Environment_h__

#include "mailiverse/core/Types.h"
#include "mailiverse/utilities/SmartPtr.h"
#include <map>
#include <string>

namespace mailiverse {
namespace core {
namespace store {

class Environment
{
public:
	typedef std::string Key;
	typedef std::string Value;

protected:
	typedef std::map<Key, Value> KeyValues; 
	KeyValues keyValues;
	
public:
	Environment ();
	virtual ~Environment ();
	
	void put(const Key &key, const Value &value);
	bool has(const Key &key) const;
	const Value &getOrDefault(const Key &key, const Value &value) const;;
	const Value &get(const Key &key) const throws_ (Exception);

	Environment *childEnvironment (const Key &key) const;
} ;

DECLARE_SMARTPTR(Environment);

} // namespace
} // namespace
} // namespace

#endif
