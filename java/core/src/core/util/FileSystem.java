/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import core.connector.FileInfo;

public class FileSystem
{
	public static void listRecursiveWorker (File original, File item, List<FileInfo> keys)
	{
	    if (item.isDirectory()) 
	    {
	        String[] children = item.list();
	        for (int i=0; i<children.length; i++) 
	        {
	        	listRecursiveWorker(original, new File(item, children[i]), keys);
	        }
	    }
	    else
	    if (item.isFile())
	    {
	    	String path = item.getPath();
	    	String originalPath = original.getPath();
	    	
	    	int i1 = path.indexOf(originalPath);
	    	int i2 = i1 + originalPath.length() + 1;
	    	String subPath = item.getPath().substring(i2);
	    	String fullPath = item.getPath().substring(i1);

	    	subPath = subPath.replace("\\", "/");
	    	keys.add(new FileInfo(fullPath, subPath, item.length(), new Date(item.lastModified()), "" + item.lastModified()));
	    }
	}
	
	public static List<FileInfo> allFilesFor (File dir)
	{
		List<FileInfo> results = new ArrayList<FileInfo>();
		listRecursiveWorker(dir, dir, results);
		
		return results;
	}
}
