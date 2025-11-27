import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

public class NotenGUI extends JFrame implements ActionListener {

    // -----------------------------------------
    // Attribute
    // -----------------------------------------
    private NotenVerwaltung verwaltung;
    private JTextField fachNameField;
    private JTextField noteWertField;
    
    // Buttons für Aktionen
    private JButton addButton;
    private JButton averageButton;
    private JButton clearButton;
    
    // Buttons für Sortierung
    private JButton sortFachButton;
    private JButton sortWertButton;
    
    private JTextArea outputArea;
    private JLabel statusLabel;

    // -----------------------------------------
    // Konstruktor
    // -----------------------------------------

    public NotenGUI(NotenVerwaltung verwaltung) {
        this.verwaltung = verwaltung;
        setTitle("Notenverwaltungssystem - Abschließende GUI");
        setSize(550, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        layoutComponents();
        
        setVisible(true);
        // Startet die Anzeige, inklusive Fachdurchschnitt
        displayAllGrades(); 
    }

    // -----------------------------------------
    // 1. Initialisierung der Komponenten und Farben
    // -----------------------------------------

    private void initComponents() {
        fachNameField = new JTextField(20);
        noteWertField = new JTextField(5); 
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusLabel = new JLabel("Bereit.");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // --- ICONS DEFINIEREN (Standard-Swing-Icons) ---
        Icon addIcon = UIManager.getIcon("FileChooser.upFolderIcon");
        Icon avgIcon = UIManager.getIcon("OptionPane.questionIcon");
        Icon clearIcon = UIManager.getIcon("InternalFrame.closeIcon");
        
        // --- FARB-DEFINITIONEN ---
        Color primaryColor = new Color(50, 100, 150); // Dunkelblau
        Color clearColor = new Color(200, 50, 50); // Rot
        Color avgColor = new Color(60, 179, 113); // Grün
        Color secondaryColor = new Color(240, 240, 240); // Heller Hintergrund
        Font buttonFont = new Font("SansSerif", Font.BOLD, 12);

        // --- BUTTONS ERSTELLEN UND STYLEN ---
        addButton = new JButton(" Note Hinzufügen", addIcon);
        addButton.addActionListener(this); 
        averageButton = new JButton(" Gesamtdurchschnitt", avgIcon);
        averageButton.addActionListener(this); 
        clearButton = new JButton(" Liste Leeren", clearIcon);
        clearButton.addActionListener(this);
        sortFachButton = new JButton("Sort. Fach");
        sortFachButton.addActionListener(this);
        sortWertButton = new JButton("Sort. Note");
        sortWertButton.addActionListener(this);

        // Hintergrundfarben
        getContentPane().setBackground(secondaryColor);
        
        // Button-Stil
        addButton.setBackground(primaryColor); addButton.setForeground(Color.WHITE); addButton.setFont(buttonFont); addButton.setFocusPainted(false);
        averageButton.setBackground(avgColor); averageButton.setForeground(Color.WHITE); averageButton.setFont(buttonFont); averageButton.setFocusPainted(false);
        clearButton.setBackground(clearColor); clearButton.setForeground(Color.WHITE); clearButton.setFont(buttonFont); clearButton.setFocusPainted(false);
        
        sortFachButton.setBackground(Color.LIGHT_GRAY); sortWertButton.setBackground(Color.LIGHT_GRAY); 

        // Status Label Stil
        statusLabel.setForeground(primaryColor);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        // Rahmen für Textfelder
        fachNameField.setBorder(BorderFactory.createLineBorder(primaryColor.darker()));
        noteWertField.setBorder(BorderFactory.createLineBorder(primaryColor.darker()));
    }

    // -----------------------------------------
    // 2. Verbesserte Layout-Methode
    // -----------------------------------------

    private void layoutComponents() {
        
        // --- Input Panel (NORTH) mit GridBagLayout ---
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("📝 Neue Note Eintragen"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Eingabefelder
        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("Fachname:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2; inputPanel.add(fachNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; inputPanel.add(new JLabel("Note (1.0 - 6.0):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(noteWertField, gbc);
        
        // Add Button
        gbc.gridx = 2; gbc.gridy = 1;
        inputPanel.add(addButton, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // --- Sortierung & Output Area (CENTER) ---
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Sortier-Panel
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sortPanel.add(new JLabel("Sortieren:"));
        sortPanel.add(sortFachButton);
        sortPanel.add(sortWertButton);
        centerPanel.add(sortPanel, BorderLayout.NORTH);
        
        // Output Area
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📖 Protokoll und Übersicht"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // --- Button Panel (SOUTH) ---
        JPanel southPanel = new JPanel(new BorderLayout(0, 5));
        
        // Unteres Button-Feld für Durchschnitt und Löschen
        JPanel buttonGroup = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonGroup.add(averageButton); 
        buttonGroup.add(clearButton);
        
        southPanel.add(buttonGroup, BorderLayout.NORTH); 
        southPanel.add(statusLabel, BorderLayout.SOUTH); 

        add(southPanel, BorderLayout.SOUTH);
    }

    // -----------------------------------------
    // 3. ActionListener Logik
    // -----------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addNewGrade();
        } else if (e.getSource() == averageButton) {
            calculateAverage();
        } else if (e.getSource() == clearButton) {
            notenLoeschen();
        } else if (e.getSource() == sortFachButton) {
            sortiereNoten("fach");
        } else if (e.getSource() == sortWertButton) {
            sortiereNoten("wert");
        }
    }
    
    // ... (addNewGrade() und calculateAverage() bleiben unverändert) ...

    private void addNewGrade() {
        String fachName = fachNameField.getText().trim();
        String wertText = noteWertField.getText().trim();
        if (fachName.isEmpty() || wertText.isEmpty()) { statusLabel.setText("Fehler: Bitte alle Felder ausfüllen!"); return; }

        try {
            double wert = Double.parseDouble(wertText.replace(",", ".")); 
            if (wert < 1.0 || wert > 6.0) { statusLabel.setText("Fehler: Note muss zwischen 1.0 und 6.0 liegen."); return; }

            verwaltung.noteHinzufuegen(new Note(new Fach(fachName), wert)); 
            
            fachNameField.setText("");
            noteWertField.setText("");
            statusLabel.setText("✅ Note erfolgreich hinzugefügt!");
            displayAllGrades(); 
        } catch (NumberFormatException ex) {
            statusLabel.setText("Fehler: Ungültiges Zahlenformat für die Note.");
        }
    }
    
    private void calculateAverage() {
        double durchschnitt = verwaltung.durchschnittBerechnen();
        outputArea.append("\n\n--- Gesamtdurchschnitt ---\n");
        
        if (verwaltung.getNoten().isEmpty()) {
            outputArea.append("Es sind keine Noten für die Berechnung vorhanden.\n");
            statusLabel.setText("Keine Noten für Durchschnitt vorhanden.");
        } else {
             outputArea.append(String.format("Der Gesamtdurchschnitt aller %d Noten beträgt: **%.2f**\n", 
                                             verwaltung.getNoten().size(), durchschnitt));
             statusLabel.setText(String.format("✅ Durchschnitt berechnet: %.2f", durchschnitt));
        }
    }
    
    // -----------------------------------------
    // 4. NEUE FUNKTIONEN: Sortieren & Löschen
    // -----------------------------------------
    
    private void notenLoeschen() {
        int dialogResult = JOptionPane.showConfirmDialog(this, 
            "Sind Sie sicher, dass Sie ALLE Noten löschen möchten?", 
            "Bestätigung", JOptionPane.YES_NO_OPTION);
        
        if (dialogResult == JOptionPane.YES_OPTION) {
             // Zugriff auf die interne Liste für den Reset. Dies setzt voraus, 
             // dass getNoten() die interne ArrayList zurückgibt (häufig in Uni-Projekten)
            try {
                ((ArrayList<Note>) verwaltung.getNoten()).clear(); 
                outputArea.append("\n\n*** Notenliste erfolgreich zurückgesetzt. ***");
                statusLabel.setText("Alle Noten gelöscht.");
                displayAllGrades();
            } catch (Exception e) {
                 JOptionPane.showMessageDialog(this, "FEHLER: NotenVerwaltung lässt das Löschen nicht zu (Listenkopie).", "Löschfehler", JOptionPane.ERROR_MESSAGE);
                 statusLabel.setText("Löschfehler: Verwaltung blockiert Reset.");
            }
        }
    }
    
    private void sortiereNoten(String kriterium) {
        List<Note> noten = verwaltung.getNoten();
        if (noten.isEmpty()) {
            statusLabel.setText("Keine Noten zum Sortieren vorhanden.");
            return;
        }

        List<Note> sortierteNoten = new ArrayList<>(noten);
        Comparator<Note> comparator;

        if (kriterium.equalsIgnoreCase("fach")) {
            // Sortiert alphabetisch nach Fachname
            comparator = Comparator.comparing(note -> note.getFach().getFachName());
            statusLabel.setText("✅ Nach Fachnamen sortiert.");
        } else if (kriterium.equalsIgnoreCase("wert")) {
            // Sortiert nach Notenwert (1.0 ist besser)
            comparator = Comparator.comparingDouble(Note::getWert); 
            statusLabel.setText("✅ Nach Notenwert sortiert.");
        } else {
            return;
        }

        sortierteNoten.sort(comparator);
        displaySortedGrades(sortierteNoten);
    }
    
    // -----------------------------------------
    // 5. Anzeige und Fachdurchschnitt
    // -----------------------------------------

    private void displayAllGrades() {
        // Zeigt die unsortierte Liste und ruft den Fachdurchschnitt auf
        outputArea.setText("--- AKTUELLE NOTENLISTE ---\n");
        List<Note> noten = verwaltung.getNoten();

        if (noten.isEmpty()) {
            outputArea.append("Es sind noch keine Noten gespeichert.\n");
            outputArea.append("\n\n--- DURCHSCHNITTE PRO FACH ---\nKeine Daten.");
            return;
        }

        for (Note note : noten) {
            outputArea.append(String.format("  - %-15s: %.2f\n", note.getFach().toString(), note.getWert()));
        }
        outputArea.append(String.format("\nGespeicherte Noten insgesamt: %d", noten.size()));
        
        durchschnittProFachAnzeigen(noten);
    }
    
    private void displaySortedGrades(List<Note> sortedList) {
        // Zeigt die sortierte Liste an
        outputArea.setText("--- SORTIERTE NOTENLISTE ---\n");

        for (Note note : sortedList) {
            outputArea.append(String.format("  - %-15s: %.2f\n", note.getFach().toString(), note.getWert()));
        }
        outputArea.append(String.format("\nGespeicherte Noten insgesamt: %d (sortiert)", sortedList.size()));
        durchschnittProFachAnzeigen(sortedList);
    }
    
    private void durchschnittProFachAnzeigen(List<Note> notenZurBerechnung) {
        
        if (notenZurBerechnung.isEmpty()) return;
        
        outputArea.append("\n\n--- DURCHSCHNITTE PRO FACH ---\n");

        // Gruppiert Noten und berechnet den Durchschnitt pro Fach
        Map<Fach, Double> durchschnittMap = notenZurBerechnung.stream()
            .collect(Collectors.groupingBy(Note::getFach, 
                     Collectors.averagingDouble(Note::getWert)));

        // Ausgabe
        durchschnittMap.forEach((fach, durchschnitt) -> {
            outputArea.append(
                String.format("  - %-15s: %.2f\n", fach.getFachName(), durchschnitt)
            );
        });
    }
}
