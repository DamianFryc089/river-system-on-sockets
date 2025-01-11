package pl.edu.pwr.student.damian_fryc.lab6.retention_basin;

import java.io.*;
import java.net.*;
import java.util.*;

public class RetentionBasin implements IRetentionBasin {
	private final int port;
	private final int maxVolume;
	private int currentVolume = 0;
	private int waterDischarge;

	private String outflowRiverHost;
	private int outflowRiverPort;

	private final Map<Integer, Integer> inflows = new HashMap<>();
	private Thread serverThread;
	private ServerSocket serverSocket;

	public RetentionBasin(int maxVolume, int port ) {
		this.maxVolume = maxVolume;
		this.port = port;
		waterDischarge = maxVolume / 10;
	}

	public boolean setControlCenter(String controlCenterHost, int controlCenterPort){
		try (Socket socket = new Socket(controlCenterHost, controlCenterPort);
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
			out.println("arb:"+port+","+InetAddress.getLocalHost().getHostAddress());
		} catch (IOException e) {
			System.err.println("Failed to send message to control center: " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean addInflowRiver(String inflowRiverHost, int inflowRiverPort){
		try (Socket socket = new Socket(inflowRiverHost, inflowRiverPort);
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
			out.println("arb:"+port+","+InetAddress.getLocalHost().getHostAddress());
		} catch (IOException e) {
			System.err.println("Failed to send message to inflow river: " + e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public int getWaterDischarge() {
		return waterDischarge;
	}

	@Override
	public long getFillingPercentage() {
		return (long) (100.0 * currentVolume / maxVolume);
	}

	@Override
	public void setWaterDischarge(int waterDischarge) {
		this.waterDischarge = waterDischarge;
	}

	@Override
	public void setWaterInflow(int waterInflow, int port) {
		inflows.put(port, waterInflow);
	}

	@Override
	public void assignRiverSection(int port, String host) {
		outflowRiverPort = port;
		outflowRiverHost = host;
		System.out.println("Assigned river section at host: " + host + ", port: " + port);
	}

	public void startServer() {
		 serverThread = new Thread(() -> {
			try {
				serverSocket = new ServerSocket(port);
				System.out.println("Retention basin server is running on port " + port);
				while (true) {
					Socket clientSocket = serverSocket.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

					String request = in.readLine();
					String response = handleRequest(request);
					if (response != null) {
//						if(response.equals("0")) return;
						out.println(response);
					}
				}
			} catch (IOException ignored) {}
		});

		serverThread.start();
	}

	public void stopServer() {
		serverThread.interrupt();
		try {
			serverSocket.close();
		} catch (IOException ignored) {}
	}
	private String handleRequest(String request) {
		if (request.equals("gwd")) {
			return String.valueOf(getWaterDischarge());

		} else if (request.equals("gfp")) {
			return String.valueOf(getFillingPercentage());

		} else if (request.startsWith("swd:")) {
			setWaterDischarge(Integer.parseInt(request.split(":")[1]));
		} else if (request.startsWith("swi:")) {
			String[] params = request.split("[:,]");
			setWaterInflow(Integer.parseInt(params[1]), Integer.parseInt(params[2]));
		} else if (request.startsWith("ars:")) {
			String[] params = request.split("[:,]");
			assignRiverSection(Integer.parseInt(params[1]), params[2]);
		}
		else System.err.println("Unknown request: " + request);
		return null;
	}

	public void basinLogic(){
		int totalInflow = 0;
		for (int inflow : inflows.values())
			totalInflow += inflow;
		inflows.clear();

		currentVolume += totalInflow;
		currentVolume -= waterDischarge;

		int realDischarge = waterDischarge;
		if(currentVolume < 0){
			realDischarge = waterDischarge + currentVolume;
			currentVolume = 0;
		}
		if(currentVolume > maxVolume) {
			realDischarge += currentVolume - maxVolume;
			currentVolume = maxVolume;
		}

		if(realDischarge == 0)
			return;
		if(outflowRiverHost == null)
			return;

		try (Socket socket = new Socket(outflowRiverHost, outflowRiverPort);
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
			out.println("srd:"+realDischarge);
		} catch (IOException e) {
			System.err.println("Failed to send message to outflow river: " + e.getMessage());
		}
	}

	public int getCurrentVolume() {
		return currentVolume;
	}
	public int getMaxVolume() {
		return maxVolume;
	}
}