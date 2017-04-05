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
	
	public LinkStateVender(Router router, DatagramSocket clientSocket, LinkState state, int port)
	{
		this.router = router;
		this.receivePacket = clientSocket;
		this.state = state;
		this.port = port;
	}
	
	
	@Override
	public void run() {
		
		
		// TODO Auto-generated method stub
		
	}

}
