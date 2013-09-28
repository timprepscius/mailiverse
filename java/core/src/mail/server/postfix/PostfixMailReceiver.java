package mail.server.postfix;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import mail.server.handler.UserInformation;
import mail.server.handler.UserInformationFactory;

import core.util.LogOut;
import core.util.Streams;


public class PostfixMailReceiver 
{
	static LogOut log = new LogOut(PostfixMailReceiver.class);
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception 
	{
		File me = new File(args[0]);
		File logFile = new File(me.getParent(), "run.log");
		
		System.setOut(new PrintStream(new FileOutputStream(logFile)));
		System.setErr(System.out);
		
		try
		{
			log.debug("Running with log file as ",logFile);
			
			Class.forName("com.mysql.jdbc.Driver");
			String toAddress = args[1];
			byte[] bytes = Streams.readFullyBytes(System.in);
	
			UserInformation userInfo = UserInformationFactory.getInstance().getUserInformation(toAddress);
			userInfo.handleIn(new ByteArrayInputStream(bytes));
		}
		finally
		{
			System.out.flush();
		}
	}
}
