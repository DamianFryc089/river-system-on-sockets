package pl.edu.pwr.student.damian_fryc.lab6.environment;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Environment implements IEnvironment {
	private final int port;
	private final ArrayList<RiverSectionInfo> riverSections = new ArrayList<>();

	public Environment(int port) {
		this.port = port;
		startServer();
	}

	@Override
	public void assignRiverSection(int port, String host) {
		RiverSectionInfo riverSection = new RiverSectionInfo(port, host);
		riverSections.add(riverSection);
		System.out.println(riverSection);
	}

	private void startServer() {
		Thread serverThread = new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(port)) {
				System.out.println("Environment Server is running on port " + port);
				while (true) {
					Socket clientSocket = serverSocket.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

					String request = in.readLine();
					if (request.startsWith("ars:")) {
						String[] data = request.split(":")[1].split(",");
						assignRiverSection(Integer.parseInt(data[0]), data[1]);
						System.out.println("River section registered at " + data[1] + ":" + Integer.parseInt(data[0]));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		serverThread.start();
	}

	public void setRainfall(int riverId, int rainAmount){
		riverSections.get(riverId).rainAmount = rainAmount;
	}
	public void deliverNewRain(){
		for (RiverSectionInfo riverSection : riverSections) {
			try (Socket socket = new Socket(riverSection.getHost(), riverSection.getPort());
			     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
				out.println("srf:" + riverSection.rainAmount);

			} catch (IOException e) {
				System.err.println("Failed to send message to River Section: " + e.getMessage());
			}
		}
	}
	public ArrayList<RiverSectionInfo> getRiverSections(){
		return riverSections;
	}
}
