/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Initializer.h"
#include "Master.h"
#include "Events.h"
#include "../model/Settings.h"
#include "mailiverse/External.h"
#include "mailiverse/core/constants/ConstantsSettings.h"
#include "mailiverse/core/constants/ConstantsPushNotifications.h"

using namespace mailiverse::mail::manager;
using namespace mailiverse::core::constants;

Initializer::Initializer() :
	ready(false)
{
}

Initializer::~Initializer()
{
}

bool Initializer::isReady ()
{
	return ready;
}

void Initializer::start ()
{
	getMaster()->getEventPropagator()->add(
		Events::Initialize_FirstRun, this,
		EventPropagator::Callback (
			utilities::newbindC_GV(this, &Initializer::onFirstRun)
		)
	);

	getMaster()->getEventPropagator()->add(
		Events::Initialize_IndexedCacheLoadComplete, this,
		EventPropagator::Callback (
			utilities::newbindC_GV(this, &Initializer::onCacheInitialized)
		)
	);

	getMaster()->getEventPropagator()->add(
		Events::Initialize_IndexedCacheLoadFailed, this,
		EventPropagator::Callback (
			utilities::newbindC_GV(this, &Initializer::onCacheFailed)
		)
	);

	getMaster()->getEventPropagator()->add(
		Events::Initialize_FolderLoadComplete, this,
		EventPropagator::Callback (
			utilities::newbindC_GV(this, &Initializer::onFolderLoadComplete)
		)
	);

	getMaster()->getEventPropagator()->add(
		Events::Initialized, this,
		EventPropagator::Callback (
			utilities::newbindC_GV(this, &Initializer::onInitialized)
		)
	);

	getMaster()->getEventPropagator()->add(
		Events::SettingsLoaded, this,
		EventPropagator::Callback (
			utilities::newbindC_GV(this, &Initializer::onSettingsLoaded)
		)
	);

	getMaster()->getCacheManager()->initialize();
}

void Initializer::onFirstRun ()
{
	getMaster()->getCacheManager()->onFirstRun();
	getMaster()->getIndexer()->onFirstRun();

	ready = true;
	getMaster()->getEventPropagator()->signal(Events::Initialized, NULL);
}

void Initializer::onCacheInitialized ()
{
	getMaster()->getIndexer()->initialize();

	ready = true;	
	getMaster()->getEventPropagator()->signal(Events::Initialized, NULL);
}

void Initializer::onCacheFailed ()
{

}

void Initializer::onFolderLoadComplete ()
{

}

void Initializer::onInitialized()
{
	getMaster()->getActions()->onInitialized();
}

void Initializer::onSettingsLoaded ()
{
	model::SettingsPtr settings = getMaster()->getCacheManager()->getSettings();
	
	bool hasNotifications = 
		settings->get(ConstantsSettings::NOTIFICATION_TYPE,ConstantsPushNotifications::NOTIFICATION_TYPE_NONE) !=
		ConstantsPushNotifications::NOTIFICATION_TYPE_NONE;
		
	if (hasNotifications)
		__onEnableDeviceNotifications__(true);
}