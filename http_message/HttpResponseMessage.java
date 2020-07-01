package http_message;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

class HttpResponseMessage {
	String message = "HTTP/1.1 200 OK\n" + 
			"Date: Sun, 10 Oct 2010 23:26:07 GMT\n" + 
			"Server: Apache/2.2.8 (Ubuntu) mod_ssl/2.2.8 OpenSSL/0.9.8g\n" + 
			"Last-Modified: Sun, 26 Sep 2010 22:04:35 GMT\n" + 
			"ETag: \"45b6-834-49130cc1182c0\"\n" + 
			"Accept-Ranges: bytes\n" + 
			"Content-Length: 12\n" + 
			"Connection: close\n" + 
			"Content-Type: text/html\n" + 
			"\n" +  //9
			"Hello world!";
	
	String noBodyMessage = "HTTP/1.1 200 OK\n" + 
			"Date: Sun, 10 Oct 2010 23:26:07 GMT\n" + 
			"Server: Apache/2.2.8 (Ubuntu) mod_ssl/2.2.8 OpenSSL/0.9.8g\n" + 
			"Last-Modified: Sun, 26 Sep 2010 22:04:35 GMT\n" + 
			"ETag: \"45b6-834-49130cc1182c0\"\n" + 
			"Accept-Ranges: bytes\n" + 
			"Content-Length: 12\n" + 
			"Connection: close\n" + 
			"Content-Type: text/html\n" + 
			"\n";
	
	String noHeadersMessage = "HTTP/1.1 200 OK\n" +			
			"\n" +  //1
			"Hello world!";
	@Test
	void testGetStartLine() throws MalformedURLException {
		HttpParser parser = new HttpParser(message);
		assertEquals(HttpStatusCode.OK, parser.getStartLine().getStatus());
		assertEquals(HttpStatusCode.OK.getCode(), 200);		
		assertEquals(HttpVersion.HTTP_1_1, parser.getStartLine().getHttpVersion());		
	}

	@Test
	void testGetHeader() throws MalformedURLException {
		HttpParser parser = new HttpParser(message);
		assertEquals(" text/html", parser.getHeader().getHeader("Content-Type"));
		assertEquals(" close", parser.getHeader().getHeader("Connection"));

	}

	@Test
	void testGetBody() throws MalformedURLException {
		HttpParser parser = new HttpParser(message);
		assertEquals("Hello world!", parser.getBody().getBodyText());
		}
	@Test
	void testNoBody() throws MalformedURLException {
		HttpParser parser = new HttpParser(noBodyMessage);
		assertNull(parser.getBody());
		}
	@Test
	void testNoHeaders() throws MalformedURLException {
		HttpParser parser = new HttpParser(noHeadersMessage);
		assertNull(parser.getHeader());
		}
//
//	@Test
//	void testIsRequest() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testIsReply() {
//		fail("Not yet implemented");
//	}

}
