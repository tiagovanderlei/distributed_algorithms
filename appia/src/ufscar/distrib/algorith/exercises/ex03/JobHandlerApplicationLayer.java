package ufscar.distrib.algorith.exercises.ex03;

import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.channel.ChannelInit;

/**
 * Layer of the Sync Job Handler Application.
 * 
 * @author tiagovanderlei
 */
public class JobHandlerApplicationLayer extends Layer {

  /** Creates a new instance of PrintApplicationLayer */
  public JobHandlerApplicationLayer() {
    /* events that the protocol will create */
    evProvide = new Class[1];
    evProvide[0] = SubmitEvent.class;

    /*
     * events that the protocol requires to work This is a subset of the
     * accepted events.
     */
    evRequire = new Class[0];

    /* events that the protocol will accept */
    evAccept = new Class[3];
    evAccept[0] = ConfirmEvent.class;
    evAccept[1] = JobStatusEvent.class;
    evAccept[2] = ChannelInit.class;
    //evAccept[3] = PrintAlarmEvent.class;
    
  }

  public Session createSession() {
    return new JobHandlerApplicationSession(this);
  }
}
