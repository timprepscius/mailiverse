/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

import mail.client.CacheManager;

@Export()
public class FolderFilterSimple extends FolderFilter
{
	@NoExport
	public FolderFilterSimple(CacheManager manager) 
	{
		super(manager);
	}
	
	@Override
	public boolean matchesFilter (Conversation conversation)
	{
		return folderDefinition.matchesFilter(conversation);
	}
}
