import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * NOTEN-GUI v3.0 - "Architect Edition" (DEUTSCH)
 * Features:
 * 1. Fokus-Timer (Pomodoro) mit Kreisanimation
 * 2. Partikel-System (Konfetti bei guten Noten)
 * 3. Live Histogramm & Ziel-Rechner
 * 4. Cyberpunk UI Theme (Deutsch)
 */
public class NotenGUI extends JFrame {

    // ----------------------------
    // Domain & Reflection
    // ----------------------------
    private final NotenVerwaltung verwaltung;
    private final Field fNotenListe;
    private final Field fNoteWert;

    // ----------------------------
    // UI Komponenten
    // ----------------------------
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel status = new JLabel("System bereit // v3.0");
    private final ToastHost toastHost = new ToastHost();
    private final ConfettiLayer confettiLayer = new ConfettiLayer(); // Partikel System
    private final List<GlowNavButton> navButtons = new ArrayList<>();

    // Inputs
    private DefaultTableModel notenModel;
    private JTable notenTable;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField, fachField, noteField;

    // Dashboard Widgets
    private PerformanceRing ring;
    private JLabel kpiAvg, kpiCritical, kpiCount;
    private JPanel badgesPanel;
    private JTextField targetField;
    private JLabel targetResultLabel;

    // Stats Chart
    private StatsChart statsChart;
    
    // Focus Timer Elements
    private FocusTimerPanel focusTimerPanel;

    // Konstanten
    private static final DecimalFormat ONE_DEC = new DecimalFormat("0.0");
    private static final DecimalFormat TWO_DEC = new DecimalFormat("0.00");

    // Cyberpunk Neon Palette
    private static final Color BG = new Color(0x0B0D14); // Deep Dark
    private static final Color SURFACE = new Color(0x151922);
    private static final Color SURFACE_2 = new Color(0x1F2430);
    private static final Color TEXT = new Color(0xEEF1F5);
    private static final Color MUTED = new Color(0x687388);
    
    private static final Color ACCENT_CYAN = new Color(0x00F0FF);
    private static final Color ACCENT_PURPLE = new Color(0xBC13FE);
    private static final Color SUCCESS = new Color(0x00FF9D);
    private static final Color WARNING = new Color(0xFFD600);
    private static final Color DANGER  = new Color(0xFF2E63);

    public NotenGUI(NotenVerwaltung verwaltung) {
        this.verwaltung = verwaltung;
        Locale.setDefault(Locale.GERMANY); // Deutsches Format

        // Reflection Setup
        try {
            fNotenListe = NotenVerwaltung.class.getDeclaredField("notenListe");
            fNotenListe.setAccessible(true);
            fNoteWert = Note.class.getDeclaredField("wert");
            fNoteWert.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Init Fehler", e);
        }

        applyTechTheme();
        initWindow();
    }

    private void initWindow() {
        setTitle("NotenManager // ARCHITECT");
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // LayeredPane für Confetti-Effekt über der UI
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        setContentPane(layeredPane);

        // Main UI Panel
        JPanel mainUI = new JPanel(new BorderLayout());
        mainUI.setBackground(BG);
        
        mainUI.add(createHeader(), BorderLayout.NORTH);
        mainUI.add(createSidebar(), BorderLayout.WEST);
        
        content.setBackground(BG);
        content.add(createDashboardView(), "DASH");
        content.add(createNotesView(), "NOTES");
        content.add(createStatsView(), "STATS");
        content.add(createFocusView(), "FOCUS");
        content.add(createExportView(), "EXPORT");
        
        mainUI.add(content, BorderLayout.CENTER);
        mainUI.add(createStatusBar(), BorderLayout.SOUTH);

        // Z-Order: Toast ganz oben, dann Confetti, dann UI
        layeredPane.add(toastHost, JLayeredPane.POPUP_LAYER); 
        layeredPane.add(confettiLayer, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(mainUI, JLayeredPane.DEFAULT_LAYER);

        toastHost.setVisible(true);
        confettiLayer.setVisible(true);

        refreshAll();
        setVisible(true);
    }

    private void applyTechTheme() {
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.background", SURFACE_2);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", ACCENT_CYAN);
        UIManager.put("Button.background", SURFACE_2);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.gridColor", new Color(0x2A2F3D));
        UIManager.put("TableHeader.background", SURFACE_2);
        UIManager.put("TableHeader.foreground", TEXT);
    }

    // =========================================================
    // Layout Aufbau
    // =========================================================
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(25, 25, 15, 25));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("NOTEN-SYSTEM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        JLabel subtitle = new JLabel("v3.0 // ARCHITECT");
        subtitle.setForeground(ACCENT_CYAN);
        subtitle.setFont(new Font("Monospaced", Font.BOLD, 12));
        left.add(title);
        left.add(subtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setOpaque(false);
        
        searchField = techTextField("Datenbank durchsuchen...");
        searchField.setPreferredSize(new Dimension(220, 35));
        installSearchFilter(searchField);
        
        JButton quickAdd = new TechButton("+ DATEN EINGABE", ACCENT_CYAN, ACCENT_PURPLE);
        quickAdd.addActionListener(e -> {
            cards.show(content, "NOTES");
            fachField.requestFocusInWindow();
            toastHost.showToast("Eingabemodus Aktiv", ToastKind.INFO);
            navButtons.forEach(b -> b.setSelectedState(false));
        });

        right.add(searchField);
        right.add(quickAdd);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(new EmptyBorder(10, 15, 15, 10));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(SURFACE);
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x333A4A)),
            new EmptyBorder(20, 10, 20, 10)
        ));

        container.add(navLabel("STEUERZENTRALE"));
        container.add(createNavBtn("Übersicht", "DASH", true));
        container.add(Box.createVerticalStrut(8));
        container.add(createNavBtn("Notenliste", "NOTES", false));
        container.add(Box.createVerticalStrut(8));
        container.add(createNavBtn("Statistik", "STATS", false));
        container.add(Box.createVerticalStrut(20));
        
        container.add(navLabel("WERKZEUGE"));
        container.add(createNavBtn("Fokus-Timer", "FOCUS", false));
        container.add(Box.createVerticalStrut(8));
        container.add(createNavBtn("Exportieren", "EXPORT", false));
        
        container.add(Box.createVerticalGlue());
        
        JLabel sys = new JLabel("SYSTEM BEREIT");
        sys.setForeground(SUCCESS);
        sys.setFont(new Font("Monospaced", Font.BOLD, 10));
        container.add(sys);

        sidebar.add(container, BorderLayout.CENTER);
        return sidebar;
    }

    private JLabel navLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setBorder(new EmptyBorder(0, 8, 10, 0));
        return l;
    }

    private GlowNavButton createNavBtn(String text, String card, boolean active) {
        GlowNavButton b = new GlowNavButton(text);
        b.setSelectedState(active);
        navButtons.add(b);
        b.addActionListener(e -> {
            navButtons.forEach(btn -> btn.setSelectedState(false));
            b.setSelectedState(true);
            cards.show(content, card);
            
            if(card.equals("STATS") && statsChart != null) statsChart.updateData(getInternalNotenListe());
            if(card.equals("FOCUS")) focusTimerPanel.startAnimation(); 
        });
        return b;
    }

    // =========================================================
    // Views (Ansichten)
    // =========================================================

    private JPanel createDashboardView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(10, 25, 25, 25));

        JPanel grid = new JPanel(new GridLayout(1, 2, 25, 0));
        grid.setOpaque(false);

        // Links: Performance Ring
        JPanel leftCard = techCard("AKADEMISCHE LEISTUNG");
        ring = new PerformanceRing();
        
        kpiAvg = new JLabel("0.0");
        kpiAvg.setFont(new Font("Segoe UI", Font.BOLD, 48));
        kpiAvg.setForeground(TEXT);
        kpiAvg.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel ringWrapper = new JPanel(new BorderLayout());
        ringWrapper.setOpaque(false);
        ringWrapper.add(ring, BorderLayout.CENTER);
        ringWrapper.add(kpiAvg, BorderLayout.SOUTH);
        
        badgesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        badgesPanel.setOpaque(false);
        badgesPanel.setBorder(new EmptyBorder(15,0,0,0));
        
        leftCard.add(ringWrapper, BorderLayout.CENTER);
        leftCard.add(badgesPanel, BorderLayout.SOUTH);

        // Rechts: KPIs + Ziel-Rechner
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 25));
        rightPanel.setOpaque(false);

        JPanel kpiGrid = new JPanel(new GridLayout(2, 2, 15, 15));
        kpiGrid.setOpaque(false);
        kpiCritical = kpiValue("0");
        kpiCount = kpiValue("0");
        kpiGrid.add(kpiTile("KRITISCH (>4.0)", kpiCritical, DANGER));
        kpiGrid.add(kpiTile("GESAMTANZAHL", kpiCount, ACCENT_CYAN));
        kpiGrid.add(createCalculatorTile());

        JPanel kpiCard = techCard("METRIKEN & PROGNOSE");
        kpiCard.add(kpiGrid, BorderLayout.CENTER);
        
        rightPanel.add(kpiCard);
        
        grid.add(leftCard);
        grid.add(rightPanel);
        root.add(grid, BorderLayout.CENTER);
        return root;
    }
    
    private JPanel createFocusView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(10, 25, 25, 25));
        
        JPanel card = techCard("DEEP WORK / FOKUS-SESSION");
        focusTimerPanel = new FocusTimerPanel();
        card.add(focusTimerPanel, BorderLayout.CENTER);
        
        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private JPanel createNotesView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(10, 25, 25, 25));

        // Eingabebereich
        JPanel inputCard = techCard("NEUER EINTRAG");
        JPanel inputGrid = new JPanel(new GridLayout(1, 5, 15, 0)); 
        inputGrid.setOpaque(false);
        
        fachField = techTextField("Fach (z.B. Java)");
        noteField = techTextField("Note (1.0 - 6.0)");
        JButton btnSave = new TechButton("SPEICHERN", SUCCESS, SUCCESS.darker());
        JButton btnReset = new TechButton("RESET DB", DANGER, DANGER.darker());
        
        btnSave.addActionListener(e -> actionAddNote());
        btnReset.addActionListener(e -> actionReset());

        inputGrid.add(fachField);
        inputGrid.add(noteField);
        inputGrid.add(btnSave);
        inputGrid.add(new JLabel("")); // Platzhalter
        inputGrid.add(btnReset);
        
        inputCard.add(inputGrid, BorderLayout.CENTER);

        // Tabelle
        JPanel tableCard = techCard("DATENREGISTER (Editierbar)");
        notenModel = new DefaultTableModel(new String[]{"FACH", "NOTE", "BEWERTUNG"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 1; }
        };
        notenTable = new JTable(notenModel);
        notenTable.setRowHeight(40);
        notenTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notenTable.setShowVerticalLines(false);
        sorter = new TableRowSorter<>(notenModel);
        notenTable.setRowSorter(sorter);
        notenTable.getColumnModel().getColumn(2).setCellRenderer(new RatingRenderer());
        
        notenModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 1) {
                handleInlineEdit(e.getFirstRow());
            }
        });

        JScrollPane sp = new JScrollPane(notenTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(SURFACE);
        tableCard.add(sp, BorderLayout.CENTER);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(inputCard, BorderLayout.NORTH);
        topContainer.add(Box.createVerticalStrut(20), BorderLayout.CENTER);
        
        root.add(topContainer, BorderLayout.NORTH);
        root.add(tableCard, BorderLayout.CENTER);
        return root;
    }

    private JPanel createStatsView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(10, 25, 25, 25));
        
        JPanel card = techCard("NOTENVERTEILUNG");
        statsChart = new StatsChart();
        card.add(statsChart, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);
        return root;
    }
    
    private JPanel createExportView() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG);
        
        JPanel card = techCard("DATEN-EXPORT");
        card.setPreferredSize(new Dimension(500, 250));
        
        JTextArea txt = new JTextArea("Generiert ein Abbild der aktuellen Datenbank.\nFormat: Plain Text (.txt)\nSpeicherort: Projektverzeichnis");
        txt.setOpaque(false);
        txt.setForeground(MUTED);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setEditable(false);
        txt.setBorder(new EmptyBorder(10, 10, 20, 10));
        
        JButton btn = new TechButton("EXPORT STARTEN", ACCENT_CYAN, ACCENT_PURPLE);
        btn.addActionListener(e -> actionExport());
        
        card.add(txt, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        
        root.add(card);
        return root;
    }
    
    // Ziel-Rechner UI
    private JPanel createCalculatorTile() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SURFACE_2);
        p.setBorder(BorderFactory.createLineBorder(new Color(0x333A4A)));
        
        JLabel title = new JLabel("ZIEL-RECHNER");
        title.setForeground(MUTED);
        title.setFont(new Font("Segoe UI", Font.BOLD, 10));
        title.setBorder(new EmptyBorder(10, 10, 0, 0));
        
        JPanel body = new JPanel(new FlowLayout(FlowLayout.LEFT));
        body.setOpaque(false);
        
        targetField = techTextField("Wunsch-Ø");
        targetField.setPreferredSize(new Dimension(90, 30));
        
        JButton calcBtn = new TechButton("?", ACCENT_CYAN, ACCENT_PURPLE);
        calcBtn.setPreferredSize(new Dimension(60, 30));
        calcBtn.setBorder(null);
        
        targetResultLabel = new JLabel(" -> ?");
        targetResultLabel.setForeground(TEXT);
        targetResultLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        calcBtn.addActionListener(e -> calculateTarget());

        body.add(targetField);
        body.add(calcBtn);
        body.add(targetResultLabel);
        
        p.add(title, BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    // =========================================================
    // Logik
    // =========================================================

    private void actionAddNote() {
        String f = fachField.getText();
        String nStr = noteField.getText().replace(",", ".");
        Double val = parseDouble(nStr);
        
        if(f.isEmpty() || val == null || val < 1 || val > 6) {
            toastHost.showToast("FEHLER: Ungültige Eingabe", ToastKind.ERROR);
            return;
        }
        
        verwaltung.noteHinzufuegen(new Note(new Fach(f), val));
        fachField.setText("");
        noteField.setText("");
        fachField.requestFocus();
        
        // CONFETTI Trigger bei guten Noten
        if(val <= 2.0) {
            confettiLayer.explode();
            toastHost.showToast("HERVORRAGENDE LEISTUNG!", ToastKind.SUCCESS);
        } else {
            toastHost.showToast("Daten gespeichert", ToastKind.INFO);
        }
        
        refreshAll();
    }
    
    private void handleInlineEdit(int row) {
        try {
            String valStr = notenModel.getValueAt(row, 1).toString().replace(",", ".");
            Double val = parseDouble(valStr);
            if(val == null || val < 1 || val > 6) throw new Exception();
            
            ArrayList<Note> list = getInternalNotenListe();
            Note n = list.get(row); 
            fNoteWert.set(n, val);
            
            toastHost.showToast("Eintrag aktualisiert", ToastKind.SUCCESS);
            refreshAll();
        } catch(Exception e) {
            toastHost.showToast("Update fehlgeschlagen", ToastKind.ERROR);
            refreshAll();
        }
    }
    
    private void calculateTarget() {
        Double target = parseDouble(targetField.getText().replace(",", "."));
        if (target == null) { targetResultLabel.setText(" Fehler"); return; }
        
        List<Note> list = getInternalNotenListe();
        double currentSum = list.stream().mapToDouble(Note::getWert).sum();
        double needed = (target * (list.size() + 1)) - currentSum;
        needed = Math.round(needed * 10.0) / 10.0;
        
        String colorHex = (needed < 1.0) ? "#00FF9D" : (needed > 4.0 ? "#FF2E63" : "#EEF1F5");
        targetResultLabel.setText("<html>-> <span style='color:"+colorHex+"'>" + needed + "</span></html>");
    }
    
    private void actionReset() {
        if(JOptionPane.showConfirmDialog(this, "Wirklich alle Daten löschen?", "Bestätigung", JOptionPane.YES_NO_OPTION) == 0) {
            getInternalNotenListe().clear();
            refreshAll();
            toastHost.showToast("Datenbank geleert", ToastKind.INFO);
        }
    }
    
    private void actionExport() {
        try (FileWriter fw = new FileWriter("Noten_Export.txt")) {
            fw.write("NOTEN-SNAPSHOT " + java.time.LocalDateTime.now() + "\n====================\n");
            for(Note n : getInternalNotenListe()) {
                fw.write(String.format("%-20s : %.1f\n", n.getFach().getFachName(), n.getWert()));
            }
            fw.write("====================\nSCHNITT: " + TWO_DEC.format(verwaltung.durchschnittBerechnen()));
            toastHost.showToast("Export erfolgreich erstellt", ToastKind.SUCCESS);
        } catch (IOException e) {
            toastHost.showToast("Schreibfehler", ToastKind.ERROR);
        }
    }

    private void refreshAll() {
        // Table
        notenModel.setRowCount(0);
        List<Note> list = getInternalNotenListe();
        for(Note n : list) notenModel.addRow(new Object[]{ n.getFach().getFachName(), n.getWert(), getBewertung(n.getWert()) });
        
        // Ring & KPIs
        double avg = list.isEmpty() ? 0.0 : verwaltung.durchschnittBerechnen();
        ring.setValue(avg);
        kpiAvg.setText(TWO_DEC.format(avg));
        kpiCount.setText(String.valueOf(list.size()));
        
        long crit = list.stream().filter(n -> n.getWert() > 4.0).count();
        kpiCritical.setText(String.valueOf(crit));
        
        // Badges
        badgesPanel.removeAll();
        if(list.size() > 0) {
            if(avg < 1.5) badgesPanel.add(createBadge("ELITE", SUCCESS));
            else if(avg < 2.5) badgesPanel.add(createBadge("GUT", ACCENT_CYAN));
            if(crit == 0 && list.size() > 2) badgesPanel.add(createBadge("SAUBER", WARNING));
            if(list.size() >= 5) badgesPanel.add(createBadge("VETERAN", MUTED));
        }
        badgesPanel.revalidate();
        badgesPanel.repaint();
        
        if(statsChart != null) statsChart.updateData(list);
    }

    // =========================================================
    // Hilfsmethoden
    // =========================================================
    
    @SuppressWarnings("unchecked")
    private ArrayList<Note> getInternalNotenListe() {
        try { return (ArrayList<Note>) fNotenListe.get(verwaltung); } 
        catch (Exception e) { return new ArrayList<>(); }
    }
    
    private Double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch(Exception e) { return null; }
    }
    
    private String getBewertung(double n) {
        if (n <= 1.5) return "Sehr Gut";
        if (n <= 2.5) return "Gut";
        if (n <= 3.5) return "Befriedigend";
        if (n <= 4.0) return "Ausreichend";
        return "Mangelhaft";
    }

    // =========================================================
    // Custom UI Klassen ("Die Geheimwaffe")
    // =========================================================

    // 1. CONFETTI SYSTEM (Advanced Graphics)
    private static class ConfettiLayer extends JComponent {
        private final List<Particle> particles = new ArrayList<>();
        private Timer timer;

        ConfettiLayer() {
            setOpaque(false);
            setBounds(0,0,3000,3000); // Überlagert den gesamten Screen
        }

        void explode() {
            Random r = new Random();
            int cx = getWidth()/2;
            int cy = getHeight()/2;
            for(int i=0; i<50; i++) {
                particles.add(new Particle(cx, cy, r));
            }
            if(timer == null || !timer.isRunning()) {
                timer = new Timer(20, e -> update());
                timer.start();
            }
        }

        private void update() {
            Iterator<Particle> it = particles.iterator();
            while(it.hasNext()) {
                Particle p = it.next();
                p.update();
                if(p.life <= 0) it.remove();
            }
            if(particles.isEmpty()) timer.stop();
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for(Particle p : particles) {
                g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), (int)(p.life * 255)));
                g2.fill(new Ellipse2D.Double(p.x, p.y, p.size, p.size));
            }
        }

        private static class Particle {
            double x, y, vx, vy, life = 1.0;
            double size;
            Color color;
            Particle(int startX, int startY, Random r) {
                x = startX; y = startY;
                double angle = r.nextDouble() * Math.PI * 2;
                double speed = r.nextDouble() * 10 + 5;
                vx = Math.cos(angle) * speed;
                vy = Math.sin(angle) * speed;
                size = r.nextDouble() * 8 + 4;
                Color[] cols = {ACCENT_CYAN, ACCENT_PURPLE, SUCCESS, WARNING};
                color = cols[r.nextInt(cols.length)];
            }
            void update() {
                x += vx; y += vy;
                vy += 0.5; // Schwerkraft
                life -= 0.02;
            }
        }
    }

    // 2. FOCUS TIMER (Pomodoro)
    private static class FocusTimerPanel extends JPanel {
        private Timer timer;
        private int totalSeconds = 25 * 60; // 25 min
        private int remaining = totalSeconds;
        private boolean running = false;
        private JLabel timeLbl;
        private float angle = 360f;

        FocusTimerPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);
            
            timeLbl = new JLabel("25:00");
            timeLbl.setFont(new Font("Monospaced", Font.BOLD, 60));
            timeLbl.setForeground(TEXT);
            timeLbl.setHorizontalAlignment(SwingConstants.CENTER);
            
            JButton toggle = new TechButton("START / STOPP", ACCENT_CYAN, ACCENT_PURPLE);
            toggle.addActionListener(e -> toggleTimer());
            
            JPanel center = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int s = Math.min(getWidth(), getHeight()) - 40;
                    int x = (getWidth()-s)/2, y = (getHeight()-s)/2;
                    
                    g2.setStroke(new BasicStroke(8));
                    g2.setColor(SURFACE_2);
                    g2.drawOval(x, y, s, s);
                    
                    g2.setColor(running ? SUCCESS : ACCENT_CYAN);
                    g2.drawArc(x, y, s, s, 90, (int)angle);
                }
            };
            center.setLayout(new GridBagLayout());
            center.setOpaque(false);
            center.add(timeLbl);
            
            add(center, BorderLayout.CENTER);
            add(toggle, BorderLayout.SOUTH);
            
            timer = new Timer(1000, e -> tick());
        }
        
        void startAnimation() { repaint(); }
        
        private void toggleTimer() {
            if(running) timer.stop();
            else timer.start();
            running = !running;
            repaint();
        }
        
        private void tick() {
            if(remaining > 0) {
                remaining--;
                int m = remaining / 60;
                int s = remaining % 60;
                timeLbl.setText(String.format("%02d:%02d", m, s));
                angle = 360f * ((float)remaining / totalSeconds);
                repaint();
            } else {
                timer.stop();
                running = false;
                timeLbl.setText("FERTIG");
            }
        }
    }

    // 3. UI KOMPONENTEN (Buttons, Cards, Charts)
    private JPanel techCard(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x333A4A)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(MUTED);
        l.setBorder(new EmptyBorder(0, 0, 15, 0));
        p.add(l, BorderLayout.NORTH);
        return p;
    }
    
    private JLabel createBadge(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
        l.setForeground(c);
        l.setFont(new Font("Monospaced", Font.BOLD, 12));
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(c),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return l;
    }

    private JPanel kpiTile(String title, JLabel valLabel, Color c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SURFACE_2);
        p.setBorder(BorderFactory.createLineBorder(new Color(0x333A4A)));
        JLabel t = new JLabel(title);
        t.setForeground(MUTED);
        t.setFont(new Font("Segoe UI", Font.BOLD, 10));
        t.setBorder(new EmptyBorder(10, 15, 0, 0));
        JPanel valWrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        valWrap.setOpaque(false);
        valLabel.setForeground(c);
        valWrap.add(valLabel);
        p.add(t, BorderLayout.NORTH);
        p.add(valWrap, BorderLayout.CENTER);
        return p;
    }
    
    private JLabel kpiValue(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 24));
        return l;
    }

    private JTextField techTextField(String ph) {
        JTextField tf = new JTextField();
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x333A4A)),
            new EmptyBorder(10, 10, 10, 10)
        ));
        // Placeholder-Logik hier vereinfacht
        return tf;
    }
    
    // Charts & Grafik
    private static class StatsChart extends JComponent {
        private final int[] distribution = new int[6];
        private int maxCount = 0;

        void updateData(List<Note> notes) {
            Arrays.fill(distribution, 0);
            maxCount = 0;
            for(Note n : notes) {
                int idx = (int) Math.round(n.getWert()) - 1;
                if(idx >= 0 && idx < 6) distribution[idx]++;
            }
            for(int c : distribution) maxCount = Math.max(maxCount, c);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int barWidth = (w / 6) - 20;
            
            g2.setColor(new Color(255,255,255,10));
            g2.drawLine(0, h-30, w, h-30);
            
            if(maxCount == 0) {
                g2.setColor(MUTED);
                g2.drawString("KEINE DATEN VORHANDEN", w/2 - 70, h/2);
                return;
            }

            for(int i=0; i<6; i++) {
                int count = distribution[i];
                if(count == 0) continue;
                int barH = (int) ((double)count / maxCount * (h-50));
                int x = i * (w/6) + 15;
                int y = (h - 30) - barH;
                GradientPaint gp = new GradientPaint(x, y+barH, ACCENT_CYAN, x, y, ACCENT_PURPLE);
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barWidth, barH, 4, 4);
                g2.setColor(TEXT);
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2.drawString((i+1) + ".0", x + barWidth/2 - 10, h - 10);
                g2.setColor(MUTED);
                g2.drawString(String.valueOf(count), x + barWidth/2 - 4, y - 5);
            }
        }
    }

    private static class PerformanceRing extends JComponent {
        private double value = 0;
        void setValue(double v) { this.value = v; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight()) - 20;
            int x = (getWidth()-s)/2, y = (getHeight()-s)/2;
            g2.setStroke(new BasicStroke(15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(SURFACE_2);
            g2.drawArc(x, y, s, s, 0, 360);
            double normalized = 1.0 - ((value - 1.0) / 5.0); 
            if(normalized < 0) normalized = 0;
            Color c1 = value <= 2.0 ? SUCCESS : (value <= 4.0 ? WARNING : DANGER);
            GradientPaint gp = new GradientPaint(x, y, c1, x+s, y+s, c1.darker());
            g2.setPaint(gp);
            g2.drawArc(x, y, s, s, 90, -(int)(normalized * 360));
        }
    }

    private static class TechButton extends JButton {
        private final Color c1, c2;
        TechButton(String t, Color start, Color end) {
            super(t);
            this.c1 = start; this.c2 = end;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(10,20,10,20));
            setForeground(BG);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
            super.paintComponent(g);
        }
    }

    private static class GlowNavButton extends JButton {
        boolean selected = false;
        GlowNavButton(String t) {
            super(t);
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(12, 20, 12, 20));
            setForeground(MUTED);
            setHorizontalAlignment(LEFT);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        void setSelectedState(boolean b) { selected = b; setForeground(b ? TEXT : MUTED); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            if(selected) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(ACCENT_CYAN.getRed(), ACCENT_CYAN.getGreen(), ACCENT_CYAN.getBlue(), 20));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(ACCENT_CYAN);
                g2.fillRect(0, 0, 4, getHeight());
            }
            super.paintComponent(g);
        }
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar(); mb.setBackground(BG); mb.setBorderPainted(false);
        JMenu m = new JMenu("SYSTEM"); m.setForeground(TEXT); m.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JMenuItem ex = new JMenuItem("BEENDEN"); ex.addActionListener(e -> System.exit(0));
        m.add(ex); mb.add(m);
        return mb;
    }
    
    private JPanel createStatusBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(BG);
        status.setForeground(MUTED);
        status.setFont(new Font("Monospaced", Font.PLAIN, 10));
        p.add(status);
        return p;
    }
    
    private void installSearchFilter(JTextField tf) {
        tf.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            void filter() {
                String text = tf.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
    }

    private enum ToastKind { INFO, SUCCESS, ERROR }
    private static class ToastHost extends JPanel {
        ToastHost() { setLayout(null); setOpaque(false); }
        void showToast(String msg, ToastKind k) {
            JLabel l = new JLabel(msg);
            l.setOpaque(true);
            l.setBackground(k == ToastKind.ERROR ? DANGER : (k == ToastKind.SUCCESS ? SUCCESS : SURFACE_2));
            l.setForeground(k == ToastKind.SUCCESS ? BG : TEXT);
            l.setFont(new Font("Monospaced", Font.BOLD, 12));
            l.setBorder(new EmptyBorder(12, 25, 12, 25));
            int w = 300; int h = 45;
            l.setBounds(getWidth()/2 - w/2, getHeight() - 120, w, h);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            add(l); repaint();
            new Timer(3000, e -> { remove(l); repaint(); ((Timer)e.getSource()).stop(); }).start();
        }
    }
    
    private static class RatingRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
            String s = String.valueOf(v);
            if(s.equals("Sehr Gut") || s.equals("Gut")) comp.setForeground(SUCCESS);
            else if(s.contains("Mangel") || s.contains("Ungenügend")) comp.setForeground(DANGER);
            else comp.setForeground(TEXT);
            return comp;
        }
    }
}

