import java.util.ArrayList; 

public class NotenVerwaltung {

    private ArrayList<Note> notenListe;

    public NotenVerwaltung() {
        this.notenListe = new ArrayList<>();
    }

    public void noteHinzufuegen(Note note) {
        this.notenListe.add(note);
        System.out.println("DEBUG: Note " + note.getWert() + " hinzugefügt.");
    }

    public double durchschnittBerechnen() {
        System.out.println("DEBUG: durchschnittBerechnen() aufgerufen.");
        
        if (notenListe.isEmpty()) {
            return 0.0;
        }
        
        double summe = 0;
        for (Note note : notenListe) {
            summe += note.getWert();
        }
        return summe / notenListe.size(); 
    }

    public ArrayList<Note> getNoten() {
        return this.notenListe;
    }
}