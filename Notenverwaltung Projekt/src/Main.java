import javax.swing.SwingUtilities; // WICHTIG: Wird für den GUI-Start benötigt

public class Main {

    public static void main(String[] args) {
        
        // --- 1. Systeminitialisierung und Datenanlage ---
        System.out.println("--- Notensystem Initialisierung ---");

        // Erstellung der Objekte 
        Schueler schueler = new Schueler("Max Mustermann", 17);
        Fach mathe = new Fach("Mathematik");
        Fach deutsch = new Fach("Deutsch");
        Fach englisch = new Fach("Englisch"); // Fächer aus dem Ursprungscode 
        
        NotenVerwaltung verwaltung = new NotenVerwaltung();
        System.out.println("Notenverwaltung gestartet.");
        
        // Füge Start-Noten hinzu
        verwaltung.noteHinzufuegen(new Note(mathe, 2.0));
        verwaltung.noteHinzufuegen(new Note(mathe, 1.3));
        verwaltung.noteHinzufuegen(new Note(deutsch, 2.7));
        verwaltung.noteHinzufuegen(new Note(englisch, 1.0));

        // --- 2. Konsolenausgabe der Berechnung ---
        System.out.println("\n--- Berechnung der Ergebnisse ---");
        double gesamtDurchschnitt = verwaltung.durchschnittBerechnen();
        System.out.printf("Gesamtdurchschnitt von %s: %.2f\n", schueler.getName(), gesamtDurchschnitt);
        // ... (Weitere Konsolenausgaben) ...


        // --- 3. Start der Grafischen Oberfläche (GUI) ---
        System.out.println("\n--- NotenGUI wird gestartet... ---"); // <-- DIESE ZEILE MUSS JETZT ERSCHEINEN

        // **Wichtig:** Startet die GUI im Event Dispatch Thread (EDT) 
        // und macht das Fenster sichtbar.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NotenGUI(verwaltung);
            }
        });
        
        // ACHTUNG: Das Programm beendet sich jetzt nicht mehr sofort.
        // Es läuft weiter und wartet auf die Interaktion mit der GUI.
    }
}
