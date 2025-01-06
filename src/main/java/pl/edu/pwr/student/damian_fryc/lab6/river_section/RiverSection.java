package pl.edu.pwr.student.damian_fryc.lab6.river_section;
import java.io.*;
import java.net.*;

public class RiverSection implements IRiverSection {
	private final int port;
	public final int[] waterFragments;
	private String outflowBasinHost;
	private int outflowBasinPort;
	private int realDischarge = 0;
	private int rainfall = 0;

	public RiverSection(int riverSize, int port) {
		this.port = port;
		waterFragments = new int[riverSize];
		startServer();
	}

	@Override
	public void setRealDischarge(int realDischarge) {
		this.realDischarge = realDischarge;
	}

	@Override
	public void setRainfall(int rainfall) {
		this.rainfall = rainfall;
	}

	@Override
	public void assignRetentionBasin(int port, String host) {
		outflowBasinPort = port;
		outflowBasinHost = host;
		System.out.println("Assigned retention basin at host: " + host + ", port: " + port);
	}

	private void startServer() {
		Thread serverThread = new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(port)) {
				System.out.println("River Section Server is running on port " + port);
				while (true) {
					Socket clientSocket = serverSocket.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

					String request = in.readLine();
					String response = handleRequest(request);
					if (response != null) {
						out.println(response);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		serverThread.start();
	}

	private String handleRequest(String request) {
		if (request.startsWith("srd:")) {
			setRealDischarge(Integer.parseInt(request.split(":")[1]));
		} else if (request.startsWith("srf:")) {
			setRainfall(Integer.parseInt(request.split(":")[1]));
		} else if (request.startsWith("arb:")) {
			String[] params = request.split("[:,]");
			assignRetentionBasin(Integer.parseInt(params[1]), params[2]);
		}
		else System.err.println("Unknown request: " + request);
		return null;
	}

	public void riverLogic(){
		int waterToRemove = waterFragments[waterFragments.length - 1];
		for (int i = waterFragments.length - 1; i >= 1; i--) {
			waterFragments[i] = waterFragments[i-1];
		}
		waterFragments[0] = realDischarge + rainfall;
		realDischarge = 0;
		rainfall = 0;

		if(waterToRemove == 0)
			return;

		try (Socket socket = new Socket(outflowBasinHost, outflowBasinPort);
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
			out.println("swi:" + waterToRemove+","+port);
		} catch (IOException e) {
			System.err.println("Failed to water info to retention basin: " + e.getMessage());
		}
	}

	public boolean setInflowBasin(String inflowRiverHost, int inflowRiverPort){
		try (Socket socket = new Socket(inflowRiverHost, inflowRiverPort);
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
			out.println("ars:"+port+","+InetAddress.getLocalHost().getHostAddress());
		} catch (IOException e) {
			System.err.println("Failed to send message to inflow basin: " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean setEnvironment(String environmentHost, int environmentPort){
		try (Socket socket = new Socket(environmentHost, environmentPort);
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
			out.println("ars:"+port+","+InetAddress.getLocalHost().getHostAddress());
		} catch (IOException e) {
			System.err.println("Failed to send message to environment: " + e.getMessage());
			return false;
		}
		return true;
	}

}
