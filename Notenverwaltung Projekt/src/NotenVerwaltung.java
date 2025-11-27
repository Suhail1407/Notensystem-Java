import java.util.ArrayList;
import java.util.List;

public class NotenVerwaltung {

    // 1. ArrayList<Note> notenListe
    private ArrayList<Note> notenListe;

    /**
     * Konstruktor zur Initialisierung der ArrayList.
     */
    public NotenVerwaltung() {
        this.notenListe = new ArrayList<>();
    }

    // 2. Methode: noteHinzufuegen(Note note)
    /**
     * Fügt ein Note-Objekt zur internen Liste hinzu.
     * @param note Das hinzuzufügende Note-Objekt.
     */
    public void noteHinzufuegen(Note note) {
        if (note != null) {
            this.notenListe.add(note);
            System.out.println("Note " + note.getWert() + " (" + note.getFach() + ") wurde hinzugefügt.");
        }
    }

    // 3. Methode: durchschnittBerechnen()
    /**
     * Berechnet den arithmetischen Durchschnitt aller gespeicherten Noten.
     * @return Der berechnete Durchschnitt als double. 0.0, wenn keine Noten vorhanden sind.
     */
    public double durchschnittBerechnen() {
        if (notenListe.isEmpty()) {
            return 0.0; // Vermeidet Division durch Null
        }

        double summe = 0.0;
        for (Note note : notenListe) {
            summe += note.getWert();
        }

        return summe / notenListe.size();
    }

    // 4. Methode: getNoten()
    /**
     * Gibt die Liste aller gespeicherten Noten zurück.
     * Hier verwenden wir List<Note> als Rückgabetyp, was allgemeiner ist
     * und eine bessere Praxis darstellt (Programmier gegen Interfaces).
     * @return Eine (unveränderliche) Liste der Note-Objekte.
     */
    public List<Note> getNoten() {
        // Wir geben eine Kopie der Liste oder besser ein Read-Only-View zurück,
        // um die interne Liste vor unbeabsichtigten Änderungen von außen zu schützen.
        // Mit List.copyOf() (ab Java 10) oder Collections.unmodifiableList()
        // Wenn man die ArrayList direkt zurückgibt, könnte externer Code die Liste verändern,
        // was die Klasse NotenVerwaltung "kaputt" machen würde.
        // Fürs Studium ist aber oft auch die direkte Rückgabe der ArrayList akzeptabel:
        
        // return this.notenListe; // Alternative, weniger sichere Variante

        return List.copyOf(this.notenListe); // Sicherere Variante (Java 9+)
    }
}

