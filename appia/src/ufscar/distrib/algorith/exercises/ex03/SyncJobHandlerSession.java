package ufscar.distrib.algorith.exercises.ex03;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.channel.ChannelInit;

/**
 * Session implementing the Layer of the Sync Job Handler Protocol.
 * 
 * After a defined number of print requests, an alarm
 * is sent and further request are denied.
 * 
 * @author tiagovanderlei
 */
public class SyncJobHandlerSession extends Session {

  /** Creates a new instance of BoundedPrintSession */
  public SyncJobHandlerSession(Layer layer) {
    super(layer);
  }

  public void handle(Event event) {
    if (event instanceof ChannelInit) {
      handleChannelInit((ChannelInit) event);
    } else if (event instanceof SubmitEvent) {
      handleSubmit((SubmitEvent) event);
    } /*else if (event instanceof ConfirmEvent) {
      handleConfirm((ConfirmEvent) event);
    }*/ // Não manipula resultado do tipo ConfirmEvent, 
    	// o mesmo é retornado p/ camada superior s/ modificação.
  }

  private void handleChannelInit(ChannelInit init) {
    try {
      init.go();
    } catch (AppiaEventException e) {
      e.printStackTrace();
    }
  }

  private void handleSubmit(SubmitEvent request) {

	  try {
        request.go();
        
        ConfirmEvent ack = new ConfirmEvent();
        ack.setChannel(request.getChannel());

		ack.setDir(Direction.UP);
		ack.setSourceSession(this);
		ack.setId(request.getJh());

		ack.init();
		ack.go();

		handleConfirm(ack);
        
      } catch (AppiaEventException e) {
        e.printStackTrace();
      }
  }

  private void handleConfirm(ConfirmEvent conf) {
    JobStatusEvent status = new JobStatusEvent();
    status.setId(conf.getId());
    status.setStatus(Status.OK);

    try {
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