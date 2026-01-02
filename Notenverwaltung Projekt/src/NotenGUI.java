import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * High-End GUI für das Notenverwaltungssystem.
 * Bereinigte Version ohne unnötige Warnungen.
 * Verantwortung: Person 4
 */
public class NotenGUI extends JFrame {

    // --- Daten & Logik ---
    private NotenVerwaltung verwaltung;
    
    // --- UI Komponenten ---
    private JTabbedPane tabbedPane;
    private JTable notenTabelle;
    private DefaultTableModel tableModel;
    private JTextField fachInput, noteInput;
    private JLabel statusBar;
    private JPanel statistikPanel;

    // --- Konstruktor ---
    public NotenGUI(NotenVerwaltung verwaltung) {
        this.verwaltung = verwaltung;

        // 1. Modernes "Look and Feel" aktivieren (Nimbus)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Falls Nimbus nicht verfügbar ist, Standard nutzen
        }

        // 2. Fenster-Grundeinstellungen
        setTitle("Noten-Manager Pro [Premium Edition]");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Zentriert
        setLayout(new BorderLayout());

        // 3. UI aufbauen
        initUI();

        // 4. Sichtbar machen
        setVisible(true);
        refreshData();
    }

    // --- Initialisierung ---
    private void initUI() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Tab 1: Eingabe
        tabbedPane.addTab("📝 Eingabe", createInputPanel());

        // Tab 2: Tabelle
        tabbedPane.addTab("📋 Noten-Tabelle", createTablePanel());

        // Tab 3: Statistik
        tabbedPane.addTab("📊 Analyse & Diagramm", createStatsPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Statusleiste
        statusBar = new JLabel(" System bereit.");
        statusBar.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(statusBar, BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------
    // PANEL 1: EINGABE
    // ---------------------------------------------------------
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Neue Note erfassen");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(50, 50, 50));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(title, c);

        c.gridwidth = 1; c.gridy = 1;
        panel.add(new JLabel("Fachbezeichnung:"), c);
        fachInput = new JTextField(20);
        c.gridx = 1; 
        panel.add(fachInput, c);

        c.gridx = 0; c.gridy = 2;
        panel.add(new JLabel("Note (1.0 - 6.0):"), c);
        noteInput = new JTextField(20);
        c.gridx = 1; 
        panel.add(noteInput, c);

        JButton addButton = new JButton("Speichern");
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        addButton.addActionListener(e -> actionAddNote());
        
        c.gridx = 1; c.gridy = 3;
        panel.add(addButton, c);

        return panel;
    }

    // ---------------------------------------------------------
    // PANEL 2: TABELLE
    // ---------------------------------------------------------
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Fach", "Note", "Bewertung"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        notenTabelle = new JTable(tableModel);
        notenTabelle.setRowHeight(25);
        notenTabelle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        notenTabelle.setAutoCreateRowSorter(true); // Automatische Sortierung

        JScrollPane scrollPane = new JScrollPane(notenTabelle);
        panel.add(scrollPane, BorderLayout.CENTER);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        JButton btnExport = new JButton("Exportieren (TXT)");
        btnExport.addActionListener(e -> actionExport());
        
        JButton btnReset = new JButton("Alle Löschen");
        btnReset.setBackground(new Color(231, 76, 60));
        btnReset.setForeground(Color.WHITE);
        btnReset.addActionListener(e -> actionReset());

        toolbar.add(btnExport);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(btnReset);
        
        panel.add(toolbar, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------------------------------------------------
    // PANEL 3: STATISTIK
    // ---------------------------------------------------------
    private JPanel createStatsPanel() {
        statistikPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChart(g, getWidth(), getHeight());
            }
        };
        statistikPanel.setBackground(Color.WHITE);
        statistikPanel.setLayout(new BorderLayout());
        
        JLabel infoLabel = new JLabel("Visualisierung der Notenverteilung");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setBorder(new EmptyBorder(10,0,0,0));
        statistikPanel.add(infoLabel, BorderLayout.NORTH);

        return statistikPanel;
    }

    // ---------------------------------------------------------
    // LOGIK
    // ---------------------------------------------------------

    private void actionAddNote() {
        try {
            String fachName = fachInput.getText().trim();
            String notenText = noteInput.getText().trim().replace(",", ".");

            if (fachName.isEmpty() || notenText.isEmpty()) {
                throw new IllegalArgumentException("Bitte alle Felder ausfüllen.");
            }

            double wert = Double.parseDouble(notenText);
            if (wert < 1.0 || wert > 6.0) {
                throw new IllegalArgumentException("Note muss zwischen 1.0 und 6.0 liegen.");
            }

            // Speichern über Verwaltung
            verwaltung.noteHinzufuegen(new Note(new Fach(fachName), wert));

            fachInput.setText("");
            noteInput.setText("");
            fachInput.requestFocus();
            
            setStatus("✅ Note hinzugefügt: " + fachName + " (" + wert + ")", new Color(39, 174, 96));
            
            refreshData();
            tabbedPane.setSelectedIndex(1);

        } catch (NumberFormatException e) {
            setStatus("❌ Fehler: Note muss eine Zahl sein (z.B. 1.3)", Color.RED);
        } catch (IllegalArgumentException e) {
            setStatus("⚠️ " + e.getMessage(), Color.ORANGE.darker());
        }
    }

    private void actionReset() {
        int choice = JOptionPane.showConfirmDialog(this, 
            "Möchten Sie wirklich alle Daten löschen?", "Reset", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Zugriff auf ArrayList und leeren
                ((ArrayList<Note>) verwaltung.getNoten()).clear();
                refreshData();
                setStatus("🗑️ Datenbank bereinigt.", Color.BLACK);
            } catch (Exception e) {
                setStatus("❌ Fehler beim Löschen.", Color.RED);
            }
        }
    }

    private void actionExport() {
        try (FileWriter fw = new FileWriter("Noten_Export.txt")) {
            fw.write("NOTEN-EXPORT\n");
            fw.write("================================\n");
            for (Note n : verwaltung.getNoten()) {
                fw.write(String.format("Fach: %-20s | Note: %.1f\n", n.getFach().getFachName(), n.getWert()));
            }
            fw.write("================================\n");
            fw.write("Durchschnitt: " + String.format("%.2f", verwaltung.durchschnittBerechnen()));
            
            setStatus("💾 Export erfolgreich gespeichert.", Color.BLUE);
        } catch (IOException e) {
            setStatus("❌ Fehler beim Exportieren.", Color.RED);
        }
    }

    private void refreshData() {
        // Tabelle leeren und neu füllen
        tableModel.setRowCount(0);
        List<Note> noten = verwaltung.getNoten();
        
        for (Note n : noten) {
            String bewertung = getBewertungText(n.getWert());
            Object[] rowData = {n.getFach().getFachName(), n.getWert(), bewertung};
            tableModel.addRow(rowData);
        }

        statistikPanel.repaint();
        
        if (!noten.isEmpty()) {
            double schnitt = verwaltung.durchschnittBerechnen();
            tabbedPane.setTitleAt(2, String.format("📊 Statistik (Ø %.2f)", schnitt));
        }
    }

    private String getBewertungText(double note) {
        if (note <= 1.5) return "Sehr Gut";
        if (note <= 2.5) return "Gut";
        if (note <= 3.5) return "Befriedigend";
        if (note <= 4.0) return "Ausreichend";
        return "Mangelhaft/Ungenügend";
    }

    private void setStatus(String text, Color color) {
        statusBar.setText(" " + text);
        statusBar.setForeground(color);
    }

    private void drawChart(Graphics g, int w, int h) {
        List<Note> noten = verwaltung.getNoten();
        if (noten.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int[] counts = new int[6];
        for (Note n : noten) {
            int rounded = (int) Math.round(n.getWert());
            if (rounded >= 1 && rounded <= 6) {
                counts[rounded - 1]++;
            }
        }

        int barWidth = (w - 100) / 6;
        int maxCount = 0;
        for (int c : counts) if (c > maxCount) maxCount = c;
        if (maxCount == 0) maxCount = 1;

        int x = 50;
        int bottomY = h - 50;
        int availableHeight = h - 100;

        for (int i = 0; i < 6; i++) {
            int barHeight = (int) ((double) counts[i] / maxCount * availableHeight);
            
            if (i < 2) g2d.setColor(new Color(46, 204, 113));      // 1-2 Grün
            else if (i < 4) g2d.setColor(new Color(241, 196, 15)); // 3-4 Gelb
            else g2d.setColor(new Color(231, 76, 60));             // 5-6 Rot

            g2d.fillRoundRect(x, bottomY - barHeight, barWidth - 10, barHeight, 10, 10);
            
            g2d.setColor(Color.BLACK);
            g2d.drawString("Note " + (i + 1), x + 10, bottomY + 20);
            if (counts[i] > 0) {
                g2d.drawString(counts[i] + "x", x + (barWidth/2) - 10, bottomY - barHeight - 5);
            }
            
            x += barWidth;
        }
    }
}

