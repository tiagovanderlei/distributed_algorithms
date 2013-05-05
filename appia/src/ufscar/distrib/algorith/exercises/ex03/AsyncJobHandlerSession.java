package ufscar.distrib.algorith.exercises.ex03;

import java.util.ArrayDeque;
import java.util.Queue;

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
public class AsyncJobHandlerSession extends Session {
  private String job;
  private Queue<String> buffer;
  
  /** Creates a new instance of AsyncJobHandler */
  public AsyncJobHandlerSession(Layer layer) {
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
      job = null;
	  buffer = new ArrayDeque<>(); 
	  
	  /*buffer.add("a");
	  buffer.add("e");
	  buffer.add("i");
	  System.out.println("Tam: "+buffer.size());*/
      init.go();
    } catch (AppiaEventException e) {
      e.printStackTrace();
    }
  }

  private void handleSubmit(SubmitEvent request) {
	  try {

        buffer.add(request.getJob());

        ConfirmEvent ack = new ConfirmEvent();
        ack.setChannel(request.getChannel());

		ack.setDir(Direction.UP);
		ack.setSourceSession(this);
		ack.setId(request.getJh());

		ack.init();
		ack.go();

		handleConfirm(ack);

        while(!buffer.isEmpty()) {
			//Retrieves head from Queue
			job=buffer.element();

			SubmitEvent request2 = new SubmitEvent();
			request2.setJh(request.getJh());
			request2.setJob(job);

		    request2.setChannel(request.getChannel());
		    request2.setDir(Direction.DOWN);
		    request2.setSourceSession(this);
		    request2.init();
		    request2.go();

			buffer.remove();
        }
      } catch (AppiaEventException e) {
        e.printStackTrace();
      }
      //Propriedade garante o processamento.
      /*Resultado da chamada deve passar pela camada e ir direto para camada superior (s/ implement.)*/
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