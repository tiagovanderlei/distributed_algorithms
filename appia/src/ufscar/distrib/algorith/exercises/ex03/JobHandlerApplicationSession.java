package ufscar.distrib.algorith.exercises.ex03;

import net.sf.appia.core.*;
import net.sf.appia.core.events.channel.ChannelInit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Session implementing the Sync Job Handler protocol.
 * <br>
 * Reads strings, requests their printing and displays 
 * confirmations.
 * 
 * @author tiagovanderlei
 */
public class JobHandlerApplicationSession extends Session {

  public JobHandlerApplicationSession(Layer layer) {
    super(layer);
  }

  public void handle(Event event) {
    System.out.println();

    if (event instanceof ChannelInit)
      handleChannelInit((ChannelInit) event);
    else if (event instanceof ConfirmEvent)
      handleJobConfirm((ConfirmEvent) event);
    else if (event instanceof JobStatusEvent)
      handleJobStatus((JobStatusEvent) event);
    /*else if (event instanceof PrintAlarmEvent)
      handlePrintAlarm((PrintAlarmEvent) event);*/
  }

  private JobReader reader = null;

  private void handleChannelInit(ChannelInit init) {
    try {
      init.go();
    } catch (AppiaEventException ex) {
      ex.printStackTrace();
    }

    if (reader == null)
      reader = new JobReader(init.getChannel());
  }

  private void handleJobConfirm(ConfirmEvent conf) {
    System.out.println("[JobApplication: received confirmation of request "
        + conf.getId() + "]");

    try {
      conf.go();
    } catch (AppiaEventException ex) {
      ex.printStackTrace();
    }
  }

  /*private void handlePrintAlarm(PrintAlarmEvent alarm) {
    System.out.println("[PrintApplication: received ALARM]");

    try {
      alarm.go();
    } catch (AppiaEventException ex) {
      ex.printStackTrace();
    }
  }*/

  private void handleJobStatus(JobStatusEvent status) {
    System.out.print("[JobApplication: received");
    System.out.print(" status "
        + (status.getStatus().equals(Status.OK) ? "OK" : "NOK"));
    System.out.println(" for request " + status.getId() + "]");

    try {
      status.go();
    } catch (AppiaEventException ex) {
      ex.printStackTrace();
    }
  }

  private class JobReader extends Thread {

    public boolean ready = false;
    public Channel channel;
    private BufferedReader stdin = new BufferedReader(new InputStreamReader(
        System.in));
    private int rid = 0;

    public JobReader(Channel channel) {
      ready = true;
      if (this.channel == null)
        this.channel = channel;
      this.start();
    }

    public void run() {
      boolean running = true;

      while (running) {
        ++rid;
        System.out.println();
        System.out.print("[JobApplication](" + rid + ")> ");
        try {
          String s = stdin.readLine();

          SubmitEvent request = new SubmitEvent();
          request.setJh(rid);
          request.setJob(s);
          request.asyncGo(channel, Direction.DOWN);
        } catch (AppiaEventException ex) {
          ex.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }

        try {
          Thread.sleep(1500);
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        synchronized (this) {
          if (!ready)
            running = false;
        }
      }
    }
  }
}
