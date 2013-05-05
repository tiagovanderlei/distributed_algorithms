package ufscar.distrib.algorith.exercises.ex07;

import irdp.protocols.tutorialDA.utils.ProcessSet;

import java.net.InetSocketAddress;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Channel;
import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.channel.ChannelClose;
import net.sf.appia.core.events.channel.ChannelInit;
import net.sf.appia.protocols.common.RegisterSocketEvent;

/**
 * 
 * @author nuno
 * @author tiago
 */
public class SampleApplSession extends Session {

	private Channel channel;
	private ProcessSet processes;
	private SampleApplReader reader;

	/**
	 * @param layer
	 */
	public SampleApplSession(Layer layer) {
		super(layer);
	}

	/**
	 * @param processes
	 */
	public void init(ProcessSet processes) {
		this.processes = processes;
	}

	/**
	 * @param event
	 */
	public void handle(Event event) {
		if (event instanceof P2PSendableEvent)
			handleP2PSendableEvent((P2PSendableEvent) event);
		else if (event instanceof ChannelInit)
			handleChannelInit((ChannelInit) event);
		else if (event instanceof ChannelClose)
			handleChannelClose((ChannelClose) event);
		else if (event instanceof RegisterSocketEvent)
			handleRegisterSocket((RegisterSocketEvent) event);
	}

	/**
	 * @param event
	 */
	private void handleRegisterSocket(RegisterSocketEvent event) {
		if (event.error) {
			System.out.println("Address already in use!");
			System.exit(2);
		}
	}

	/**
	 * @param init
	 */
	private void handleChannelInit(ChannelInit init) {
		try {
			init.go();
		} catch (AppiaEventException e) {
			e.printStackTrace();
		}
		channel = init.getChannel();

		try {
			// sends this event to open a socket in the layer that is used has
			// perfect
			// point to point
			// channels or unreliable point to point channels.
			RegisterSocketEvent rse = new RegisterSocketEvent(channel,
					Direction.DOWN, this);
			rse.port = ((InetSocketAddress) processes.getSelfProcess()
					.getSocketAddress()).getPort();
			rse.localHost = ((InetSocketAddress) processes.getSelfProcess()
					.getSocketAddress()).getAddress();
			rse.go();

			ProcessInitEvent processInit = new ProcessInitEvent(channel,
					Direction.DOWN, this);
			processInit.setProcessSet(processes);
			processInit.go();

		} catch (AppiaEventException e1) {
			e1.printStackTrace();
		}
		System.out.println("Channel is open.");
		// starts the thread that reads from the keyboard.
		reader = new SampleApplReader(this);
		reader.start();
	}

	/**
	 * @param close
	 */
	private void handleChannelClose(ChannelClose close) {
		channel = null;
		System.out.println("Channel is closed.");
	}

	/**
	 * @param event
	 */
	private void handleP2PSendableEvent(P2PSendableEvent event) {
		if (event.getDir() == Direction.DOWN)
			handleOutcomingEvent(event);
		else
			handleIncomingEvent(event);
	}

	/**
	 * @param event
	 */
	private void handleIncomingEvent(P2PSendableEvent event) {
		String message = event.getMessage().popString();
		System.out.print("Received event with message: " + message + "\n>");
	}

	/**
	 * @param event
	 */
	private void handleOutcomingEvent(P2PSendableEvent event) {
		try {
			event.go();
		} catch (AppiaEventException e) {
			e.printStackTrace();
		}
	}

	public Channel getChannel() {
		return channel;
	}

	/**
	 * @param channel
	 */
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
}