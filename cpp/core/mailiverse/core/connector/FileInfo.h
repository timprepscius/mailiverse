/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_connector_FileInfo_h__
#define __mailiverse_core_connector_FileInfo_h__

#include "ConnectorException.h"
#include "mailiverse/Exception.h"
#include "mailiverse/Types.h"
#include "mailiverse/core/Types.h"

namespace mailiverse {
namespace core {
namespace connector {

class FileInfo 
{

public:
	String path;
	String relativePath;

	typedef long Size;
	Size size;

	typedef mailiverse::Date Date;
	Date date;

	typedef String Version;
	Version version;

	FileInfo () : date(Date::None) {}

	FileInfo(const std::string &_path, const std::string &_relativePath, Size _size, Date _date, const Version &_version) :
		path(_path),
		relativePath(_relativePath),
		size(_size),
		date(_date),
		version(_version)
	{
	}

	std::string getFileName() const
	{
		int lastSlash = path.rfind('/');
		if (lastSlash == -1)
			return path;
		
		return path.substr(lastSlash+1);
	}

	static bool orderByDate (const FileInfo &l, const FileInfo &r)
	{
		return l.date < r.date;
	}
} ;


} // namespace
} // namespace
} // namespace

#endif
