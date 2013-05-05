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

package ufscar.distrib.algorith.exercises.ex12;
import irdp.protocols.tutorialDA.utils.Debug;
import irdp.protocols.tutorialDA.utils.MessageID;
import irdp.protocols.tutorialDA.utils.ProcessSet;
import irdp.protocols.tutorialDA.utils.SampleProcess;

import java.util.ArrayList;
import java.util.List;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Channel;
import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.events.channel.ChannelInit;
import net.sf.appia.core.message.Message;

/**
 * Session implementing the Eager Probabilistic Broadcast protocol.
 * 
 * @author nuno
 */
public class FairLossSession extends Session {

  // set of processes
  private ProcessSet processes;
  private int seqNumber;
  private EpochUtil epochUtil;
  private List<Candidate> candidates;
  private int leader = 0;
  private Channel channel;
  private int epoch;
  private int delta = 5; //incremento delay (segundos)
  private int delay = 0;
  
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
    }else if(event instanceof LowerepochTimeoutEvent)
    	handleTimeoutLowerEpoch((LowerepochTimeoutEvent) event);
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
    epochUtil = new EpochUtil();
    candidates = new ArrayList<Candidate>();

    channel = event.getChannel();
    
    /*Recuperação*/
	if(epochUtil.existsEpoch(processes.getSelfProcess().getProcessNumber())){
		recovery_epoch();
	}else{ // Ĩnicialização 
		epochUtil.createEpoch(processes.getSelfProcess().getProcessNumber(), 0);
		epoch = 0;
		
		recovery_epoch();
	}

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
    
    String msg = event.getMessage().popString();

    // if the message was not delivered yet
	Debug.print("FL: mensagem recebida " + msgID);

	SendableEvent clone = null;
	try {
		clone = (SendableEvent) event.cloneEvent();
	} catch (CloneNotSupportedException e) {
		e.printStackTrace();
	}
    
    if(!msg.equals("HEARTBEAT")){

		// delivers the message
		requestDeliver(clone, msgID, msg);
    }else{
    	int epoch =  event.getMessage().popInt();
    	heartbeatDeliver(clone, msgID, epoch);
    }
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
  private void requestDeliver(SendableEvent event, MessageID msgID, String msg) {
    Debug.print("FL: entregando mensagem ao protocolo de cima.");
    System.out.println("Received message "+msgID.seqNumber+" from sender(process) "+msgID.process);
    
    try {
      Message message = event.getMessage();
      message.pushString(msg);//recoloca msg na pilha para camada superior acessar
      event.setDir(Direction.UP);
      event.setSourceSession(this);
      event.source = processes.getProcess(msgID.process).getSocketAddress();
      event.init();
      event.go();
    } catch (AppiaEventException e) {
      e.printStackTrace();
    }
  }

  private void recovery_epoch(){
	  SampleProcess leader;
	  leader=processes.getProcess(epochUtil.maxrank(processes.getAllProcesses()));
	  trust(leader);
	  
	  delay = delta;
	  
	  //seta delay no timer por evento UP 	  
	  epoch = epochUtil.increaseEpoch(processes.getSelfProcess().getProcessNumber()); //retrieve, increment, update epoch -> retorna nova epoch
	  sendHeartBeatToAll(epoch); //send heart beat and the new epoch to all

	  candidates = new ArrayList<Candidate>();
	  candidates.add(new Candidate(processes.getSelfProcess().getProcessNumber(), epoch));
	  
	  startTimer(delay);
  }

  private void sendHeartBeatToAll(int epoch){
	  
	  SampleProcess[] proc = processes.getAllProcesses();

	  P2PSendableEvent evt=null;
	  for(int i=0;i<proc.length;i++){
		  //Envia para todos exceto ele mesmo
		  if(proc[i].getProcessNumber() != processes.getSelfProcess().getProcessNumber()){
			  evt = new HeartbeatEvent();
			  evt.setChannel(channel);
			  evt.setSourceSession(this);
			  evt.setRecipient(proc[i].getProcessNumber());
	
			  System.out.println("Heartbeat para recipient: "+evt.getRecipient());
	
		      Message message = evt.getMessage();
		      message.pushInt(epoch);
	
		      String msg = "HEARTBEAT";
		      message.pushString(msg);
	
			  requestSend(evt);
		  }else{
			  candidates.add(new Candidate(processes.getSelfProcess().getProcessNumber(), epoch));
		  }
	  }
  }

  private void heartbeatDeliver(SendableEvent clone, MessageID msgID, int epoch){
	  System.out.println("Processo "+msgID.process+" está vivo com epoch "+epoch+"!!");
	  for(Candidate candidate:candidates){
		  if (candidate.getProcessID() == msgID.process && candidate.getEpoch() < epoch){
			  candidates.remove(candidate);
		  }
	  }
	  candidates.add(new Candidate(msgID.process, epoch));
  }

  public void handleTimeoutLowerEpoch(LowerepochTimeoutEvent event){
	  
	  List<Candidate> selected = new ArrayList<Candidate>();
	  int newleader = 0;
	  
	  //if(candidates.size() > 0){
		  candidates.add(new Candidate(processes.getSelfProcess().getProcessNumber(), epoch)); //adiciona processo atual na lista de prossibildades.
		  int lower_epoch = candidates.get(0).getEpoch();

		  for(Candidate cand:candidates){
			  if(cand.getEpoch() < lower_epoch){
				  lower_epoch = cand.getEpoch();
				  selected = new ArrayList<Candidate>();
				  selected.add(cand);
			  }else
				  if(cand.getEpoch() == lower_epoch){
					  selected.add(cand);
				  }
		  }
		  
		  newleader = selected.get(0).getProcessID();
		  
		  for (Candidate cand:selected)
			  if(cand.getProcessID() > newleader)
				  newleader = cand.getProcessID();
				  
		  if(newleader != leader){
			  
			  //aumenta delay
			  delay += delta;
			  
			  leader = newleader; 
			  trust(processes.getProcess(leader));
		  }
		  sendHeartBeatToAll(epoch);
		  candidates = new ArrayList<Candidate>();
		  
		  //atualiza starttimer com novo delay
		  startTimer(delay);
	  //}
  }

  private void startTimer(int delay){
	  //System.out.println("Send start timer delay: "+delay);
	  StartTimerEvent evt = new StartTimerEvent();
	  evt.setNovoDelay(delay);
	  evt.setDir(Direction.UP);
	  evt.setChannel(channel);
	  evt.setSourceSession(this);
	  
	  try {
		  evt.init();
		  evt.go();
	  } catch (AppiaEventException e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
	  }
  }
  
  
  private void trust(SampleProcess process){
	  System.out.println("Líder: "+process.getProcessNumber());
  }
}
