package game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import game.items.Health;
import game.items.Item;
import game.items.Key;
import game.items.Score;
import game.items.Tool;
import game.npcs.EnemyStill;
import game.npcs.EnemyWalker;
import game.npcs.NPC;

public class Player {
	private String name;
	private Point coords; //Coords relative to the game window
	private int health = 100;
	private Room currentRoom;
	private List<Room> visitedRooms = new ArrayList<>();
	private List<Item> inventory = new ArrayList<>();
	private int score = 0;
	private int speed = 2;
	private String orientation = "north";
	private Tile currentTile;
	private Tile prevTile;
	private boolean isMoving;
	private boolean north;
	private boolean south;
	private boolean east;
	private boolean west;
	private int walkState = 0;
	private int walkDelay = 0;
	private boolean invincible;
	private int invincibleCount = 120;

	public Player(){

	}

	/**
	 * Constructor for new players
	 * @param name name of player
	 * @param coords coords of player
	 * @param currentRoom starting room for the player
	 */
	public Player(String name, Point coords, Room currentRoom){
		this.name = name;
		this.coords = coords;
		this.currentRoom = currentRoom;
		this.currentTile = calcTile();
		addCurrentRoom();
	}

	/**
	 * Constructor for loading existing player. This will be necessary
	 * because players may load in previous games.
	 * @param name name of the player
	 * @param coords location of the player in terms of the game window
	 * @param health player's health
	 * @param currentRoom room player is currently in
	 * @param visitedRooms rooms player has visited
	 * @param inventory list of items the player currently has
	 * @param score current score of the player
	 */
	public Player(String name, Point coords, int health,
			Room currentRoom, List<Room> visitedRooms, List<Item> inventory, int score){
		this.name = name;
		this.coords = coords;
		this.health = health;
		this.currentRoom = currentRoom;
		this.visitedRooms = visitedRooms;
		this.inventory = inventory;
		this.score = score;
		this.currentTile = calcTile();
	}
	
	public boolean hasTool(String breakable){
		for(Item item : inventory){
			if(item instanceof Tool && ((Tool)item).getBreakable().equals(breakable)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tries to interact with anything within the surround 4 tiles (N E S or W)
	 * @return boolean whether interaction is successful or not
	 */
	public boolean tryInteract(){
		for(Object occupant : getInteractables()){
			if(occupant != null){
				((Interactable)occupant).interact();
				return true;
			}
		}
		return false;
	}
	
	public void updateWalkCycle(){
		walkState++;
		if(walkState == 8){
			walkState = 0;
		}
	}
	
	public int getWalkState(){
		return walkState;
	}
	
	/**
	 * finds any interactables in the surrounding squares of the player
	 * @return List<Object> of interactables
	 */
	public List<Object> getInteractables(){
		int currTileRow = currentTile.getRoomCoords().x;
		int currTileCol = currentTile.getRoomCoords().y;
		
		List<Object> occupants = new ArrayList<>();
		
		Tile north = currentRoom.getTileFromRoomCoords(new Point(currTileRow-1, currTileCol));
		Tile east = currentRoom.getTileFromRoomCoords(new Point(currTileRow, currTileCol+1));
		Tile south = currentRoom.getTileFromRoomCoords(new Point(currTileRow+1, currTileCol));
		Tile west = currentRoom.getTileFromRoomCoords(new Point(currTileRow, currTileCol-1));
		
		Object occupant;
		
		if(north != null && north.isOccupied()){
			occupant = currentRoom.getTileOccupant(north);
			if(occupant instanceof Interactable){
				occupants.add(occupant);
			}
		}
		if(east != null && east.isOccupied()){
			occupant = currentRoom.getTileOccupant(east);
			if(occupant instanceof Interactable){
				occupants.add(occupant);
			}
		}
		if(south != null && south.isOccupied()){
			occupant = currentRoom.getTileOccupant(south);
			if(occupant instanceof Interactable){
				occupants.add(occupant);
			}
		}
		if(west != null && west.isOccupied()){
			occupant = currentRoom.getTileOccupant(west);
			if(occupant instanceof Interactable){
				occupants.add(occupant);
			}
		}
		return occupants;
	}
	
	public void updatePlayer(){
		updateInvincibility();
		int count = 0;
		if(north){
			tryMove("north");
			count++;
		}
		if(south){
			tryMove("south");
			count++;
		}
		if(east){
			tryMove("east");
			count++;
		}
		if(west){
			tryMove("west");
			count++;
		}
		if(count > 0){
			if(walkDelay == 0){
				updateWalkCycle();
				walkDelay = 5;
			}
			else{
				walkDelay--;
			}
		}
		else{
			walkDelay = 5;
			walkState = 0;
		}
	}

	public void updateInvincibility(){
		if(invincible){
			invincibleCount--;
			if(invincibleCount == 0){
				invincible = false;
				invincibleCount = 120;
			}
		}
	}
	
	/**
	 * After a player has stepped onto a tile, this method calculates whether to push the player
	 * back or whether to pick up an item or whether to do nothing.
	 * @return whether the player can move onto the next tile or not. False = cannot change tile
	 */
	public boolean canChangeTile(){
		if(!currentTile.isOccupied()){
			return true;
		}
		Object occupant = currentRoom.getTileOccupant(currentTile);
		if(occupant instanceof Item){
			if(addInventoryItem((Item)occupant)){
				currentRoom.removeItems((Item)occupant);
				if(occupant instanceof Score){
					setScore(getScore() + ((Item)occupant).getScore());
				}
				else{
					DestinysWild.getBoard().addOffBoardItem((Item)occupant);
				}
			}
			return true;
		}
		else if(occupant instanceof EnemyWalker || occupant instanceof EnemyStill){
			if(!invincible){
				setHealth(getHealth() - ((NPC)occupant).getDamage());
				System.out.println("OUCH! Taken " + ((NPC)occupant).getDamage() + " damage!");
				invincible = true;
				if(!checkPulse()){
					partThisCruelWorldForAnother();
					return false;
				}
			}
			return (occupant instanceof EnemyWalker);
		}
		return false;
	}
	
	/**
	 * Checks whether the player is still alive
	 * @return boolean true if pulse is found
	 */
	public boolean checkPulse(){
		if(getHealth() <= 0){
			return false;
		}
		return true;
	}
	
	/**
	 * The player has died. Reinitialises everything appropriately.
	 */
	public void partThisCruelWorldForAnother(){
		setCurrentRoom(DestinysWild.getBoard().getRoomFromCoords(2, 2));
		setCoords(540, 325);
		resetInventory();
		setScore(0);
		setHealth(100);
	}
	
	/**
	 * resets the player's inventory accordingly upon death
	 */
	public void resetInventory(){
		List<Item> toRemove = new ArrayList<>();
		System.out.println("Health items before death: "+ numHealthItems());
		for(Item item : inventory){
			if(item instanceof Key){
				getCurrentRoom().addItem(item, prevTile.getRoomCoords().x, prevTile.getRoomCoords().y);
				toRemove.add(item);
			}
			else if(item instanceof Health){
				toRemove.add(item);
			}
		}
		for(Item item : toRemove){
			inventory.remove(item);
		}
		System.out.println("Health item in inv after death: "+ numHealthItems());
	}
	
	/**
	 * Attempts to heal the player upon selection of a health item from the inventory
	 * @param itemId item to eat
	 * @return boolean success
	 */
	public boolean tryEat(int itemId){
		Item healthItem = null;
		for(Item item : inventory){
			if(item.getId() == itemId){
				healthItem = item;
			}
		}
		if(healthItem != null){
			if(getHealth() == 100){
				System.out.println("You don't need to eat that yo");
				return false;
			}
			else{
				if(getHealth() + healthItem.getHealth() < 100){
					setHealth(getHealth() + healthItem.getHealth());
				}
				else{
					setHealth(100);
				}
				return true;

			}
		}
		return false;
	}
	
	/**
	 * Where the game logic player movement is done. The player will be moved onto a tile, 
	 * then that tile is tested for an occupant. If occupied, the movement is reversed. 
	 * @param direction the direction the player is trying to move
	 * @return boolean whether the player move is succesfful or not
	 */
	public boolean tryMove(String direction){
		orientation = direction;
		prevTile = currentTile;
		switch(direction){
			case "north":
				setCoords(getCoords().x, getCoords().y - speed/2);
				if(!currTileIsInRoom() && prevTile.isDoorMat().equals("north")){
					currentRoom = DestinysWild.getBoard().getRoomFromId(currentRoom.getNorth());
					changeRoom(prevTile);
				}
				else if(!currTileIsInRoom() || !canChangeTile()){
					setCoords(getCoords().x, getCoords().y + speed/2);
					currentTile = prevTile;
				}
				break;
			case "east":
				setCoords(getCoords().x + speed, getCoords().y);
				if(!currTileIsInRoom() && prevTile.isDoorMat().equals("east")){
					currentRoom = DestinysWild.getBoard().getRoomFromId(currentRoom.getEast());
					changeRoom(prevTile);
				}
				else if(!currTileIsInRoom() || !canChangeTile()){
					setCoords(getCoords().x - speed, getCoords().y);
					currentTile = prevTile;
				}
				break;
			case "south":
				setCoords(getCoords().x, getCoords().y + speed/2);
				if(!currTileIsInRoom() && prevTile.isDoorMat().equals("south")){
					currentRoom = DestinysWild.getBoard().getRoomFromId(currentRoom.getSouth());
					changeRoom(prevTile);
				}
				else if(!currTileIsInRoom() || !canChangeTile()){
					setCoords(getCoords().x, getCoords().y - speed/2);
					currentTile = prevTile;
				}
				break;
			case "west":
				setCoords(getCoords().x - speed, getCoords().y);
				if(!currTileIsInRoom() && prevTile.isDoorMat().equals("west")){
					currentRoom = DestinysWild.getBoard().getRoomFromId(currentRoom.getWest());
					changeRoom(prevTile);
				}
				else if(!currTileIsInRoom() || !canChangeTile()){
					setCoords(getCoords().x + speed, getCoords().y);
					currentTile = prevTile;
				}
				break;
			default:
				throw new Error("Invalid Direction");
		}
		return true;
	}
	
	/**
	 * whether the currentTile is in the room
	 * @return boolean whether the current tile is in the currentRoom
	 */
	public boolean currTileIsInRoom(){
		currentTile = calcTile();
		if(currentTile == null){
			return false;
		}
		return true;
	}
	
	/**
	 * Updates everything required upon changing room
	 * @param previousTile the player's previous Tile 
	 */
	public void changeRoom(Tile previousTile){
		if(!visitedRooms.contains(currentRoom)){
			addCurrentRoom();
		}
		
		int prevX = previousTile.getRoomCoords().x;
		int prevY = previousTile.getRoomCoords().y;
		
		Point newPoint;
		
		if(prevX == 0){
			newPoint = currentRoom.getTileFromRoomCoords(new Point(9, previousTile.getRoomCoords().y)).getRealCoords();
			setCoords(newPoint.x, newPoint.y);
		}
		else if(prevX == 9){
			newPoint = currentRoom.getTileFromRoomCoords(new Point(0, previousTile.getRoomCoords().y)).getRealCoords();
			setCoords(newPoint.x, newPoint.y);
		}
		else if(prevY == 0){
			newPoint = currentRoom.getTileFromRoomCoords(new Point(previousTile.getRoomCoords().x, 9)).getRealCoords();
			setCoords(newPoint.x, newPoint.y);
		}
		else if(prevY == 9){
			newPoint = currentRoom.getTileFromRoomCoords(new Point(previousTile.getRoomCoords().x, 0)).getRealCoords();
			setCoords(newPoint.x, newPoint.y);
		}
		
		currentTile = calcTile();
	}

	/**
	 * Calculates which Tile the player is standing on
	 * @return Tile object that the player is standing on
	 */
	public Tile calcTile(){
		for (int row = 0; row < currentRoom.getTiles().length; ++row) {
			for (int col = 0; col < currentRoom.getTiles()[0].length; ++col) {
				Tile current = currentRoom.getTiles()[row][col];
				if (current != null && current.isOn(coords)){
					return current;
				}
			}
		}
		//System.out.println(coords.toString());
		//System.out.println("Player isn't on a tile in their currentRoom... (This can't be right)");
		//System.exit(0);
		return null;
	}

	/**
	 * adds any room object to the visited Room list
	 * @param room room to add
	 */
	public void addRoom(Room room){
		visitedRooms.add(room);
	}

	/**
	 * adds the current room to the list of visited rooms for the player
	 */
	public void addCurrentRoom(){
		visitedRooms.add(currentRoom);
	}

	/**
	 * adds an item object to the player's inventory if there is room for one more of that Item type
	 * @param item item to add
	 * @return boolean successful
	 */
	public boolean addInventoryItem(Item item){
		if((item instanceof Health && numHealthItems()<5) || (item instanceof Key && canAddKeyItem()) || item instanceof Tool){
			return inventory.add(item);
		}
		else if(item instanceof Score){
			return true;
		}
		else{
			System.out.println("Too many " + item.toString() + " items in inventory!");
			return false;
		}
	}

	/**
	 * Checks whether a Key item can be added to the inventory (Max of 1 Key item)
	 * @return boolean true/false for above
	 */
	public boolean canAddKeyItem(){
		for(Item item : inventory){
			if(item instanceof Key){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * counts the number of health items in the player's inventory
	 * @return int number of health items
	 */
	public int numHealthItems(){
		int count = 0;
		for(Item item : inventory){
			if(item instanceof Health){
				count++;
			}
		}
		return count;
	}
	
	/**
	 * counts the number of tools in the player's inventory
	 * @return int number of tools 
	 */
	public int numToolItems(){
		int count = 0;
		for(Item item : inventory){
			if(item instanceof Tool){
				count++;
			}
		}
		return count;
	}

	/**
	 * removes an item at 'index' from the player's inventory
	 * @param index index of item to be removed
	 */
	public void removeInventoryItem(int index){
		inventory.remove(index);
	}
	
	/**
	 * removes an item by Item object from player's inventory
	 * @param item item to be removed
	 */
	public void removeInventoryItem(Item item){
		inventory.remove(item);
	}

	/**
	 * gets the player's name
	 * @return the player's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * sets the current player's name
	 * @param name current player's new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	public Point getCoords() {
		return coords;
	}

	public void setCoords(int x, int y) {
		if(coords == null){
			coords = new Point(x, y);
		}
		else{
			coords.setLocation(x, y);
		}
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public Room getCurrentRoom() {
		return currentRoom;
	}

	public void setCurrentRoom(Room currentRoom) {
		this.currentRoom = currentRoom;
	}

	public List<Room> getVisitedRooms() {
		return visitedRooms;
	}

	public List<Item> getInventory() {
		return inventory;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void setCurrentTile(Tile currentTile){
		this.currentTile = currentTile;
	}
	
	public Tile getCurrentTile(){
		return currentTile;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public String getOrientation() {
		return orientation;
	}

	public boolean isMoving() {
		return isMoving;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public void setMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}
	
	public boolean isNorth() {
		return north;
	}

	public boolean isSouth() {
		return south;
	}

	public boolean isEast() {
		return east;
	}

	public boolean isWest() {
		return west;
	}

	public void setNorth(boolean north) {
		this.north = north;
	}

	public void setSouth(boolean south) {
		this.south = south;
	}

	public void setEast(boolean east) {
		this.east = east;
	}

	public void setWest(boolean west) {
		this.west = west;
	}
	

}
