/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import java.util.HashMap;
import java.util.Map;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import mail.client.CacheManager;
import mail.client.cache.Item;

@Export()
public class Settings extends Model implements Exportable
{
	public static final String
		VERSION = "version",
		CURRENT_VERSION = "1.0";
	
	protected Map<String,String> settings = new HashMap<String,String>();
	
	public Settings(CacheManager manager) 
	{
		super(manager);
	}
	
	@Override
	public void onLoaded ()
	{
		super.onLoaded();
		manager.onSettingsChanged(this);
	}
	
	@Override
	public void onDirty ()
	{
		super.onDirty();
		manager.onSettingsChanged(this);
	}

	public String get (String key)
	{
		return settings.get(key);
	}
	
	public String get(String key, String defaultz)
	{
		String value = settings.get(key);
		if (value != null)
			return value;
		
		return defaultz;
	}
	
	public void set(String key, String value)
	{
		String existing = get(key);
		if (existing != value)
		{
			settings.put(key, value);
			markDirty();
		}
	}

	public Map<String,String> getKV() 
	{
		return settings;
	}
	
	public void setKV(Map<String,String> kv)
	{
		settings = kv;
	}
}
