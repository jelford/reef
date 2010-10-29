package uk.ac.imperial.vazels.reef.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

public abstract class EasyRequest
{
	private String proxyURL;
	
	public EasyRequest()
	{
		this(false);
	}
	
	public EasyRequest(boolean inTesting)
	{
		proxyURL = GWT.getModuleBaseURL();
		if(inTesting)
			proxyURL += "proxy";
	}
	
	public void request(RequestBuilder.Method method, String url, QueryArg[] query)
	{
		RequestBuilder builder = new RequestBuilder(method, proxyURL + url);
		
		StringBuffer queryStrBuf = new StringBuffer();
		if(query != null)
		{
			for(QueryArg q : query)
			{
				queryStrBuf
					.append("&")
					.append(URL.encode(q.getName()))
					.append("=")
					.append(URL.encode(q.getValue()));
			}
		}
		
		if(queryStrBuf.length() != 0)
			queryStrBuf.deleteCharAt(0);
		
		String queryStr = queryStrBuf == null ? null : queryStrBuf.toString();
		
		try
		{
			builder.sendRequest(queryStr.toString(), new RequestCallback()
			{
				@Override
				public void onResponseReceived(Request request, Response response)
				{
					requested(response.getStatusCode(), response.getStatusText(), response.getText());
				}
				
				@Override
				public void onError(Request request, Throwable exception)
				{
					requested(null, null, null);
				}
			});
		}
		catch(RequestException e)
		{
			requested(null, null, null);
		}
	}
	
	// Sets all if it can.
	// If there was a problem with the request, everything is null.
	protected abstract void requested(Integer code, String reason, String content);
	
	public class QueryArg
	{
		private final String name;
		private final String value;
		
		public QueryArg(String name, String value)
		{
			this.name = name;
			this.value = value;
		}
		
		public String getName()
    {
	    return name;
    }

		public String getValue()
    {
	    return value;
    }
	}
}
