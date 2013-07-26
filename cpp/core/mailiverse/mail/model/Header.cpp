/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Header.h"
#include "mailiverse/utilities/Algorithm.h"
#include "mailiverse/utilities/Strings.h"

using namespace mailiverse::utilities;

using namespace mailiverse;
using namespace mailiverse::mail;
using namespace mailiverse::mail::model;

String Header::getRelativeDate ()
{
	String result = "Unknown";

	if (!date)
		result = "Infinity + 1";
	else
	{
		Date _now = Date::now();
		std::string now = simpleDateFormat("%Y%m%d%H%M%S", _now.getTime());
		std::string then = simpleDateFormat("%Y%m%d%H%M%S", date->getTime());

		if (startsWith(then, now.substr(0,10)))
		{
			long diff = (_now.getTime()/(60 * 1000)) - (date->getTime()/(60 * 1000));
			result = toString(diff) + " min";
		}
		else
		if (startsWith(then, now.substr(0,8)))
		{
			result = toLowerCase(simpleDateFormat("%I:%M %p", date->getTime()));
		}
		else
		{
			if (startsWith(then, now.substr(0,4)))
			{
				result = simpleDateFormat("%b %e", date->getTime());
			}
			else
			{
				result = simpleDateFormat("%m/%d/%y", date->getTime());
			}
		}
	}

	return result;
}
