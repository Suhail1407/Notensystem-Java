/**
 * Die Klasse Schueler bildet einen einzelnen Schüler im Notensystem ab.
 * Jeder Schüler besitzt einen Namen und ein Alter, die beim Erstellen
 * des Objekts über den Konstruktor gesetzt werden.
 */
public class Schueler {

    // -------------------------
    // Attribute eines Schülers
    // -------------------------

    /**
     * Der Name des Schülers.
     * Dieses Attribut ist private, damit es nicht direkt von außen verändert werden kann.
     */
    private String name;

    /**
     * Das Alter des Schülers in Jahren.
     * Auch dieses Attribut ist private, um die Daten zu schützen.
     */
    private int alter;


    // -------------------------
    // Konstruktor
    // -------------------------

    /**
     * Konstruktor zum Erstellen eines neuen Schueler-Objekts.
     * Beim Erzeugen müssen Name und Alter zwingend angegeben werden.
     *
     * @param name  Der Name des Schülers
     * @param alter Das Alter des Schülers
     */
    public Schueler(String name, int alter) {
        this.name = name;
        this.alter = alter;
    }


    // -------------------------
    // Getter-Methoden
    // -------------------------

    /**
     * Liefert den Namen des Schülers zurück.
     *
     * @return Name des Schülers
     */
    public String getName() {
        return name;
    }

    /**
     * Liefert das Alter des Schülers zurück.
     *
     * @return Alter des Schülers
     */
    public int getAlter() {
        return alter;
    }


    // -------------------------
    // Ausgabe-Methode
    // -------------------------

    /**
     * Überschreibt die Standard-Ausgabe eines Objekts
     * und liefert eine gut lesbare Darstellung eines Schülers.
     *
     * Beispielausgabe:
     * "Ali (16 Jahre)"
     *
     * @return formatierter Text zum Schüler
     */
    @Override
    public String toString() {
        return name + " (" + alter + " Jahre)";
    }
}
