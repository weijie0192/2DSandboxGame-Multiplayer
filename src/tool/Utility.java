package tool;

import object.Bullet;
import object.Entity;

public class Utility {
	// return velocity of two point
	public static Point getVelocity(Point p1, Point p2, double speed) {
		double xDif = p1.getX() - p2.getX(), yDif = p1.getY() - p2.getY(), angle = Math.atan2(xDif, yDif) / Math.PI,
				xS = -Math.sin(angle * Math.PI) * speed, yS = -Math.cos(angle * Math.PI) * speed;
		return new Point(xS, yS);
	}
	
	// test collision
	public static boolean testCollide(Entity obj, Bullet bullet) {
		// ballSize * (p1.radius) + ballSize * (p2.radius) - findDist(p1, p2) >= 0;
		return (obj.halfHeight + bullet.radius) - Math.abs(bullet.p.getY() - (obj.p.getY())) >= 0;
	}
	
	//set timeout
	public static void setTimeout(Runnable runnable, int delay){
	    new Thread(() -> {
	        try {
	            Thread.sleep(delay);
	            runnable.run();
	        }
	        catch (Exception e){
	            System.err.println(e);
	        }
	    }).start();
	}
	
	public static int getAngle(Point p1, Point p2) {
		int angle;
		if(Math.abs(p2.getX()-p1.getX()) < 1500) {
			angle = (int) Math.toDegrees(Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX()));
		}
		else {
			int d1 = p2.getX() > p1.getX() ? -3000 : 3000;
			angle = (int) Math.toDegrees(Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX() + d1));
		}
		
	    if(angle < 0){
	        angle += 360;
	    }
	    return angle;
	}
}
