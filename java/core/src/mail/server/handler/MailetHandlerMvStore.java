/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.handler;


import core.connector.mv.ClientInfoMvStore;
import core.connector.mv.sync.ConnectorMvStore;
import core.connector.sync.EncryptedStoreConnector;
import core.connector.sync.StoreConnector;
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

		return new EncryptedStoreConnector(
			new CryptorRSAAES(CryptorRSAFactoryEnvironment.create(e)),
			new ConnectorMvStore(new ClientInfoMvStore(e.childEnvironment(ConstantsStorage.HANDLER_MV)))
		);
	}
}
