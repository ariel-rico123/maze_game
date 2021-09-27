// Ariel Rico
// 2020SU-COSC-1437-85420
// Lab 4 - Relationships - Shipping Network
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;					// Import the File class
import java.io.FileNotFoundException;	// Import this class to handle errors
import java.util.Random;

public class MazeGame 
{
	public static void main(String[] args)
	{
		int turnlimit = 100;
		int turn = 0;
		Board b = new Board();
		
		HumanPiece human = new HumanPiece("H");
		// setup human
		human.setupPiece(b);

		SmartPiece bot = new SmartPiece("AI");
		// setup bot
		bot.setupPiece(b);

		
		//Set up initial state for the double powerup
		DoublePowerupState = PowerupState.SPAWNING;
		
		while(turn++ < turnlimit)	
		{
			//output the stats
			System.out.println();
			System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("Turn: "+(turn));
			System.out.println("Human:    Stars= "+human.Stars + " Coins= " + human.Coins);
			System.out.println("Computer: Stars= "+bot.Stars + " Coins= " + bot.Coins);
			System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
			//Run the double powerup machine
			DoublePowerupMachine(DoublePowerupState, b);
			//Print the game board
			b.DrawBoard();
			
			// //move the players (yes the players can be in an array and you can move them all in a loop)
			boolean starFound = false;	
			starFound = human.Move(human.PlayerInput(), b);
			starFound = starFound | bot.Move(bot.AIChoose(b), b); //if either player has found the star then
			
			if (starFound) //find a new place for the star
			{
				b.findNewStarPlace();
				//do anything else that needs to be done when a star is found
			}				
			//Add points to all the nodes
			b.UpdateBoard();
		}
		//When turns end, game ends
		System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		//Print final scores
		System.out.println("Human:    Stars= "+human.Stars + " Coins= " + human.Coins);
		System.out.println("Computer: Stars= "+bot.Stars + " Coins= " + bot.Coins);
		//Declare the winner based on Stars
		if(human.Stars > bot.Stars)
			System.out.println("Human Player is the Winner!!!");
		else if(bot.Stars > human.Stars)
			System.out.println("Computer Player is the Winner!!!");
		//If its a tie, choose winner based on Coins
		else
		{
			if(human.Coins > bot.Coins)
				System.out.println("Human Player is the Winner!!!");
			else if(bot.Coins > human.Coins)
				System.out.println("Computer Player is the Winner!!!");
			else
				System.out.println("It is a tie!!!");
		}
		System.out.println();
	}
	
	//Enum for the different powerup states
	public enum PowerupState
	{
		SPAWNING, WAITING, OBTAINED
	}
	
	//Set up initial state for the double powerup
	public static PowerupState DoublePowerupState;

	//Tracks whos using double power
	public static Piece usingDoublePower;
	
	//Double powerup state machine
	static void DoublePowerupMachine(PowerupState state, Board board)
	{
		switch(state)
		{
			case SPAWNING:
				board.spawnDoublePowerup();
				break;
			case WAITING:
				break;
			case OBTAINED:
				usingDoublePower.timerDoublePower--;
				if (usingDoublePower.timerDoublePower <= 0)
				{
					usingDoublePower.hasDoublePower = false;
					usingDoublePower.Name = usingDoublePower.Name.substring(0, usingDoublePower.Name.length() - 1);
					DoublePowerupState = PowerupState.SPAWNING;
				}
				break;
		}
	}
}

//Simple board class
class Board
{
	public static final int NODE_SIZE = 16;
	public static final int NODE_NUMBER = 9;
	public static final int ROW_NUMBER = 6;
	public static final int COL_NUMBER = 6;
	
	Node Nodes[][] = new Node[ROW_NUMBER][COL_NUMBER];
	Node starPlace;	//Allows AI to find star

	public Board()
	{
		//Initialize all the Nodes
		initializeNodes();
		//Initialize all the Blanks
		initializeBlanks();
		//Spawn Random Nodes
		this.spawnRandomNodes();
		//Place star
		this.findNewStarPlace();
		//Set up directed graph node associations
		readRoutes();
	}

	private void spawnRandomNodes(){
		//double maxRand = Math.ceil(COL_NUMBER * ROW_NUMBER / 10.0);
		double maxRand = 2;
		for(int it = 0; it < (int)maxRand ; ++it){
			Node n = chooseRandomNode();
			if(n.isBlank){
				--it;
				continue;
			}
			if(n.hasStar){
				--it;
				continue;
			}
			int r = n.row;
			int c = n.col;
			Nodes[r][c] = new randomNode(r, c);
		}
	}
	
	private void initializeNodes()
	{
		for (int i=0;i<Nodes.length;i++)
		{
			for (int j=0;j<Nodes[i].length;j++)
			{
					Nodes[i][j] = new Node(0,i,j);
			}
		}
	}

	private void initializeBlanks()
	{
		//Blanks
		Nodes[3][1].isBlank = true;
		Nodes[1][3].isBlank = true;
		Nodes[3][3].isBlank = true;
		Nodes[0][5].isBlank = true;
	}

	// private void generateTestBoard()
	// {	
	// 	//Setup paths (normally this would be read into a file)
	// 	//column 1
	// 	Nodes[0][0].NextNodes.add(Nodes[1][0]);
	// 	Nodes[1][0].NextNodes.add(Nodes[2][0]);
	// 	Nodes[1][0].NextNodes.add(Nodes[1][1]);
	// 	Nodes[2][0].NextNodes.add(Nodes[2][1]);
	// 	Nodes[2][0].NextNodes.add(Nodes[3][0]);
	// 	Nodes[3][0].NextNodes.add(Nodes[4][0]);
	// 	Nodes[4][0].NextNodes.add(Nodes[4][1]);
	// 	Nodes[4][0].NextNodes.add(Nodes[5][0]);
	// 	Nodes[5][0].NextNodes.add(Nodes[5][1]);
		
	// 	//column 2
	// 	Nodes[0][1].NextNodes.add(Nodes[0][0]);
	// 	Nodes[1][1].NextNodes.add(Nodes[0][1]);
	// 	Nodes[1][1].NextNodes.add(Nodes[2][1]);
	// 	Nodes[2][1].NextNodes.add(Nodes[2][2]);
	// 	Nodes[4][1].NextNodes.add(Nodes[4][2]);
	// 	Nodes[5][1].NextNodes.add(Nodes[5][2]);
		
	// 	//column 3
	// 	Nodes[0][2].NextNodes.add(Nodes[0][1]);
	// 	Nodes[0][2].NextNodes.add(Nodes[0][3]);
	// 	Nodes[1][2].NextNodes.add(Nodes[0][2]);
	// 	Nodes[1][2].NextNodes.add(Nodes[1][1]);
	// 	Nodes[2][2].NextNodes.add(Nodes[1][2]);
	// 	Nodes[2][2].NextNodes.add(Nodes[3][2]);
	// 	Nodes[3][2].NextNodes.add(Nodes[4][2]);
	// 	Nodes[4][2].NextNodes.add(Nodes[4][3]);
	// 	Nodes[5][2].NextNodes.add(Nodes[5][3]);
		
	// 	//column 4
	// 	Nodes[0][3].NextNodes.add(Nodes[0][4]);
	// 	Nodes[2][3].NextNodes.add(Nodes[2][2]);
	// 	Nodes[4][3].NextNodes.add(Nodes[4][4]);
	// 	Nodes[5][3].NextNodes.add(Nodes[4][3]);
		
	// 	//column 5
	// 	Nodes[0][4].NextNodes.add(Nodes[1][4]);
	// 	Nodes[1][4].NextNodes.add(Nodes[2][4]);
	// 	Nodes[2][4].NextNodes.add(Nodes[2][3]);
	// 	Nodes[3][4].NextNodes.add(Nodes[2][4]);
	// 	Nodes[4][4].NextNodes.add(Nodes[3][4]);
	// 	Nodes[4][4].NextNodes.add(Nodes[5][4]);
	// 	Nodes[5][4].NextNodes.add(Nodes[5][5]);
		
	// 	//Column 6
	// 	Nodes[5][5].NextNodes.add(Nodes[4][5]);
	// 	Nodes[4][5].NextNodes.add(Nodes[3][5]);
	// 	Nodes[3][5].NextNodes.add(Nodes[2][5]);
	// 	Nodes[2][5].NextNodes.add(Nodes[1][5]);
	// 	Nodes[1][5].NextNodes.add(Nodes[1][4]);
	// }
	
	//Initialize routes to the list from routes.txt
	public void readRoutes()
	{
		try 
		{
			File file = new File("routes.txt");		//routes.txt file contains route info
			Scanner s = new Scanner(file);			//Creates Scanner object to read info from file
			s.nextLine();				//Skips first line, contains description info
			while (s.hasNextLine()) 	//Loops through every remaining line until the end
			{
				//Every line in the routes.txt file contains the information for a route object
                //For every line, make a new route object and add it to the routelist array
                Nodes[s.nextInt()][s.nextInt()].NextNodes.add(Nodes[s.nextInt()][s.nextInt()]);
			}
			s.close();
		} 
		catch (FileNotFoundException e) 
		{
		  System.out.println("An error occurred.");
		  e.printStackTrace();
		}
    }
	
	public Node getNode(int row, int col)
	{
		System.out.println("Nodes["+row+", "+col+"]");
		return Nodes[row][col];
	}	
	/**
	 * adds points to basic Nodes and each different node can update themselves however they want after each turn
	 */
	public void UpdateBoard()
	{
		for (int i=0;i<Nodes.length;i++)
		{
			for (int j=0;j<Nodes[i].length;j++)
			{
				Nodes[i][j].Update();
			}
		}
	}
	public Node chooseRandomNode()
	{
		Node node;
		while (true)
		{
			int row = (int)(Math.random()*Nodes.length);
			int col = (int)(Math.random()*Nodes[row].length);
			if (Nodes[row][col].isBlank)
				continue; //choose again
			node = Nodes[row][col];
			break;
		}
		return node;
	}
	//Spawn a star
	public void findNewStarPlace()
	{
		Node node = chooseRandomNode();
		node.hasStar = true;
		starPlace = node;	//Allows AI to find star
	}
	//Spawn a double powerup
	public void spawnDoublePowerup()
	{
		if (Math.random() < 0.2)
		{
			Node node = chooseRandomNode();
			node.hasDoublePower = true;
			JPartyGame.DoublePowerupState = JPartyGame.PowerupState.WAITING;
		}
	}
	/**
	 * Theoretically should work for any board but I wouldn't be surprised if there are bugs in this method as it has not been "stress tested"
	 * This drawing method does not support bi-directional associations
	 */
	public void DrawBoard()
	{
		//draw all the lines except the bottom one
		//String one: nodes and relationships horizontal
		//String two: relationships vertical
		//one  --  two
		// |
		//three 
		for (int i=0;i<Nodes.length-1;i++)
		{
			String line1 = "";
			String line2 = "";
			for (int j=0;j<Nodes[i].length-1;j++)
			{
				Node one = Nodes[i][j];
				Node two = Nodes[i][j+1];
				Node three = Nodes[i+1][j];
				
				//draw node 1
				line1 += FormToBoard(one.toString());
				
				//draw relationship between 1 and 2
				if (one.NextNodes.contains(two))
					line1 += FormToBoard("    >>>");
				else if (two.NextNodes.contains(one))
					line1 += FormToBoard("    <<<");
				else
					line1 += FormToBoard("  ");
				
				//draw relationship between 1 and 3
				if (one.NextNodes.contains(three))
					line2 += FormToBoard("    \\/");
				else if (three.NextNodes.contains(one))
					line2 += FormToBoard("    /\\");
				else
					line2 += FormToBoard("  ");
				
				//There needs to be an extra space on line 2
				line2 += FormToBoard("  ");
			}
			line1 += FormToBoard(Nodes[i][Nodes[i].length-1].toString());
			Node one = Nodes[i][Nodes.length-1];
			Node three = Nodes[i+1][Nodes.length-1];
			if (one.NextNodes.contains(three))
				line2 += FormToBoard("    \\/");
			else if (three.NextNodes.contains(one))
				line2 += FormToBoard("    /\\");
			else
				line2 += FormToBoard("  ");
			System.out.println(line1);
			System.out.println();
			System.out.println(line2);
			System.out.println();				
		}
		
		//draw the final line of nodes and relationships (dont have to worry about vertical relationships)
		String bottomLine = "";
		for (int j=0;j<Nodes[Nodes.length-1].length-1;j++)
		{			
			Node one = Nodes[Nodes.length-1][j];
			Node two = Nodes[Nodes.length-1][j+1];
			
			//draw node 1
			bottomLine += FormToBoard(one.toString());
			
			//draw relationship between 1 and 2
			if (one.NextNodes.contains(two))
				bottomLine += FormToBoard("    >>>");
			else if (two.NextNodes.contains(one))
				bottomLine += FormToBoard("    <<<");
			else
				bottomLine += FormToBoard("  ");
		}
		//draw the very last node
		bottomLine += FormToBoard(Nodes[Nodes.length-1][Nodes.length-1].toString());
		System.out.println(bottomLine);
		
		String BreakBetweenDraws = "";
		for (int i=0;i<Nodes.length*2-1;i++)
		{
			BreakBetweenDraws += FormToBoard("----------------------------");
		}
		System.out.println(BreakBetweenDraws);
	}
	
	/**
	 * resizes the output to fit the board
	 * @param s
	 * @return s truncated to fit the board
	 */
	public static String FormToBoard(String s)
	{
		s += "                   ";
		return s.substring(0, Board.NODE_SIZE);
	}
}
class Node
{
	public int row, col;
	public int Points;
	//Flags if node is blank
	public boolean isBlank;
	//Flags if node has star
	public boolean hasStar;
	//Flags if node has the double powerup
	public boolean hasDoublePower;
	//these are the next nodes we can reach from this location
	public ArrayList<Node> NextNodes = new ArrayList<Node>();
	//these are the pieces that are currently at this location
	public ArrayList<Piece> Occupants = new ArrayList<Piece>();
	public Node()
	{
		this(false);
		Points = 0;
	}
	public Node(boolean isBlank)
	{
		this.isBlank = isBlank;
	}
	public Node(int Points, int r, int c)
	{
		this.Points = Points;
		row = r;
		col = c;
	}
	public void Update()
	{
		Points++;		
	}
	public boolean Land(Piece p)
	{
		//If player has enough coins, buy star
		if (hasStar && p.Coins > 50)
		{
			System.out.println(p.Name + " has bought a star");
			hasStar = false;
			p.Stars++;
			Points = -1;
			p.Coins -= 50;
			Occupants.add(p);
			return true;
		}
		else if (hasStar) //and if coins are not high enough, tax them
		{
			p.Coins -= 25;//STAR Tax
		}
		//If location has DoublePowerup, give powerup to player
		if (hasDoublePower)
		{
			hasDoublePower = false;
			p.hasDoublePower = true;
			p.Name += "!";
			p.timerDoublePower = 10;
			JPartyGame.usingDoublePower = p;
			JPartyGame.DoublePowerupState = JPartyGame.PowerupState.OBTAINED;
		}
		//If player has the double powerup, give double the amount of points to player and reset node points
		if (p.hasDoublePower)
		{
			p.Coins += 2*Points;
			Points = -5;
		}
		//Else, add points to player and reset node points
		else
		{
			p.Coins += Points;
			Points = -5;
		}
		//Add player to node
		Occupants.add(p);
		//Limit players coins from 0 to 100
		if (p.Coins<0)
			p.Coins = 0;
		else if (p.Coins > 100)
			p.Coins = 100;
		
		return false;
	}
	public void UnLand(Piece p)
	{
		//Remove player from node
		Occupants.remove(p);
	}
	public String toString()
	{
		if (hasStar)
			return Board.FormToBoard("["+row+", "+col+"] * "+Occupants.toString());
		if (isBlank)
			return Board.FormToBoard("  ");
		if (hasDoublePower)
			return Board.FormToBoard("["+row+", "+col+"] "+Points+" x2 "+Occupants.toString());
		return Board.FormToBoard("["+row+", "+col+"] "+Points+" "+Occupants.toString());
	}
	public int GetPoints(int playerCoinAmount)
	{
		if (hasStar)
		{
			if (playerCoinAmount >= 50)
				return 1000; //high priority
			else
				return -25;
		}
		return Points;
	}
}

interface specialNode{
	public String toString();
	public void Update();
}


class doubleNode extends Node implements specialNode{

	public String toString()
	{
		if (hasStar)
			return Board.FormToBoard("["+row+", "+col+"] * "+Occupants.toString());
		if (isBlank)
			return Board.FormToBoard("  ");
		return Board.FormToBoard("["+row+", "+col+"] "+ "2x" +" "+Occupants.toString());
	}

}


class randomNode extends Node implements specialNode
{
	private static Random rand = new Random();

	public static int randPoints()
	{
		int val = rand.nextInt(2);
		int points;
		if(val == 0){
			points = rand.nextInt(60) + 50;            // gives you a positive amount of points
		}
		else
		{
			points = 0 - (rand.nextInt(60) + 50);      // gives you a negatvie amount of points
		}

		return points;
	}

	public randomNode(int r, int c)
	{
		super(0,r,c);
		this.Points = randPoints();
	}

	public void Update()
	{
		this.Points = randPoints();
	}

	public String toString()
	{
		if (hasStar)
			return Board.FormToBoard("["+row+", "+col+"] * "+Occupants.toString());
		if (isBlank)
			return Board.FormToBoard("  ");
		if (hasDoublePower)
			return Board.FormToBoard("["+row+", "+col+"] "+"?"+" x2 "+Occupants.toString());
		return Board.FormToBoard("["+row+", "+col+"] "+ "?" +" "+Occupants.toString());
	}

}



class Piece
{
	public enum Movement
	{
		UP, DOWN, LEFT, RIGHT
	}
	public int Coins;
	public int Stars;
	public Node Location;
	public String Name;
	//Flags if player has double powerup
	public boolean hasDoublePower;
	//Timer for double powerup
	public int timerDoublePower;
	public Piece(String name)
	{
		Name = name;
	}
	public String toString()
	{
		return Name;
	}
	public void setLocation(Node n)
	{
		Location = n;
	}
	//Setup Piece
	public void setupPiece(Board board)
	{
		setLocation(board.chooseRandomNode());
		Location.Land(this);
	}
	//Move Piece
	public boolean Move(Movement direction, Board board)
	{
		int row = Location.row;
		int col = Location.col;
		Node Next;
		boolean getStar = false;
		//How can I move from place to place... I need code here
        switch(direction)
        {
			case UP:
				//Collision detection
				if(row-1<0) break;
				//Move piece
				Next = board.getNode(row-1, col);
				if(Location.NextNodes.contains(Next))
				{
					Location.UnLand(this);
					getStar = Next.Land(this);
					Location = Next;
				}
				break;
			case DOWN:
				//Collision detection
				if(row+1>Board.ROW_NUMBER-1) break;
				//Move piece
				Next = board.getNode(row+1, col);
				if(Location.NextNodes.contains(Next))
				{
					Location.UnLand(this);
					getStar = Next.Land(this);
					Location = Next;
				}
				break;
            case LEFT:
				//Collision detection
				if(col-1<0) break;
				//Move piece
				Next = board.getNode(row, col-1);
				if(Location.NextNodes.contains(Next))
				{
					Location.UnLand(this);
					getStar = Next.Land(this);
					Location = Next;
				}
				break;
            case RIGHT:
				//Collision detection
				if(col+1>Board.COL_NUMBER-1) break;
				//Move piece
				Next = board.getNode(row, col+1);
				if(Location.NextNodes.contains(Next))
				{
					Location.UnLand(this);
					getStar = Next.Land(this);
					Location = Next;
				}
				break;
		}
		return getStar;
	}
}

class HumanPiece extends Piece
{
	public HumanPiece(String name)
	{
		super(name);
	}

	public Movement PlayerInput()
	{
		Scanner s;
		int direction = 0;
		//Loop until a proper input is received
		while (!(direction == 2 || direction == 4 || direction == 6 || direction == 8))
		{
			s = new Scanner(System.in);
			direction = s.nextInt();
		}
		System.out.println();
		//Return direction
		switch(direction)
		{
			case 2:
				return Piece.Movement.DOWN;
			case 4:
				return Piece.Movement.LEFT;
			case 6:
				return Piece.Movement.RIGHT;
			case 8:
				return Piece.Movement.UP;
			default:
				return null;
		}
	}
}

/**
 *  The smartPiece should use the directed graph to look at what is available down each path it can take
 */
class SmartPiece extends Piece
{
	public int[] sumlist = new int[6];

	public SmartPiece(String name)
	{
		super(name);
	}
	
	public Movement AIChoose(Board board)
	{
		// If only one node, go to the next node
		if (Location.NextNodes.size() == 1)
		{
			int row1 = Location.row;
			int col1 = Location.col;
			int row2 = Location.NextNodes.get(0).row;
			int col2 = Location.NextNodes.get(0).col;
			int y = row2 - row1;
			int x = col2 - col1;

			if	(y == 1) 	return Piece.Movement.DOWN;
			if	(y == -1) 	return Piece.Movement.UP;
			if	(x == 1) 	return Piece.Movement.RIGHT;
			if	(x == -1) 	return Piece.Movement.LEFT;
		}
		//Else if AI has 50 or more coins, go to star
		else if (Coins >= 50)
		{
			//Find path to star
			Node path = findPath(Location, board.starPlace);
			System.out.println("Found star. Go to: "+path);
			//Go to path
			int row1 = Location.row;
			int col1 = Location.col;
			int row2 = path.row;
			int col2 = path.col;
			int y = row2 - row1;
			int x = col2 - col1;

			if	(y == 1) 	return Piece.Movement.DOWN;
			if	(y == -1) 	return Piece.Movement.UP;
			if	(x == 1) 	return Piece.Movement.RIGHT;
			if	(x == -1) 	return Piece.Movement.LEFT;
		}
		//Else, choose the best pathway
		else
		{
			//Reset the sumlist array
			for (int i = 0; i < sumlist.length; i++)
			{
				sumlist[i] = -999;
			}
			int index = 0;
			Node n;
			//Calculate the points for each pathway
			for(Node node : Location.NextNodes)
			{
				n = node;
				sumlist[index] = 0;
				for(int i = 0; i < 4; i++)
				{
					sumlist[index] += n.GetPoints(Coins+sumlist[index]);
					System.out.println("Option "+index+": "+n+"Sum= "+sumlist[index]);
					n = n.NextNodes.get(0);
				}
				index++;	
			}
			//Find the one with the most points
			int max = sumlist[0];
			int maxIndex = 0;
			for (int i = 1; i < sumlist.length; i++)
			{
				if (sumlist[i]>max)
				{
					max = sumlist[i];
					maxIndex = i;
				}
			}
			System.out.println("Calculation: max= "+max+" maxIndex= "+maxIndex);
			//Go to the best path
			int row1 = Location.row;
			int col1 = Location.col;
			int row2 = Location.NextNodes.get(maxIndex).row;
			int col2 = Location.NextNodes.get(maxIndex).col;
			int y = row2 - row1;
			int x = col2 - col1;

			if	(y == 1) 	return Piece.Movement.DOWN;
			if	(y == -1) 	return Piece.Movement.UP;
			if	(x == 1) 	return Piece.Movement.RIGHT;
			if	(x == -1) 	return Piece.Movement.LEFT;
		}
		return null;
	}

	public static Node findPath(Node start, Node destination)
	{
		//Find path
		ArrayList<Node> visited = new ArrayList<>();
		ArrayList<ArrayList<Node>> routes = new ArrayList<ArrayList<Node>>();

		visited.add(start);
		int i = 0;
		for(Node path : start.NextNodes)
		{
			System.out.println("Option "+i+": "+path);
			routes.add(new ArrayList<Node>());
			routes.get(i).add(path);
			i++;
		}
		
		while (routes.size() > 0)
		{
			ArrayList<Node> route = routes.get(0);
			// System.out.println("expanding: "+route);
			routes.remove(route);

			Node end = route.get(route.size()-1);
			visited.add(end);

			if (end == destination)
				return route.get(0);
			for(Node path : end.NextNodes)
			{
				if (!visited.contains(path))
				{
					ArrayList<Node> copy = new ArrayList<>(route);
					copy.add(path);
					routes.add(copy);
				}
			}
		}
		return null;
	}
}