package router.router;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class LinkStateVender implements Runnable
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
