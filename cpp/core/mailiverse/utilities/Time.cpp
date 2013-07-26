/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#include "Time.h"
#include <time.h>

using namespace mailiverse;

void utilities::sleepSeconds(float seconds)
{
	usleep(1000000 * seconds);
}
