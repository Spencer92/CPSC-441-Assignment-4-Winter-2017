package router.router;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class LinkStateReceiver implements Runnable
{
	Router router;
	DatagramSocket[] clientSocket;
	
	public LinkStateReceiver(Router router, DatagramSocket[] clientSocket)
	{
		this.router = router;
		this.clientSocket = clientSocket;
	}
	
	@Override
	public void run() {
		
		
		while(!router.isEnd())
		{
			byte [] linkStateInfo = new byte[LinkState.MAX_PAYLOAD_SIZE];
			DatagramPacket receivePacket = new DatagramPacket(linkStateInfo,linkStateInfo.length);	
			
			try {
				for(int i = 0; i < clientSocket.length; i++)
				{
					if(clientSocket[i] != null)
					{
						clientSocket[i].receive(receivePacket);
						router.processUpDateDS(receivePacket);
					}
				}
			} catch (IOException e) {
				
				if(router.isEnd())
				{
					break;
				}
				else
				{
					e.printStackTrace();
				}
			}
			
			
			
		}
		
		
		// TODO Auto-generated method stub
		
	}

}
