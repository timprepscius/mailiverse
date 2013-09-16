package mail.server.db;

import mail.server.postfix.ExternalDataPostfix;

public class ExternalDataFactory {

	public static ExternalData createInstance () throws Exception
	{
		return new ExternalDataPostfix();
	}
}
