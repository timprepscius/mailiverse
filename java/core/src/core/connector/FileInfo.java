/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector;

import java.util.Comparator;
import java.util.Date;

public class FileInfo 
{
	static public class SortByDateAscending implements Comparator<FileInfo>
	{
		@Override
		public int compare(FileInfo o1, FileInfo o2)
		{
			long time = o1.date.getTime() - o2.date.getTime();
			return time == 0 ? 0 :	(time > 0 ? 1 : -1);
		}
	};
	
	enum Type
	{
		Directory
	}
	
	public String path;
	public String relativePath;
	public long size;
	public Type type;
	public Date date;
	public String version;
	public Object user;

	public FileInfo(String path, String relativePath, long size, Date date, String version) 
	{
		this.path = path;
		this.relativePath = relativePath;
		this.size = size;
		this.date = date;
		this.version = version;
	}

	public String getFileName() 
	{
		int lastSlash = path.lastIndexOf('/');
		if (lastSlash == -1)
			return path;
		
		return path.substring(lastSlash+1);
	}
}