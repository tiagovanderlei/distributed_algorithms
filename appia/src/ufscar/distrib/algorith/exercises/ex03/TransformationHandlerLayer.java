package ufscar.distrib.algorith.exercises.ex03;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.channel.ChannelInit;

/**
 * Layer of the Async Job Handler.
 * 
 * @author tiagovanderlei
 */
public class TransformationHandlerLayer extends Layer {

  public TransformationHandlerLayer() {
    /* events that the protocol will create */
    evProvide = new Class[3];
    evProvide[0] = ConfirmEvent.class;
    evProvide[1] = JobStatusEvent.class;
    evProvide[2] = JobErrorEvent.class;

    /*
     * events that the protocol requires to work. This is a subset of the
     * accepted events.
     */
    evRequire = new Class[1];
    evRequire[0] = ConfirmEvent.class;

    /* events that the protocol will accept */
    evAccept = new Class[2];
    evAccept[0] = SubmitEvent.class;
    //evAccept[1] = ConfirmEvent.class; // ConfirmEvent é gerado internamente ao empilhar, 
    									// sem ter recebido retorno da camada inferior.
    evAccept[1] = ChannelInit.class;
  }

  public Session createSession() {
    return new AsyncJobHandlerSession(this);
  }
}
