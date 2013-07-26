/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Algorithm.h"

#include <iostream>
#include <limits.h>
#include <boost/date_time.hpp>
#include "boost/date_time/c_local_time_adjustor.hpp"
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/date_time/posix_time/posix_time_io.hpp>

using namespace mailiverse::utilities;
using namespace boost::posix_time;
using namespace std;

boost::posix_time::time_duration __utc_offset;
bool __utc_offset_initialized = false;

boost::posix_time::time_duration get_utc_offset() 
{
	if (__utc_offset_initialized)
		return __utc_offset;
		
    using namespace boost::posix_time;

    // boost::date_time::c_local_adjustor uses the C-API to adjust a
    // moment given in utc to the same moment in the local time zone.
    typedef boost::date_time::c_local_adjustor<ptime> local_adj;

    const ptime utc_now = second_clock::universal_time();
    const ptime now = local_adj::utc_to_local(utc_now);

    __utc_offset = now - utc_now;
	__utc_offset_initialized = true;
	
	return __utc_offset;
}

std::string mailiverse::utilities::simpleDateFormat (const std::string &format, long long date)
{
	std::ostringstream ss;
	time_facet *facet = new time_facet(format.c_str());
	ss.imbue(locale(cout.getloc(), facet));

	time_t t = date / 1000;
	ptime time = from_time_t(t);
	ss << time + get_utc_offset();

	return ss.str();
}
