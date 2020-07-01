package http_message;

import java.util.HashMap;
import java.util.Map;

public class HttpParser {
	private static final String COLON = "[:]";
	private static final String NEW_LINE = "\r\n";
	private static final String WHITE_SPACE = "[ ]";
	private static final String NEW_LINE_SPLIT = "\\r\\n";
	private static final int EMPTY_LINE = 0;
	private static final int FIRST_ARG = 0;
	private static final int SECOND_ARG = 1;
	private static final int THIRD_ARG = 2;
	private static final int LIMIT = 2;
	private StartLineParser startLine;
	private HeaderParser header;
	private BodyParser body;
	
	public HttpParser(String message) {		
		String[] tokens = message.split(NEW_LINE_SPLIT);		
		startLine = extractStartLine(tokens[FIRST_ARG]);
		header = extractHeader(tokens);
		body = extractBody(message);
	}

	public StartLineParser getStartLine() {
		return startLine;
	}

	public HeaderParser getHeader() {
		return header;
	}

	public BodyParser getBody() {
		return body;
	}

	boolean isRequest() {
		return startLine.isRequest();
	}
	
	boolean isReply() {
		return startLine.isReply();
	}
	
	public class StartLineParser {
		private HttpStatusCode status;
		private HttpVersion version;
		private HttpMethod method;
		private String url;
		
		public StartLineParser(HttpMethod method, HttpVersion version, HttpStatusCode status, String url) {
			this.version = version;
			this.method = method;
			this.status = status;
			this.url = url;			
		}
		
		public HttpMethod getHttpMethod() {
			return method;
		}
		
		public String getURL() {
			return url;
		}
		
		public HttpStatusCode getStatus() {
			return status;
		}
		
		public HttpVersion getHttpVersion() {
			return version;
		}
		
		public boolean isReply() {
			return (method == null);
		}
		
		public boolean isRequest() {
			return (method != null);
		}
	}	
	
	public class HeaderParser {
		private Map<String, String> headers = new HashMap<>();
		
		public HeaderParser(Map<String, String> headers) {
			this.headers = headers;
		}
	
		public String getHeader(String headerString) {			
			return headers.get(headerString.toLowerCase());
		}
	
		public Map<String, String> getAllHeaders() {
			return headers;
		}
	}	
	
	public class BodyParser {
		private String body = null;
		
		public BodyParser(String body) {
			this.body = body;
		}
	
		public String getBodyText() {
			return body;
		}
	}	

	private StartLineParser extractStartLine(String startLineString) {		
		String[] startLineTokens = startLineString.split(WHITE_SPACE);		
		HttpMethod method = extractMethod(startLineTokens[FIRST_ARG]);
		
		if(null != method) {
			return requestStartLine(startLineTokens, method);
						
		} else {			
			return responseStartLine(startLineTokens);
		}
	}

	private HttpMethod extractMethod(String methodString) {
		for(HttpMethod method : HttpMethod.values()) {
			if(method.getMethodAsString().equals(methodString)) {
				return method;
			}
		}
		return null;
	}

	private StartLineParser requestStartLine(String[] startLineTokens, HttpMethod method) {
		String url = startLineTokens[SECOND_ARG];
		HttpVersion version = extractVersion(startLineTokens[THIRD_ARG]);
		return new StartLineParser(method, version, null, url);
	}
	
	private StartLineParser responseStartLine(String[] startLineTokens) {
		HttpVersion version = extractVersion(startLineTokens[FIRST_ARG]);
		HttpStatusCode statusCode = extractStatusCode(startLineTokens[SECOND_ARG]);			
		return new StartLineParser(null, version, statusCode, null);
	}

	private HttpStatusCode extractStatusCode(String codeString) {
		for(HttpStatusCode statusCode : HttpStatusCode.values()) {
			if(statusCode.asText().equals(codeString)) {				
				return statusCode;
			}
		}
		return null;
	}

	private HttpVersion extractVersion(String versionString) {
		for(HttpVersion version : HttpVersion.values()) {
			if(version.getVersionAsString().equals(versionString)) {
				return version;
			}
		}
		return null;		
	}

	private HeaderParser extractHeader(String[] tokens) {
		Map<String, String> headerMap = new HashMap<>();
		String[] header;
		int index = 1;
		
		if(!tokens[index].isEmpty()) {
			while(index < tokens.length && tokens[index].trim().length() > EMPTY_LINE) {			
				header = tokens[index].split(COLON, LIMIT);
				headerMap.put(header[FIRST_ARG].toLowerCase(), header[SECOND_ARG].trim());			
				++index;				
			}		
			return new HeaderParser(headerMap);
		}
		return null;
	}

	private BodyParser extractBody(String message) {
		String[] body = message.split(NEW_LINE+NEW_LINE, LIMIT);
		if(!body[SECOND_ARG].trim().isEmpty()) {			
			return new BodyParser(body[SECOND_ARG]);
		}
		return null;
	}
}

