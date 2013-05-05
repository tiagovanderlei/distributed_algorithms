package ufscar.distrib.algorith.exercises.ex03;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.channel.ChannelInit;

/**
 * Session implementing the Async Job Handler protocol.
 * 
 * After a defined number of print requests, an alarm
 * is sent and further request are denied.
 * 
 * @author tiagovanderlei
 */
public class TransformationHandlerSession extends Session {
  private int top, bottom, M;
  private boolean handling;

  String[] buffer;

  private String job;
  //private Queue<String> buffer;

  /** Creates a new instance of AsyncJobHandler */
  public TransformationHandlerSession(Layer layer) {
    super(layer);
  }

  public void handle(Event event) {
    if (event instanceof ChannelInit) {
      handleChannelInit((ChannelInit) event);
    } else if (event instanceof SubmitEvent) {
      handleSubmit((SubmitEvent) event);
    } else if (event instanceof ConfirmEvent) {
      handleConfirm((ConfirmEvent) event);
    }
  }

  private void handleChannelInit(ChannelInit init) {
    try {
      top = bottom = 0;
      job = null;
      handling = false;
      M = 2;
      buffer = new String[M];

      init.go();
    } catch (AppiaEventException e) {
      e.printStackTrace();
    }
  }

  private void handleSubmit(SubmitEvent request) {
	  try {

		  if(bottom + M == top){
		        JobErrorEvent alarm = new JobErrorEvent();
		        alarm.setChannel(request.getChannel());

		        alarm.setDir(Direction.UP);
		        alarm.setSourceSession(this);

		        alarm.init();
		        alarm.go();
		  }else{
			  buffer[top % M] = job;
			  top = top+1;

			  /* ----- ---- --- ---*/
			  
			  while(bottom < top && !handling){
				  job = buffer[bottom % M];
				  bottom = bottom + 1;
				  handling = true;
				  
				  //Submit
				  SubmitEvent request2 = new SubmitEvent();
				  request2.setJh(request.getJh());
				  request2.setJob(job);

				  request2.setChannel(request.getChannel());
				  request2.setDir(Direction.DOWN);
				  request2.setSourceSession(this);
				  request2.init();
				  request2.go();				  
			  }
			  
			  ConfirmEvent ack = new ConfirmEvent();
		      ack.setChannel(request.getChannel());

		      ack.setDir(Direction.UP);
		      ack.setSourceSession(this);
		      ack.setId(request.getJh());

		      ack.init();
		      ack.go();

		      handleConfirm(ack);

		  }

      } catch (AppiaEventException e) {
        e.printStackTrace();
      }
  }

  private void handleConfirm(ConfirmEvent conf) {

	handling = false;

	try{
		/*----- ------ ------ -----*/
		while(bottom < top && !handling){
			job = buffer[bottom % M];
			bottom = bottom + 1;
			handling = true;
			  
			//Submit
			SubmitEvent request2 = new SubmitEvent();
			request2.setJh(conf.getId());
			request2.setJob(job);
	
			request2.setChannel(conf.getChannel());
			request2.setDir(Direction.DOWN);
			request2.setSourceSession(this);
			request2.init();
			request2.go();				  
		}
		
		JobStatusEvent status = new JobStatusEvent();
	    status.setId(conf.getId());
	    status.setStatus(Status.OK);

	    status.setChannel(conf.getChannel());
	    status.setDir(Direction.UP);
	    status.setSourceSession(this);
	    status.init();
	    status.go();
    } catch (AppiaEventException e) {
      e.printStackTrace();
    }
  }
}