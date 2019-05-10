/*
	**********************************
	File Name: Game.java
	Package: game
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Game Interface
*/

package game;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public interface IGame {

	abstract void scoreCount();

	abstract void resetScore();

	abstract void addScore(int p);

	abstract boolean isLevelUp();

	abstract int getLevel();

	abstract int getScore();

	abstract void gameStart();

	abstract void gameReset();

	abstract void gameOver();

	abstract void gameClear();

	abstract void loadSetting();

	abstract void levelUp();

	abstract void playerMove();

	abstract void keyPressed(KeyEvent e);

	abstract void keyReleased(KeyEvent e);

	abstract void mouseWheel(MouseWheelEvent e);

	abstract void mouseClicked(MouseEvent e);

	abstract void mouseReleased(MouseEvent e);

	abstract void mouseMove(MouseEvent e);

	abstract void roundAction();

	abstract void drawBack(Graphics2D g2d);

	abstract void drawFront(Graphics2D g2d);
}
