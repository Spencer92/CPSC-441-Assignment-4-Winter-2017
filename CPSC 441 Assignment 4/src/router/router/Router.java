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

 	private int numNodes;
	private int[] prev;
	private int[] distancevector;
	private String peerip;
	private int routerid;
	private int port;
	private String configfile;
	private int neighbourupdate;
	private int routeupdate;
	private DatagramSocket clientSocket;
	private InetAddress IPAddress;
	private DataOutputStream outputStream;
	private DataInputStream inputStream;
	private static final int MAX_BYTE_SIZE = 1000;
	private int[] allNodesDistance;
	private boolean[] allNodesAdjacent;
	private static final int MAX_NODES = 10;
	LinkedList<LinkState> routers = new LinkedList<LinkState>();
	private boolean end = false;
	private static final int INFINITE_LENGTH = 999;
	private int [] origNodeDist;
	private int [][] nodeInfo;
	private LinkState [] connectors;
	private int ports[];
	private DatagramSocket[] clientSockets;
	private Timer theTimer;
	
	
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
		this.allNodesAdjacent = new boolean[MAX_NODES];
		this.origNodeDist = new int[MAX_NODES];
		this.ports = new int[MAX_NODES];
		
		for(int i = 0; i < MAX_NODES; i++)
		{
			this.allNodesDistance[i] = 999;
			this.allNodesAdjacent[i] = false;
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
        	for(int i = 0; i < numNodes; i++)
          	{
          		System.out.println(i + "\t\t   " + distancevector[i] +  "\t\t\t" +  prev[i]);
          	}
        	
//    		this.file_name = file_name;
 //   		Path path = Paths.get(this.file_name);
        	Path path = Paths.get(this.configfile);
    		byte [] fileByteInfo = null;
    		Socket socket = null;
    		byte checkForReceivedInfo = Byte.MIN_VALUE;
    		byte [] dataToSend = new byte[MAX_BYTE_SIZE];
    		byte [] configFileInfo = null;
//    		Segment segment = null;

//    		queue = new TxQueue(this.window);
    		int indexFileInfo;
    		int indexSender;
    		int seqNum = 0;
    		int index = 0;
    		int routerDist;
    		int port;
    		LinkStateReceiver receiver;
    		Timer[] timers;
    		int timerCount = 0;
    		this.theTimer = new Timer();
    		
    		
    		
    	
    		
    		try {
  //  			fileByteInfo = Files.readAllBytes(path);
 //   			socket = new Socket(this.server_name,SERVER_PORT);
    			configFileInfo = Files.readAllBytes(path);
				IPAddress = InetAddress.getByName(this.peerip);
    			
				this.nodeInfo = new int[configFileInfo[0]][];
				
				for(int i = 0; i < this.nodeInfo.length; i++)
				{
					this.nodeInfo[i] = new int[this.nodeInfo.length+2];
				}
				
				for(int i = 0; i < this.nodeInfo.length; i++)
				{
					for(int j = 0; j < this.nodeInfo[i].length; j++)
					{
						this.nodeInfo[i][j] = 999;
					}
				}
				
				
				
				
				
/*    			for(int i = 0; i < configFileInfo.length; i++)
    			{
    				System.out.print(configFileInfo[i] + " ");
    				byte[] convert = new byte[1];
    				convert[0] = configFileInfo[i];
    				System.out.print(new String(convert,"UTF-8"));
    				System.out.println();
    			}

    			System.out.println(configFileInfo[2] - 'A');
    			System.out.println(configFileInfo.length);
    			
    			this.nodeInfo = new int[configFileInfo[0]+1][];
    			timers = new Timer[configFileInfo[0]];
    			ports = new int[configFileInfo[0]];
    			
    			for(int i = 0; i < ports.length; i++)
    			{
    				ports[i] = -1;
    			}
    			
    			for(int i = 0; i < this.nodeInfo.length-1; i++)
    			{
    				this.nodeInfo[i] = new int[this.nodeInfo.length];
    				this.nodeInfo[this.nodeInfo.length][i] = Integer.MIN_VALUE;
    			}
    			
    			
    			this.connectors = new LinkState[configFileInfo[0]];
    			this.allNodesDistance = new int[configFileInfo[0] + 2];
    			this.allNodesAdjacent = new boolean[configFileInfo[0]];
    			this.allNodesDistance[this.allNodesDistance.length-2] = this.routerid;
    			this.allNodesDistance[this.allNodesDistance.length-1] = 0;
    			this.clientSockets = new DatagramSocket[configFileInfo[0]];
    			
    			for(int i = 0; i < this.clientSockets.length; i++)
    			{
    				this.clientSockets[i] = null;
    			}
    			
    			for(int i = 0; i < this.allNodesAdjacent.length; i++)
    			{
    				this.allNodesDistance[i] = INFINITE_LENGTH;
    				this.allNodesAdjacent[i] = false;
    			}
    			
    			
    			while(index < configFileInfo.length)
    			{
    				
    			}
    			
    			while(index < configFileInfo.length)
    			{
    				
    				routerDist = configFileInfo[index + 2] - 'A';
    				this.allNodesDistance[routerDist] = configFileInfo[index + 6];
    				this.origNodeDist[routerDist] = configFileInfo[index + 6];
    				port = ((configFileInfo[index + 8]-'0')*1000) + ((configFileInfo[index+9]-'0')*100) + ((configFileInfo[index+10]-'0')*10) + (configFileInfo[index+11]-'0');
  
    				System.out.println(configFileInfo[index+8]);
    				System.out.println(configFileInfo[index+9]);
    				System.out.println(configFileInfo[index+10]);
    				System.out.println(configFileInfo[index+11]);
    				System.out.println(port);
    				routers.add(new LinkState(this.routerid,port,this.allNodesDistance));
    				this.allNodesAdjacent[routerDist] = true;
    				
    				if(routerDist < this.clientSockets.length)
    				{
    					this.clientSockets[routerDist] = new DatagramSocket(port);
    				}
    				
    				
    				/*    				if(timerCount < timers.length)
    				{
    					
    					ports[timerCount] = port;
        				connectors[timerCount] = new LinkState(this.routerid,port,this.allNodesDistance);    					
    					timers[timerCount] = new Timer();
    					timers[timerCount].scheduleAtFixedRate(new LinkStateVender(this,clientSocket,connectors[timerCount],port), 0,1000);
    				}*/
    				timerCount++;
    				index += 13;    				
 /*   			}
    			
    			
    			
    			
    			
    			checkForReceivedInfo = 1;*/
    			
    			
//    			segment = new Segment();
/*    			clientSocket = new DatagramSocket(this.routerid);
        		receiver = new LinkStateReceiver(this,clientSocket);
        		Thread aThread = new Thread(receiver);
        		aThread.start();*/
        		
        		
        		
    			//Start the thread that will receive the acks
//    			AckHandler handler = new AckHandler(this, clientSocket);
//    			Thread aThread = new Thread(handler);
 //   			aThread.start();
//    			aTimer = new Timer();
    			
    			
//    			IPAddress = InetAddress.getByName("localhost");

    			
 //   			outputStream = new DataOutputStream(socket.getOutputStream());
//    			outputStream.writeUTF(this.file_name);			
//    			outputStream.flush();
 //   			inputStream = new DataInputStream(socket.getInputStream());
 //   			checkForReceivedInfo = inputStream.readByte();				
    			
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
	
	public synchronized void processUpDateDS(DatagramPacket receivepacket)
	{
		LinkState state = new LinkState(receivepacket);
		boolean infiniteLength = true;
		int [] distUpdate = state.cost;
		int index;
		
		for(index = 0; index < state.getCost().length-2; index++)
		{
			if(state.getCost()[index] == 0)
			{
				break;
			}
		}
		
		if(this.nodeInfo[this.nodeInfo.length-1][index] == state.getCost()[state.getCost().length-1])
		{
			return;
		}
		else
		{
			
			try {
				this.nodeInfo[index] = copyArray(this.nodeInfo[index],state.getCost());

				this.theTimer.cancel();
				
				this.theTimer.scheduleAtFixedRate(new LinkStateVender(this,state), 0, 1000);
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return;
			}
			
		}
		
		
		
/*		
		
		for(int i = 0; i < distUpdate.length; i++)
		{
			if(distUpdate[i] < INFINITE_LENGTH)
			{
				infiniteLength = false;
				break;
			}
		}
		
		if(infiniteLength)
		{
			return;
		}
		
		for(int i = 0; i < this.allNodesDistance.length && i < distUpdate.length; i++)
		{
			if(this.allNodesDistance[i] > distUpdate[i])
			{
				this.allNodesDistance[i] = distUpdate[i];
			}
		}
		
		LinkedList<LinkState> newRouters = new LinkedList<LinkState>();
		
		
		for(int i = 0; i < allNodesAdjacent.length; i++)
		{
			if(allNodesAdjacent[i])
			{
				
			}
		}
		
		for(LinkState routerThrough : this.routers)
		{
			if(routerThrough.destId < allNodesAdjacent.length && allNodesAdjacent[routerThrough.destId])
			{
				
			}
		}

*/		
		
	// Update data structure(s).
	// Forward link state message received to neighboring nodes
	// based on broadcast algorithm used.
	}
	
	
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
	
	int addUpTo999(int num1, int num2)
	{
		if(num1+num2 >= INFINITE_LENGTH)
		{
			return INFINITE_LENGTH;
		}
		else
		{
			return (num1+num2);
		}
	}
	

	
	public synchronized void processUpdateNeighbor(LinkState state)
	{
		DatagramPacket sendPacket;
		
		for(int i = 0; i < clientSockets.length; i++)
		{
			if(clientSockets[i] != null)
			{
				sendPacket = new DatagramPacket(state.getBytes(),state.getBytes().length,this.IPAddress,clientSockets[i].getPort());
				try {
					clientSockets[i].send(sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	public synchronized void processUpdateNeighbor(LinkState state, int port){
		
		Socket socket;
		DatagramSocket clientSocket;
		DataOutputStream outputStream;
		DatagramPacket sendPacket;
		try {
			socket = new Socket(this.peerip,port);
			clientSocket = new DatagramSocket(port);
//			outputStream = new DataOutputStream(socket.getOutputStream());
			sendPacket = new DatagramPacket(state.getBytes(),state.getBytes().length);
//			outputStream.writeUTF(this.peerip);
//			outputStream.flush();
			clientSocket.send(sendPacket);
			clientSocket.close();
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	
		
	// Send node�s link state vector to neighboring nodes as link
	// state message.
	// Schedule task if Method-1 followed to implement recurring
	// timer task.
	}
	public synchronized void processUpdateRoute(){
	// If link state vectors of all nodes received,
	// Yes => Compute route info based on Dijkstra�s algorithm
	// and print as per the output format.
	// No => ignore the event.
	// Schedule task if Method-1 followed to implement recurring
	// timer task.
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
