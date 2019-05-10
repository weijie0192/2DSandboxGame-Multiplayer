
package object;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import tool.Point;

public class Bullet extends Obj {

	public PlayerType property;
	public String owner;
	public double travelRange;

	// bullet extend of object
	public Bullet(Point p, Point v, PlayerType property, String owner, Color color) {

		super(null, property.size, property.bulletHP, color, "", v);
		if (property.type == "Melee") {
			this.p = new Point(p.getX() - 10, p.getY() - 10);
		} else {
			this.p = new Point(p.getX() , p.getY() - 10);
		}
		// owner of the bullet
		this.owner = owner;
		// bullet property: size, speed, damage, and lifespan
		this.property = property;
		this.travelRange = property.travelRange;
	}

	// bullet move
	public void move(BufferedImage blockCanvas) {
		// update x and y by speed times velocity
		this.p.setX(this.p.getX() + this.v.getX());

		if (this.p.getX() >= blockCanvas.getWidth()) {
			this.p.setX(this.p.getX() - blockCanvas.getWidth());
		} else if (this.p.getX() < 0) {
			this.p.setX(this.p.getX() + blockCanvas.getWidth());
		}
		this.p.setY(this.p.getY() + this.v.getY());
		// reduce hp when travel
		this.travelRange--;
	};

	// return if collision block exist
	public LinkedList<Block> getCollideBlock() {
		// collided blocks
		LinkedList<Block> cblocks = new LinkedList<Block>();

		this.blocks.forEach(b -> {
			if (b.stObj != null || (!b.objs.isEmpty() && !b.objs.get(0).id.equals(this.owner))) {
				// push into collided blocks array if there is a obj in this block
				cblocks.add(b);
			}
		});
		// return collided blocks array
		return cblocks;
	};
}