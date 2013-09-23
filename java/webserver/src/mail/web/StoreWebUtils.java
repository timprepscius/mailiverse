package mail.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import core.connector.FileInfo;
import core.util.DateFormat;
import core.util.LogOut;

import store.server.ConstantsMvServer;
import store.server.StoreUtils;

public class StoreWebUtils 
{
	static LogOut log = new LogOut(StoreWebUtils.class);
	
	public static String verifyUser (String action, HttpServletRequest request, String resource, byte[] content) throws Exception
	{
		String possibleHeaders[] = {
			ConstantsMvServer.HEADER_DATE,
			ConstantsMvServer.HEADER_CONTENT_TYPE,
			ConstantsMvServer.HEADER_CONTENT_LENGTH,
			ConstantsMvServer.HEADER_AUTHORIZATION
		};
		
		Map<String, String> headers = new HashMap<String,String>();
		for (String possibleHeader : possibleHeaders)
		{
			String value = request.getHeader(possibleHeader);
			if (value != null)
				headers.put(possibleHeader, value);
		}
		
		return StoreUtils.verifyUser(action, headers, resource, content);
	}
	
	static public String transformFileInfoListToJson (List<FileInfo> fileInfos) throws Exception
	{
		DateFormat dateTimeFormat = new DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z GMT'");
		
		JSONArray a = new JSONArray();
		for (FileInfo fileInfo : fileInfos)
		{
			JSONObject o = new JSONObject();
			o.put("path", fileInfo.path);
			o.put("size", fileInfo.size);
			o.put("version", fileInfo.version);
			o.put("date", dateTimeFormat.format(fileInfo.date));
			
			log.debug ("converted date", fileInfo.date, "to", o.get("date"));
			
			a.put(o);
		}
		
		JSONObject r = new JSONObject();
		r.put("contents", a);
		r.put("isTruncated", false);
		
		return r.toString();
	}
}
