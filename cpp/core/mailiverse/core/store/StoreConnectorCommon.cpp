/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "StoreConnectorCommon.h"
#include "mailiverse/utilities/Strings.h"

using namespace mailiverse::core::store;
using namespace mailiverse::utilities;

StoreConnectorCommon::Headers StoreConnectorCommon::parseHeaders (const std::string &s)
{
	Headers headers;
	
	std::istringstream ss(s);
	while (!ss.eof())
	{
		const int bufSize = 4096;
		char buffer[bufSize];
		
		ss.getline(buffer, bufSize);
		int lineSize = ss.gcount();
		buffer[lineSize] = 0;
		std::string line(buffer);
		int colon = line.find(':');
		if (colon == -1)
			continue;
			
		std::string left = line.substr(0,colon);
		std::string right = line.substr(colon+1);
		right = trim(right);
		headers[left] = right;
	}
	
	return headers;
}

