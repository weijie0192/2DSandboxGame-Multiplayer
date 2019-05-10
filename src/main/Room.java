package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;
import object.Entity;
import tool.Board;
import tool.NetController;

public class Room extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4475574918509514818L;
	private JTextArea chatPane;
	private final static Font font = new Font("SansSerif", Font.PLAIN, 16);
	private NetController net;
	private String name;
	private HashMap<String, Entity> playerList;
	private HashMap<String, JLabel> spotList;
	private int port;
	private Entity player;
	private JPanel roomPane;
	private JScrollPane centerPane;
	private Board board;
	private JPanel btnGroup;
	private JButton btnExit;
	private int posX = 0, posY = 0;

	public Room(String name, int port) {
		this.name = name;
		this.port = port;
		this.initUI();
		this.spotList = new HashMap<String, JLabel>();
		this.getContentPane().add(botPane(), BorderLayout.SOUTH);
		this.playerList = new HashMap<String, Entity>();
		this.net = new NetController(this, port);
		this.player = new Entity(name, net.getSelfAddress(), Color.DARK_GRAY);
		centerPane = roomPane();
		this.getContentPane().add(centerPane, BorderLayout.CENTER);
		this.getContentPane().add(btnGroup(), BorderLayout.NORTH);
		this.playerList.put(net.getSelfAddress(), player);
		this.updateChat("System","Welcome! You can change team by typing 'white', 'gray', or 'black'.");
		this.updateChat("System", name + " has enter the room.");
		this.net.receive();
		this.net.sendEnterRoom(this.player);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	// room exit message
	public void exit(String msg) {
		JOptionPane.showMessageDialog(null, msg);
		dispose();
		new App(name);
	}

	// player exit room
	public void playerExit(String id, String name) {
		updateChat("System", name + " has exit the room");
		roomPane.remove(spotList.get(id));
		spotList.remove(id);
		if (board == null) {
			playerList.remove(id);
			roomPane.revalidate();
			roomPane.repaint();
		} else {
			playerList.get(id).isReady = false;
		}
	}

	// enter a player
	public void enterPlayer(String name, String id, String isReady, String color) {
		if (!playerList.containsKey(id)) {
			updateChat("System", name + " has enter the room");
			Color c = Color.DARK_GRAY;
			if (color.equals("black")) {
				c = Color.BLACK;
			} else if (color.equals("white")) {
				c = Color.WHITE;
			}
			Entity newPlayer = new Entity(name, id, c);
			newPlayer.isReady = isReady.equals("1");
			playerList.put(id, newPlayer);
			this.net.sendEnterRoom(this.player);
			String status = (newPlayer.isReady ? "<td style='color:green'>Ready" : "<td style='color:red'>Not Ready");
			JLabel spot = new JLabel("<html><table border='2' ><tr><td style='color:" + color
					+ "; width:450px;'>Player: " + name + "</td>" + status + "</td></tr></table></html>");
			this.spotList.put(id, spot);
			roomPane.add(spot);
			roomPane.revalidate();
			roomPane.repaint();
		}
	}

	public void playerReady(String id, String name) {
		updateChat("System", name + " is ready");
		playerList.get(id).isReady = true;
		setSpot(id);
		checkStart(id);
	}

	private void setSpot(String id) {
		String color = "gray";
		Entity player = playerList.get(id);
		if (player.color == Color.BLACK) {
			color = "black";
		} else if (player.color == Color.WHITE) {
			color = "white";
		}
		String status = (player.isReady ? "<td style='color:green'>Ready" : "<td style='color:red'>Not Ready");
		spotList.get(id).setText("<html><table border='2' ><tr><td style='color:"+color+"; width:450px;'>Player: "
				+ player.name + "</td>" + status + "</td></tr></table></html>");
		roomPane.revalidate();
		roomPane.repaint();
	}

	public void changePlayerType(String id, int index) {
		playerList.get(id).typeIndex(index);
	}
	
	public void changePlayerTeam(String id, String color) {
		Color c = Color.DARK_GRAY;
		if(color.equalsIgnoreCase("black")){
			c = Color.black;
		}
		else if(color.equalsIgnoreCase("white")) {
			c=Color.WHITE;
		}
		changeTeam(playerList.get(id), c);
	}

	// check if all player is ready, if yes start the game
	private void checkStart(String id) {
		playerList.get(id).isReady = true;
		for (Map.Entry<String, Entity> entry : playerList.entrySet()) {
			if (!entry.getValue().isReady) {
				return;
			}
		}
		gameStart();
	}

	private void gameStart() {
		// wait 3 second before start
		new Thread(() -> {
			int pass = 3;
			while (pass >= 0) {
				if (pass == 0) {
					updateChat("System", "Game Start!!!");
					this.getContentPane().remove(centerPane);
					this.board = new Board(this, net, player, playerList);
					this.getContentPane().add(board, BorderLayout.CENTER);
					this.btnGroup.removeAll();
					this.btnGroup.add(btnExit);
					if (player.typeIndex == 1) {
						this.setSize(1000, 900);
					} else {
						this.setSize(900, 800);
					}
					this.setLocationRelativeTo(null);
					this.revalidate();
					this.repaint();
					break;
				} else {
					updateChat("System", "Game start in " + pass + " seconds");
					pass--;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public void gameFinish() {

		this.getContentPane().remove(btnGroup);
		this.getContentPane().add(btnGroup(), BorderLayout.NORTH);
		this.getContentPane().remove(board);
		board.getTimer().stop();
		this.board = null;
		this.net.setGame(null);
		Iterator<Map.Entry<String, Entity>> it = playerList.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Entity> entry = it.next();
			if (entry.getValue().isReady == false) {
				it.remove();
			} else {
				entry.getValue().isReady = false;
			}
		}
		for (Map.Entry<String, JLabel> spot : spotList.entrySet()) {
			Entity p = playerList.get(spot.getKey());

			spot.getValue().setText("<html><table border='2' ><tr><td style='color:gray; width:450px;'>Player: "
					+ p.name + "</td><td style='color:red'>Not Ready</td></tr></table></html>");

		}
		roomPane.revalidate();
		roomPane.repaint();
		centerPane.revalidate();
		centerPane.repaint();
		this.getContentPane().add(centerPane, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}

	private void initUI() {
		this.setLayout(new BorderLayout());
		this.setTitle("Room " + port);
		this.setSize(700, 700);
		this.setLocationRelativeTo(null);
		this.setUndecorated(true);
		this.setVisible(true);

		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				posX = e.getX();
				posY = e.getY();
			}
		});
		this.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent evt) {
				// sets frame position when mouse dragged
				setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);

			}
		});
	}

	private JScrollPane roomPane() {
		roomPane = new JPanel();
		roomPane.setLayout(new BoxLayout(roomPane, BoxLayout.Y_AXIS));
		JLabel spot = new JLabel("<html><table border='2' ><tr><td style='color:gray; width:450px;'>Player: " + name
				+ "</td><td style='color:red'>Not Ready" + "</td></tr></table></html>");
		spotList.put(player.id, spot);
		roomPane.add(spot);
		return new JScrollPane(roomPane);
	}

	private JPanel btnGroup() {
		JButton btnReady = new JButton("Ready");
		btnReady.setFont(font);
		btnReady.setBackground(new Color(70, 123, 90));
		btnReady.setForeground(Color.WHITE);
		btnReady.addActionListener(e -> {
			btnReady.setEnabled(false);
			player.isReady = true;
			setSpot(net.getSelfAddress());
			updateChat("System", player.name + " is ready!");
			checkStart(player.id);
			net.sendPlayerReady(player.name);
		});

		btnGroup = new JPanel();

		btnGroup.add(btnReady);

		btnGroup.add(getRoleChangeBTN("RifleMan", 0));
		btnGroup.add(getRoleChangeBTN("Sniper", 1));
		btnGroup.add(getRoleChangeBTN("Melee", 2));

		btnExit = new JButton("Exit Room");
		btnExit.setBackground(new Color(150, 40, 40));
		btnExit.setForeground(Color.WHITE);
		btnExit.addActionListener(e -> {
			if (board == null) {
				net.sendExitRoom(player.name);
			} else {
				board.getTimer().stop();
				net.sendMessage(player.name + " is disconneted From the game", "System");
				net.setGame(null);
				net.setRoom(null);
			}
			dispose();
			new App(name);
		});
		btnExit.setFont(font);
		btnGroup.add(btnExit);
		return btnGroup;
	}

	private JButton getRoleChangeBTN(String role, int index) {
		JButton btn = new JButton(role);
		btn.addActionListener(e -> {
			player.typeIndex(index);
			net.sendRoleChange(index);
			updateChat("System", "type change to " + role);
		});
		return btn;
	}

	private JPanel botPane() {
		JPanel botPane = new JPanel(new BorderLayout());
		botPane.setPreferredSize(new Dimension(400, 200));
		botPane.add(chatPane(), BorderLayout.CENTER);
		botPane.add(inputField(), BorderLayout.SOUTH);
		return botPane;
	}

	private JScrollPane chatPane() {
		this.chatPane = new JTextArea();
		chatPane.setBackground(Color.BLACK);
		chatPane.setForeground(Color.WHITE);
		chatPane.setFont(font);
		DefaultCaret caret = (DefaultCaret) chatPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		chatPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
		chatPane.setEditable(false);
		return new JScrollPane(chatPane);
	}

	private JPanel inputField() {
		JPanel inputPane = new JPanel(new BorderLayout());
		JTextField input = new JTextField(30);
		input.setFont(font);
		input.addActionListener(e -> {
			inputAction(input);
		});
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(e -> {
			inputAction(input);
		});
		btnSend.setFont(font);
		inputPane.add(input, BorderLayout.CENTER);
		inputPane.add(btnSend, BorderLayout.EAST);
		return inputPane;
	}

	private void inputAction(JTextField input) {

		String text = input.getText();
		if (board == null) {
			if (text.equalsIgnoreCase("black")) {
				changeTeam(player, Color.BLACK);
				net.changeTeam(text);
			}
			else if (text.equalsIgnoreCase("white")) {
				changeTeam(player, Color.WHITE);
				net.changeTeam(text);
			}
			else if (text.equalsIgnoreCase("gray")) {
				changeTeam(player, Color.DARK_GRAY);
				net.changeTeam(text);
			}
		}
		net.sendMessage(input.getText(), name);
		updateChat("You", input.getText());
		input.setText("");
	}
	
	private void changeTeam(Entity player, Color color) {
		player.color = color;
		setSpot(player.id);
		
	}

	public void updateChat(String sender, String msg) {
		chatPane.append(sender + ": " + msg + "\n");
	}
}
