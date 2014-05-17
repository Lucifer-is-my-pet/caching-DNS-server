import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import org.xbill.DNS.*;

public class Handler implements Runnable {

	private DatagramSocket serverSocket;
	private DatagramPacket fromNslookup;
	private DNScache cache;
	private String forwarderAddress;
	private DatagramSocket back;

	public Handler(DatagramPacket fromNslookup, DNScache cache, String address, int prt, DatagramSocket server) {
		try {
			serverSocket = new DatagramSocket(prt);
			this.back = server;
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.fromNslookup = fromNslookup;
		this.cache = cache;
		this.forwarderAddress = address;
	}

	@Override
	public void run() {
		System.out.println("======");
		InetAddress IPAddress = fromNslookup.getAddress();
		int port = fromNslookup.getPort();
		byte[] data1 = Arrays.copyOfRange(fromNslookup.getData(), 0, fromNslookup.getLength());
//		System.out.println("Received packet of " + data1.length + " bytes from nslookup");

		Header header = null;
		Message message = null;
		try {
			header = new Header(data1);
			message = new Message(data1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int ID = header.getID();		
		Record record = message.getQuestion();
		String name = record.getName().toString();
		//int type = record.getType();
		int type = 1;

		synchronized (cache) {
			cache.update();
			ArrayList<Record> recs = cache.findRecords(name, type);
			if (recs.isEmpty()) {
				System.out.println("I don't know about " + name);
				DatagramPacket toForwarder = null;
				try {
					toForwarder = new DatagramPacket(data1, data1.length, InetAddress.getByName(forwarderAddress), 53);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				try {
					serverSocket.send(toForwarder);
				} catch (IOException e) {
					e.printStackTrace();
				}
				byte[] bytesfromForwarder = new byte[8096];
				DatagramPacket fromForwarder = new DatagramPacket(bytesfromForwarder, bytesfromForwarder.length); 
				try {
					serverSocket.receive(fromForwarder);
				} catch (IOException e) {
					e.printStackTrace();
				}
				byte[] data2 = Arrays.copyOfRange(fromForwarder.getData(), 0, fromForwarder.getLength());
//				System.out.println("Received answer of " + data2.length + " bytes from 8.8.8.8");

				Message mess = null;
				try {
					mess = new Message(data2);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Record[] records = mess.getSectionArray(1);
				for (int i = 0; i < records.length; i++) {
					cache.addRecord(records[i]);
				}
				DatagramPacket toNslookup = new DatagramPacket(data2, data2.length, IPAddress, port);

				try {
					this.back.send(toNslookup);		
//					System.out.println("send back to " + IPAddress);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				System.out.println("Found some records about " + name);
				Message mess = new Message(ID);
				for (int i = 0; i < recs.size(); i++) {				
					mess.addRecord(recs.get(i), 1);
				}
				DatagramPacket toNslookup = new DatagramPacket(mess.toWire(), mess.toWire().length, IPAddress, port);					
				try {
					this.back.send(toNslookup);	
//					System.out.println("send back to " + IPAddress);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		serverSocket.close();
		System.out.println("What in cache:");
		cache.printRecords();
		System.out.println("######");
	}

}
