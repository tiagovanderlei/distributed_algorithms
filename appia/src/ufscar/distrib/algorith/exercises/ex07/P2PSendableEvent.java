package ufscar.distrib.algorith.exercises.ex07;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Channel;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.SendableEvent;

public class P2PSendableEvent extends SendableEvent {
	private int recipient;

	public P2PSendableEvent() {
		super();
	}

	public P2PSendableEvent(Channel c, int dir, Session s)
			throws AppiaEventException {
		super(c, dir, s);
	}

	public int getRecipient() {
		return recipient;
	}

	public void setRecipient(int recipient) {
		this.recipient = recipient;
	}
}
