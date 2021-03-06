package dorespek.lenlroosterapp;

import android.util.Log;

/**
 * Created by Spek on 29-12-2014.
 */
public class Rooster {
    private String type;
    private Dag[] dagen;

    public Rooster(String t_type){
        dagen = new Dag[5];
        type = t_type;
    }

    public void setDagen(String[] w_rooster, String[] j_rooster){
        dagen[0]= new Dag("Maandag");
        dagen[1]= new Dag("Dinsdag");
        dagen[2]= new Dag("Woensdag");
        dagen[3]= new Dag("Donderdag");
        dagen[4]= new Dag("Vrijdag");

        int dag=0;
        int plek=0;
        int uur=0;

        while(uur < 6){
            while(dag < 5) {

                dagen[dag].setUur(uur, w_rooster[(plek)], isVeranderd(w_rooster[(plek)], j_rooster[(plek)], plek));
                //Log.d("w", w_rooster[(plek)]);
                //Log.d("j", j_rooster[(plek+1)]);
                dag++;
                plek++;
            }
            dag=0;
            uur++;
        }
    }

    public Dag getDag(int i){
        return dagen[i];
    }

    public boolean isVeranderd(String wtext, String jtext, int plek){
        if(!(plek==29)) {
            if (wtext.equals(jtext)) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
