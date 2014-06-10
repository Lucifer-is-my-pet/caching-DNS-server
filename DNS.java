import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

// nslookup, server 127.0.0.1

public class DNS {

	public static void main(String args[]) throws IOException {
		try {
			System.out.println("Input IP of forwarder");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String forwarderAddress = in.readLine();

			System.out.println("Server starts");
			DatagramSocket server = new DatagramSocket(53);
			DNScache cache = new DNScache();
			int newPort = 2048;
			while (true) {
				
				byte[] bytesfromNslookup = new byte[8096];
				DatagramPacket fromNslookup = new DatagramPacket(bytesfromNslookup, bytesfromNslookup.length); 

				server.receive(fromNslookup);
//				System.out.println("----Recieved something " + fromNslookup);
				Thread th = new Thread(new Handler(fromNslookup, cache, forwarderAddress, newPort, server));
				th.start();
				newPort += 1;

			}

		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
