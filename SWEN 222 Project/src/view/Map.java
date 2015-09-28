package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JComponent;
import game.Board;
import game.Player;
import game.Room;
import game.obstacles.Obstacle;

/**
 * A Minimap should be an item that will be used in the player
 * interface. It will display all rooms that the player has been in
 * so far, and show the connections between the rooms. It should also
 * show the location of the player.
 * @author DanielYska
 */
public class Map extends JComponent {
	private Player player;
	private Image mapImage;
	private Board board;
	private static final int BOARD_LENGTH = 5;
	
	public Map(Player player, Board board) {
		this.player = player;
		this.board = board;
		initialiseMapImage();
		//TODO: Add Compass
	}

	/**
	 * This method should be called to initialise the
	 * base image for what the map of the board will look
	 * like. This image can be stored so that when
	 * paintComponent is called, this image can simply be
	 * painted over. This should reduce the amount of work
	 * that is done in updating the map each time the
	 * player moves.
	 */
	private void initialiseMapImage() {
		//create image that will be the background
		mapImage = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = ((BufferedImage) mapImage).createGraphics();

		//read in the rooms from the board
		Room[][] rooms = board.getBoard();
		//draw each room individually
		for (int i = 0; i < BOARD_LENGTH; ++i) {
			for (int j = 0; j < BOARD_LENGTH; ++j) {
				Room room = rooms[i][j];
				//only draw rooms that have been initialised
				if (room != null) {
					initialiseRoomImage(graphics, room);
				}
			}
		}
	}

	/**
	 * This method should draw to the map image a single room.
	 * @param graphics
	 * @param room
	 */
	private void initialiseRoomImage(Graphics graphics, Room room) {
		Point point = room.getBoardPos();
		int xCoord = point.x * 50;
		int yCoord = point.y * 50;

		//draw in the background of the room first, including any obstacles
		for (int i = xCoord; i < 50 + xCoord; ++i) {
			for (int j = yCoord; j < 50 + yCoord; ++j) {
				//check if there is an obstacle to draw
				if (room.getObstacles()[(i-xCoord)/5][(j-yCoord)/5] != null) {
					Obstacle obstacle = room.getObstacles()[(i-xCoord)/5][(j-yCoord)/5];
					//draw each obstacle differently
					switch (obstacle.getType()) {
					case "brokenblock":
						graphics.setColor(new Color(143, 143, 143));
						graphics.fillRect(j, i, 1, 1);
						break;
					case "brokenstone1":
						graphics.setColor(new Color(143, 143, 143));
						graphics.fillRect(j, i, 1, 1);
						break;
					case "stoneblock":
						graphics.setColor(new Color(143, 143, 143));
						graphics.fillRect(j, i, 1, 1);
						break;
					case "cobblestone":
						graphics.setColor(new Color(143, 143, 143));
						graphics.fillRect(j, i, 1, 1);
						break;
					default:
						System.out.println("Water?");
						System.out.println("Error");
					}
				} else {
					//print out normal unoccupied square (forest floor)	
					graphics.setColor(new Color(172,211,115));
					graphics.fillRect(j, i, 1, 1);
				}
			}
		}

		//draw in walls or doors in y direction
		for (int i = yCoord; i <= yCoord + 50; ++i) {
			for (int j = xCoord; j <= xCoord + 50; j += 50) {
				//check for north door
				if (room.getNorth() != -1 && i >= (20 + yCoord) && i < (30 + yCoord)
						&& j == xCoord) {
					//if we are here, then a north door is present, so nothing will be
					//drawn to show the gap in the wall
					//System.out.println("North door");
				}
				//check for west door
				else if (room.getSouth() != -1 && i >= (20 + yCoord) && i < (30 + yCoord)
						&& j == xCoord + 50) {
					//if we are here, then a south door is present, so nothing will be
					//drawn to show the gap in the wall
					//System.out.println("South door");
				}
				else {
					//safe to draw the wall
					graphics.setColor(new Color(0,100,0));
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}

		//draw in walls or doors in x direction
		for (int i = yCoord; i <= yCoord + 50; i += 50) {
			for (int j = xCoord; j <= xCoord + 50; ++j) {
				//check for east door
				if (room.getEast() != -1 && j >= (20 + xCoord) && j < (30 + xCoord)
						&& i == yCoord) {
					//if we are here, then a east door is present, so nothing will be
					//drawn to show the gap in the wall
					//System.out.println("East door");
				}
				//check for west door
				else if (room.getWest() != -1 && i >= (20 + xCoord) && i < (30 + xCoord)
						&& j == yCoord + 50) {
					//if we are here, then a west door is present, so nothing will be
					//drawn to show the gap in the wall
					//System.out.println("West door");
				}
				else {
					//safe to draw the wall
					graphics.setColor(new Color(0,100,0));
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		List<Room> visitedRooms = player.getVisitedRooms();

		g.drawImage(mapImage, 0, 0, null, null);

		//put up a black background so that rooms that aren't drawn
		//are shown to be unexplored
		boolean drawOnlyIfDiscovered = true; //for testing
		if (drawOnlyIfDiscovered) {
			//draw black onto each room that has not been visited yet
			for (int i = 0; i < BOARD_LENGTH; ++i) {
				for (int j = 0; j < BOARD_LENGTH; ++j) {
					//check if this room has been visited before
					boolean visitedRoom = false;
					for (Room room: visitedRooms) {
						if (room.getBoardPos().equals(new Point(i,j))) {
							//been here before
							visitedRoom = true;
						}
					}
					if (!visitedRoom) {
						//haven't visited this room
						//so user shouldn't be able to see it
						g.setColor(Color.DARK_GRAY);
						g.fillRect(j*50, i*50, 50, 50);
					}
				}
			}
		}

		//now draw location of player
		//Room currentRoom = player.getCurrentRoom();
		Room currentRoom = visitedRooms.get(0);
		Point roomCoord = currentRoom.getBoardPos();
		Point coord = player.getCoords();

		g.setColor(Color.MAGENTA);
		g.fillRect(roomCoord.y * 50 + coord.y * 5, roomCoord.x * 50 + coord.x * 5, 5, 5);
	}
}
