package game;

import java.awt.Color;
import java.awt.Font;

import object.PlayerType;
import tool.Point;

public interface GameParams {
	final static Color bldCanvasColor = new Color(235, 34, 37, 180);
	final static Color energyCanvasColor = new Color(34, 225, 235, 180);
	final static Color flyCanvasColor = new Color(37, 8, 88, 180);
	final static Font gameFont = new Font("TimesRomandw", Font.BOLD, 14);
	final static Font alertFont = new Font("TimesRomandw", Font.BOLD, 30);
	final static Point mapSize = new Point(200, 200);
	final static int blockSize = 15; // size of each block. Make sure all speed cannot greater than block size
	final static double jumpFuel = 300; // jump power
	final static int initEnergy = 1000;
	final static int blockEnergy = 30;
	final static int blockhp = 10; // default block health
	final static int startTime = 10; // time to start the game

	final static PlayerType[] playerType = {
			// player type (type, playerHP, playerSpeed, bulletSpeed, size, dmg, travelRange, cooldown, consume, bulletHP, color)
			// rifle
			new PlayerType("Rifleman", 100, 2,  8, 3, 8, 90, 90, 10, 1, Color.BLACK),
			// sniper
			new PlayerType("Sniper", 100, 1.5, 12, 8, 20, 140, 1000,120, 3, Color.BLACK),
			// melee
			new PlayerType("Melee", 100, 3.5, 8,  15, 30, 4, 140, 20, 2, Color.BLACK)};
}
