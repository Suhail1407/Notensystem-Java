import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Die Klasse NotenGUI erstellt die grafische Benutzeroberfläche 
 * für das Notensystem in Java (Swing).
 * Sie ist für die Interaktion mit dem Benutzer zuständig.
 * Verantwortung: Person 4
 */
public class NotenGUI extends JFrame implements ActionListener {

    // -----------------------------------------
    // Attribute / Komponenten
    // -----------------------------------------

    // Abhängigkeit zur Datenverwaltung (Zuständigkeit Person 3)
    private NotenVerwaltung verwaltung;

    // Eingabefelder für neue Noten
    private JTextField fachNameField;
    private JTextField noteWertField;

    // Buttons für Aktionen
    private JButton addButton;
    private JButton averageButton;

    // Ausgabe-Bereich
    private JTextArea outputArea;


    // -----------------------------------------
    // Konstruktor
    // -----------------------------------------

    /**
     * Erstellt die GUI und speichert die Referenz zur NotenVerwaltung.
     * @param verwaltung Das zentrale NotenVerwaltung-Objekt
     */
    public NotenGUI(NotenVerwaltung verwaltung) {
        this.verwaltung = verwaltung;

        // 1. Fenster konfigurieren (JFrame)
        setTitle("Notenverwaltungssystem - Projektarbeit (Person 4)");
        setSize(500, 600);
        // Stellt sicher, dass das Programm beim Schließen des Fensters beendet wird
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setLayout(new BorderLayout(10, 10)); // Layout-Manager für das Hauptfenster

        // 2. Komponenten initialisieren
        initComponents();
        
        // 3. Komponenten anordnen
        layoutComponents();

        // 4. Fenster sichtbar machen
        setVisible(true); // <--- Dieser Aufruf macht das Fenster sichtbar!

        // Zeige initial alle Noten in der TextArea
        displayAllGrades();
    }


    // -----------------------------------------
    // Initialisierungsmethoden
    // -----------------------------------------

    private void initComponents() {
        // Eingabefelder
        fachNameField = new JTextField(15);
        noteWertField = new JTextField(5);

        // Buttons und Zuweisung des ActionListeners
        addButton = new JButton("Note Hinzufügen");
        addButton.addActionListener(this); 
        
        averageButton = new JButton("Gesamtdurchschnitt Berechnen");
        averageButton.addActionListener(this); 

        // Textarea für das Protokoll
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

    private void layoutComponents() {
        // --- Input Panel (NORTH) ---
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Note eintragen"));

        inputPanel.add(new JLabel("Fachname:"));
        inputPanel.add(fachNameField);
        inputPanel.add(new JLabel("Note (1.0-6.0):"));
        inputPanel.add(noteWertField);
        inputPanel.add(addButton);

        add(inputPanel, BorderLayout.NORTH);

        // --- Output Area (CENTER) ---
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Protokoll und Notenübersicht"));
        add(scrollPane, BorderLayout.CENTER);

        // --- Average Button (SOUTH) ---
        add(averageButton, BorderLayout.SOUTH);
    }


    // -----------------------------------------
    // ActionListener Logik
    // -----------------------------------------

    /**
     * Reagiert auf Button-Klicks und ruft die entsprechende Logik auf.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addNewGrade();
        } else if (e.getSource() == averageButton) {
            calculateAverage();
        }
    }
    
    /**
     * Verarbeitet die Eingaben und fügt eine neue Note hinzu.
     */
    private void addNewGrade() {
        String fachName = fachNameField.getText().trim();
        String wertText = noteWertField.getText().trim();
        
        if (fachName.isEmpty() || wertText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bitte Fachname und Notenwert eingeben.", "Fehlende Eingabe", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Erlaubt Komma oder Punkt als Dezimaltrennzeichen
            double wert = Double.parseDouble(wertText.replace(",", ".")); 
            
            // Einfache Noten-Validierung
            if (wert < 1.0 || wert > 6.0) {
                 JOptionPane.showMessageDialog(this, "Notenwert muss zwischen 1.0 und 6.0 liegen.", "Ungültiger Wert", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            // Objekte erstellen (Fach von Person 1, Note von Person 2)
            Fach neuesFach = new Fach(fachName);
            Note neueNote = new Note(neuesFach, wert);
            
            // Hinzufügen über die Verwaltung (Methode von Person 3)
            verwaltung.noteHinzufuegen(neueNote); // Nutzt noteHinzufuegen() [cite: 33]
            
            // GUI aktualisieren
            fachNameField.setText("");
            noteWertField.setText("");
            displayAllGrades(); // Zeigt die aktualisierte Liste an

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ungültiges Format für die Note. Bitte eine Zahl (z.B. 1.0) eingeben.", "Formatfehler", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Berechnet und zeigt den Durchschnitt aller Noten an.
     */
    private void calculateAverage() {
        // Methode von Person 3 nutzen
        double durchschnitt = verwaltung.durchschnittBerechnen(); // Nutzt durchschnittBerechnen() [cite: 33]
        
        outputArea.append("\n\n--- Durchschnittsberechnung ---\n");
        
        if (verwaltung.getNoten().isEmpty()) {
            outputArea.append("Es sind keine Noten für die Berechnung vorhanden.\n");
        } else {
             outputArea.append(String.format("Der Gesamtdurchschnitt aller %d Noten beträgt: **%.2f**\n", 
                                             verwaltung.getNoten().size(), durchschnitt));
        }
    }
    
    /**
     * Zeigt die gesamte Liste der Noten in der TextArea an.
     */
    private void displayAllGrades() {
        outputArea.setText("--- AKTUELLE NOTENLISTE ---\n");
        // Methode von Person 3 nutzen
        List<Note> noten = verwaltung.getNoten(); // Nutzt getNoten() [cite: 33]

        if (noten.isEmpty()) {
            outputArea.append("Es sind noch keine Noten gespeichert.\n");
            return;
        }

        // Formatierte Ausgabe
        for (Note note : noten) {
            outputArea.append(String.format("  - %-15s: %.2f\n", note.getFach().toString(), note.getWert()));
        }
        outputArea.append(String.format("\nGespeicherte Noten insgesamt: %d", noten.size()));
    }
}

