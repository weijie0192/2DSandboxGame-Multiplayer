package object;

import java.awt.Color;

public class PlayerType {
    public double playerHP, playerSpeed, speed, size, dmg, travelRange, consume, bulletHP;
    public int cd;
    public Color color;
    public String type;

    // player type
    public PlayerType(String type, double playerHP, double playerSpeed, double speed, double size, double dmg,
    		double travelRange, int cooldown, double consume, double bulletHP, Color color) {
    	this.type = type;
    	this.playerHP = playerHP;
    	this.playerSpeed = playerSpeed;
        this.speed = speed; // bullet travel speed
        this.size = size; // bullet size
        this.dmg = dmg; // damage doing
        this.travelRange = travelRange; // travel range
        this.cd = cooldown; // fire speed
        this.consume = consume; // energy consume
        this.bulletHP = bulletHP;
        this.color = color;
    }

}