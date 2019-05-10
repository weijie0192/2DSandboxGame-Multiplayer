package object;

import java.awt.Color;
import tool.Point;
import tool.Utility;

public class Entity extends Obj {

	public double width, height, halfHeight, halfWidth, digpower, initJF, jf, speed;
	public PlayerType weapon;
	public Obj target;
	public int click, kill, death;
	public boolean up, left, right, down, moved, fire, isReady;
	public String name;
	public int typeIndex;

	public Entity(String name, String id, Color color) {
		super(id, color);
		this.name = name;
		this.isReady = false;
	}

	/*// player extend of object
	public Entity(double blockSize, String id, PlayerType type, Point p, double jumpFuel, Color color, Obj target) {
		// call super class constructor
		super(p, blockSize, type.playerHP, color, id);
	}*/

	public void setup(double blockSize, PlayerType type, Point p, double jumpFuel, Obj target) {
		// shoot at
		this.target = target;
		// weapon property: bullet size, bullet speed, damage, lifespan, cooldown, fire
		this.weapon = type;
		// -1 indicate there is no clicking
		this.click = -1;
		this.width = blockSize;
		this.height = blockSize * 2;
		this.halfHeight = this.height / 2;
		this.halfWidth = this.width / 2;
		this.up = false;
		this.left = false;
		this.right = false;
		this.down = false;
		// player moved
		this.moved = true;
		// jump fuel
		this.initJF = jumpFuel;
		this.jf = jumpFuel;
		this.speed = type.playerSpeed;
		this.fire = false;
		this.update(p, type.playerHP);
	}

	public void typeIndex(int index) {
		this.typeIndex = index;
	}

	// change player type
	public void changeType(PlayerType type) {
		this.weapon = type;
		this.speed = type.playerSpeed;
	}

	public String getType() {
		return this.weapon.type;
	}

	public void recoverJump() {
		if (this.jf < 20) {
			this.jf = 20;
		}
		if (this.jf < this.initJF) {
			this.jf += 2;
		}
	};

	// count shooting cool down
	public void setCD() {
		this.fire = true;

		Utility.setTimeout(() -> {
			this.fire = false;
		}, this.weapon.cd);
		/*
		 * setTimeout(public void () { self.fire = false; },self.weapon.cd);
		 */
	};
}