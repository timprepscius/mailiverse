/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Master.h"
#include "Lib.h"
#include "mailiverse/core/connector/Lib.h"
#include "mailiverse/core/store/Lib.h"
#include "mailiverse/core/crypt/Lib.h"
#include "EventDispatcherThreaded.h"
#include "AsyncActions.h"

using namespace mailiverse::mail::manager;
using namespace mailiverse::core::connector;
using namespace mailiverse::core::constants;
using namespace mailiverse::mail::model;
using namespace mailiverse::core::store;
using namespace mailiverse::core::crypt;
using namespace mailiverse::utilities;
using namespace mailiverse;

Master *Master::create (const String &identityString, core::store::Environment *environment)
{
	EnvironmentPtr clientEnvironment = environment->childEnvironment(ConstantsEnvironmentKeys::CLIENT);
	TrackingConnector *connector = new TrackingConnector(StoreConnectorFactory::create(*clientEnvironment));
	
	AddressBookPtr addressBook = new AddressBook();
	IdentityPtr identity = addressBook->getIdentity(
		new UnregisteredIdentity(identityString)
	);
	identity->setPrimary(true);
	
	CryptorRSAPtr cryptorRSA = CryptorRSAFactory::create(*clientEnvironment);
	CryptorRSAAESPtr cryptorRSAAES = new CryptorRSAAES(cryptorRSA);
	CryptorSeed cryptorSeed(Base64::decode(toString(cryptorRSA->getPrivateKey())));

	Master *master = new Master (
		new manager::Store(cryptorRSAAES, connector),
		identity,
		clientEnvironment,
		new Indexer(),
		addressBook,
		new ArrivalsProcessor(),
		new ArrivalsMonitorDefault(cryptorRSAAES, connector),
		new EventDispatcherThreaded(),
		new AsyncActions(),
		new Mailer(NULL),
		cryptorRSAAES,
		new CacheManager(cryptorSeed, connector)
	);

	connector->setMaster(master);
	
	return master;
}
