package router.router;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.TimerTask;

public class LinkStateVender extends TimerTask
{
	private Router router;
	private DatagramSocket receivePacket;
	
	public LinkStateVender(Router router, DatagramSocket clientSocket)
	{
		this.router = router;
		this.receivePacket = clientSocket;
	}
	
	
	@Override
	public void run() {
		
		
		// TODO Auto-generated method stub
		
	}

}
