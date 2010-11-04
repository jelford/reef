package uk.ac.imperial.vazels.reef.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;

public abstract class EasyRequest
{
	private AddressResolution addRes;
	
	public EasyRequest()
	{
		addRes = new AddressResolution();
	}
	
	public EasyRequest(boolean inTesting)
	{
		addRes = new AddressResolution(inTesting);
	}
	
	public RequestTicket request(RequestBuilder.Method method, String url, QueryArg[] query)
	{
	  final RequestTicket ticket = new RequestTicket();
	  
		RequestBuilder builder = new RequestBuilder(method, addRes.resolve(url));
		builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
		
		StringBuffer queryStrBuf = new StringBuffer();
		if(query != null)
		{
			for(QueryArg q : query)
			{
				queryStrBuf
					.append("&")
					.append(q.toString());
			}
		}
		
		if(queryStrBuf.length() != 0)
			queryStrBuf.deleteCharAt(0);
		
		String queryStr = queryStrBuf == null ? null : queryStrBuf.toString();
		
		try
		{
			builder.sendRequest(queryStr, new RequestCallback()
			{
				@Override
				public void onResponseReceived(Request request, Response response)
				{
					requested(ticket, response.getStatusCode(), response.getStatusText(), response.getText());
				}
				
				@Override
				public void onError(Request request, Throwable exception)
				{
					requested(ticket, null, null, null);
				}
			});
		}
		catch(RequestException e)
		{
			requested(ticket, null, null, null);
		}
		
		return ticket;
	}
	
	// Sets all if it can.
	// If there was a problem with the request, everything is null.
	protected abstract void requested(RequestTicket ticket, Integer code, String reason, String content);
	
	public static class QueryArg
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
		
		public String toString(){
		  if(getValue() == null)
		    return URL.encode(getName());
		  else
		    return URL.encode(getName()) + "=" + URL.encode(getValue());
		}
	}
	
	// Given on each request to identify the reply
	public class RequestTicket{
	  public boolean equals(RequestTicket t){
	    return this == t;
	  }
	}
}
