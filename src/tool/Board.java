package tool;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.Timer;

import game.IGame;
import game.SPBLXS;
import main.Room;
import object.Entity;

public class Board extends JPanel {
	private static final long serialVersionUID = 1L;
	private Timer timer = null;
	private IGame game = null;
	private Room room;

	public Board(Room room, NetController net, Entity player, HashMap<String, Entity> playerList) {
		this.room = room;
		//this.setBackground(new Color(57, 54, 58));
		this.game = new SPBLXS(this, net, player, playerList);
		net.setGame((SPBLXS)game);
		this.addEvent();
		// game loop
		this.timer = new Timer(13, e -> {
			game.roundAction();
			repaint();
		});
		this.game.gameStart();
		this.timer.start();
	}

	
	public Timer getTimer() {
		return timer;
	}

	private void addEvent() {
	
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
		
				if (game != null) {
					game.keyPressed(e);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (game != null) {
					game.keyReleased(e);
				}
			}
		});

		this.addMouseWheelListener(e -> {
			game.mouseWheel(e);
		});

		MouseAdapter ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(!isFocusOwner()) {
					requestFocus();
				}
				game.mouseClicked(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				game.mouseReleased(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				game.mouseMove(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				game.mouseMove(e);
			}
		};

		this.addMouseListener(ml);

		this.addMouseMotionListener(ml);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		// draw game
		if (game != null) {
			game.drawBack(g2d);
		}

		// draw game front
		if (game != null) {
			game.drawFront(g2d);
		}

	}

	public Room getRoom() {
		// TODO Auto-generated method stub
		return room;
	}
}