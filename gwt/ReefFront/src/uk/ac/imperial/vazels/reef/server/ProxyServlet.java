package uk.ac.imperial.vazels.reef.server;

/**
 *    Copyright 2009 Stou Sandalski (Siafoo.net)
 *    Copyright 1999-2008 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 * Edited by Andy Gurden
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

public class ProxyServlet extends HttpServlet {

  /**
     * 
     */
  private static final long serialVersionUID = 9L;

  private static final String targetServer = "http://localhost:8000/";
  private static final String srvMapping = "/reeffront/proxy/";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    handleRequest(req, resp, false);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    handleRequest(req, resp, true);
  }

  @SuppressWarnings({ "unchecked", "deprecation" })
  protected void handleRequest(HttpServletRequest req,
      HttpServletResponse resp, boolean isPost) throws ServletException,
      IOException {

    HttpClient httpclient = new DefaultHttpClient();

    StringBuffer sb = new StringBuffer();

    sb.append(targetServer);
    sb.append(req.getRequestURI().substring(srvMapping.length())); // The bit of
                                                                   // the
                                                                   // request
                                                                   // after the
                                                                   // proxy
                                                                   // mapping

    // Keep query string
    if (req.getQueryString() != null) {
      sb.append("?" + req.getQueryString());
    }

    HttpRequestBase targetRequest = null;

    if (isPost) {
      HttpPost post = new HttpPost(sb.toString());

      // If we have a multipart then split it, otherwise do as normal
      if (req.getContentType().toLowerCase().startsWith("multipart/form-data")) {
        // Create entity to fill
        MultipartEntity entity = new MultipartEntity(
            HttpMultipartMode.BROWSER_COMPATIBLE);

        MultipartParser mp = new MultipartParser(req, 1 * 1024 * 1024); // 10MB
        Part part;

        while ((part = mp.readNextPart()) != null) {
          
          String name = part.getName();
          if (part.isParam()) {
            // Get param data
            ParamPart param = (ParamPart) part;
            String content = param.getStringValue();
            
            // Add param
            entity.addPart(name, new StringBody(content));
          } else if (part.isFile()) {
            // Get param data
            FilePart param = (FilePart) part;
            final String filename = param.getFileName();

            // If there's a file
            if (filename != null) {
              File temp = null;
              
              try {
                temp = File.createTempFile("proxy", ".dump");
                temp.deleteOnExit();

                // Write to temp file
                OutputStream out = new FileOutputStream(temp);
                InputStream in = param.getInputStream();
                
                // Copy
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                  out.write(buf, 0, len);
                }
                in.close();
                out.close(); 
              } catch (IOException e) {}
              
              // Create param with correct type and filename
              // The mime type appears to be ignored by FileBody though...
              String type = param.getContentType();
              FileBody fBody = new FileBody(temp, type){
                public String getFilename(){
                  return filename;
                }
              };
              
              entity.addPart(name, fBody);
            }
          }
        }

        // post.setHeader(entity.getContentType());
        // post.setHeader(entity.getContentEncoding());
        post.setEntity(entity);
        System.out.println(entity.getContentType());
        System.out.println(entity.getContentType().getName());
        System.out.println(entity.getContentType().getValue());
        // post.
      } else { // not multipart
        Enumeration<String> paramNames = req.getParameterNames();
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        while (paramNames.hasMoreElements()) {
          String paramName = paramNames.nextElement();
          params.add(new BasicNameValuePair(paramName, req
              .getParameterValues(paramName)[0]));
        }

        post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
      }

      targetRequest = post;
    } else {
      HttpGet get = new HttpGet(sb.toString());
      targetRequest = get;
    }

    // // This copies the headers but I never really cared to get it to work
    // properly
    // System.out.println("Request Headers");
    // Enumeration<String> headerNames = req.getHeaderNames();
    // String headerName = null;
    // while(headerNames.hasMoreElements()){
    // headerName = headerNames.nextElement();
    // targetRequest.addHeader(headerName, req.getHeader(headerName));
    // System.out.println(headerName + " : " + req.getHeader(headerName));
    // }

    System.out.println("Content Type");
    System.out.println("Length: " + targetRequest.getAllHeaders().length);
    for (Header h : targetRequest.getAllHeaders())
      System.out.println(h);
    HttpResponse targetResponse = httpclient.execute(targetRequest);
    HttpEntity entity = targetResponse.getEntity();

    // Forward correct status
    resp.setStatus(targetResponse.getStatusLine().getStatusCode(),
        targetResponse.getStatusLine().getReasonPhrase());

    // Forward correct mimetype
    resp.setContentType(entity.getContentType().getValue());

    // Send the Response
    InputStream input = entity.getContent();
    OutputStream output = resp.getOutputStream();

    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
    String line = reader.readLine();

    while (line != null) {
      writer.write(line + "\n");
      line = reader.readLine();
    }

    reader.close();
    writer.close();
    httpclient.getConnectionManager().shutdown();
  }

}
