/**
 * Die Klasse Fach repräsentiert ein einzelnes Schulfach im Notensystem.
 * Ein Fach besitzt genau einen Namen, wie z.B. "Mathe" oder "Deutsch".
 * Diese Klasse wird später in der Klasse Note verwendet, um festzulegen,
 * auf welches Fach sich eine Note bezieht.
 */
public class Fach {

    // -----------------------------------------
    // Attribut
    // -----------------------------------------

    /**
     * Name des Faches, zum Beispiel "Mathe" oder "Englisch".
     * Das Attribut ist private, um die Daten zu schützen.
     */
    private String fachName;

    // -----------------------------------------
    // Konstruktor
    // -----------------------------------------

    /**
     * Konstruktor zum Erstellen eines Fach-Objekts.
     * Beim Erstellen muss zwingend ein Fachname angegeben werden.
     *
     * @param fachName Name des Faches
     */
    public Fach(String fachName) {
        this.fachName = fachName;
    }

    // -----------------------------------------
    // Getter Methode
    // -----------------------------------------

    /**
     * Gibt den Namen des Faches zurück.
     * Andere Klassen (z.B. Note oder GUI) können so den Namen auslesen,
     * ohne dass das Attribut direkt verändert werden kann.
     *
     * @return Name des Faches
     */
    public String getFachName() {
        return fachName;
    }

    // -----------------------------------------
    // Ausgabeformat
    // -----------------------------------------

    /**
     * Überschreibt die Standard-Textausgabe eines Objekts.
     * Dadurch wird beim Ausdrucken des Fach-Objekts nur der Name zurückgegeben.
     *
     * Beispiel:
     * Fach mathe = new Fach("Mathe");
     * System.out.println(mathe); --> Ausgabe: Mathe
     *
     * @return Der Fachname als Text
     */
    @Override
    public String toString() {
        return fachName;
    }
}


