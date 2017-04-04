package router.router;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
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
    		
    		
    		try {
  //  			fileByteInfo = Files.readAllBytes(path);
 //   			socket = new Socket(this.server_name,SERVER_PORT);
    			configFileInfo = Files.readAllBytes(path);
    			
    			for(int i = 0; i < configFileInfo.length; i++)
    			{
    				System.out.print(configFileInfo[i] + " ");
    				byte[] convert = new byte[1];
    				convert[0] = configFileInfo[i];
    				System.out.print(new String(convert,"UTF-8"));
    				System.out.println();
    			}

    			System.out.println(configFileInfo[2] - 'A');
    			System.out.println(configFileInfo.length);
    			
    			
    			while(index < configFileInfo.length)
    			{
    				routerDist = configFileInfo[index + 2] - 'A';
    				this.allNodesDistance[routerDist] = configFileInfo[index + 6];
    				port = ((configFileInfo[index + 8]-'0')*1000) + ((configFileInfo[index+9]-'0')*100) + ((configFileInfo[index+10]-'0')*10) + (configFileInfo[index+11]-'0');

    				System.out.println(configFileInfo[index+8]);
    				System.out.println(configFileInfo[index+9]);
    				System.out.println(configFileInfo[index+10]);
    				System.out.println(configFileInfo[index+11]);
    				System.out.println(port);
    				routers.add(new LinkState(this.routerid,port,this.allNodesDistance));
    				this.allNodesAdjacent[routerDist] = true;
    				index += 13;    				
    			}
    			
    			
    			
    			checkForReceivedInfo = 1;
//    			segment = new Segment();
    			clientSocket = new DatagramSocket(this.routerid);
        		receiver = new LinkStateReceiver(this,clientSocket);
        		Thread aThread = new Thread(receiver);
        		aThread.start();
        		
    			//Start the thread that will receive the acks
//    			AckHandler handler = new AckHandler(this, clientSocket);
//    			Thread aThread = new Thread(handler);
 //   			aThread.start();
//    			aTimer = new Timer();
    			
    			
    			IPAddress = InetAddress.getByName("localhost");

    			
    			outputStream = new DataOutputStream(socket.getOutputStream());
//    			outputStream.writeUTF(this.file_name);			
//    			outputStream.flush();
    			inputStream = new DataInputStream(socket.getInputStream());
    			checkForReceivedInfo = inputStream.readByte();				
    			
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

		
		for(int i = 0; i < distUpdate.length; i++)
		{
			if(distUpdate[i] < 999)
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
		

		
		
	// Update data structure(s).
	// Forward link state message received to neighboring nodes
	// based on broadcast algorithm used.
	}
	public synchronized void processUpdateNeighbor(){
	// Send node’s link state vector to neighboring nodes as link
	// state message.
	// Schedule task if Method-1 followed to implement recurring
	// timer task.
	}
	public synchronized void processUpdateRoute(){
	// If link state vectors of all nodes received,
	// Yes => Compute route info based on Dijkstra’s algorithm
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
