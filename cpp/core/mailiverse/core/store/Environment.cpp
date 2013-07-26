/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Environment.h"
#include "mailiverse/utilities/Log.h"

using namespace mailiverse::core::store;

Environment::Environment ()
{
	LogDebug(mailiverse::core::store, "Environment");
}

Environment::~Environment ()
{
	LogDebug(mailiverse::core::store, "~Environment");
}

void Environment::put(const Key &key, const Value &value)
{
	keyValues[key] = value;
}

bool Environment::has(const Key &key) const
{
	return keyValues.find(key)!=keyValues.end();
}

const Environment::Value &Environment::getOrDefault (const Key &key, const Value &_default) const
{
	KeyValues::const_iterator i = keyValues.find(key);
	if (i==keyValues.end())
		return _default;
		
	return i->second;
}

const Environment::Value &Environment::get(const Key &key) const throws_ (Exception)
{
	KeyValues::const_iterator i = keyValues.find(key);
	if (i==keyValues.end())
		throw Exception("Key not found " + key);
		
	return i->second;
}

Environment *Environment::childEnvironment (const Key &key) const
{
	std::string keySlash = key + "/";
	Environment *e = new Environment();
	for (auto &i : keyValues)
	{
		if (utilities::startsWith(i.first, keySlash))
			e->put(i.first.substr(keySlash.length()), i.second);
	}

	return e;
}
