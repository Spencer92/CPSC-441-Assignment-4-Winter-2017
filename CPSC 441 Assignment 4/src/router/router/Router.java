package router.router;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Timer;



/**
 * Router Class
 * 
 * Router implements Dijkstra's algorithm for computing the minumum distance to all nodes in the network
 * @author      XYZ
 * @version     1.0
 *
 */
public class Router {

	private String peerip;
	private int routerid;
	private int port;
	private String configfile;
	private int neighbourupdate;
	private int routeupdate;
	private DatagramSocket clientSocket;
	private InetAddress IPAddress;
	private int[] allNodesDistance;
	private boolean[] receivedRouterInfo;
	private static final int MAX_NODES = 10;
	private boolean end = false;
	private static final int INFINITE_LENGTH = 999;
	private int [][] nodeInfo;
//	private DatagramSocket[] clientSockets;
	private Timer theTimer;
	private int amountOfTimesSent = 0;
	private static final int NEXT_ROUTER = 11;
	private int clientPortNumbers[];
	private Timer calcTimer;
	private int numRouters;
	private int[] closestRouters;
	
	
	
	/**
     	* Constructor to initialize the program 
     	* 
     	* @param peerip		IP address of other routers (we assume that all routers are running in the same machine)
     	* @param routerid	Router ID
     	* @param port		Router UDP port number
     	* @param configfile	Configuration file name
	* @param neighborupdate	link state update interval - used to update router's link state vector to neighboring nodes
        * @param routeupdate 	Route update interval - used to update route information using Dijkstra's algorithm
 
     */
	public Router(String peerip, int routerid, int port, String configfile, int neighborupdate, int routeupdate) {
	
		this.peerip = peerip;
		this.routerid = routerid;
		this.port = port;
		this.configfile = configfile;
		this.neighbourupdate = neighborupdate;
		this.routeupdate = routeupdate;
		this.allNodesDistance = new int[MAX_NODES];
		this.receivedRouterInfo = new boolean[MAX_NODES];
		
		for(int i = 0; i < MAX_NODES; i++)
		{
			this.allNodesDistance[i] = 999;
			this.receivedRouterInfo[i] = false;
		}
		this.allNodesDistance[routerid] = 0;

	
	}
	

    	/**
     	*  Compute route information based on Dijkstra's algorithm and print the same
     	* 
     	*/
	public void compute() {


	  	/**** You may use the follwing piece of code to print routing table info *******/
        	System.out.println("Routing Info");
        	System.out.println("RouterID \t Distance \t Prev RouterID");
        	
        	Path path = Paths.get(this.configfile);
    		byte [] configFileInfo = null;
    		int index = 0;
    		int port;
    		this.theTimer = new Timer();
    		this.calcTimer = new Timer();
    		int otherRouter;
    		Thread aThread;
    	
    		
    		try {
    			configFileInfo = Files.readAllBytes(path);
				IPAddress = InetAddress.getByName(this.peerip);
    			
				
				numRouters = configFileInfo[0] - '0';
				this.nodeInfo = new int[numRouters][];
//				this.clientSockets = new DatagramSocket[numRouters];
				this.receivedRouterInfo = new boolean[numRouters];
				this.clientPortNumbers = new int[numRouters];
				this.closestRouters = new int[numRouters];
				clientSocket = new DatagramSocket(this.port);
				
				
				
				//The purpose of this was to get the routers
				//that were the closest, couldn't get it
				//to work, however
				for(int i = 0; i < this.closestRouters.length; i++)
				{
					this.closestRouters[i] = 999;
				}
				
				//This was to initialize the array that carries the 
				//array. The reason its two added to the array
				//is to carry what sent out the array,
				//and how many times the information was sent out
				for(int i = 0; i < this.nodeInfo.length; i++)
				{
					this.nodeInfo[i] = new int[this.nodeInfo.length+2];
				}
				
				
				//Assume that there is an infinite amount of length between the
				//nodes
				for(int i = 0; i < this.nodeInfo.length; i++)
				{
					for(int j = 0; j < this.nodeInfo[i].length; j++)
					{
						this.nodeInfo[i][j] = INFINITE_LENGTH;
					}
				}
				
				
				//Initially no ports or router info would
				//be known or received
				for(int i = 0; i < this.clientPortNumbers.length; i++)
				{
					this.clientPortNumbers[i] = 0;
					this.receivedRouterInfo[i] = false;
				}
				
				
				//When sending out what the node knows,
				//Specify that it was this node that originally
				//sent it
				for(int i = 0; i < this.nodeInfo.length; i++)
				{
					this.nodeInfo[i][this.nodeInfo[i].length-2] = this.routerid;
				}
				
				this.nodeInfo[this.routerid][this.routerid] = 0;
				this.receivedRouterInfo[this.routerid] = true;
				this.clientPortNumbers[this.routerid] = this.port;
				index += 2;
				
				while(index < configFileInfo.length)
				{
					//In case there is a line feed
					if(configFileInfo[index] == 10)
					{
						index++;
						if(index >= configFileInfo.length)
						{
							break;
						}
					}
					
					//otherRouter is the router that is currently being read
					otherRouter = configFileInfo[index]-'A';
					this.receivedRouterInfo[otherRouter] = true;
					this.nodeInfo[this.routerid][otherRouter] = configFileInfo[index+4]-'0';
    				port = ((configFileInfo[index + 6]-'0')*1000) + ((configFileInfo[index+7]-'0')*100) + ((configFileInfo[index+8]-'0')*10) + (configFileInfo[index+9]-'0');
     				this.clientPortNumbers[otherRouter] = port;    				
    				index += NEXT_ROUTER;
				}//while(index < configFileInfo.length)
				
				
				
				//Start the threads that need to send and receive
				aThread = new Thread(new LinkStateReceiver(this,this.clientSocket));
				aThread.start();
				
				this.theTimer.scheduleAtFixedRate(new LinkStateVender(this), 0, 1000);
				
				this.calcTimer.scheduleAtFixedRate(new MinimumDistanceCalculate(this), 10000, 10000);
			
    			
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    		}
		
	 	/*******************/

	}
	
	
	public boolean isEnd()
	{
		return this.end;
	}
	
	public int getRouterId()
	{
		return this.routerid;
	}
	
	
	/**
	 * 
	 * Receives a packet from another router, and processes it
	 * 
	 * @param receivepacket The packet that is to be received
	 */
	public synchronized void processUpDateDS(DatagramPacket receivepacket)
	{
		LinkState state = new LinkState(receivepacket);
		int index;
	
		
		//Find out where the node originated
		//If it is zero than that's where the information
		//originated
		for(index = 0; index < state.getCost().length; index++)
		{
			if(state.getCost()[index] == 0)
			{
				break;
			}
		}
		
		

		
	
		//If the information originated here,
		//The array is out of bounds
		//Or it already received the information being sent
		//Do nothing
		if(state.getCost()[state.getCost().length-2] == this.routerid || state.getCost()[this.routerid] == 0)
		{
			return;
		}
		if(index >= this.nodeInfo.length || index == this.routerid)
		{
			return;
		}
		if(this.nodeInfo[index][this.nodeInfo[index].length-2] == 
				state.getCost()[state.getCost().length-1])
		{
			return;
		}
		
		
		
		this.receivedRouterInfo[index] = true;
		
		//Copy the array contents
		//and update the timer with the new info
		
		try {
			this.nodeInfo[index] = copyArray(this.nodeInfo[index],state.getCost());
			
			this.theTimer.cancel();
			this.theTimer = new Timer();
			this.theTimer.scheduleAtFixedRate(new LinkStateVender(this), 0, 1000);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	// Update data structure(s).
	// Forward link state message received to neighboring nodes
	// based on broadcast algorithm used.
	}
	
	
	/**
	 * CopyArray
	 * 
	 * Copies the array
	 * Used to make 2d array copying more bearable
	 * 
	 * @param arrayOne The array to be copied to
	 * @param arrayTwo The array to copy
	 * @return the copied array
	 * @throws Exception The arrays are different sizes
	 */
	
	private int[] copyArray(int[] arrayOne, int[] arrayTwo) throws Exception
	{
		int[] newArray = new int[arrayOne.length];
		if(arrayOne.length == arrayTwo.length)
		{
			for(int i = 0; i < arrayOne.length; i++)
			{
				newArray[i] = arrayTwo[i];
			}
			return newArray;
		}
		else
		{
			throw new Exception("Arrays different size");
		}
	}

	
	/**
	 * ProcessUpdateNeighbor
	 * 
	 * Takes the information got from any of the routers, and
	 * forwards it to all of its neighbours
	 * 
	 */
	
	public synchronized void processUpdateNeighbor()
	{
		LinkState state;
		DatagramPacket sendPacket;
		int preSendInfo[];
		
		//Where the amount of times sent is placed in the array
		this.nodeInfo[this.routerid][this.nodeInfo[this.routerid].length-1] = this.amountOfTimesSent;

		this.amountOfTimesSent++;
		
		//Just so the numbers don't get absurdly high
		if(this.amountOfTimesSent > 999)
		{
			this.amountOfTimesSent = 0;
		}
		
		for(int i = 0; i < this.receivedRouterInfo.length; i++)
		{
			

			if(this.receivedRouterInfo[i])
			{
				for(int j = 0; j < this.clientPortNumbers.length; j++)
				{
					//If the node is connected, then it should have a port number
					if(this.clientPortNumbers[j] != 0)
					{
						
						//A new array needs to be created in
						//Order for the information of 
						//what router sent the information
						//to be preserved
						preSendInfo = new int[this.nodeInfo[i].length];
						for(int k = 0; k < preSendInfo.length; k++)
						{
							preSendInfo[k] = this.nodeInfo[i][k];
						}
						preSendInfo[preSendInfo.length-2] = this.routerid;
						state = new LinkState(this.routerid,i,preSendInfo);
						sendPacket = new DatagramPacket(state.getBytes(),state.getBytes().length,this.IPAddress,this.clientPortNumbers[j]);
						try {
							clientSocket.send(sendPacket);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}//if(this.clientPortNumbers[j] != 0)
				}//for(int j = 0; j < this.clientSockets.length; j++)
			}//if(this.receivedRouterInfo[i])

		}//for(int i = 0; i < this.receivedRouterInfo.length; i++)
		
	}
	
	/**
	 * ProcessUpdateRoute
	 * 
	 * This is where dijkstra's algorithm is implemented
	 * 
	 * Which takes the minimum distance between all possible paths
	 *
	 */

	public synchronized void processUpdateRoute(){
				
		int size = 999;
		int minNode = -1;
		boolean usedNodes[] = new boolean[this.numRouters];

		
		//If not all of the router information
		//has been received, do nothing
		for(int i = 0; i < this.receivedRouterInfo.length; i++)
		{
			if(!this.receivedRouterInfo[i])
			{
				return;
			}
		}
		
		//This is used to simulate N'
		//If any nodes are in N', then
		//The minimum for that run has 
		//already been reached
		for(int i = 0; i < usedNodes.length; i++)
		{
			usedNodes[i] = false;
		}


		usedNodes[this.routerid] = true;
		
		do
		{
			for(int i = 0; i < usedNodes.length; i++)
			{

				//Get the minimum node
				if(this.nodeInfo[this.routerid][i] < size && !usedNodes[i])
				{
					size = this.nodeInfo[this.routerid][i];
					minNode = i;
				}//for(int i = 0; i < usedNodes.length; i++)


			}
			usedNodes[minNode] = true;
			size = 999;
			
			for(int i = 0; i < usedNodes.length; i++)
			{
				
				//Equivalent to D(v) = min(D(v), D(w)+c(w,v))
				this.nodeInfo[this.routerid][i] = minimum(this.nodeInfo[this.routerid][i],
						this.nodeInfo[this.routerid][minNode] + this.nodeInfo[minNode][i]);
				
				
				//This is where the closest routers
				//Would be shown, but it doesn't work
				if(this.nodeInfo[this.routerid][i] < this.closestRouters[i])
				{
					this.closestRouters[i] = this.nodeInfo[minNode][this.nodeInfo[minNode].length-2];

				}
			}//for(int i = 0; i < usedNodes.length; i++)
			
			
			
			
		}while(!allNodesUsed(usedNodes));
		
		
		//Display the info
    	System.out.println("Routing Info");
    	System.out.println("RouterID \t Distance");
    	for(int i = 0; i < this.numRouters; i++)
      	{
      		System.out.println(i + "\t\t   " + this.nodeInfo[this.routerid][i]);
      	}
		
		
		this.theTimer.cancel();
		
		this.theTimer = new Timer();
		this.theTimer.scheduleAtFixedRate(new LinkStateVender(this), 0, 1000);
		
		
		
	// If link state vectors of all nodes received,
	// Yes => Compute route info based on Dijkstra’s algorithm
	// and print as per the output format.
	// No => ignore the event.
	// Schedule task if Method-1 followed to implement recurring
	// timer task.
	}
	
	
	/**
	 * AllNodesUsed
	 * 
	 * Uses an array to check if all the values are true
	 * 
	 * 
	 * @param usedNodes The information about whether the node has
	 * 					been processes
	 * @return true if all nodes have been processes
	 * 		   false otherwise
	 */
	private boolean allNodesUsed(boolean[] usedNodes)
	{
		for(int i = 0; i < usedNodes.length; i++)
		{
			if(!usedNodes[i])
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Minimum
	 * 
	 * finds the smaller number
	 * 
	 * @param firstNum The first number
	 * @param secondNum The second number
	 * @return the smaller number
	 */
	
	private int minimum(int firstNum, int secondNum)
	{
		if(firstNum < secondNum)
		{
			return firstNum;
		}
		else
		{
			return secondNum;
		}
	}
	
	/* A simple test driver 
     	
	*/
	public static void main(String[] args) {
		
		String peerip = "127.0.0.1"; // all router programs running in the same machine for simplicity
		String configfile = "";
		int routerid = 999;
                int neighborupdate = 1000; // milli-seconds, update neighbor with link state vector every second
		int forwardtable = 10000; // milli-seconds, print route information every 10 seconds
		int port = -1; // router port number
	
		// check for command line arguments
		if (args.length == 3) {
			// either provide 3 parameters
			routerid = Integer.parseInt(args[0]);
			port = Integer.parseInt(args[1]);	
			configfile = args[2];
		}
		else {
			System.out.println("wrong number of arguments, try again.");
			System.out.println("usage: java Router routerid routerport configfile");
			System.exit(0);
		}

		
		Router router = new Router(peerip, routerid, port, configfile, neighborupdate, forwardtable);
		
		System.out.printf("Router initialized..running");
		router.compute();
	}

}
