package map;

import java.awt.geom.Point2D;

public class Point extends Point2D {
    public double x;
    public double y;
	
    public Point(double x, double y){
    	this.x = x;
    	this.y = y;
    }
    
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}

	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;	
	}

}