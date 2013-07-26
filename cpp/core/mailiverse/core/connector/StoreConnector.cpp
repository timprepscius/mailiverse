/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "StoreConnector.h"
#include "mailiverse/utilities/Log.h"

using namespace mailiverse::core::connector;

StoreConnector::StoreConnector ()
{
	LogDebug(mailiverse::core::connector, "StoreConnector");
}

StoreConnector::~StoreConnector ()
{
	LogDebug(mailiverse::core::connector, "~StoreConnector");
}

StoreConnector::FileListing StoreConnector::list (const std::string &path, const std::pair<FileInfo::Date,FileInfo::Date> &dateRange) throws_ (ConnectorException)
{
	FileListing results;
	FileListing files = list(path);
	for (auto &i : files)
	{
		if ((dateRange.first < i.date || dateRange.first.getTime()==-1) &&
			(i.date < dateRange.second || dateRange.second.getTime()==-1))
		{
			results.push_back(i);
		}
	}

	return results;
}
