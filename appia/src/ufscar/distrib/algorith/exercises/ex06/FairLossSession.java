/*
 *
 * Hands-On code of the book Introduction to Reliable Distributed Programming
 * by Christian Cachin, Rachid Guerraoui and Luis Rodrigues
 * Copyright (C) 2005-2011 Luis Rodrigues
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * Contact
 * 	Address:
 *		Rua Alves Redol 9, Office 605
 *		1000-029 Lisboa
 *		PORTUGAL
 * 	Email:
 * 		ler@ist.utl.pt
 * 	Web:
 *		http://homepages.gsd.inesc-id.pt/~ler/
 * 
 */

package ufscar.distrib.algorith.exercises.ex06;
import irdp.protocols.tutorialDA.utils.Debug;
import irdp.protocols.tutorialDA.utils.MessageID;
import irdp.protocols.tutorialDA.utils.ProcessSet;
import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.events.channel.ChannelInit;

/**
 * Session implementing the Eager Probabilistic Broadcast protocol.
 * 
 * @author nuno
 */
public class FairLossSession extends Session {

  // set of processes
  private ProcessSet processes;
  private int seqNumber;
  
  /**
   * @param layer
   */
  public FairLossSession(Layer layer) {
    super(layer);
    seqNumber = 0;
  }

  public void handle(Event event) {
    // Init events. Channel Init is from Appia and ProcessInitEvent is to know
    // the elements of the group
    if (event instanceof ChannelInit)
      handleChannelInit((ChannelInit) event);
    else if (event instanceof ProcessInitEvent)
      handleProcessInitEvent((ProcessInitEvent) event);
    else if (event instanceof P2PSendableEvent) {
      if (event.getDir() == Direction.DOWN)
        // UPON event from the above protocol (or application)
        requestSend((P2PSendableEvent) event);
      else
        // UPON event from the bottom protocol (or perfect point2point links)
        up2pDeliver((P2PSendableEvent) event);
    }
  }

  /**
   * @painitram init
   */
  private void handleChannelInit(ChannelInit init) {
    try {
      init.go();
    } catch (AppiaEventException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * @param event
   */
  

  
  private void handleProcessInitEvent(ProcessInitEvent event) {
    processes = event.getProcessSet();
    try {
      event.go();
    } catch (AppiaEventException e) {
      e.printStackTrace();
    }
  }
  /**
   * Requests up2pSend to send the message to udp simple layer
   * (SendableEvent) 
   * @param event
   */
  private void requestSend(P2PSendableEvent event) {

    MessageID msgID = new MessageID(processes.getSelfRank(), seqNumber);
    seqNumber++;

    SendableEvent clone = null;
    try {
      clone = (SendableEvent) event.cloneEvent();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    clone.getMessage().pushObject(msgID);

    try{
    	//Envia a mensagem para o recipient solicitado
        up2pSend(clone, ((P2PSendableEvent) event).getRecipient());
    }catch (Exception e) {
		e.printStackTrace();
	}
  }

  /**
   * Receives the message from the udp simple layer 
   * 
   * @param event
   */

  private void up2pDeliver(SendableEvent event) {
    // and the remaining header
    MessageID msgID = (MessageID) event.getMessage().popObject();
    // if the message was not delivered yet

	Debug.print("FL: mensagem recebida " + msgID);
    //System.out.println("FL: mensagem recebida " + msgID);

	SendableEvent clone = null;
	try {
		clone = (SendableEvent) event.cloneEvent();
	} catch (CloneNotSupportedException e) {
		e.printStackTrace();
	}
	// delivers the message
	requestDeliver(clone, msgID);
  }

  /**
   * Called by this protocol to send a message to the lower protocol.
   * 
   * @param event
   * @param dest
   */
  private void up2pSend(SendableEvent event, int dest) {    
	Debug.print("FL: enviando mensagem ao destino up2p " + dest + "      .");

    try {
      //Monta evento P2PSendable c/ o recipient
      P2PSendableEvent ev = (P2PSendableEvent)event;
      ev.setRecipient(dest);
      event.setDir(Direction.DOWN);
      event.setSourceSession(this);
      event.dest = processes.getProcess(dest).getSocketAddress();
      event.init();
      event.go();
    } catch (AppiaEventException e) {
      e.printStackTrace();
    }
  }
  /**
   * Delivers the message to above protocol or application
   * 
   * @param event
   * @param msgID
   */
  private void requestDeliver(SendableEvent event, MessageID msgID) {
    Debug.print("FL: entregando mensagem ao protocolo de cima.");
    System.out.println("Received message"+msgID.seqNumber+" from sender(process) "+msgID.process);
    try {
      event.setDir(Direction.UP);
      event.setSourceSession(this);
      event.source = processes.getProcess(msgID.process).getSocketAddress();
      event.init();
      event.go();
    } catch (AppiaEventException e) {
      e.printStackTrace();
    }
  }
}
