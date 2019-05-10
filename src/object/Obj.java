package object;

import java.awt.Color;
import java.util.LinkedList;

import tool.Point;

// object
public class Obj {

	public Point p;
	public double diameter;
	public double hp;
	public Color color;
	public double radius;
	public LinkedList<Block> blocks;
	public String id;
	public Point v;

	public Obj(double x, double y) {
		this.p = new Point(x, y);
	}

	public Obj(Point p) {
		this.p = p;
	}

	public Obj(Point p, double diameter, double hp, Color color) {
		// object color
		this.color = color;
		// object health
		this.hp = hp;
		// object diameter
		this.diameter = diameter;
		// object radius
		this.radius = diameter / 2;
		// object position
		if (p != null) {
			this.p = new Point(p.getX(), p.getY());
		}
		// block occupied
		this.blocks = new LinkedList<Block>();
	}

	public Obj(Point p, double diameter, double hp, Color color, String id) {
		this(p, diameter, hp, color);
		this.id = id;
	}

	public Obj(Point p, double diameter, double hp, Color color, String id, Point v) {
		this(p, diameter, hp, color, id);
		this.v = v;
	}

	public Obj(String id, Color color) {
		this.id = id;
		this.color = color;
	}

	public void update(Point p, double hp) {
		// block occupied
		this.blocks = new LinkedList<Block>();
		this.p = p;
		this.hp = hp;
	}

	public void updateBlock(LinkedList<Block> newBlock) {
		// remove object reference in all occupied block
		this.destroy();

		// update new block data
		this.blocks = newBlock;

		// add object reference to new occupied block
		this.blocks.forEach(b -> {
			// if type is not undefined, then the object is a entity
			if (this.id != null && !this.id.isEmpty()) {
				// System.out.println("Hey: "+this.type);
				b.objs.add(Obj.this);
			}
		});
	}

	public void destroy() {
		// check if the object is a entity
		boolean isEntity = (this.id != null && !this.id.isEmpty());
		if (isEntity) {
			// remove object reference in all occupied block
			this.blocks.forEach(b -> {
				// if (b.objs.contains(this)) {
				b.objs.remove(Obj.this);
				// }
			});
		}
	}
}