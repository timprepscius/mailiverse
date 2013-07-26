/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Store.h"

using namespace mailiverse::mail::manager;
using namespace mailiverse;
using namespace mailiverse::mail::model;

/*
public Original get(String path)
{
	Original original = new Original(path);
	connector.get(path, new CallbackWithVariables(original){
		@Override
		public void invoke(Object... arguments)
		{
			Original original = V(0);
			if (arguments[0] instanceof Exception)
				original.setException((Exception)arguments[0]);
			else
				original.setData((byte[])arguments[0]);

			master.getEventPropagator().signal(Events.OriginalLoaded, original);
		}
	});

	return original;
}

*/
