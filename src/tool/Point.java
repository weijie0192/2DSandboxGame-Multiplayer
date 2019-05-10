/*
	**********************************
	File Name: Point.java
	Package: object
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*store velocity
	*store coordinate
	*display the shape
*/

package tool;


public class Point {

	protected double x;
	protected double y;
	
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}


	public void setX(double x) {
		this.x = x;
	}

	public double getX() {
		return x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getY() {
		return y;
	}
}