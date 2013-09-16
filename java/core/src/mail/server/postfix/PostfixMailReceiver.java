package mail.server.postfix;

import java.io.ByteArrayInputStream;

import mail.server.handler.UserInformation;
import mail.server.handler.UserInformationFactory;

import core.util.Streams;


public class PostfixMailReceiver 
{
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception 
	{
		Class.forName("com.mysql.jdbc.Driver");
		String toAddress = args[1];
		byte[] bytes = Streams.readFullyBytes(System.in);

		UserInformation userInfo = UserInformationFactory.getInstance().getUserInformation(toAddress);
		userInfo.handleIn(new ByteArrayInputStream(bytes));
	}
}
