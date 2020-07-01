package http_message;

import java.util.Map;

public class HttpBuilder {
	private static final String NEW_LINE = "\r\n";
	private static final String COLON = ": ";
	private static final String SPACE = " ";

	public static String createHttpRequestMessage(HttpMethod method, HttpVersion version, String url, Map<String, String> header, String body){
		String startLine = HttpBuilder.StartLine.createStartLineRequest(method, version, url);
		return createResponseString(startLine, header, body);
	}

	public static String createHttpResponseMessage(HttpVersion version, HttpStatusCode code, Map<String, String> header, String body){
		String startLine = HttpBuilder.StartLine.createStartLineResponse(version, code);
		return createResponseString(startLine, header, body);
	}
	
	private static String createResponseString(String startLine, Map<String, String> header, String body) {
		String headers = HttpBuilder.Header.createHeader(header);
		String bodyString = HttpBuilder.Body.createBody(body);
		
		if(null == headers && null == bodyString) {
			return startLine + NEW_LINE;		
		} else if(null == headers) {
			return startLine + NEW_LINE + bodyString;
		} else if(null == bodyString) {
			return startLine + headers + NEW_LINE;		
		}
		return startLine + headers + NEW_LINE + bodyString;
	}
	
	private static class StartLine {
		private static String createStartLineRequest(HttpMethod method, HttpVersion version, String url) {
			return method.getMethodAsString() + SPACE + url + SPACE + version.getVersionAsString() + NEW_LINE;
		}
		
		private static String createStartLineResponse(HttpVersion version, HttpStatusCode code) {
			return version.getVersionAsString() + SPACE + code.asText() + SPACE + code.getDescription() + NEW_LINE;
		}
	}
	
	private static class Header {
		private static String createHeader(Map<String, String> header) {
			if(null == header) {
				return null;
			}
			
			StringBuilder headerString = new StringBuilder();
			for(String key : header.keySet()) {
				headerString.append(key);
				headerString.append(COLON);
				headerString.append(header.get(key));
				headerString.append(NEW_LINE);
			}
			return headerString.toString();
		}
	}
	
	private static class Body {
		private static String createBody(String body) {			
			return body;
		}
	}
}