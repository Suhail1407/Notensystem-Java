// Platzhalter für Person 1
public class Schueler {
    
    // Attribute
    private String name;
    private int alter;

    // Konstruktor
    public Schueler(String name, int alter) {
        this.name = name;
        this.alter = alter;
    }

    // Getter-Methoden
    public String getName() {
        return this.name;
    }

    public int getAlter() {
        return this.alter;
    }
}