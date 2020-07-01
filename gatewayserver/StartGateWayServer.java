package gatewayserver;

import java.io.IOException;

public class StartGateWayServer {

	public static void main(String[] args) throws IOException {
		System.out.println("creating");
		GatewayServer gatewayServer = new GatewayServer("/home/gal/Desktop/jar1");
		gatewayServer.addUdpServer(ServerPort.UDP_SERVER_PORT);
		gatewayServer.addTcpServer(ServerPort.TCP_SERVER_PORT);
		//gatewayServer.addHttpServer(ServerPort.HTTP_SERVER_PORT);
		gatewayServer.addSunHttpServer(ServerPort.HTTP_SERVER_PORT);
		gatewayServer.run();
	}
}

