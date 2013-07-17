/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

import mail.client.CacheManager;
import mail.client.cache.Type;

@Export()
public class FolderRepository extends FolderSet 
{
	@NoExport
	public FolderRepository(CacheManager manager) 
	{
		super(manager, Type.FolderPart);
	}
	
	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		preCacheMostRecentFolder();
	}
}
