/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package core.util;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;

public class DateFormat 
{
	static LogNull log = new LogNull(DateFormat.class);
	DateTimeFormat f;
	
	public DateFormat (String fmt)
	{
		f = DateTimeFormat.getFormat(fmt);
	}
	
	public Date parse (String s)
	{
		log.trace("parsing",s,f.getPattern());
		
		return f.parse(s);
	}
	
	public String format (Date d)
	{
		return f.format(d);
	}

	public String format(Date date, int offset) 
	{
		return f.format(date, TimeZone.createTimeZone(offset));
	}
}
