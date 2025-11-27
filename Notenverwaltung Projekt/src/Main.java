public class Main {

    public static void main(String[] args) {
        // --- 1. Systeminitialisierung ---
        System.out.println("--- Notensystem Initialisierung ---");

        // Erstellung des Schülers (Zuständigkeit Person 1)
        Schueler schueler = new Schueler("Max Mustermann", 17);
        System.out.println("Schüler erstellt: " + schueler.toString());

        // Erstellung der Fächer (Zuständigkeit Person 1)
        Fach mathe = new Fach("Mathematik");
        Fach deutsch = new Fach("Deutsch");
        Fach englisch = new Fach("Englisch");
        System.out.println("Fächer erstellt: " + mathe + ", " + deutsch + ", " + englisch);

        // Erstellung der Notenverwaltung (Zuständigkeit Person 3)
        NotenVerwaltung verwaltung = new NotenVerwaltung();
        System.out.println("Notenverwaltung gestartet.");

        // --- 2. Noteneingabe und -speicherung ---
        System.out.println("\n--- Noten hinzufügen ---");

        // Erstellung der Noten und Hinzufügen zur Verwaltung (Note-Objekt: Zuständigkeit Person 2)
        
        // Mathematik-Noten
        Note matheNote1 = new Note(mathe, 2.0); // Note-Objekt
        verwaltung.noteHinzufuegen(matheNote1); // Hinzufügen in Verwaltung
        
        Note matheNote2 = new Note(mathe, 1.3);
        verwaltung.noteHinzufuegen(matheNote2);

        // Deutsch-Note
        Note deutschNote = new Note(deutsch, 2.7);
        verwaltung.noteHinzufuegen(deutschNote);

        // Englisch-Note
        Note englischNote = new Note(englisch, 1.0);
        verwaltung.noteHinzufuegen(englischNote);


        // --- 3. Ergebnisausgabe ---
        System.out.println("\n--- Berechnung der Ergebnisse ---");

        // Gesamtdurchschnitt berechnen (Methode aus Person 3's Klasse)
        double gesamtDurchschnitt = verwaltung.durchschnittBerechnen();
        
        // Ausgabe des Ergebnisses
        System.out.printf("Gesamtdurchschnitt von %s: %.2f\n", schueler.getName(), gesamtDurchschnitt);
        
        // Ausgabe aller gespeicherten Noten
        System.out.println("\nAlle gespeicherten Noten:");
        for (Note note : verwaltung.getNoten()) {
            // Wir nutzen die getFach().toString() Methode, um den Fachnamen zu erhalten,
            // und getWert() für den Notenwert.
            System.out.println("  - Fach: " + note.getFach() + ", Wert: " + note.getWert());
        }

    }
}
