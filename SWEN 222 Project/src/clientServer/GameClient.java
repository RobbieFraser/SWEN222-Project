package clientServer;

import game.Board;
import game.Player;
import game.Room;
import game.items.Item;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import clientServer.packets.DisconnectPacket;
import clientServer.packets.LoginPacket;
import clientServer.packets.MovePacket;
import clientServer.packets.Packet;
import clientServer.packets.Packet.PacketTypes;
import clientServer.packets.RemoveItemPacket;

public class GameClient extends Thread {

	private InetAddress ipAddress;
	private DatagramSocket socket;
	// testing change
	private Board board;
	private Multiplayer multiplayer;

	public GameClient(Board board, String ipName, Multiplayer multiplayer) {
		this.board = board;
		this.multiplayer = multiplayer;
		try {
			this.socket = new DatagramSocket();
			this.ipAddress = InetAddress.getByName(ipName);
			System.out.println("Client initialisng");
		} catch (SocketException e) {
			System.out.println("Socket exception.");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.out.println("Unknown host exception.");
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			// System.out.println("Packet created");
			try {
				// System.out.println("Client attempting to receive packet");
				this.socket.receive(packet);
				// System.out.println("Client received packet");
			} catch (IOException e) {
				// System.out.println("Something went wrong");
				e.printStackTrace();
			}
			// System.out.println("Client getting ready to parse packet");
			this.parsePacket(packet.getData(), packet.getAddress(),
					packet.getPort());
			// String msg = new String(packet.getData());
			// System.out.println(msg);
			// System.out.println("SERVER > " + msg);
		}
	}

	private void parsePacket(byte[] data, InetAddress address, int port) {
		String msg = new String(data).trim();
		PacketTypes packetTypes = Packet.getPacket(msg.substring(0, 2));
		Packet packet = null;
		switch (packetTypes) {
		default:
		case INVALID:
			break;
		case LOGIN:
			// System.out.println("Login Packet found");
			packet = new LoginPacket(data);
			handleLogin((LoginPacket) packet, address, port);
			break;
		case DISCONNECT:
			packet = new DisconnectPacket(data);
			System.out.println("[" + address.getHostAddress() + ":" + port
					+ "] " + ((DisconnectPacket) packet).getUserName()
					+ " has left the Wild...");
			board.removePlayers(board.getPlayer(((DisconnectPacket) packet)
					.getUserName()));
			break;
		case MOVE:
			packet = new MovePacket(data);
			handleMovePacket((MovePacket) packet);
			break;
			// System.out.println("Player is moving online");
		case REMOVEITEM:
			packet = new RemoveItemPacket(data);
			handleRemoveItemPacket((RemoveItemPacket) packet);
			break;
		}

	}

	public void handleRemoveItemPacket(RemoveItemPacket packet) {
		Room itemRoom = board.getRoomFromId(packet.getRoomID());
		Item itemToRemove = itemRoom.getItemFromId(packet.getItemID());
		itemRoom.removeItems(itemToRemove);
	}
	public void handleMovePacket(MovePacket packet) {
		Player player = board.getPlayer(packet.getUserName());
		player.setHealth(packet.getHealth());
		int playerX = packet.getX();
		int playerY = packet.getY();
		int roomID = packet.getRoomID();
		Room room = board.getRoomFromId(roomID);
		player.setRoom(room);
		player.setCoords(playerX, playerY);
		player.setNorth(intToBool(packet.getNorth()));
		player.setEast(intToBool(packet.getEast()));
		player.setWest(intToBool(packet.getWest()));
		player.setSouth(intToBool(packet.getSouth()));
		player.updatePlayer();
		player.setCurrentTile(player.getCurrentRoom().calcTile(
				player.getCoords()));
	}

	public int boolToInt(boolean bool) {
		int boolInt = bool ? 1 : 0;
		return boolInt;
	}

	public boolean intToBool(int i) {
		if (i == 1) {
			return true;
		} else {
			return false;
		}
	}

	private void handleLogin(LoginPacket packet, InetAddress address, int port) {
		System.out.println("[" + address.getHostAddress() + ":" + port + "]"
				+ ((LoginPacket) packet).getUserName()
				+ " has entered the Wild");
		Point point = new Point(500, 300);
		Player pm = new Player(((LoginPacket) packet).getUserName(), point,
				board.getRoomFromId(0), address, port);
		// System.out.println("Handling login of: "+ pm.getName());
		// board.getPlayers().add(pm);
		// System.out.println("Current players on board are: " +
		// board.getPlayers());
		if (!pm.getName().equals(multiplayer.getCurrentPlayer().getName())) {
			board.addPlayers(pm);
			System.out.println("Added: " + pm.getName());
		}
	}

	public void sendData(byte[] data) {
		DatagramPacket packet = new DatagramPacket(data, data.length,
				ipAddress, 9772);
		try {
			this.socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
