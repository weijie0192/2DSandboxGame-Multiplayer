package object;

import java.util.LinkedList;

import tool.Point;

public class Block {
    public Point p;
    public Obj stObj;
    public String background;
    public LinkedList<Obj> objs;

    public Block(Obj stObj, Point p, String background) {
        this(stObj, p);
        // background on the block (eg. flowers)
        this.background = background;
    }

    public Block(Obj stObj, Point p) {
        this.p = new Point(p.getX(), p.getY());
        // entity objs on the block (eg. player, enemy ...)
        this.objs = new LinkedList<Obj>();
        // static object on the block (eg. dirt, rock ...)
        this.stObj = stObj;
    }
}