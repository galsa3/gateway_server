package gatewayserver;

import org.json.JSONException;
import org.json.JSONObject;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.SQLException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import database_management.DatabaseManagement;
import http_message.HttpBuilder;
import http_message.HttpParser;
import http_message.HttpStatusCode;
import http_message.HttpVersion;
import jarloader.JarLoader;
import observer.Callback;
import observer.Dispatcher;

public class GatewayServer implements Runnable {
	private static final String FACTORY_COMMAND_MODIFIER = "FactoryCommandModifier";
	private static final String ALREADY_RUNNING = "server is already running, can't add new connections";
	private static final String INVALID_PORT = "this port number was already added: ";
	private static final String CONTENT_LENGTH = "Content-Length";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String JSON_TYPE = "application/json";
	private static final String CONNECTION = "Connection";
	private static final String CLOSE = "close";
	private static final String UTF_8 = "UTF-8";
	private static final String DOT_JAR = ".jar";
	private final String JAR_DIRECTORY;
	private String URL = "jdbc:mysql://localhost:3306/";
	private String password = "305088155";
	private String user = "root";
	private CMDFactory<FactoryCommand, String, Object> commandFactory = CMDFactory.getFactory();
	private GatewayMessageHandler messageHandler;
	private ConnectionHandler connectionHandler = new ConnectionHandler();
	private Map<String, DatabaseManagement> companiesMap = new HashMap<>();
	private TaskCreator taskCreator = new TaskCreator();
	private ThreadPoolExecutor threadPool;
	private SunHttpServer sunHttpServer;
	private DatabaseManagement companyDatabase;
	private static JarMonitor jarMonitor;
	private FactoryCommandLoader commandLoader;
	private boolean isRunning = false;
	private int numOfThreads;

	public GatewayServer(int numOfThreads, String jarDirPath) {
		try {
			messageHandler = new GatewayMessageHandler();
		}
			catch (Exception e) {
				e.printStackTrace();
			}
		System.out.println("1");
		JAR_DIRECTORY = jarDirPath;
		this.numOfThreads = numOfThreads;

		System.out.println("1");
		initJarMonitor();

		System.out.println("1");
		initCommandLoader();

		initCommandFactory();
	}	

	public GatewayServer(String jarDirPath) {
		this(Runtime.getRuntime().availableProcessors(), jarDirPath);		
	}
	
	public void addSunHttpServer(ServerPort port) throws IOException {
		validateRequest(port.getPort());
		sunHttpServer = new SunHttpServer(port);
		connectionHandler.addConnection(sunHttpServer);
	}
	
	public void addHttpServer(ServerPort port) throws IOException {
		validateRequest(port.getPort());
		connectionHandler.addConnection(new HttpConnection(port));
	}
	
	public void addTcpServer(ServerPort port) {
		validateRequest(port.getPort());
		connectionHandler.addConnection(new TcpConnection(port));
	}
	
	public void addUdpServer(ServerPort port) {
		validateRequest(port.getPort());
		connectionHandler.addConnection(new UdpConnection(port));
	}	

	@Override
	public void run() {		
		start();		
	}
	
	public void start() {
		System.out.println("start");
		isRunning = true;
		threadPool = new ThreadPoolExecutor(numOfThreads, 
											Integer.MAX_VALUE, 1, 
											TimeUnit.DAYS, 
											new SynchronousQueue<Runnable>());
		connectionHandler.startServers();
	}
	
	public void stop() {
		isRunning = false;
		connectionHandler.stopServers();
		threadPool.shutdown();
	}
	
	public void setNumOfThreads(int numOfThread) {
		threadPool.setCorePoolSize(numOfThread);
	}
	
	/**********************************************
	 * Gateway Connection Handler
	 **********************************************/
	private class ConnectionHandler { 
		private static final int ZERO = 0;	
		private static final int TIME_OUT = 10000;
		private static final int BUFFER_SIZE = 2048;
		private List<ServerConnection> connectionsList = new LinkedList<>();
		private Map<Channel, ServerConnection> channelConnectionsMap = new HashMap<>();
		private Selector selector;		
		
		private void startServers() {			
			try {
				selector = Selector.open();
				initConnections();
				startSelectorWork();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} 
		}

		private void startSelectorWork() throws IOException, ClassNotFoundException {
			Channel currentChannel;
			ServerConnection currentConnection;
			SelectionKey key;
			
			while(isRunning) {			
				try {					
					if(ZERO == selector.select(TIME_OUT)) {
						if(isRunning) {
							System.out.println("server is up and running");							
						}
					} else {
						Set<SelectionKey> selectedKeys = selector.selectedKeys();
						Iterator<SelectionKey> iter = selectedKeys.iterator();
						System.out.println("new key");
						while(iter.hasNext()) {	
							key = iter.next();
							currentChannel = key.channel();
							currentConnection = channelConnectionsMap.get(currentChannel);

							if(key.isValid()) {
								if(key.isAcceptable()) {								
									registerTcpClient(selector, (ServerSocketChannel)currentChannel);
								}							
								if(key.isReadable()) {									
									channelConnectionsMap.get(currentChannel).handleRequestMessage(currentChannel, currentConnection);							
								}							
								iter.remove();
							}
						}						
					}
				} catch (ClosedSelectorException | ConcurrentModificationException | ClosedChannelException e) {
					System.out.println("exiting...");
					System.err.println("error: " + e);
				}
			}
		}
		
		private void stopServers() {
			try {
				closeChannels();
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			sunHttpServer.stop();
		}
		
		private void closeChannels() throws IOException {
			for (SelectionKey key : selector.keys()) {				
				key.channel().close();				
			}			
		}
		
		private void addConnection(ServerConnection connection) {
			connectionsList.add(connection);
		}

		private void initConnections() throws ClosedChannelException, IOException {
			for(ServerConnection connection : connectionsList) {
				connection.initServerConnection(selector);
				channelConnectionsMap.put(connection.getChannel(), connection);
			}
		}

		private void registerTcpClient(Selector selector, ServerSocketChannel serverChannel) throws IOException {
			SocketChannel clientChannel = serverChannel.accept();
			channelConnectionsMap.put(clientChannel, channelConnectionsMap.get(serverChannel));
			clientChannel.configureBlocking(false);
			clientChannel.register(selector, SelectionKey.OP_READ);			
		}
	}	

	/***********************************************
	 * High Level Sun Http Server
	 **********************************************/
	public class SunHttpServer implements ServerConnection {
		private static final String ROOT = "/";
		private static final int DELAY = 0;
		private ServerPort portNumber;
		private HttpServer httpServer;

		private SunHttpServer(ServerPort portNumber) throws IOException {
			this.portNumber = portNumber;
			httpServer = HttpServer.create(new InetSocketAddress(portNumber.getPort()), 0);
			addContexts();			
		}
		
		@Override
		public void initServerConnection(Selector selector) throws IOException {
			httpServer.start();			
		}
		
		@Override
		public void sendResponse(ClientInfo clientInfo, ByteBuffer buffer) throws IOException {
			sendResponseMessage(clientInfo.getHttpExchange(), HttpStatusCode.OK, byteBufferToString(buffer));
		}
		
		@Override
		public void sendErrorResponse(ClientInfo clientInfo, ByteBuffer buffer) throws IOException {
			sendResponseMessage(clientInfo.getHttpExchange(), HttpStatusCode.BAD_REQUEST, byteBufferToString(buffer));	
		}

		@Override
		public int getPortNumber() {
			return portNumber.getPort();
		}
		
		private void stop() {
			httpServer.stop(DELAY);
		}

		private void addContexts() {					
			httpServer.createContext(ROOT, new RootHandler());
		}
		
		private class RootHandler implements HttpHandler {
			@Override
			public void handle(HttpExchange httpExchange) throws IOException {				
				handleMessage(httpExchange);	    	
			}			
		}
		
		private void handleMessage(HttpExchange httpExchange) throws IOException {
			messageHandler.handleMessage(parseBody(httpExchange), new ClientInfo(httpExchange, this));
		}
		
		private String parseBody(HttpExchange httpExchange) throws IOException {
			return new String(httpExchange.getRequestBody().readAllBytes());
		}		
		
		private void sendResponseMessage(HttpExchange httpExchange, HttpStatusCode statusCode, String body) throws IOException {
			Headers headers = httpExchange.getResponseHeaders();
			headers.add(CONTENT_TYPE, JSON_TYPE);
			httpExchange.sendResponseHeaders(statusCode.getCode(), body.length());
			OutputStream outputStream = httpExchange.getResponseBody();
			outputStream.write(body.getBytes());
			outputStream.close();
		}		
		
		@Override
		public Channel getChannel() {
			// NOT IMPLEMENTED
			return null;
		}

		@Override
		public void handleRequestMessage(Channel currentChannel, ServerConnection currentConnection)
				throws IOException {
			// NOT IMPLEMENTED			
		}
	}
	
	/**********************************************
	 * HTTP low - level Server
	 **********************************************/
	private class HttpConnection implements ServerConnection {
		private final ServerPort portNumber;
		private ServerSocketChannel httpServerChannel;		
		
		public HttpConnection(ServerPort port) {
			this.portNumber = port;
		}

		@Override
		public void initServerConnection(Selector selector) throws IOException {
			httpServerChannel = ServerSocketChannel.open();
			httpServerChannel.bind(new InetSocketAddress(portNumber.getPort()));
			httpServerChannel.configureBlocking(false);
			httpServerChannel.register(selector, SelectionKey.OP_ACCEPT);
		}
		
		@Override
		public void handleRequestMessage(Channel httpClientChannel, ServerConnection currentConnection) 
				throws IOException {
			ByteBuffer buffer = ByteBuffer.allocate(ConnectionHandler.BUFFER_SIZE);
			int bytes = ((SocketChannel)httpClientChannel).read(buffer);

	    	if(-1 == bytes) {
	    		removeTcpConnection(httpClientChannel);	    		
	    	}	    	
	    	else {
	    		HttpParser parser = new HttpParser(byteBufferToString(buffer));
	    		messageHandler.handleMessage(parser.getBody().getBodyText(), new ClientInfo((SocketChannel) httpClientChannel, currentConnection));
	    	}
		}

		@Override
		public int getPortNumber() {
			return portNumber.getPort();
		}

		@Override
		public Channel getChannel() {
			return httpServerChannel;
		}
		
		@Override
		public void sendResponse(ClientInfo clientInfo, ByteBuffer buffer) throws IOException {
			sendHttpMessage(clientInfo, HttpStatusCode.OK, byteBufferToString(buffer));
		}

		@Override
		public void sendErrorResponse(ClientInfo clientInfo, ByteBuffer buffer) throws IOException {
			sendHttpMessage(clientInfo, HttpStatusCode.BAD_REQUEST, byteBufferToString(buffer));
		}

		private void sendHttpMessage(ClientInfo clientInfo, HttpStatusCode status, String body) throws IOException {
			String responseString = HttpBuilder.createHttpResponseMessage(HttpVersion.HTTP_1_1, 
												status, createHeader(body.length()), body);
			ByteBuffer message = stringToByteBuffer(responseString);
			
			while(message.hasRemaining()) {
				((SocketChannel)clientInfo.getTcpSocketChannel()).write(message);
			}			
			
			message.clear();
		}
		
		private Map<String, String> createHeader(int bodyLength) {
			Map<String, String> header = new HashMap<>();			
			header.put(CONTENT_LENGTH, String.valueOf(bodyLength));
			header.put(CONNECTION, CLOSE);
			header.put(CONTENT_TYPE, JSON_TYPE);
			return header;
		}		
	}
		
	/**********************************************
	 * TCP Server
	 **********************************************/
	private class TcpConnection implements ServerConnection {
		private final ServerPort portNumber;
		private ServerSocketChannel tcpServerChannel;		
		
		public TcpConnection(ServerPort port) {
			this.portNumber = port;
		}
		
		@Override
		public void initServerConnection(Selector selector) throws IOException {
			tcpServerChannel = ServerSocketChannel.open();
			tcpServerChannel.bind(new InetSocketAddress(portNumber.getPort()));
			tcpServerChannel.configureBlocking(false);
			tcpServerChannel.register(selector, SelectionKey.OP_ACCEPT);
		}
		
		@Override
		public void handleRequestMessage(Channel currentChannel, ServerConnection currentConnection)
				throws IOException {			
			ByteBuffer buffer = ByteBuffer.allocate(ConnectionHandler.BUFFER_SIZE);
			int bytes = ((SocketChannel)currentChannel).read(buffer);
	    	if(-1 == bytes) {
	    		removeTcpConnection(currentChannel);	    		
	    	} else {	    		
				messageHandler.handleMessage(byteBufferToString(buffer) , new ClientInfo((SocketChannel) currentChannel, currentConnection));
	    	}
		}

		@Override
		public Channel getChannel() {
			return tcpServerChannel;
		}		

		@Override
		public void sendResponse(ClientInfo clientInfo, ByteBuffer message) throws IOException {
			try {
				while(message.hasRemaining()) {
					((SocketChannel)clientInfo.getTcpSocketChannel()).write(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			message.clear();
		}

		@Override
		public void sendErrorResponse(ClientInfo clientInfo, ByteBuffer message) throws IOException {
			sendResponse(clientInfo, message);
		}
		
		@Override
		public int getPortNumber() {
			return portNumber.getPort();
		}
	}
	
	/**********************************************
	 * UDP Connection
	 **********************************************/
	private class UdpConnection implements ServerConnection {
		private DatagramChannel UdpChannel;
		private final ServerPort portNumber;
				
		public UdpConnection(ServerPort port) {
			this.portNumber = port;			
		}

		@Override
		public Channel getChannel() {
			return UdpChannel;
		}

		@Override
		public void initServerConnection(Selector selector) throws IOException {
			UdpChannel = DatagramChannel.open();
			UdpChannel.socket().bind(new InetSocketAddress(portNumber.getPort()));
			UdpChannel.configureBlocking(false);
			UdpChannel.register(selector, SelectionKey.OP_READ);
		}

		@Override
		public void handleRequestMessage(Channel currentChannel, ServerConnection currentConnection)
				throws IOException {
			System.out.println("UDP recieved message...");
			ByteBuffer buffer = ByteBuffer.allocate(ConnectionHandler.BUFFER_SIZE);
			SocketAddress address = ((DatagramChannel)currentChannel).receive(buffer);
			if(null != address) {
				messageHandler.handleMessage(byteBufferToString(buffer), new ClientInfo(address, currentConnection));				
			}
		}

		@Override
		public void sendResponse(ClientInfo clientInfo, ByteBuffer message) throws IOException {
			UdpChannel.send(message, clientInfo.getUdpSocketAddress());			
		}

		@Override
		public int getPortNumber() {
			return portNumber.getPort();
		}

		@Override
		public void sendErrorResponse(ClientInfo clientInfo, ByteBuffer message) throws IOException {
			UdpChannel.send(message, clientInfo.getUdpSocketAddress());	
		}
	}
	
	/***********************************************
	 * Client Info
	 **********************************************/
	private class ClientInfo {		
		private SocketChannel tcpSocketChannel;
		private SocketAddress udpSocketAddress;
		private HttpExchange httpExchange;
		private ServerConnection connection;

		public ClientInfo(SocketChannel tcpSocketChannel, ServerConnection connection) {
			this.tcpSocketChannel = tcpSocketChannel;
			this.connection = connection;
		}

		public ClientInfo(SocketAddress udpSocketAddress, ServerConnection connection) {
			this.udpSocketAddress = udpSocketAddress;
			this.connection = connection;
		}

		public ClientInfo(HttpExchange httpExchange, ServerConnection connection) {
			this.connection = connection;
			this.httpExchange = httpExchange;
		}

		public SocketChannel getTcpSocketChannel() {
			return tcpSocketChannel;
		}

		public SocketAddress getUdpSocketAddress() {
			return udpSocketAddress;
		}

		public HttpExchange getHttpExchange() {
			return httpExchange;
		}

		public ServerConnection getConnection() {
			return connection;
		}
	}
	
	/**********************************************
	 * Gateway Message Handler
	 **********************************************/
	private class GatewayMessageHandler {
		private void handleMessage(String jsonObjectsString, ClientInfo clientInfo) throws IOException {			
			System.out.println("GatewayMessageHandler");
			
			try {
				JSONObject jsonObject = new JSONObject(jsonObjectsString);
				Runnable task = taskCreator.convertToRunnable(jsonObject , clientInfo);
				threadPool.submit(task);
			} catch (JSONException e) {
				clientInfo.getConnection().sendErrorResponse(clientInfo, stringToByteBuffer(e.getMessage()));
			}
		}
	}
	
	/**********************************************
	 * JSON To Runnable Converter
	 **********************************************/
	private class TaskCreator {
		private static final String DATA = "Data";
		private static final String COMMAND_KEY = "CommandKey";

		private Runnable convertToRunnable(JSONObject jsonBody, ClientInfo clientInfo) throws JSONException {
			String factoryCommanndKey = getFactoryCommandKey(jsonBody);
			JSONObject factoryData = getFactoryData(jsonBody);
			String companyName = getCompanyName(factoryData);
			
			return new Runnable() {
				@Override
				public void run() {
					try {
						companyDatabase = getCompanyFromMap(companyName);						
						if(commandFactory.map.containsKey(factoryCommanndKey)) {					
							String response = commandFactory.create(factoryCommanndKey).run(factoryData.toString(), companyDatabase);
							clientInfo.getConnection().sendResponse(clientInfo, stringToByteBuffer(response));			
						} else {
							clientInfo.getConnection().sendErrorResponse(clientInfo, stringToByteBuffer("nope"));
						}
					} catch (SQLException | IOException | JSONException e) {
						try {
							clientInfo.getConnection().sendErrorResponse(clientInfo, stringToByteBuffer(e.getMessage()));
							e.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			};
		}
		
		private String getCompanyName(JSONObject jsonObject) throws JSONException {
			return jsonObject.getString("dbName");
		}
		
		private JSONObject getFactoryData(JSONObject jsonObject) throws JSONException {
			System.out.println("Data: "+jsonObject.get(DATA));
			return jsonObject.getJSONObject(DATA);
		}

		private String getFactoryCommandKey(JSONObject jsonObject) throws JSONException {			
			System.out.println("key: "+jsonObject.getString(COMMAND_KEY));
			return jsonObject.getString(COMMAND_KEY);
		}
		
		private DatabaseManagement getCompanyFromMap(String databaseName) throws SQLException {
			companyDatabase = companiesMap.get(databaseName);			
			if(null == companyDatabase) {
				companyDatabase = new DatabaseManagement(URL, user, password, databaseName);
				companiesMap.put(databaseName, companyDatabase);				
			}
			
			return companyDatabase;
		}
	}
	
	/**********************************************
	 * Jar Monitor 
	 **********************************************/
	private class JarMonitor implements DirMonitor {
		private WatchService watcher = FileSystems.getDefault().newWatchService();
		private Dispatcher<String> dispatcher = new Dispatcher<>();
		private WorkerThread thread;
		private Path directory;
		private WatchKey key;
		
		private JarMonitor(String path) throws IOException {
			initWatchService(path);		
			createAndStartThread();
		}
		
		private void initWatchService(String path) throws IOException {
			watcher = FileSystems.getDefault().newWatchService();
			directory = Paths.get(path);
			key = directory.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);		
		}

		private void createAndStartThread() {
			thread = new WorkerThread();
			thread.start();	
		}

		@Override
		public void register(Callback<String> callback) {
			dispatcher.register(callback);
		}

		@Override
		public void unregister(Callback<String> callback) {
			dispatcher.unregister(callback);
		}

		public void updateAll(String path) {
			dispatcher.updateAll(path);
		}
		
		public void stopUpdate() throws IOException {
			thread.continueRunning = false;
			watcher.close();
		}
		
		private class WorkerThread extends Thread {
			private static final String SLASH = "/";
			private boolean continueRunning = true;
			
			@Override
			public void run() {
				while (continueRunning) {
				    try {
						key = watcher.take();
					} catch (InterruptedException e) {
						break;
					} catch (ClosedWatchServiceException e) {
						continue;
					}
				    
				    for(WatchEvent<?> event : key.pollEvents()) {
				    	final Path changedFile = (Path) event.context();
				    	if (changedFile.toString().endsWith(DOT_JAR)) {
				    		updateAll(directory.toString() + SLASH + changedFile.toString());
				    	}
				    } 
				    checkIfKeyValid();			        
				}
			}

			private void checkIfKeyValid() {
				boolean isKeyValid = key.reset();			        
		        if (!isKeyValid) {
		        	continueRunning = false;
		        }				
			}
			
		}	
	}
	
	/**********************************************
	 * Factory Command Loader - observer
	 **********************************************/
	private class FactoryCommandLoader {
		private static final String GET_VERSION = "getVersion";
		private static final String ADD_TO_FACTORY = "addToFactory";
		private Map<String, Integer> classVersionMap = new HashMap<>();
		private Callback<String> callback;
		
		private FactoryCommandLoader() {
			Consumer<String> updateCallback = (jarFilePath) -> {
				try {
					load(jarFilePath);
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException | InstantiationException
						| IOException e) {
					e.printStackTrace();
				}
			};
			
			callback = new Callback<String>(updateCallback, (Void)->System.out.println("done for the day"));
			jarMonitor.register(callback);
		}

		private void load(String jarFilePath) throws ClassNotFoundException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
			List<Class<?>> classes = JarLoader.load(FACTORY_COMMAND_MODIFIER, jarFilePath);
			int version;
			
			for(Class<?> clazz : classes) {
				version = getVersion(clazz);				
				if(isNewClass(clazz)|| isNewVersion(version, clazz)) {
					addToClassMap(version, clazz);
					addToFactory(clazz);
				}
			}
		}

		private void addToClassMap(int version, Class<?> clazz) {
			classVersionMap.put(clazz.getSimpleName(), version);
		}

		private boolean isNewClass(Class<?> clazz) {
			return (classVersionMap.get(clazz.getSimpleName()) == null);
		}

		private boolean isNewVersion(int version, Class<?> clazz) {
			return (version > classVersionMap.get(clazz.getSimpleName()));
		}

		private void addToFactory(Class<?> clazz) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
			Method addToFactory = clazz.getDeclaredMethod(ADD_TO_FACTORY);				
			addToFactory.invoke(clazz.getConstructor().newInstance());
		}

		private int getVersion(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException,
				InvocationTargetException, InstantiationException {
			Method getVersion = clazz.getDeclaredMethod(GET_VERSION);				
			return (int) getVersion.invoke(clazz.getConstructor().newInstance());
		}
	}
	
	/**********************************************
	 * Util Methods  
	 **********************************************/	
	private void initCommandFactory() {		
		File[] files = new File(JAR_DIRECTORY).listFiles();
		for (File file : files) {
			if(file.getName().endsWith(DOT_JAR)) {
				loadIntoFactory(file);
			}
		}
	}

	private void loadIntoFactory(File file) {
		try {
			commandLoader.load(file.getAbsolutePath());
		} catch (ClassNotFoundException | NoSuchMethodException | 
				SecurityException | IllegalAccessException | 
				IllegalArgumentException | InvocationTargetException | 
				InstantiationException | IOException e) {
			e.printStackTrace();
		}
	}

	private void initJarMonitor() {
		try {
			jarMonitor = new JarMonitor(JAR_DIRECTORY);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initCommandLoader() {
		commandLoader = new FactoryCommandLoader();
	}
	
	private ByteBuffer stringToByteBuffer(String msg){
		Charset charset = Charset.forName(UTF_8);
		return ByteBuffer.wrap(msg.getBytes(charset));
	}	
	
	private String byteBufferToString(ByteBuffer buffer) {
	    byte[] bytes;
	    if(buffer.hasArray()) {
	        bytes = buffer.array();
	    } else {
	        bytes = new byte[buffer.remaining()];
	        buffer.get(bytes);
	    }
	    return new String(bytes);
	}

	private void validateRequest(int portNumber) {		
		if(isRunning) {
			throw new IllegalAccessError(ALREADY_RUNNING);						
		}		
		for(ServerConnection connection : connectionHandler.connectionsList) {
			if(portNumber == connection.getPortNumber()) {
				throw new IllegalAccessError(INVALID_PORT + portNumber);
			}			
		}		
	}	

	private void removeTcpConnection(Channel tcpClientChannel) throws IOException {
		tcpClientChannel.close();	    		
		connectionHandler.channelConnectionsMap.remove((SocketChannel)tcpClientChannel);			
	}	
	
	/**********************************************
	 * Server Connection interface
	 **********************************************/
	private interface ServerConnection {
		public void initServerConnection(Selector selector) throws IOException;
		public void sendErrorResponse(ClientInfo clientInfo, ByteBuffer stringToByteBuffer) throws IOException;
		public int getPortNumber();
		public void handleRequestMessage(Channel currentChannel, ServerConnection currentConnection) throws IOException;
		public void sendResponse(ClientInfo clientInfo, ByteBuffer buffer) throws IOException;
		public Channel getChannel();
	}		
}
