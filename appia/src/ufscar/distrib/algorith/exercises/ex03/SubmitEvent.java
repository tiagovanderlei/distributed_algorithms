package ufscar.distrib.algorith.exercises.ex03;
import net.sf.appia.core.Event;
/**
 * JobHandler request event.
 * 
 * @author tiagovanderlei
 */
public class SubmitEvent extends Event {
  int jh;
  String job;

  void setJh(int jh) {
    this.jh = jh;
  }

  void setJob(String job) {
	  this.job = job;
  }

  int getJh() {
    return jh;
  }

  String getJob() {
    return job;
  }
}