package game;

import java.util.Comparator;

import object.Entity;

public class ScoreSorter implements Comparator<Entity> {

	@Override
	public int compare(Entity e1, Entity e2) {
		// TODO Auto-generated method stub
        return e2.kill- e1.kill  ; 
	}

}
