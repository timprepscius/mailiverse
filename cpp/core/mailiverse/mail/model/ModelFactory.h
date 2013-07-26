/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_ModelFactory_h__
#define __mailiverse_mail_model_ModelFactory_h__

#include "../cache/ItemFactory.h"
#include "Folder.h"
#include "FolderFilterSimple.h"
#include "FolderFilterSet.h"
#include "FolderSet.h"
#include "FolderMaster.h"
#include "FolderPart.h"
#include "FolderRepository.h"
#include "mailiverse/utilities/Log.h"
#include "Types.h"

namespace mailiverse {
namespace mail {
namespace model {

class ModelFactory : public cache::ItemFactory
{
protected:
	Library *library;

public:
	ModelFactory(Library *_library) 
	{
		library = _library;
	}
	
	virtual ~ModelFactory() {}

	virtual cache::Item *instantiate(const cache::Type &type) override
	{
		Model *result = NULL;
		switch (type)
		{
			case Types::Mail:
				result = new Mail();
			break;
			case Types::Conversation:
				result = new Conversation();
			break;
			case Types::FolderMaster :
				result = new FolderMaster();
			break;
			case Types::FolderFilter:
				result = new FolderFilterSimple();
			break;
			case Types::FolderRepository:
				result = new FolderRepository();
			break;
			case Types::FolderFilterSet:
				result = new FolderFilterSet();
			break;
			case Types::FolderPart:
				result = new FolderPart();
			break;
			default:
				assert(false);
			break;
		}

		if (result)
			result->setLibrary(library);
			
		LogDebug(mailiverse::mail::model::ModelFactory, "instantiated " << result->getClassName());
		return result;
	}
};

DECLARE_SMARTPTR(ModelFactory);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_manager_FolderFactory_h__ */
