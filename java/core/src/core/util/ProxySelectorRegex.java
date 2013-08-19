/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ProxySelectorRegex extends ProxySelector
{
	static public String[] DEFAULT_EXCLUDED_HOSTS = {
		"localhost",
		"127\\.0.*",
		"192\\.168\\.1\\..*",
		".*\\.dropbox\\.com",
		"s3\\.amazonaws\\.com",
		"s3\\.amazonaws\\.com\\.",
		".*\\.s3\\.amazonaws\\.com",
		".*\\.s3\\.amazonaws\\.com\\.",
		".*\\.push\\.apple\\.com",
		".*" + (Hosts.getHostFor("primary_prod").replace(".", "\\."))
	} ;
		
	List<Proxy> NoProxy = Arrays.asList(new Proxy[] { Proxy.NO_PROXY });
	Set<Pattern> excludedHosts = new HashSet<Pattern>();
	Map<String, List<Proxy> > schemeToProxyList = new HashMap<String, List<Proxy> >();
	
	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe)
	{
	}

	public void excludeHost (String host)
	{
		excludedHosts.add(Pattern.compile(host.toLowerCase()));
	}
	
	public void addProxy (String scheme, Proxy proxy)
	{
		if (!schemeToProxyList.containsKey(scheme))
			schemeToProxyList.put(scheme, new ArrayList<Proxy>(1));
		
		schemeToProxyList.get(scheme).add(proxy);
	}
	
	@Override
	public List<Proxy> select(URI uri)
	{
		String host = uri.getHost().toLowerCase();
//		System.err.println("ProxySelectorRegex.select host " + host);
		for (Pattern excluded : excludedHosts)
		{
			if (excluded.matcher(host).matches())
			{
//				System.err.println("ProxySelectorRegex.select host matched " + excluded.toString());
				return NoProxy;
			}
		}
		
		String scheme = uri.getScheme();
//		System.err.println("ProxySelectorRegex.select scheme " + scheme);
		
		List<Proxy> proxyList = schemeToProxyList.get(scheme);
		if (proxyList == null)
		{
//			System.err.println("ProxySelectorRegex.select did not find a proxy for " + scheme);
			return NoProxy;
		}		
		
		System.err.println("ProxySelectorRegex.select found a proxy for " + scheme + " " + host);
		return proxyList;
	}

	public void install ()
	{
		setDefault(this);
	}

	public void excludeHosts(String[] hosts)
	{
		for (String host : hosts)
		{
			excludeHost(host);
		}
	}
}
