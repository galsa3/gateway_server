package http_message;

import java.net.MalformedURLException;

import http_message.HttpParser.BodyParser;
import http_message.HttpParser.HeaderParser;
import http_message.HttpParser.StartLineParser;

public class HttpMain {

	public static void main(String[] args) throws InterruptedException, MalformedURLException {
		HttpServer server = new HttpServer();
		new Thread(server).start();
		Thread.sleep(10000);
		String requestMessage = server.getRaw();
		
		HttpParser parser = new HttpParser(requestMessage);
		StartLineParser requestStartLine = parser.getStartLine();
		HeaderParser requestHeader = parser.getHeader();
		BodyParser requestBody = parser.getBody();
		System.out.println(requestStartLine.getHttpMethod());
		System.out.println(requestStartLine.getURL());
		System.out.println(requestHeader.getAllHeaders());
		System.out.println("body: "+requestBody);
	}

}
