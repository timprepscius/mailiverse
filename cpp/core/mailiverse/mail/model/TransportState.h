/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef TRANSPORTSTATE_H_
#define TRANSPORTSTATE_H_

#include "mailiverse/utilities/Algorithm.h"
#include "mailiverse/utilities/Strings.h"
#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/Types.h"

namespace mailiverse {
namespace mail {
namespace model {

class TransportState
{
protected:
	List<String> state;

public:
	TransportState() {}
	virtual ~TransportState() {}

	static const String
		RECEIVED,
		SENT,
		DRAFT,
		SENDING,
		TRASH,
		SPAM,
		READ, 
		ATTACHMENT;

	static const String
		DELIMITER;

	static TransportState *NONE()
	{
		return new TransportState();
	}

	void mark(const String &flag)
	{
		if (state.contains(flag))
			return;

		state.add(utilities::toUpperCase(flag));
	}

	void unmark (const String &flag)
	{
		state.remove(utilities::toUpperCase(flag));
	}

	void mark(const String &flag, bool state)
	{
		if (state)
			mark(flag);
		else
			unmark(flag);
	}

	void mark(TransportState *flags)
	{
		if (!flags)
			return;

		for (auto &flag : flags->state)
		{
			mark(flag);
		}
	}

	bool has (const String &flag)
	{
		return state.contains(utilities::toUpperCase(flag));
	}

	bool hasOne (TransportState *state)
	{
		for (auto &flag : state->state)
		{
			if (has(flag))
				return true;
		}

		return false;
	}

	bool hasAll (TransportState *state)
	{
		for (auto &flag : state->state)
		{
			if (has(flag))
				return false;
		}

		return true;
	}

	bool hasNot (const String &flag)
	{
		return !has(flag);
	}

	bool hasNone (TransportState *state)
	{
		return !hasOne(state);
	}

	String toString ()
	{
		return utilities::join(state, DELIMITER);
	}

	static TransportState *fromString (const String &flagString)
	{
		TransportState *state = new TransportState();

		if (!flagString.empty())
		{
			auto flags = utilities::split(flagString,DELIMITER);
			for (auto &flag : flags)
			{
				state->mark(flag);
			}
		}
		return state;
	}

	static TransportState *fromList (const List<String> &flags)
	{
		TransportState *state = new TransportState();

		for (auto &flag : flags)
		{
			state->mark(flag);
		}

		return state;
	}

};

DECLARE_SMARTPTR(TransportState);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* TRANSPORTSTATE_H_ */
