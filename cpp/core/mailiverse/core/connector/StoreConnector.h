/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_connector_StoreConnector_h__
#define __mailiverse_core_connector_StoreConnector_h__

#include "ConnectorException.h"
#include "mailiverse/Exception.h"
#include "mailiverse/core/Types.h"
#include <list>
#include "mailiverse/core/connector/FileInfo.h"
#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/Types.h"

namespace mailiverse {
namespace core {
namespace connector {

class StoreConnector 
{	
public:
	StoreConnector ();
	virtual ~StoreConnector();
	
	typedef std::pair<Block,FileInfo::Version> VersionedBlock;
	
	virtual void open () throws_ (ConnectorException) = 0;
	virtual void close () throws_ (ConnectorException) = 0;
	
	typedef List<FileInfo> FileListing;
	virtual FileListing list (const std::string &path) throws_ (ConnectorException) = 0;
	virtual FileListing list (const std::string &path, const std::pair<FileInfo::Date,FileInfo::Date> &dateRange) throws_ (ConnectorException);

	virtual void createDirectory (const std::string &path) throws_ (ConnectorException) = 0;
	
	virtual VersionedBlock get (const std::string &path) throws_ (ConnectorException) = 0;
	virtual VersionedBlock get (const std::string &path, FileInfo::Size size) throws_ (ConnectorException) = 0;

	virtual FileInfo::Version put (const std::string &path, const Block &contents) throws_ (ConnectorException) = 0;
	virtual void move (const std::string &from, const std::string &to) throws_ (ConnectorException) = 0;
	virtual void remove (const std::string &path) throws_ (ConnectorException) = 0;
	
	virtual bool ensureDirectories (const std::list<std::string> &folders) = 0;
} ;

DECLARE_SMARTPTR (StoreConnector);

} // namespace
} // namespace
} // namespace

#endif
