package router.router;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.TimerTask;

public class LinkStateVender extends TimerTask
{
	private Router router;
//	private DatagramSocket receivePacket;
//	private LinkState state;
//	private int port;
//	private int amountOfTimesSent;
	
	public LinkStateVender(Router router)
	{
		this.router = router;
	}
	
	@Override
	public void run() {
		router.processUpdateNeighbor();
		// TODO Auto-generated method stub
		
	}

}
