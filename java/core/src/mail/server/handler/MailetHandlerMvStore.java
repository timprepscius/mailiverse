/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.handler;


import core.connector.mv.ClientInfoMvStore;
import core.connector.mv.sync.ConnectorMvStore;
import core.connector.sync.EncryptedStoreConnector;
import core.connector.sync.StoreConnector;
import core.constants.ConstantsClient;
import core.constants.ConstantsStorage;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAFactoryEnvironment;
import core.util.Environment;
import core.util.LogOut;


public class MailetHandlerMvStore extends MailetHandlerDefault
{
	static LogOut log = new LogOut(MailetHandlerMvStore.class);

	public StoreConnector createConnector (Environment e) throws Exception
	{
		log.debug("createConnector");
		
		ConnectorMvStore connector = 
			new ConnectorMvStore(new ClientInfoMvStore(e.childEnvironment(ConstantsStorage.HANDLER_MV)));

		// this is a really dirty hack getting around that when you install
		// using a fake key set (your first install, 
		connector.setEndpointOverride("http://127.0.0.1" + ConstantsClient.TOMCAT_URL_PORTION);
		
		return new EncryptedStoreConnector(
			new CryptorRSAAES(CryptorRSAFactoryEnvironment.create(e)),
			connector
		);
	}
}
