package tool;

import java.awt.Color;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import game.SPBLXS;
import main.Room;
import object.Bullet;
import object.Entity;

/**
 * NetController
 * 
 * Packet formatted in "type"&"foo"&"foo"....
 * 
 * "m" : send message
 * 
 * 
 * "r" : set player ready "e" : enter a room "x" : exist the room "i" : indicate
 * game already started "t" : role change send "i" saying game already started
 * 
 * 
 * "p" : update player location "o" : create block "b" : create bullet "e" :
 * when a player enter room
 * 
 * 
 * 
 * 
 * 
 */
public class NetController {

	private SPBLXS game = null;
	private Room room = null;
	private DatagramSocket socket;
	private String addr = "255.255.255.255";
	private int port = 1234;
	String selfAddress = null;

	public NetController(Room room, int port) {
		this.room = room;
		this.port = port;
		try {
			this.selfAddress = InetAddress.getLocalHost().getHostAddress();
			this.socket = new DatagramSocket();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	// set game after all player is read
	public void setGame(SPBLXS game) {
		this.game = game;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public String getSelfAddress() {
		return this.selfAddress;
	}

	// thread to receive packet
	public void receive() {
		new Thread(() -> {
			try {
				byte[] buf = new byte[64];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				DatagramSocket receiver = new DatagramSocket(port);
				do {
					// receive a packet
					receiver.receive(packet);
					// display response
					analyzePacket(packet);

				} while (room != null);
				receiver.close();
			} catch (IllegalArgumentException | BindException e) {
				e.printStackTrace();
				this.room.exit("Invalid Port");
			} catch (SocketException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
	}

	// analyze received packet
	private void analyzePacket(DatagramPacket packet) throws UnknownHostException {
		String data = new String(packet.getData(), 0, packet.getLength());
		System.out.println(data);
		String[] params = data.split("&");
		String raddress = packet.getAddress().getHostAddress();
		if (!raddress.equals(selfAddress)) {
			utilSwitch(params, raddress);
			if (game == null) {
				roomSwitch(params, raddress);
			} else {
				gameSwitch(params, raddress);
			}
		}
	}

	// handles all utility packet
	private void utilSwitch(String[] params, String raddress) {
		/**
		 * "m" : send message
		 */
		switch (params[0]) {
		case "m":
			room.updateChat(params[1], params[2]);
			break;
		}
	}

	// handles all room function packet
	private void roomSwitch(String[] params, String raddress) {
		/**
		 * "r" : set player ready "e" : enter a room "x" : exit the room "i" : indicate
		 * game already started
		 */
		switch (params[0]) {
		case "e":
			// add player and send a hi message
			room.enterPlayer(params[1], raddress, params[2], params[3]);
			break;
		case "i":
			if (room != null) {
				room.exit("Game already started, try other port.");
				this.room = null;
			}
			break;
		case "x":
			room.playerExit(raddress, params[1]);
			
			break;
		case "r":
			room.playerReady(raddress, params[1]);

			break;

		case "t":
			room.changePlayerType(raddress, Integer.parseInt(params[1]));
			break;
		case "c":
			room.changePlayerTeam(raddress,params[1]);
			break;
		}
		

	}

	// handles all game packet
	private void gameSwitch(String[] params, String raddress) {
		/**
		 * "p" : update player location "o" : create block "b" : create bullet "e" :
		 * when a player enter room, send "i" saying game already started
		 */
		switch (params[0]) {
		case "p":
			game.setPlayerLocation(raddress, Integer.parseInt(params[1]), Integer.parseInt(params[2]), Double.parseDouble(params[3]));
			break;
		case "b":
			game.playerShoot(raddress, Integer.parseInt(params[1]), Integer.parseInt(params[2]),
					Integer.parseInt(params[3]));
			break;
		case "o":
			game.createBlock(Integer.parseInt(params[1]), Integer.parseInt(params[2]));
			break;
		case "e":
			send("i&", raddress);
			break;
		case "z":
			//ms = "z&" + target.hp+"&"+bullet.p.getX()+"&"+bullet.p.getY()+"&"+bullet.owner;
			game.bulletHitPacket(raddress, params[1], Double.parseDouble(params[2]), Double.parseDouble(params[3]), Double.parseDouble(params[4]));
		}
	}

	// send packet
	private void send(String params, InetAddress address) {
		try {
			byte[] buf = params.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// send packet
	private void send(String params, String addr) {
		try {
			InetAddress address = InetAddress.getByName(addr);
			this.send(params, address);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getName(String name) {
		if (name == null || name.equals("")) {
			return selfAddress;
		}
		return name;
	}
	
	// change team
	public void changeTeam(String color) {
		this.send("c&"+color, addr);
	}

	// send new message
	public void sendMessage(String msg, String name) {
		String params = "m&" + getName(name) + "&" + msg;
		this.send(params, addr);
	}

	/** Room Sender */

	public void sendPlayerReady(String name) {
		String params = "r&" + getName(name);
		this.send(params, addr);
	}

	public void sendEnterRoom(Entity player) {
		this.sendEnterRoom(player, addr);
	}

	public void sendEnterRoom(Entity player, String addr) {
		String isReady = player.isReady ? "1" : "0";
		String color = "gray";
		if(player.color == Color.BLACK) {
			color = "black";
		}
		else if(player.color == Color.WHITE) {
			color = "white";
		}
		String params = "e&" + getName(player.name) + "&" + isReady + "&" + color;
		this.send(params, addr);
	}

	public void sendExitRoom(String name) {
		String params = "x&" + getName(name);
		this.send(params, addr);
		this.room = null;
	}

	public void sendRoleChange(int index) {
		String params = "t&" + index;
		this.send(params, addr);
	}
	
	public void bulletHit(Entity target, Bullet bullet) {
		String params = "z&"+bullet.owner +"&"+ target.hp+"&"+bullet.p.getX()+"&"+bullet.p.getY();
		this.send(params, addr);
	}

	/** Game Sender */

	// send player location
	public void sendPlayerLocation(Entity player) {
		String params = "p&" + (int) player.p.getX() + "&" + (int) player.p.getY()+"&"+player.hp;
		this.send(params, addr);
	}

	// send new bullet
	public void sendBullet(Entity player, Point p, String type) {
		// find player type
		switch (type) {
		case "Rifleman":
			type = "0";
			break;
		case "Sniper":
			type = "1";
			break;
		case "Melee":
			type = "2";
			break;
		}
		String params = "b&" + type + "&" + (int) p.getX() + "&" + (int) p.getY();
		this.send(params, addr);
	}

	// send new block
	public void sendBlock(int x, int y) {
		String params = "o&" + x + "&" + y;
		this.send(params, addr);
	}

}