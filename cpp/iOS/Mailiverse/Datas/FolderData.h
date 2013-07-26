/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef PirateMailViewer_UserDatas_h
#define PirateMailViewer_UserDatas_h

#include "mailiverse/mail/model/Lib.h"
#include "MMUtilities.h"

struct FolderData : public mailiverse::mail::model::Model::UserData
{
	mailiverse::mail::cache::ID key;
	mailiverse::mail::cache::Version version;

	mailiverse::Set<mailiverse::mail::cache::ID> selectedConversations;
	NSString *name;
	int count;
	
	FolderData(mailiverse::mail::model::Folder *f)
	{
		key = f->getID();
	}
	
	static FolderData *load(mailiverse::mail::model::Folder *f)
	{
		if (!f->getUserData<FolderData>())
			f->setUserData(new FolderData(f));
			
		FolderData *data = f->getUserData<FolderData>();
		data->refresh(f);
		
		return data;
	}
	
	void refresh (mailiverse::mail::model::Folder *f)
	{
		if (f->isLoaded())
		{
			if (version != f->getLocalVersion())
			{
				version = f->getLocalVersion();

				name = toNSString(f->getName());
				count = f->getNumConversations();
			}
		}
	}
} ;


DECLARE_SMARTPTR(FolderData);


#endif
