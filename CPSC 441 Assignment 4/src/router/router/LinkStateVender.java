package router.router;

import java.util.TimerTask;

/**
 * 
 * allows processUpdateNeighbour to run
 *
 */

public class LinkStateVender extends TimerTask
{
	private Router router;
	
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
