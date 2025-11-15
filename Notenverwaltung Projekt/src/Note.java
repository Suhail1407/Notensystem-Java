// Platzhalter für Person 2
public class Note {

    // Attribute
    private Fach fach;
    private double wert;

    // Konstruktor
    public Note(Fach fach, double wert) {
        this.fach = fach;
        this.wert = wert;
    }

    // Getter-Methoden
    public Fach getFach() {
        return this.fach;
    }

    public double getWert() {
        return this.wert;
    }
}