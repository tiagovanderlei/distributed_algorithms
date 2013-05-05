package ufscar.distrib.algorith.exercises.ex07;

import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.channel.ChannelInit;

public class ClockLayer extends Layer {

	  public ClockLayer() {
	    /* events that the protocol will create */
	    evProvide = new Class[1];
	    evProvide[0] = RetransmitEvent.class;
	    /*
	     * events that the protocol require to work. This is a subset of the
	     * accepted events
	     */
	    evRequire = new Class[0];

	    /* events that the protocol will accept */
	    evAccept = new Class[1];
	    evAccept[0] = ChannelInit.class;
	  }

	  /**
	   * Creates a new session to this protocol.
	   * 
	   * @see appia.Layer#createSession()
	   */
	  public Session createSession() {
	    return new ClockSession(this);
	  }
}