package ufscar.distrib.algorith.exercises.ex03;

import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.channel.ChannelInit;

/**
 * Layer of the Sync Job Handler Protocol.
 * 
 * @author tiagovanderlei
 */
public class SyncJobHandlerLayer extends Layer {

  public SyncJobHandlerLayer() {
    /* events that the protocol will create */
    evProvide = new Class[2];
    evProvide[0] = ConfirmEvent.class;
    evProvide[1] = JobStatusEvent.class;
    //evProvide[1] = PrintAlarmEvent.class;

    /*
     * events that the protocol requires to work. This is a subset of the
     * accepted events.
     */
    evRequire = new Class[0];
    //evRequire[0] = ConfirmEvent.class;

    /* events that the protocol will accept */
    evAccept = new Class[2];
    evAccept[0] = SubmitEvent.class;
    //evAccept[1] = ConfirmEvent.class;
    evAccept[1] = ChannelInit.class;
  }

  public Session createSession() {
    return new SyncJobHandlerSession(this);
  }
}
