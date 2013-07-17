/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.handler;


import core.connector.s3.ClientInfoS3;
import core.connector.s3.sync.S3Connector;
import core.connector.sync.EncryptedStoreConnector;
import core.connector.sync.StoreConnector;
import core.constants.ConstantsStorage;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAFactoryEnvironment;
import core.util.Environment;
import core.util.LogOut;


public class MailetHandlerS3 extends MailetHandlerDefault
{
	static LogOut log = new LogOut(MailetHandlerS3.class);

	public StoreConnector createConnector (Environment e) throws Exception
	{
		log.debug("MailetHandlerS3.createConnector");

		return new EncryptedStoreConnector(
			new CryptorRSAAES(CryptorRSAFactoryEnvironment.create(e)),
			new S3Connector(new ClientInfoS3(e.childEnvironment(ConstantsStorage.HANDLER_S3)))
		);
	}
}
