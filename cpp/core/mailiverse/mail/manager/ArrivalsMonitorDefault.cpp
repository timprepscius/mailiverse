/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "ArrivalsMonitorDefault.h"
#include "mailiverse/utilities/Thread.h"
#include "mailiverse/utilities/Log.h"
#include "mailiverse/utilities/Strings.h"
#include "Master.h"
#include "Events.h"
#include "Constants.h"

using namespace mailiverse::mail::manager;
using namespace mailiverse::core::connector;
using namespace mailiverse::core::crypt;
using namespace mailiverse;
using namespace mailiverse::utilities;

ArrivalsMonitorDefault::ArrivalsMonitorDefault(Cryptor *cryptor, StoreConnector *connector) :
	store(new core::connector::StoreConnectorEncrypted(cryptor, connector)),
	finished(false),
	checking(false),
	checkMailLock(
		connector, Constants::MAIL_IN_JSON_PREFIX + Constants::CHECK_MAIL_LOCK_FILE_NAME,
		Constants::CHECK_MAIL_LOCK_TIME_SECONDS, 
		Constants::CHECK_MAIL_LOCK_TIME_ALLOWED_BEFORE_RELOCK_SECONDS
	)
{
}

ArrivalsMonitorDefault::~ArrivalsMonitorDefault()
{
}

void ArrivalsMonitorDefault::markFinished ()
{
	finished = true;
}

void ArrivalsMonitorDefault::check ()
{
	try
	{
		checking = true;
		getMaster()->getCacheManager()->update();
		doCheck();
	}
	catch (Exception &e)
	{
		getMaster()->getEventPropagator()->signal(Events::CheckFailure, NULL);
		checking = false;
		throw e;
	}
}

bool ArrivalsMonitorDefault::isChecking ()
{
	return checking;
}

Vector<ArrivalsMonitorDefault::Listing> ArrivalsMonitorDefault::getListing ()
{
	StoreConnector::FileListing in = store->list(Constants::MAIL_IN_JSON_PREFIX, getMaster()->getArrivalsProcessor()->getRequestDateRange());
	StoreConnector::FileListing out = store->list(Constants::MAIL_OUT_JSON_PREFIX, getMaster()->getArrivalsProcessor()->getRequestDateRange());

	checkMailLock.testLock(in);

	Vector<Listing> result;
	for (auto &i : in)
	{
		if (endsWith(i.path, ".lock"))
			continue;
	
		Listing l;
		l.file = i;
		l.direction = Direction::IN;
		l.file.path = l.file.path;
		result.add(l);
	}

	for (auto &i : out)
	{
		Listing l;
		l.file = i;
		l.direction = Direction::OUT;
		l.file.path = l.file.path;
		result.add(l);
	}

	std::sort(result.begin(), result.end(), ListingComparator());
	return result;
}

void ArrivalsMonitorDefault::doCheck ()
{
	ArrivalsProcessor *arrivalsProcessor = getMaster()->getArrivalsProcessor();

	checking = true;
	getMaster()->getEventPropagator()->signal(Events::CheckBegin, NULL);
	
	checkMailLock.relock();
	auto listing = getListing();

	int totalOutstanding = 0;
	List<Listing> itemsToCheck;
	for (auto &i : listing)
	{
		if (!arrivalsProcessor->alreadyProcessed(i.file.path))
		{
			totalOutstanding++;
			
			if (totalOutstanding <= Constants::NUM_ITEMS_TO_CHECK_IN_ONE_CYCLE)
				itemsToCheck.push_back(i);
		}
	}
	
	int numChecked = 0;
	for (auto &i : itemsToCheck)
	{
		if (finished)
			break;

		checkMailLock.relock();
		
		numChecked++;

		arrivalsProcessor->getMaster()->getEventPropagator()->signal(
			Events::CheckStep, 
			utilities::newArg<StringPtr>(
				new String(toString(numChecked) + ":" + toString(itemsToCheck.size()) + ":" + toString(totalOutstanding))
			)
		);

		try
		{
			LogDebug (mailiverse::mail::manager::ArrivalsMonitor, "processing " << i.file.path);

			arrivalsProcessor->processSuccess (
				i.direction, i.file.path, i.file.date, store->get(i.file.path).first
			);
		}
		catch (Exception &e)
		{
			LogDebug (mailiverse::mail::manager::ArrivalsMonitor, "processing failing! " << i.file.path << " what " << e.what());

			arrivalsProcessor->processFailure (
				i.direction, i.file.path, i.file.date, e
			);
		}

/*
		if (++numChecked % NUM_PROCESSED_FLUSH == 0)
			getMaster()->getCacheManager()->flush();
*/
	}
	
	getMaster()->getEventPropagator()->signal(Events::CheckEnd, NULL);
	checking = false;
}
