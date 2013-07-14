/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.server.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

public class Tail
{
	static final int TAIL_INTERVAL = 1000;
	
	String fileName;
	String lastTail;
	Date lastTailTime;
	
	public Tail (String fileName)
	{
		this.fileName = fileName;
	}
	
	public String getTail () throws IOException 
	{
		Date now = new Date();
		if (lastTailTime != null && 
			now.getTime() < lastTailTime.getTime() + TAIL_INTERVAL)
			return lastTail;
		
		lastTailTime = now;
		lastTail = null;
		
		File file = new File(fileName);
		RandomAccessFile fileHandler = new RandomAccessFile( file, "r" );
        long fileLength = file.length() - 1;
        long blockLength = Math.min(fileLength, 4096);
        
        fileHandler.seek(fileLength - blockLength);
        
        byte[] bytes = new byte[(int) blockLength];
        fileHandler.readFully(bytes);
        fileHandler.close();
  
        lastTail = new String(bytes, "UTF-8");
        return lastTail;
	}
}
