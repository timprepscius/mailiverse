/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import core.util.Environment;
import core.util.Streams;


public class MailViewUtil
{
	public static File createTempDirectory()
		    throws IOException
	{
	    final File temp;

	    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

	    if(!(temp.delete()))
	    {
	        throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
	    }

	    if(!(temp.mkdir()))
	    {
	        throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
	    }

	    return (temp);
	}
	
	public static void integrateOSXMail (String name) throws Exception
	{
		String script = 
			Streams.readFullyString(
				MailViewUtil.class.getResourceAsStream(AppConstants.INTEGRATE_MACMAIL_SCRIPT),
				"UTF-8"
			).replace("#NAME#", name);
		
		File temporary = new File(createTempDirectory().toString() + "/" + AppConstants.INTEGRATE_MACMAIL_SCRIPT);
		
		FileWriter writer = new FileWriter(temporary);
		writer.write(script);
		writer.close();
		
		if (!temporary.exists())
			throw new Exception("Failed to write " + AppConstants.INTEGRATE_MACMAIL_SCRIPT);
		
		Runtime.getRuntime().exec( new String[] { "osascript", temporary.getAbsolutePath() } );
	}
}
