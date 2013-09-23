/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import mail.client.cache.JSON;
import mail.client.model.AddressBook;
import mail.client.model.Identity;
import mail.client.model.UnregisteredIdentity;
import core.connector.async.AsyncStoreConnector;
import core.connector.async.AsyncStoreConnectorBase64;
import core.connector.async.AsyncStoreConnectorEncrypted;
import core.connector.dropbox.ClientInfoDropbox;
import core.connector.dropbox.async.ConnectorDropbox;
import core.connector.mv.ClientInfoMvStore;
import core.connector.mv.async.ConnectorMvStore;
import core.connector.s3.ClientInfoS3;
import core.connector.s3.async.S3Connector;
import core.constants.ConstantsEnvironmentKeys;
import core.constants.ConstantsClient;
import core.constants.ConstantsStorage;
import core.crypt.Cryptor;
import core.crypt.CryptorNone;
import core.crypt.CryptorRSA;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorSeed;
import core.crypt.CryptorRSAFactoryEnvironment;
import core.util.Environment;
import core.util.HttpDelegate;
import core.util.LogNull;
import core.util.LogOut;

public class Client 
{
	static LogNull log = new LogNull (Client.class);
	public static String HOST = ConstantsClient.KEY_AUTH_HOST;
	public static int KEYSERVER_PORT = ConstantsClient.KEY_AUTH_PORT;
	public static int SENDMAIL_PORT = 8080;
	
	HttpDelegate httpDelegate;
	Master master;
	
	public static Client start (Environment e, String identityString, HttpDelegate httpDelegate, EventDispatcher eventDispatcher) throws Exception
	{
		return new Client(e, identityString, httpDelegate, eventDispatcher);
	}
	
	public Client (Environment e, String identityString, HttpDelegate httpDelegate, EventDispatcher eventDispatcher) throws Exception
	{	
		ArrivalsMonitor arrivalsMonitor = null;
		this.httpDelegate = httpDelegate;
		
		Environment mailBoxEnvironment = e.childEnvironment("client");

		log.debug("environment");
		for (String k : mailBoxEnvironment.keySet())
			log.debug(k, mailBoxEnvironment.get(k));

		AddressBook addressBook = new AddressBook();

		Identity identity = addressBook.getIdentity(
			new UnregisteredIdentity(identityString)
		);
		identity.setPrimary(true);
		
		String handler = mailBoxEnvironment.get(ConstantsEnvironmentKeys.HANDLER);
		CryptorRSA cryptorRSA = CryptorRSAFactoryEnvironment.create (mailBoxEnvironment);
		CryptorRSAAES cryptorRSAAES = new CryptorRSAAES(cryptorRSA);
		
		AsyncStoreConnector connector = null;
		
		if (handler.equals(ConstantsStorage.HANDLER_DROPBOX))
		{
			Environment dbEnvironment = mailBoxEnvironment.childEnvironment(handler);
			ClientInfoDropbox clientInfo = new ClientInfoDropbox (dbEnvironment);
			connector = new ConnectorDropbox(clientInfo, httpDelegate);
		}
		else
		if (handler.equals(ConstantsStorage.HANDLER_S3))
		{
			Environment s3Environment = mailBoxEnvironment.childEnvironment(handler);
			ClientInfoS3 clientInfo = new ClientInfoS3 (s3Environment);
			connector = new S3Connector(clientInfo, httpDelegate);
		}
		else
		if (handler.equals(ConstantsStorage.HANDLER_MV))
		{
			Environment mvEnvironment = mailBoxEnvironment.childEnvironment(handler);
			ClientInfoMvStore clientInfo = new ClientInfoMvStore(mvEnvironment);
			connector = new ConnectorMvStore(clientInfo, httpDelegate);
		}
		else
		{
			throw new Exception ("Unknown handler");
		}
			
		TrackingConnector trackingConnector =  new TrackingConnector(connector);
		arrivalsMonitor = new ArrivalsMonitorDefault(cryptorRSAAES, trackingConnector);
		
		CryptorSeed cryptorSeed = new CryptorSeed(cryptorRSA.getPrivateKey());
		
		CacheManager manager = new CacheManager(
			cryptorSeed,
			new AsyncStoreConnectorBase64 (
				trackingConnector
			)
		);

		master = 
			new Master(
				new Store(cryptorRSAAES, trackingConnector),
				identity,
				mailBoxEnvironment,
				new Indexer (),
				addressBook,
				new ArrivalsProcessor (),
				arrivalsMonitor,
				eventDispatcher,
				new Actions(),
				new Mailer(httpDelegate),
				cryptorRSAAES,
				manager,
				new JSON()
			);
		
		trackingConnector.setMaster(master);
		
		master.start();
	}
	
	public HttpDelegate getHttpDelegate ()
	{
		return httpDelegate;
	}
	
	public Master getMaster ()
	{
		return master;
	}
	
	public void stop ()
	{
		master = null;
	}	
}
