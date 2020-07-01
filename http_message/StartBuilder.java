package http_message;

import java.util.HashMap;
import java.util.Map;

public class StartBuilder {

	public static void main(String[] args) {
		Map<String, String> header = new HashMap<>();
		header.put("Accept", "image/gif, image/jpeg");
		header.put("Host", "www.nowhere123.com");
		header.put("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
		header.put("Accept-Language", "en-us");
		
		String request = HttpBuilder.createHttpRequestMessage(HttpMethod.GET, HttpVersion.HTTP_1_1, "www.nowhere123.com", 
				null, null);
		System.out.println(request);
		
		String response = HttpBuilder.createHttpResponseMessage(HttpVersion.HTTP_1_1, HttpStatusCode.OK, header, "hello world!\r\nhello hello");
		System.out.println(response);
	}
}
