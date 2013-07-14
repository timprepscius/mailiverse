/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileFilterStandard extends FileFilter
{

	String name, extension;

	public FileFilterStandard(String name, String extension)
	{
		this.name = name;
		this.extension = extension;
	}

	public boolean accept(File f)
	{
		if (f.isDirectory())
		{
			return true;
		}

		return (f.getPath().toLowerCase().endsWith(extension));
	}

	// The description of this filter
	public String getDescription()
	{
		return name;
	}
}