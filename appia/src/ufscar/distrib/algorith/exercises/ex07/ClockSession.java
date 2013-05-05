package ufscar.distrib.algorith.exercises.ex07;

import java.util.Timer;
import java.util.TimerTask;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Channel;
import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.channel.ChannelInit;

public class ClockSession extends Session {
	private Timer timer;
	private Channel channel;
	
	  public ClockSession(Layer layer) {
		  super(layer);
	  }
	  public void handle(Event event){
		  if (event instanceof ChannelInit)
		    	handleChannelInit((ChannelInit) event);
	  }
	  
	  private void handleChannelInit(Event event){
		  channel = event.getChannel();
		  
		  try {
			    timer = new Timer();
			    timer.schedule(new TimerTask() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						RetransmitEvent evt = new RetransmitEvent();
						try {
							evt.asyncGo(channel, Direction.DOWN);
						} catch (AppiaEventException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				},
			               0,        //initial delay
			               5*1000);  //subsequent rate
			event.go();
		} catch (AppiaEventException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }

}
