/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.handler;

import core.connector.dropbox.ClientInfoDropbox;
import core.connector.dropbox.sync.DropboxConnector;
import core.connector.sync.EncryptedStoreConnector;
import core.connector.sync.StoreConnector;
import core.constants.ConstantsStorage;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAFactoryEnvironment;
import core.util.Environment;

public class MailetHandlerDropbox extends MailetHandlerDefault
{
	public StoreConnector createConnector (Environment e) throws Exception
	{
		log.info("MailetHandlerDropbox.createConnector");

		return new EncryptedStoreConnector(
			new CryptorRSAAES(CryptorRSAFactoryEnvironment.create(e)),
			new DropboxConnector(new ClientInfoDropbox(e.childEnvironment(ConstantsStorage.HANDLER_DROPBOX)))
		);
	}
}
