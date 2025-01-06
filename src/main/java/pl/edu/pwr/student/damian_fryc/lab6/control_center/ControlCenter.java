package pl.edu.pwr.student.damian_fryc.lab6.control_center;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ControlCenter implements IControlCenter {
	private final int port;
	private final ArrayList<RetentionBasinInfo> retentionBasins = new ArrayList<>();

	public ControlCenter(int port) {
		this.port = port;
		startServer();
	}

	@Override
	public void assignRetentionBasin(int port, String host) {
		RetentionBasinInfo newRetentionBasin = new RetentionBasinInfo(port, host);
		try (Socket socket = new Socket(host, port);
		     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

			out.println("gwd");
			String response = in.readLine();
			newRetentionBasin.waterDischarge = Integer.parseInt(response);

		} catch (IOException e) {
			System.err.println("Failed to send message to retention basin: " + e.getMessage());
		}

		try (Socket socket = new Socket(host, port);
		     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

			out.println("gfp");
			String response = in.readLine();
			newRetentionBasin.fillingPercentage = Integer.parseInt(response);

		} catch (IOException e) {
			System.err.println("Failed to send message to retention basin: " + e.getMessage());
		}

		retentionBasins.add(newRetentionBasin);
		System.out.println(newRetentionBasin);
	}

	private void startServer() {
		Thread serverThread = new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(port)) {
				System.out.println("Control Center Server is running on port " + port);
				while (true) {
					Socket clientSocket = serverSocket.accept();

					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

					String request = in.readLine();
					if (request.startsWith("arb:")) {
						String[] data = request.split(":")[1].split(",");
						assignRetentionBasin(Integer.parseInt(data[0]), data[1]);
						System.out.println("Retention Basin registered at " + data[1] + ":" + Integer.parseInt(data[0]));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		serverThread.start();
	}

	public void setWaterDischarge(int basinId, int waterDischarge){
		RetentionBasinInfo retentionBasin = retentionBasins.get(basinId);
		try (Socket socket = new Socket(retentionBasin.getHost(), retentionBasin.getPort());
		     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

			out.println("swd:" + waterDischarge);
			retentionBasin.waterDischarge = waterDischarge;

		} catch (IOException e) {
			System.err.println("Failed to send message to retention basin: " + e.getMessage());
		}
	}

	public ArrayList<RetentionBasinInfo> updatePercentages(){
		for (RetentionBasinInfo retentionBasin : retentionBasins) {
			try (Socket socket = new Socket(retentionBasin.getHost(), retentionBasin.getPort());
			     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

				out.println("gfp");
				String response = in.readLine();
				retentionBasin.fillingPercentage = Integer.parseInt(response);

			} catch (IOException e) {
				System.err.println("Failed to send message to retention basin: " + e.getMessage());
			}
		}
		return retentionBasins;
	}
}
