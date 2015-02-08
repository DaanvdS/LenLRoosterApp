package dorespek.lenlroosterapp;

/**
 * Created by Spek on 29-12-2014.
 */
public class Uur {
    private String text;
    private boolean veranderd;

    public Uur(String t_text, boolean t_veranderd){
        text = t_text;

        veranderd = t_veranderd;
    }

    public String getText(){
        if(text.equals("ZZZ zzz Z001")){ return ""; } else { return text; }
    }

    public boolean getVeranderd(){
        return veranderd;
    }
}
