package game;

public class Board {
	private Room[][] board = new Room[5][5];

	public Board(){
		//yo
	}

	/**
	 * @return the board
	 */
	public Room[][] getBoard() {
		return board;
	}
	
	public void addRoom(Room room, int x, int y){
		board[x][y] = room;
	}
	
	public void printBoard(){
		for(int i=0; i<board.length; i++){
			System.out.print("| ");
			for(int j=0; j<board[i].length;j++){
				if(board[i][j] == null){
					System.out.print("null | ");
				}
				else{
					System.out.print(board[i][j].getID() + " | ");
				}
			}
			System.out.println();
		}
	}



}
