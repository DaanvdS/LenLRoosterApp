package dorespek.lenlroosterapp;

/**
 * Created by Spek on 29-12-2014.
 */
public class Uur {
    private String docent;
    private String lokaal;
    private String les;
    private boolean veranderd;

    public Uur(String t_docent, String t_lokaal, String t_les, boolean t_veranderd){
        docent = t_docent;
        lokaal = t_lokaal;
        les = t_les;
        veranderd = t_veranderd;
    }

    public String getDocent(){
        if(!docent.equals("ZZZ")){ return docent; } else { return ""; }
    }

    public String getLokaal(){
        if(!lokaal.equals("Z001")){ return lokaal; } else { return ""; }
    }

    public String getLes(){
        if(!les.equals("zzz")){ return les; } else { return ""; }
    }

    public String getUur(){
        if(!les.equals("zzz")){ return docent + " " + les + " " + lokaal; } else { return ""; }
    }

    public boolean getVeranderd(){
        return veranderd;
    }
}
