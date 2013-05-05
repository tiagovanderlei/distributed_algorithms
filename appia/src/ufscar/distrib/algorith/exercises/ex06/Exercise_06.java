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

import irdp.protocols.tutorialDA.utils.ProcessSet;
import irdp.protocols.tutorialDA.utils.SampleProcess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.StringTokenizer;

import net.sf.appia.core.Appia;
import net.sf.appia.core.AppiaCursorException;
import net.sf.appia.core.AppiaDuplicatedSessionsException;
import net.sf.appia.core.AppiaInvalidQoSException;
import net.sf.appia.core.Channel;
import net.sf.appia.core.ChannelCursor;
import net.sf.appia.core.Layer;
import net.sf.appia.core.QoS;
import net.sf.appia.protocols.udpsimple.UdpSimpleLayer;

/**import irdp.protocols.tutorialDA.sampleAppl.SampleApplLayer;
import irdp.protocols.tutorialDA.sampleAppl.SampleApplSession;
 * This class is the MAIN class to run the Reliable Broadcast protocols.
 * 
 * @author nuno
 */
public class Exercise_06 {

  /**
   * Builds the Process set, using the information in the specified file.
   * 
   * @param filename
   *          the location of the file
   * @param selfProc
   *          the number of the self process
   * @return a new ProcessSet
   */
  private static ProcessSet buildProcessSet(String filename, int selfProc) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(
          filename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(0);
    }
    String line;
    StringTokenizer st;
    boolean hasMoreLines = true;
    ProcessSet set = new ProcessSet();
    // reads lines of type: <process number> <IP address> <port>
    while(hasMoreLines) {
      try {
        line = reader.readLine();
        if (line == null)
          break;
        st = new StringTokenizer(line);
        if (st.countTokens() != 3) {
          System.err.println("Wrong line in file: "+st.countTokens());
          continue;
        }
        int procNumber = Integer.parseInt(st.nextToken());
        InetAddress addr = InetAddress.getByName(st.nextToken());
        int portNumber = Integer.parseInt(st.nextToken());
        boolean self = (procNumber == selfProc);
        SampleProcess process = new SampleProcess(new InetSocketAddress(addr,
            portNumber), procNumber, self);
        set.addProcess(process, procNumber);
      } catch (IOException e) {
        hasMoreLines = false;
      } catch (NumberFormatException e) {
        System.err.println(e.getMessage());
      }
    } // end of while
    return set;
  }

  /**
   * Builds a new Channel with Point-to-point links.
   * 
   * @param processes
   *          set of processes
   * @param link
   *          link abstraction used: fairloss
   * @return a new uninitialized Channel
   */
  private static Channel getChannel(ProcessSet processes){

	  	/* Create layers and put them on a array */
	  	Layer[] qos = {new UdpSimpleLayer(), new FairLossLayer(), new SampleApplLayer()}; 

	    /* Create a QoS */
	    QoS myQoS = null;
	    try {
	      myQoS = new QoS("Fair Loss QoS", qos);
	    } catch (AppiaInvalidQoSException ex) {
	      System.err.println("Invalid QoS");
	      System.err.println(ex.getMessage());
	      System.exit(1);
	    }
	    /* Create a channel. Uses default event scheduler. */
	    Channel channel = myQoS
	        .createUnboundChannel("Fair Loss Channel");
	    /*
	     * Application Session requires special arguments: filename and . A session
	     * is created and binded to the stack. Remaining ones are created by default
	     */
	    SampleApplSession sas = (SampleApplSession) qos[qos.length - 1]
	    		.createSession();
	    sas.init(processes);
	    ChannelCursor cc = channel.getCursor();
	    /*
	     * Application is the last session of the array. Positioning in it is simple
	     */
	    try {
	      cc.top();
	      cc.setSession(sas);
	    } catch (AppiaCursorException ex) {
	      System.err.println("Unexpected exception in main. Type code:" + ex.type);
	      System.exit(1);
	    }
	    return channel;
  }

  public static void main(String[] args) {

	BufferedReader keyb = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
	int id_proc = 0;

	System.out.print("Id do processo:\n");
    try {
    	id_proc = Integer.parseInt(keyb.readLine());
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}

	String[] param = {"-f", "../rede/config_rede", "-n", String.valueOf(id_proc), "-qos", "fairloss"};
	args = param;

    int arg = 0, self = -1;

    String filename = null, qos = null;

    try {
      while (arg < args.length) {

        if (args[arg].equals("-f")) {
          arg++;
          filename = args[arg];
          System.out.println("Reading from file: " + filename);
        } else if (args[arg].equals("-n")) {
          arg++;
          try {
            self = Integer.parseInt(args[arg]);
            System.out.println("Process number: " + self);
          } catch (NumberFormatException e) {
            e.printStackTrace();
          }
        } else if (args[arg].equals("-qos")) {
          arg++;
          qos = args[arg];
          if (qos.equals("pb")) {
            qos = qos + " " + args[++arg] + " " + args[++arg];
          }
          System.out.println("Starting with QoS: " + qos);
        }
        arg++;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      e.printStackTrace();
    }

    /*
     * gets a new uninitialized Channel with the specified QoS and the Applfilename
     * session created. Remaining sessions are created by default. Just tell the
     * channel to start.
     */
    Channel channel = getChannel(buildProcessSet(filename, self));
    try {
      channel.start();
    } catch (AppiaDuplicatedSessionsException ex) {
      System.err.println("Sessions binding strangely resulted in "
          + "one single sessions occurring more than " + "once in a channel");
      System.exit(1);
    }

    /* All set. Appia main class will handle the rest */
    System.out.println("Starting Appia...");
    Appia.run();
  }
}