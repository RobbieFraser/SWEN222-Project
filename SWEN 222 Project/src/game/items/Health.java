package game.items;

import java.awt.Point;

public class Health implements Item{

	private String type;

	private Point coords;

	private int id;

	private int health; //health that will be restored upon use

	public Health(String type, Point coords, int health, int id){
		this.type = type;
		this.id = id;
		this.coords = coords;
		this.health = health;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	public int getId(){
		return id;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the coords
	 */
	public Point getCoords() {
		return coords;
	}

	/**
	 * @param coords the coords to set
	 */
	public void setCoords(Point coords) {
		this.coords = coords;
	}

	/**
	 * @return the health
	 */
	public int getHealth() {
		return health;
	}

	/**
	 * @param health the health to set
	 */
	public void setHealth(int health) {
		this.health = health;
	}

	@Override
	public int getScore() {
		return 0;
	}
	
	public String toString(){
		return "health";
	}

}
