package dorespek.lenlroosterapp;

/**
 * Created by Spek on 29-12-2014.
 */
public class Dag {
    private String dag;
    private Uur[] uren;

    public Dag(String t_dag){
        dag = t_dag;
        uren = new Uur[6];
    }

    public String getDag(){
        return dag;
    }

    public void setUur(int i, String t_text, boolean t_veranderd){
        uren[i] = new Uur(t_text, t_veranderd);
    }

    public Uur getUur(int i){
        return uren[i];
    }
}
