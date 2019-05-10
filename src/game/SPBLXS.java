package game;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import tool.*;
import object.*;

public class SPBLXS extends Game implements GameParams {

	/*----------------GLOABL VARIABLES----------------*/
	private Mouse mouse = new Mouse();

	private BufferedImage blockCanvas;

	private int energy = initEnergy; // consuming energy
	private Entity player; // player
	private HashMap<String, Entity> playerList = null; // players
	private Block blockMap[][]; // data structure to store each block's data
	private Collection<Bullet> bulletList = new ConcurrentLinkedQueue<Bullet>(); // bullet data structure
	private Collection<Obj> bloodList = new ConcurrentLinkedQueue<Obj>();
	private List<Entity> scoreBoard = null;
	private Point spawnPoint = null;
	private NetController net;
	private ScoreSorter scoreSolter = new ScoreSorter();
	private int time = 0;

	private boolean respawning = false;

	private boolean showScore = true;

	public SPBLXS(Board board, NetController net, Entity player, HashMap<String, Entity> playerList) {
		super(board);
		this.net = net;
		this.player = player;
		this.playerList = playerList;
		this.scoreBoard = new ArrayList<Entity>(playerList.size());
		spawnPoint = new Point(Math.random() * 3000, ((0.2 * Math.random() + 0.3)) * 3000);
		for (Map.Entry<String, Entity> entry : playerList.entrySet()) {
			Entity p = entry.getValue();
			if (p == player) {
				p.setup(blockSize, playerType[p.typeIndex], spawnPoint, jumpFuel, mouse);
			} else {
				p.setup(blockSize, playerType[p.typeIndex], new Point(1400, 1400), jumpFuel, null);
			}
			scoreBoard.add(p);
		}
	}

	public void setPlayerLocation(String id, int x, int y, double hp) {
		Entity entity = playerList.get(id);
		entity.hp = hp;
		entity.p.setLocation(x, y);
		updateOccupied(entity, entity.halfWidth, entity.halfHeight);
	}

	public void playerShoot(String id, int type, int x, int y) {
		Entity entity = playerList.get(id);
		if (entity.weapon != playerType[type]) {
			entity.changeType(playerType[type]);
		}
		shootBullet(entity, new Point(x, y));
	}

	public void createBlock(int x, int y) {
		if (x < blockMap.length && y < blockMap[0].length) {
			Block mapBlock = blockMap[x][y];
			Point p = new Point(x * blockSize, y * blockSize);
			Obj obj = new Obj(p, blockSize, blockhp * 2, Color.LIGHT_GRAY);
			if (mapBlock == null) {
				blockMap[x][y] = new Block(obj, p);
				renderBlock(obj.p, Color.BLACK);
			} else {
				mapBlock.stObj = obj;
				renderBlock(obj.p, Color.BLACK);
			}
		}
	}

	/*----------------READY EVENT----------------*/
	@Override
	public void gameClear() {
		gameReset();
	}

	// double blockSize, String id, PlayerType type, Point p, double jumpFuel, Color
	// color, Obj target

	@Override
	public void gameStart() {
		board.requestFocus();
		// create blockCanvas
		newBlockCanvas();

		// generate block
		generateMap(0, (int) mapSize.getX());
		secondCounter();
		// draw the game
		// draw();
		// start counting
	};

	@Override
	public void gameReset() {
		bulletList.forEach(b -> b.destroy());
		bulletList.clear();
		energy = initEnergy;
		// player.p.setLocation(spawnPoint.getX(), spawnPoint.getY());
		score = 0;
		player.moved = true;
	};

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_D: // d
			player.right = true;
			break;
		case KeyEvent.VK_S: // s
			player.down = true;
			break;
		case KeyEvent.VK_A: // a
			player.left = true;
			break;
		case KeyEvent.VK_SPACE: // space
		case KeyEvent.VK_W: // w
			player.up = true;
			break;
		case KeyEvent.VK_1:
			if (respawning) {
				player.changeType(playerType[0]);
			}
			break;
		case KeyEvent.VK_2:
			if (respawning) {
				player.changeType(playerType[1]);
			}
			break;
		case KeyEvent.VK_3:
			if (respawning) {
				player.changeType(playerType[2]);
			}
			break;
		case KeyEvent.VK_T:
			showScore = !showScore;
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE: // space
		case KeyEvent.VK_W:
			this.player.up = false;
			break;
		case KeyEvent.VK_S:
			this.player.down = false;
			break;
		case KeyEvent.VK_A:
			this.player.left = false;
			break;
		case KeyEvent.VK_D:
			this.player.right = false;
			break;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (board.getTimer().isRunning()) {
			player.click = e.getButton();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		player.click = -1;
	};

	@Override
	public void mouseMove(MouseEvent e) {

		mouse.p.setLocation(e.getX(), e.getY());
	};

	@Override
	public void playerMove() {
		double newX, newY;
		// for player walk left or right
		if (player.left || player.right) {
			// create a new x position based on direction.
			newX = (player.left) ? player.p.getX() - player.speed : player.p.getX() + player.speed;
			// teleport player to other side if player walk out of the map
			if (newX >= blockCanvas.getWidth()) {
				newX -= blockCanvas.getWidth();
			} else if (newX < 0) {
				newX += blockCanvas.getWidth();
			}
			// if there is new barrier set player x position to new x position
			if (!barrier_LeftRight(player, newX, player.left)) {
				player.p.setX(newX);
				// player moved
				player.moved = true;
			}
			net.sendPlayerLocation(player);
		}
		// update block player occupied
		if (player.moved) {
			updateOccupied(player, player.halfWidth, player.halfHeight);
		}

		// jump action
		if (player.up && player.jf > 0) {

			newY = player.p.getY() - player.speed;
			// check if there is a barrier on top
			if (!barrier_UpDown(player, newY - player.halfHeight, false)) {
				// player moved
				player.moved = true;
				// set new position
				player.p.setY(newY);
				// decrease jump time
			}
			net.sendPlayerLocation(player);
			player.jf--;
		}
		// test if player will fall if player is not jumping and has moved
		else if (player.moved) {
			// falling speed
			if (player.down) {
				newY = player.p.getY() + player.speed * 1.5;
			} else {
				newY = player.p.getY() + player.speed;
			}

			if (!barrier_UpDown(player, (newY + player.halfHeight), true)) {
				player.p.setY(newY);

			} else {
				// reset jump
				player.recoverJump();
				player.moved = false;
				updateOccupied(player, player.halfWidth, player.halfHeight);
			}
			net.sendPlayerLocation(player);
		} else
			player.recoverJump();
	}

	@Override
	public void roundAction() {
		/*
		 * if (!board.isFocusOwner()) { board.requestFocus(); }
		 */
		gameLoop();
	};

	private void respawn() {
		respawning = true;
		/*
		 * player.p.setLocation(1000, 1000); updateOccupied(player, player.halfWidth,
		 * player.halfHeight);
		 */
		Utility.setTimeout(() -> {
			respawning = false;
			player.p.setLocation(Math.random() * 3000, ((0.2 * Math.random() + 0.3)) * 3000);
			player.hp = 100;
			energy = initEnergy;
			player.moved = true;
		}, 3000);
	}

	private void respawn(Entity p) {
		Utility.setTimeout(() -> {
			p.hp = 100;
		}, 3000);
	}

	@Override
	public void drawBack(Graphics2D g2d) {
		draw(g2d);
	};

	// generate new map
	private void generateMap(int start, int end) {
		int horizontalLine = (int) Math.round(mapSize.getY() / 2);
		blockMap = new Block[(int) mapSize.getX()][(int) mapSize.getY()];
		for (int x = start; x < end; x++) {

			if (x < mapSize.getX() * 0.2) {
				horizontalLine++;
			} else if (x > mapSize.getX() * 0.8) {

				horizontalLine--;
			}
			// generate each vertical block from half
			for (int y = (int) horizontalLine; y < mapSize.getY(); y++) {

				// different layer different block
				int hp;
				Color color = new Color(Math.abs(x - 100) + 80, y, y);
				;
				if (y < mapSize.getY() * 0.85 && y > mapSize.getY() * 0.15) {
					hp = blockhp;
				} else {
					hp = blockhp * 1000;
				}
				Point p = new Point(x * blockSize, y * blockSize);
				// create block object
				Obj obj = new Obj(p, blockSize, hp, color);
				Block block = new Block(obj, p, "");
				// draw block into block canvas
				renderBlock(obj.p, obj.color);

				// insert block
				blockMap[x][y] = block;

			}
		}
	}

	/*----------------GAME CONTENT----------------*/

	private void gameLoop() {
		// loop game only if

		if (energy < initEnergy) {
			energy++;
		}

		if (!respawning) {
			// player move action
			playerMove();
			// player click action
			playerClick();
		} else {
			net.sendPlayerLocation(player);
		}
		// refresh canvas;
		// draw();
		// bullet event and render bullet
		bulletMove();

		// move blood
		bloodMove();
		if (player.hp <= 0 && !respawning) {
			respawn();
		}
	}

	// count game second
	private void secondCounter() {
		new Thread(() -> {
			while (board.getTimer().isRunning()) {
				try {
					Thread.sleep(1000);
					time++;
					if (time >= 180) {
						Entity winner = scoreBoard.get(0);
						board.getRoom().updateChat("System", "The Winner is " + winner.name.toUpperCase()
								+ " with kdr of " + winner.kill + "/" + winner.death + "!!!");
						board.getRoom().gameFinish();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	// convert block coordinate to map index
	private int indexConvert(double val, double limit) {
		int index = (int) Math.round(val / blockSize);
		if (index < 0) {
			index = (int) (limit + index);
		} else if (index >= limit) {
			index -= limit;
		}
		return index;
	}

	/*----------------ALL CLICK private void----------------*/

	// player click action
	private void playerClick() {

		if (time > startTime && !respawning && energy >= player.weapon.consume) {
			switch (player.click) {
			// left click
			case MouseEvent.BUTTON1:
				// shooting from player to mouse location
				shootBullet(player, mouse.mapXY());
				net.sendBullet(player, mouse.mapXY(), player.weapon.type);
				break;
			// middle click
			case MouseEvent.BUTTON3:
				// place block
				placeObject();
				// destroyObject();
				break;
			// right click
			default:

				break;
			// right click
			}
		}

	}

	// shoot bullet event
	public void shootBullet(Entity shooter, Point target) {
		if (!shooter.fire) {

			Point velocity = Utility.getVelocity(shooter.p, target, shooter.weapon.speed);

			// create a bullet from shooter position and use shooter's bullet property
			Bullet bullet = new Bullet(shooter.p, velocity, shooter.weapon, shooter.id, shooter.color);
			// add bullet into the list
			bulletList.add(bullet);
			shooter.setCD();
			// decrease energy if shooter is player
			if (shooter == player) {
				energy -= shooter.weapon.consume;
			}
		}
	}

	// player place object event
	private void placeObject() {
		// check click range
		double centerX = board.getWidth() / 2;
		double centerY = (board.getHeight() / 2) - blockSize / 2;
		// make sure clicking location is inside block range of player
		if (Math.abs(mouse.p.getX() - centerX) <= blockSize * 5.5
				&& Math.abs(mouse.p.getY() - centerY) <= blockSize * 5.5) {
			Point mapmouse = mouse.mapXY();

			// get mosue x and y based on player location
			int x = indexConvert(mapmouse.getX(), mapSize.getX());
			int y = indexConvert(mapmouse.getY(), mapSize.getY());
			// get block
			Block mapBlock = blockMap[x][y];
			// call success private void if there is a block in this coordinate
			if (mapBlock != null) {
				if (mapBlock.stObj == null && !mapBlock.objs.contains(player)) {
					Obj obj = new Obj(new Point(x * blockSize, y * blockSize), blockSize, blockhp * 2, Color.BLACK);
					mapBlock.stObj = obj;
					renderBlock(obj.p, Color.BLACK);
					energy -= blockEnergy;
					net.sendBlock(x, y);
				}
			}
			// call fail private void if fail is defined and there is not block in this
			// coordinate
			else {
				Point p = new Point(x * blockSize, y * blockSize);
				// if this block is empty create the block with the object added
				Obj obj = new Obj(p, blockSize, blockhp * 2, Color.BLACK);
				blockMap[x][y] = new Block(obj, p);
				renderBlock(obj.p, Color.BLACK);
				energy -= 2;
				net.sendBlock(x, y);
			}

		}
	}

	/*----------------MOVEMENT COLLISION----------------*/

	// detect upper or lower barrier
	private boolean barrier_UpDown(Entity obj, double newY, boolean land) {
		// upper and lower limit
		if (newY > blockCanvas.getHeight() * 0.9 || newY < blockCanvas.getHeight() * 0.15) {
			return true;
		}
		// index of min x coordinate
		int minxi = indexConvert(obj.p.getX() - obj.halfWidth + 1, mapSize.getX());
		// index of max x coordinate
		int maxxi = indexConvert(obj.p.getX() + obj.halfWidth - 1, mapSize.getX());
		// index of y coordinate
		int yi = indexConvert(newY, mapSize.getY());
		// if left and right is inside same block
		if (minxi == maxxi) {
			// return true if block has a static object
			if (checkBlock(minxi, yi)) {
				if (land) {
					// make sure player wont stuck into the block
					obj.p.setY((yi * blockSize) - (obj.halfHeight) - blockSize / 2);
				} else {
					obj.p.setY((yi * blockSize) + (obj.halfHeight) + blockSize / 2);
				}
				return true;
			}
		} else {
			// extreme case when minii is greater than maxxi when cross boundary
			if (minxi >= maxxi) {
				while (minxi != maxxi + 1) {
					// console.log(minxi+" "+maxxi);
					// return true if there is a barrier
					if (checkBlock(minxi, yi)) {
						if (land) {
							// make sure player wont stuck into the block
							obj.p.setY((yi * blockSize) - (obj.halfHeight) - blockSize / 2);
						} else {
							obj.p.setY((yi * blockSize) + (obj.halfHeight) + blockSize / 2);
						}
						return true;
					} else {
						minxi++;
						if (minxi >= mapSize.getX()) {
							minxi = 0;
						}
					}
				}
			} else {
				// travel from top to bottom to see if any body part collide with an object
				while (minxi <= maxxi) {
					// return true if there is a barrier
					if (checkBlock(minxi, yi)) {
						if (land) {
							// make sure player wont stuck into the block
							obj.p.setY((yi * blockSize) - (obj.halfHeight) - blockSize / 2);
						} else {
							obj.p.setY((yi * blockSize) + (obj.halfHeight) + blockSize / 2);
						}
						return true;
					} else {
						minxi++;
					}
				}
			}
		}
		return false;
	}

	// check if block has static object
	private boolean checkBlock(int x, int y) {
		return blockMap[x][y] != null && blockMap[x][y].stObj != null;
	}

	// detect left or right barrier
	private boolean barrier_LeftRight(Entity obj, double newX, boolean left) {
		// get map x index;
		int xi = indexConvert(((left) ? newX - obj.halfWidth : newX + obj.halfWidth), mapSize.getX());
		// check if there is vertical blocks in this horizontal position
		if (blockMap[xi] != null) {
			// get obj's top y index
			int minyi = indexConvert((obj.p.getY() + 1 - obj.halfHeight), mapSize.getY());
			// get obj's bottom y index
			int maxyi = indexConvert((obj.p.getY() - 1 + obj.halfHeight), mapSize.getY());
			// if top and bottom is in same block
			if (minyi == maxyi) {
				// return true is there is no static object else return false
				if (checkBlock(xi, minyi)) {
					// adjust obj coordinate so it wont stuck into the block
					obj.p.setX((xi * blockSize) + ((left) ? blockSize : -blockSize));
					updateOccupied(obj, obj.halfWidth, obj.halfHeight);
					return true;
				}
			} else {
				// travel from top to bottom to see if any body part collide with an object
				while (minyi <= maxyi) {
					// return true if there is a barrier
					if (checkBlock(xi, minyi)) {
						// adjust obj coordinate so it wont stuck into the block
						obj.p.setX((xi * blockSize) + ((left) ? blockSize : -blockSize));
						updateOccupied(obj, obj.halfWidth, obj.halfHeight);
						return true;
					}
					minyi++;
				}
			}
		}
		// return no barrier
		return false;
	}

	// update the block player occupied
	private void updateOccupied(Obj obj, double width, double height) {

		try {
			int left = indexConvert((obj.p.getX() - width), mapSize.getX());
			int right = indexConvert((obj.p.getX() - 1 + width), mapSize.getX());
			int top = indexConvert(obj.p.getY() - height, mapSize.getY());
			int bot = indexConvert((obj.p.getY() - 1 + height), mapSize.getY());

			// get all block player occupied
			LinkedList<Block> newBlocks = new LinkedList<Block>();

			// extreme case when crossing border
			if (left > right) {
				while (left != right + 1) {
					int tempTop = top;
					while (tempTop <= bot) {
						if (blockMap[left][tempTop] == null) {
							blockMap[left][tempTop] = new Block(null, new Point(left * blockSize, tempTop * blockSize));
						}
						newBlocks.add(blockMap[left][tempTop]);
						tempTop++;
					}
					left++;
					if (left >= mapSize.getX()) {
						left = 0;
					}
				}
			} else {
				while (left <= right) {
					int tempTop = top;
					while (tempTop <= bot) {
						if (blockMap[left][tempTop] == null) {
							blockMap[left][tempTop] = new Block(null, new Point(left * blockSize, tempTop * blockSize));
						}
						newBlocks.add(blockMap[left][tempTop]);
						tempTop++;
					}
					left++;
				}
			}
			// update
			obj.updateBlock(newBlocks);
		} catch (Exception e) {

		}
	}

	/*----------------OBJECT EVENT----------------*/

	private void bulletMove() {
		if (!bulletList.isEmpty()) {
			Iterator<Bullet> it = bulletList.iterator();
			while (it.hasNext()) {
				Bullet bullet = it.next();
				// draw bullet
				// renderBullet(bullet);
				// get collided block array
				LinkedList<Block> blockArr = bullet.getCollideBlock();
				// do this if there is more than one collided block
				blockArr.forEach(block -> {
					// if it is a block
					if (block.stObj != null) {
						bulletAndObj(block, bullet);
					}
					// else it is a entity
					else {
						bulletAndEntity(block, bullet);
					}
				});
				// System.out.println(bullet.p.getX());
				if (bullet.hp > 0 && bullet.travelRange > 0) {
					// move bullet if travel range and hp is greater than 0
					bullet.move(blockCanvas);
					updateOccupied(bullet, bullet.radius, bullet.radius);
				} else {
					// remove this node if the bullet hp is 0 or collide with a block
					it.remove();
					bullet.destroy();
				}
			}
		}

	}

	// when bullet collide with stobj
	private void bulletAndObj(Block block, Bullet bullet) {
		// reduce block hp
		block.stObj.hp -= bullet.property.dmg;
		// clear block if block hp is 0 or less
		if (block.stObj.hp <= 0) {
			block.stObj = null;
			clearBlock(block.p);
			player.moved = true;
		}
		// decrease bullet hp
		bullet.hp--;
	}

	// when bullet collide with entity
	private void bulletAndEntity(Block block, Bullet bullet) {
		try {
		Iterator<Obj> it = block.objs.iterator();
		while (it.hasNext()) {
			Entity obj = (Entity) it.next();
			// make sure it is not the shooter of the bullet
			if (!obj.id.equalsIgnoreCase(bullet.owner) && obj.hp > 0
					&& (bullet.color == Color.DARK_GRAY || obj.color != bullet.color)
					&& Utility.testCollide(obj, bullet)) {
				if (obj == player) {
					obj.hp -= bullet.property.dmg;
					net.bulletHit(obj, bullet);
					bulletHitPlayer(bullet.owner, obj, bullet.p);
				}
				bullet.hp--;
			}
		}
		}
		catch(Exception e) {
			
		}
	}

	public void bulletHitPlayer(String shooterID, Entity target, Point p) {
		Entity shooter = playerList.get(shooterID);
		generateBlood(p);
		// destory obj if its hp is 0 or less and the object is a enemy
		if (target.hp <= 0) {
			// add score
			shooter.kill++;
			target.death++;
			if (target != player) {
				respawn(target);
				if(shooter == player) {
					energy += 300;
				}
			}
			Collections.sort(scoreBoard, scoreSolter);
			board.getRoom().updateChat("System", target.name + " was killed by " + shooter.name);
		}
	}

	public void bulletHitPacket(String targetID, String shooterID, double hp, double x, double y) {
		Point p = new Point(x, y);
		Entity target = playerList.get(targetID);
		target.hp = hp;
		bulletHitPlayer(shooterID, target, p);
	}

	/*
	 * private void modifyBoundary(Obj obj) { if (obj.p.getX() >=
	 * blockCanvas.getWidth()) { obj.p.setX(obj.p.getX() - blockCanvas.getWidth());
	 * } else if (obj.p.getX() < 0) { obj.p.setX(obj.p.getX() +
	 * blockCanvas.getWidth()); } }
	 */

	private void bloodMove() {
		if (!bloodList.isEmpty()) {
			Iterator<Obj> it = bloodList.iterator();
			while (it.hasNext()) {

				Obj bld = it.next();
				if (bld.hp < 0) {
					it.remove();
					bld.destroy();
				} else {
					bld.p.setX(bld.p.getX() + bld.v.getX());
					bld.p.setY(bld.p.getY() + bld.v.getY());
					bld.hp -= 0.03;
				}
			}
		}
	}

	private void generateBlood(Point p) {
		double num = 2 + Math.random() * 5;
		for (int i = 0; i < num; i++) {
			double size = 3 + Math.random() * (blockSize / 4);
			Point v = new Point((2 - Math.random() * 4), (2 - Math.random() * 4));
			// p, diameter, hp, color
			Obj blood = new Obj(p, size, 1, Color.CYAN.darker(), "", v);
			bloodList.add(blood);
		}
	}

	/* ----------------CANVAS-DRAW private void---------------- */

	// generate new block canvas
	private void newBlockCanvas() {
		int width = (int) (blockSize * mapSize.getX());
		int height = (int) (blockSize * mapSize.getY());
		blockCanvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = blockCanvas.createGraphics();
		GradientPaint paint = new GradientPaint(0.5f, 0.3f, new Color(0.4f, 0.1f, 0.7f, 0.7f), 0.0f, height,
				new Color(0.3f, 0.5f, 0.3f, 0.2f));
		g.setPaint(paint);
		g.fill(new Rectangle2D.Double(0, 0, width, height));
		g.dispose();
		blockCanvas.flush();
	}

	// render block
	private void renderBlock(Point p, Color c) {
		Graphics2D g2d = (Graphics2D) blockCanvas.getGraphics();
		g2d.setColor(c);
		g2d.fillRect((int) p.getX(), (int) p.getY(), blockSize, blockSize);
	}

	// clear block
	private void clearBlock(Point p) {
		Graphics2D g2d = (Graphics2D) blockCanvas.getGraphics();
		// if (obj.stObj == null) {
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRect((int) p.getX(), (int) p.getY(), blockSize, blockSize);
		/*
		 * } else { renderBlock(obj.p, obj.stObj.color); }
		 */
	}

	/*
	 * // render bullet private void renderBullet(Bullet bullet) { Graphics2D
	 * g2d=(Graphics2D)blockCanvas.getGraphics(); Point p = getXYRatio(bullet.p);
	 * g2d.setColor(Color.BLACK); g2d.fillOval((int)p.getX(), (int)p.getY(),
	 * (int)bullet.diameter, (int)bullet.diameter);
	 * 
	 * }
	 */

	// get x and y range based on player position
	private Point getXYRatio(Point p) {
		Point p2 = new Point(0, 0);
		if (Math.abs(p.getX() - player.p.getX()) > blockCanvas.getWidth() / 2) {
			if (p.getX() > player.p.getX()) {
				p2.setX(p.getX() - blockCanvas.getWidth() - (player.p.getX()) + board.getWidth() / 2);
			} else {
				p2.setX(blockCanvas.getWidth() + p.getX() - (player.p.getX()) + board.getWidth() / 2);
			}
		} else {
			p2.setX(p.getX() - (player.p.getX()) + board.getWidth() / 2);
		}
		p2.setY(p.getY() - (player.p.getY() + blockSize / 2) + board.getHeight() / 2);
		return p2;
	}

	private void renderObj(Collection<? extends Obj> objs, Graphics2D g2d) {
		if (!objs.isEmpty()) {
			for (Obj obj : objs) {
				g2d.setColor(obj.color);
				Point p = getXYRatio(obj.p);
				g2d.fillOval((int) p.getX(), (int) p.getY(), (int) obj.diameter, (int) obj.diameter);
			}
		}
	}

	// draw canvas
	private void draw(Graphics2D g2d) {

		drawBlockCanvas(g2d);
		try {
			renderObj(bloodList, g2d);
			renderObj(bulletList, g2d);
		} catch (Exception e) {

		}

		// other player

		for (Map.Entry<String, Entity> entry : playerList.entrySet()) {
			Entity player = entry.getValue();
			if (player.hp > 0) {
				g2d.setColor(player.color);
				Point p = getXYRatio(player.p);
				g2d.fillRect((int) (p.getX() - player.halfWidth) + 1, (int) (p.getY() - player.halfHeight),
						(int) player.width, (int) player.height);
				g2d.drawString(player.name, (int) (p.getX() - player.halfWidth) - 4,
						(int) (p.getY() - player.halfHeight - 10));
				if (player.hp < 100) {
					g2d.setColor(Color.ORANGE);
				} else if (player.hp < 50) {
					g2d.setColor(Color.RED);
				} else {
					g2d.setColor(Color.GREEN);
				}
				g2d.fillRect((int) (p.getX() - player.halfWidth) - 10, (int) (p.getY() - player.halfHeight - 6),
						(int) (player.hp / 3), 3);
				if (player != this.player) {
					int angle = Utility.getAngle(this.player.p, player.p);
					if ((player.color == Color.DARK_GRAY || player.color != this.player.color)) {
						g2d.setColor(Color.RED);
					} else {
						g2d.setColor(Color.GREEN);
					}

					g2d.drawArc((int) (board.getWidth() / 2 - player.width) - 25,
							(int) (board.getHeight() / 2 - player.height) - 25, 75, 75, -angle - 2, 4);
				}
			}
		}

		drawScoreBoard(g2d);
		g2d.setColor(Color.white);

		g2d.setFont(gameFont);

		g2d.drawString("Coordinate: (" + (int) player.p.getX() + "," + (int) player.p.getY() + ")",
				board.getWidth() / 2 - 80, board.getHeight() - 10);

		g2d.setColor(flyCanvasColor);
		int jf = (int) (player.jf * (board.getHeight() / player.initJF));
		g2d.fillRect(board.getWidth() - 10, board.getHeight() - jf, 20, jf);

		g2d.setColor(bldCanvasColor);
		g2d.fillRect(board.getWidth() - (int) player.hp * 2, 0, (int) player.hp * 2, 17);

		g2d.setColor(energyCanvasColor);
		g2d.fillRect(board.getWidth() - (energy / 5), 17, (energy / 5), 17);

		int textX = board.getWidth() - 80;
		g2d.setColor(Color.WHITE);
		g2d.drawString("HP: " + (int) player.hp, textX, 12);
		g2d.drawString("Energy: " + energy, textX - 40, 30);
		g2d.drawString("Jump Fuel: " + player.jf, textX - 50, board.getHeight() - 10);
		g2d.drawString("Kills: " + player.kill, textX - 20, 50);
		g2d.drawString("Deaths:" + player.death, textX - 20, 70);
		g2d.drawString("Type: " + player.getType(), textX - 30, 90);

		if (respawning) {
			g2d.setColor(Color.BLACK);
			g2d.drawString("Press '1' for Rifleman", board.getWidth() / 2 - 70, 230);
			g2d.drawString("Press '2' for Sniper", board.getWidth() / 2 - 70, 260);
			g2d.drawString("Press '3' for Melee", board.getWidth() / 2 - 70, 290);
			g2d.setFont(alertFont);
			g2d.setColor(Color.BLUE);
			g2d.drawString("Respawning.....", board.getWidth() / 2 - 80, 160);
		} else if (time <= startTime) {
			g2d.setColor(Color.BLUE);
			g2d.setFont(alertFont);
			g2d.drawString("Fire power release in ....." + (startTime - time), board.getWidth() / 2 - 180,
					board.getHeight() / 2 - 200);
			g2d.drawString("Click to move!", board.getWidth() / 2 - 110, 150);
		} else {
			g2d.drawString("Time Remain: " + (2 - (time / 60)) + ":" + (60 - (time % 60)), board.getWidth() / 2 - 70,
					board.getHeight() / 2 - 250);
		}
	}

	// draw score board
	private void drawScoreBoard(Graphics2D g2d) {
		if (showScore) {
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.drawString("Rank", 20, 20);
			g2d.drawString("Name:", 80, 20);
			g2d.drawString("KDR: ", 150, 20);
			for (int i = 0; i < scoreBoard.size(); i++) {
				Entity p = scoreBoard.get(i);
				if (p == player) {
					g2d.setColor(Color.green);
				} else if (g2d.getColor() != Color.WHITE) {
					g2d.setColor(Color.WHITE);
				}
				int pos = (i * 15) + 40;
				g2d.drawString((i + 1) + ".", 20, pos);
				g2d.drawString(p.name, 80, pos);
				g2d.drawString(p.kill + " / " + p.death, 150, pos);
			}
		}
	}

	// draw sub image from block canvas
	private void drawBlockCanvas(Graphics2D g2d) {
		int centerX = board.getWidth() / 2;
		int centerY = board.getHeight() / 2;

		int sx = (int) (player.p.getX() + (player.halfWidth) - centerX);
		int sy = (int) (player.p.getY() + (player.halfHeight) - centerY);
		// connect ending point of block map to the starting point of the map when
		// player come to end boundary.
		if (sx + board.getWidth() > blockCanvas.getWidth()) {
			int vp = sx + board.getWidth();
			int w2 = vp - blockCanvas.getWidth();
			int w1 = board.getWidth() - w2;

			g2d.drawImage(blockCanvas.getSubimage(sx, sy, w1, board.getHeight()), 0, 0, w1, board.getHeight(), null);
			g2d.drawImage(blockCanvas.getSubimage(0, sy, w2, board.getHeight()), w1, 0, w2, board.getHeight(), null);
		}
		// connect starting point of block map to the ending point of the map when
		// player is over the starting boundary.
		else if (sx < 0) {
			int w2 = sx * -1;
			int w1 = board.getWidth() - w2;
			g2d.drawImage(blockCanvas.getSubimage(0, sy, w1, board.getHeight()), w2, 0, w1, board.getHeight(), null);
			g2d.drawImage(blockCanvas.getSubimage(blockCanvas.getWidth() - w2, sy, w2, board.getHeight()), 0, 0, w2,
					board.getHeight(), null);
		} else {

			g2d.drawImage(blockCanvas.getSubimage(sx, sy, board.getWidth(), board.getHeight()), 0, 0, board.getWidth(),
					board.getHeight(), null);
		}
	}

	private class Mouse extends Obj {

		private Mouse() {
			super(new Point(0, 0));
		}

		private Point mapXY() {
			return new Point((player.p.getX()) + this.p.getX() - board.getWidth() / 2,
					(player.p.getY() + blockSize / 2) + this.p.getY() - board.getHeight() / 2);
		}
	}

}
