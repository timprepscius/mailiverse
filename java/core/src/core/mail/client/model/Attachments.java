/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import java.util.ArrayList;
import java.util.List;

import mail.client.ArrivalsProcessor;

import org.json.JSONArray;
import org.json.JSONObject;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import core.util.Base64;
import core.constants.ConstantsMailJson;
import core.util.JSON_;
import core.util.JSON_.JSONException;
import core.util.LogNull;
import core.util.LogOut;
import core.util.Strings;

@Export
public class Attachments implements Exportable
{
	static LogNull log = new LogNull(Attachments.class);

	List<Attachment> attachments;
	boolean loaded = false;
	
	public Attachments ()
	{
		attachments = new ArrayList<Attachment>();
	}
	
	public void addAttachment (Attachment attachment)
	{
		attachments.add(attachment);
	}
	
	public void removeAttachmentId (Attachment attachment)
	{
		attachments.remove(attachment);
	}
	
	public List<Attachment> getList ()
	{
		return attachments;
	}
	
	public Attachment getAttachment (String id)
	{
		log.debug("getAttachment", id);
		
		if (id == null)
			return null;
		
		for (Attachment a : attachments)
		{
			log.debug("comparing",a.getId(), id);
			if (a.getId().equals(id))
				return a;
		}
		
		return null;
	}
	
	public void setLoaded (boolean loaded)
	{
		this.loaded = loaded;
	}
	
	public boolean isLoaded ()
	{
		return loaded;
	}

	public void loadFrom(byte[] bs) throws Exception 
	{
		String text = Strings.toString(bs);
		log.debug("loading json", text);
		Object json = JSON_.parse(text);
		Object content = JSON_.getObject(json, ConstantsMailJson.Content);
		
		List<Object> contents = new ArrayList<Object>();
		contents.add(content);
		while (contents.size() > 0)
		{
			Object c = contents.get(0);
			contents.remove(0);

			String clazz = JSON_.getString(c, ConstantsMailJson.Class);
			Object value = JSON_.has(c, ConstantsMailJson.Value) ? 
				JSON_.get(c,ConstantsMailJson.Value) : null;

			if (clazz.equals(ConstantsMailJson.MultiPart))
			{
				for (int i=0; i<JSON_.size(value); ++i)
				{
					Object valueContent = JSON_.getObject(value, i);
					contents.add(valueContent);
				}
			}
			else
			if (clazz.equals(ConstantsMailJson.Bytes))
			{
				String contentDisposition = ArrivalsProcessor.getFirstHeader(c, "Content-Disposition", "None");
				String contentId = ArrivalsProcessor.getFirstHeader(c, "Content-Id", "None");
				
				String attachmentId = Attachment.getAttachmentId(contentDisposition, contentId);
				if (attachmentId != null)
				{
					for (Attachment i : attachments)
					{
						if (attachmentId.equals(i.getId()))
							i.setData(Base64.decode(JSON_.asString(value)));
					}
				}
			}
		}
		
		setLoaded(true);
	}
}
