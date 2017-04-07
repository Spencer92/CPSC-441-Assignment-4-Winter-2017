package router.router;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.TimerTask;

public class LinkStateVender extends TimerTask
{
	private Router router;
	private DatagramSocket receivePacket;
	private LinkState state;
	private int port;
	private int amountOfTimesSent;
	
	public LinkStateVender(Router router, DatagramSocket clientSocket, LinkState state, int port)
	{
		this.router = router;
		this.receivePacket = clientSocket;
		this.state = state;
		this.port = port;
	}
	
	
	public LinkStateVender(Router router, LinkState state)
	{
		this.router = router;
		this.state = state;
	}
	
	public LinkStateVender(Router router, int amountOfTimesSent)
	{
		this.router = router;
		this.amountOfTimesSent = amountOfTimesSent;
	}
	
	public LinkStateVender(Router router)
	{
		this.router = router;
	}
	
	@Override
	public void run() {
		System.out.println("Vending");
//		router.processUpdateNeighbor(state);
		router.processUpdateNeighbor();
		// TODO Auto-generated method stub
		
	}

}
