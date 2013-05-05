package ufscar.distrib.algorith.exercises.ex03;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Event;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.channel.ChannelInit;

/**
 * Session implementing the JobHandler protocol.
 * 
 * Receives Job Handler's transformation requests and prints them on screen.
 * 
 * @author tiagovanderlei
 */
public class JobHandlerSession extends Session {

  public JobHandlerSession(Layer layer) {
    super(layer);
  }

  public void handle(Event event) {
    if (event instanceof ChannelInit)
      handleChannelInit((ChannelInit) event);
    else if (event instanceof SubmitEvent) {
      handleSubmit((SubmitEvent) event);
    }
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

      System.out.println();
      System.out.println("[Job] " + request.getJob().toUpperCase());
      request.go();
      
      /*
      
      ConfirmEvent ack = new ConfirmEvent();
      
      ack.setChannel(request.getChannel()); // set the Appia channel where the
                                            // event will travel
      ack.setDir(Direction.UP); // set events direction
      ack.setSourceSession(this); // set the session that created the event
      ack.setId(request.getJh()); // set the request ID
      // initializes the event, defining all internal structures,
      // for instance the path the event will take (sessions to visit)
      ack.init();
      ack.go(); // send the event
      
      */
    } catch (AppiaEventException e) {
      e.printStackTrace();
    }
  }
}