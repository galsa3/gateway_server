package http_message;

import java.net.MalformedURLException;

import http_message.HttpParser.BodyParser;
import http_message.HttpParser.HeaderParser;
import http_message.HttpParser.StartLineParser;

public class StartParser {

	public static void main(String[] args) throws MalformedURLException {
		String responseMessage = "HTTP/1.1 200 OK\r\n" + 
//				"Date: Sun, 10 Oct 2010 23:26:07 GMT\r\n" + 
//				"Server: Apache/2.2.8 (Ubuntu) mod_ssl/2.2.8 OpenSSL/0.9.8g\r\n" + 
//				"Last-Modified: Sun, 26 Sep 2010 22:04:35 GMT\r\n" + 
//				"ETag: \"45b6-834-49130cc1182c0\"\r\n" + 
//				"Accept-Ranges: bytes\r\n" + 
//				"Content-Length: 12\r\n" + 
//				"Connection: close\r\n" + 
//				"Content-Type: text/html\r\n" + 
				"\r\n" + //9
				"my my my \r\n" +
				"Hello world!";
		HttpParser responseparser = new HttpParser(responseMessage);
		StartLineParser responseStartLine = responseparser.getStartLine();
		HeaderParser responseHeader = responseparser.getHeader();
		@SuppressWarnings("unused")
		BodyParser responseBody = responseparser.getBody();
		System.out.println(responseStartLine.getStatus().getCode());
		System.out.println(responseHeader);
		//System.out.println("body: "+responseBody.getBodyText());
//		System.out.println("is response? " + responseparser.isReply());
//		System.out.println("is request? " + responseparser.isRequest());
		
		
		String requestMessage = "GET / HTTP/1.1 \r\n"+
		"Host: www.nowhere123.com \r\n"+
		"Accept: image/gif, image/jpeg, \r\n"+
		"Accept-Language: en-us \r\n"+
		"Accept-Encoding: gzip, deflate \r\n"+
		"User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1) \r\n" +
		"\r\n";
		
		//System.out.println(requestMessage);
		
		HttpParser parser = new HttpParser(requestMessage);
		StartLineParser startLine = parser.getStartLine();
		HeaderParser header = parser.getHeader();
		BodyParser body = parser.getBody();
		System.out.println("URL: "+startLine.getURL());
		System.out.println(header.getAllHeaders());
		System.out.println("body: " + body);
		System.out.println("hi");
//		System.out.println(startLine);
//		System.out.println(header);
//		System.out.println(body);
//		System.out.println("is response? " + parser.isReply());
//		System.out.println("is request? " + parser.isRequest());
	}

}
