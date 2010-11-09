package uk.ac.imperial.vazels.reef.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;

/**
 * Easy way to make requests.
 * <p>
 * This automatically uses {@link AddressResolution} to determine the correct
 * address to poke. To use you must subclass this with your own request handler.
 * Note that is is much easier to use {@link MultipleRequester} to make requests.
 * </p>
 */
public abstract class EasyRequest
{
	private AddressResolution addRes;
	
	public EasyRequest()
	{
		addRes = new AddressResolution();
	}
	
	/**
	 * 
	 * Initialise assuming we are in testing (or not).
	 * 
	 * @param inTesting Are we in testing?
	 * 
	 * @deprecated Use {@link EasyRequest#EasyRequest()}.
	 * 
	 * This remains only for legacy code that may have used it when address
	 * resolution was here.
	 */
	public EasyRequest(boolean inTesting)
	{
		addRes = new AddressResolution(inTesting);
	}
	
	/**
	 * Send a request.
	 * 
	 * @param method The {@link RequestBuilder.Method} to use (POST or GET).
	 * @param url The URL to request (e.g. "/settings/").
	 * @param query A list of {@link QueryArg}s to pass with the request.
	 * @return A {@link RequestTicket} to identify the response.
	 */
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
	/**
	 * Callback for any responses.
	 * <p>
	 * This will always receive a ticket identifying the originating request.
	 * If something goes wrong with sending or receiving the response, all
	 * other parameters will be {@code null}. Otherwise the parameters will
	 * be filled in where possible (depending on the returned code).
	 * </p>
	 * 
	 * @param ticket Identifies the originating request.
	 * @param code The returned response code.
	 * @param reason The text following the response code.
	 * @param content A {@link String} containing the response data (without headers).
	 */
	protected abstract void requested(RequestTicket ticket, Integer code, String reason, String content);
	
	/**
	 * Used to when sending a request to hold the arguments.
	 * <p>
	 * These are in the usual name=value pairs that you would see in the query
	 * string of a GET request.
	 * </p>
	 */
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
		
		/**
		 * Takes {@link QueryArg#getName} and {@link QueryArg#getValue()} to
		 * provide a string of the form used in application/x-www-form-urlencoded data
		 * <p>
		 * i.e. name=value
		 * </p>
		 */
		public String toString(){
		  if(getValue() == null)
		    return URL.encode(getName());
		  else
		    return URL.encode(getName()) + "=" + URL.encode(getValue());
		}
	}
	
	/**
	 * Used to identify requests. This does not define any new methods.
	 */
	public class RequestTicket{
	  public boolean equals(RequestTicket t){
	    return this == t;
	  }
	}
}
