package router.router;

import java.util.TimerTask;

public class MinimumDistanceCalculate extends TimerTask
{
	Router router;
	public MinimumDistanceCalculate(Router router)
	{
		this.router = router;
	}
	
	
	@Override
	public void run() {
		
		router.processUpdateRoute();
		// TODO Auto-generated method stub
		
	}

}
