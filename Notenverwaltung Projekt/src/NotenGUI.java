import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
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
    private final JLabel status = new JLabel("System v5.0 // Ready");
    private final ToastHost toastHost = new ToastHost();
    private final ConfettiLayer confettiLayer = new ConfettiLayer(); 
    private final List<ModernNavButton> navButtons = new ArrayList<>();

    // Inputs
    private DefaultTableModel notenModel;
    private JTable notenTable;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField fachField, noteField;

    // Dashboard Widgets
    private PerformanceRing ring;
    private JLabel kpiAvg, kpiCritical, kpiCount;
    private JPanel badgesPanel;
    private JTextField targetField;
    private JLabel targetResultLabel;
    
    // Tools
    private DefaultListModel<String> todoModel; 
    private JList<String> todoList; // Referenz für Theme-Update
    private JLabel quoteLabel; 

    // Stats Chart & Timer
    private StatsChart statsChart;
    private FocusTimerPanel focusTimerPanel;

    // Theme Switcher Button
    private JButton themeSwitchBtn;
    private boolean isDarkMode = true;

    // Konstanten
    private static final DecimalFormat TWO_DEC = new DecimalFormat("0.00");

    // ==========================================
    // DYNAMISCHE FARBPALETTE
    // ==========================================
    // Wir machen die Farben nicht mehr 'static final', damit wir sie ändern können.
    private Color cBgDark, cBgPanel, cBgInput;
    private Color cTextMain, cTextMuted;
    private Color cAccent1, cAccent2;
    private Color cSuccess, cWarning, cDanger;

    public NotenGUI(NotenVerwaltung verwaltung) {
        this.verwaltung = verwaltung;
        Locale.setDefault(Locale.GERMANY);

        try {
            fNotenListe = NotenVerwaltung.class.getDeclaredField("notenListe");
            fNotenListe.setAccessible(true);
            fNoteWert = Note.class.getDeclaredField("wert");
            fNoteWert.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Init Fehler", e);
        }

        setDarkTheme(); // Standard: Dark Mode
        initWindow();
        applyThemeToComponents(); // Initiales Styling
    }

    // --- THEME DEFINITIONEN ---
    private void setDarkTheme() {
        cBgDark    = new Color(46, 52, 64);   
        cBgPanel   = new Color(59, 66, 82);   
        cBgInput   = new Color(67, 76, 94);   
        cTextMain  = new Color(236, 239, 244);
        cTextMuted = new Color(216, 222, 233);
        cAccent1   = new Color(136, 192, 208); // Frost Blue
        cAccent2   = new Color(129, 161, 193); 
        cSuccess   = new Color(163, 190, 140);
        cWarning   = new Color(235, 203, 139);
        cDanger    = new Color(191, 97, 106);
    }

    private void setLightTheme() {
        cBgDark    = new Color(240, 242, 245); // Soft Paper White/Grey
        cBgPanel   = new Color(255, 255, 255); // Pure White Cards
        cBgInput   = new Color(230, 232, 235);
        cTextMain  = new Color(46, 52, 64);    // Dark Text
        cTextMuted = new Color(100, 110, 120);
        cAccent1   = new Color(94, 129, 172);  // Blue
        cAccent2   = new Color(129, 161, 193);
        cSuccess   = new Color(100, 160, 100);
        cWarning   = new Color(220, 160, 50);
        cDanger    = new Color(200, 80, 80);
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        if(isDarkMode) setDarkTheme(); else setLightTheme();
        
        // UI aktualisieren
        applyThemeToComponents();
        themeSwitchBtn.setText(isDarkMode ? "☀ LIGHT MODE" : "☾ DARK MODE");
        this.repaint();
    }

    private void applyThemeToComponents() {
        // Globale UI Defaults updaten (für Standard-Komponenten)
        UIManager.put("Panel.background", cBgDark);
        UIManager.put("Label.foreground", cTextMain);
        UIManager.put("TextField.background", cBgInput);
        UIManager.put("TextField.foreground", cTextMain);
        UIManager.put("List.background", cBgInput);
        UIManager.put("List.foreground", cTextMain);
        UIManager.put("Table.background", cBgPanel);
        UIManager.put("Table.foreground", cTextMain);
        
        // Manuelle Updates für unsere Custom Components
        getContentPane().setBackground(cBgDark);
        content.setBackground(cBgDark);
        
        // Rekursiv durch Container gehen (vereinfacht für Hauptkomponenten)
        updateContainerColors(this.getContentPane());
        
        // Spezifische Komponenten neu setzen
        if(notenTable != null) {
            notenTable.setBackground(cBgPanel);
            notenTable.setForeground(cTextMain);
            notenTable.setGridColor(cBgInput);
            notenTable.getTableHeader().setBackground(cBgInput);
            notenTable.getTableHeader().setForeground(cTextMain);
        }
        if(todoList != null) {
            todoList.setBackground(cBgInput);
            todoList.setForeground(cTextMain);
        }
        if(fachField != null) { fachField.setBackground(cBgInput); fachField.setForeground(cTextMain); }
        if(noteField != null) { noteField.setBackground(cBgInput); noteField.setForeground(cTextMain); }
    }
    
    private void updateContainerColors(Component c) {
        if(c instanceof JPanel) {
            // Wir unterscheiden Panel-Typen anhand ihrer Namen oder Struktur (hier vereinfacht: Background setzen)
            // Da wir Custom Painting in vielen Panels haben, nutzen diese direkt die Variablen cBg... beim repaint()
            c.setBackground(cBgDark); 
        }
        if(c instanceof Container) {
            for(Component child : ((Container)c).getComponents()) {
                updateContainerColors(child);
            }
        }
    }

    // ------------------------------------------

    private void initWindow() {
        setTitle("NotenManager // v5.0");
        setSize(1300, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        setContentPane(layeredPane);

        JPanel mainUI = new JPanel(new BorderLayout());
        mainUI.setBackground(cBgDark);
        
        mainUI.add(createHeader(), BorderLayout.NORTH);
        mainUI.add(createSidebar(), BorderLayout.WEST);
        
        content.setBackground(cBgDark);
        content.add(createDashboardView(), "DASH");
        content.add(createNotesView(), "NOTES");
        content.add(createStatsView(), "STATS");
        content.add(createFocusView(), "FOCUS");
        content.add(createTodoView(), "TODO");
        content.add(createExportView(), "EXPORT");
        
        mainUI.add(content, BorderLayout.CENTER);
        mainUI.add(createStatusBar(), BorderLayout.SOUTH);

        layeredPane.add(toastHost, JLayeredPane.POPUP_LAYER); 
        layeredPane.add(confettiLayer, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(mainUI, JLayeredPane.DEFAULT_LAYER);

        toastHost.setVisible(true);
        confettiLayer.setVisible(true);

        refreshAll();
        setVisible(true);
    }

    // =========================================================
    // Layout Aufbau
    // =========================================================
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(cBgDark); // Dynamischer Background
            }
        };
        header.setBorder(new EmptyBorder(25, 25, 15, 25));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("Notenverwaltungs System");
        title.setFont(new Font("Verdana", Font.BOLD, 24));
        title.setForeground(cAccent1); // Nutzung der Variable statt Konstante
        
        JLabel subtitle = new JLabel("");
        subtitle.setForeground(cAccent2);
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        left.add(title);
        left.add(subtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setOpaque(false);
        
        // THEME SWITCHER
        themeSwitchBtn = new ModernButton("☀ LIGHT MODE", cAccent2, cBgDark);
        themeSwitchBtn.addActionListener(e -> toggleTheme());

        JButton quickAdd = new ModernButton("+ NEUE NOTE", cAccent1, cTextMain);
        quickAdd.addActionListener(e -> {
            cards.show(content, "NOTES");
            fachField.requestFocusInWindow();
            toastHost.showToast("Eingabemodus Aktiv", ToastKind.INFO);
            navButtons.forEach(b -> b.setSelectedState(false));
        });

        right.add(themeSwitchBtn);
        right.add(Box.createHorizontalStrut(15));
        right.add(quickAdd);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
             @Override protected void paintComponent(Graphics g) {
                setBackground(cBgDark);
                super.paintComponent(g);
             }
        };
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(new EmptyBorder(10, 15, 15, 10));

        JPanel container = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                setBackground(cBgPanel); // Dynamisch
                super.paintComponent(g);
            }
        };
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(cBgInput),
            new EmptyBorder(20, 10, 20, 10)
        ));

        container.add(navLabel("DASHBOARD"));
        container.add(createNavBtn("Übersicht", "DASH", true));
        container.add(Box.createVerticalStrut(8));
        container.add(createNavBtn("Noten-Datenbank", "NOTES", false));
        container.add(Box.createVerticalStrut(8));
        container.add(createNavBtn("3D-Statistik", "STATS", false));
        container.add(Box.createVerticalStrut(20));
        
        container.add(navLabel("TOOLS"));
        container.add(createNavBtn("Aufgaben-Planer", "TODO", false)); 
        container.add(Box.createVerticalStrut(8));
        container.add(createNavBtn("Fokus-Timer", "FOCUS", false));
        container.add(Box.createVerticalStrut(8));
        container.add(createNavBtn("Daten-Export", "EXPORT", false));
        
        container.add(Box.createVerticalGlue());
        
        quoteLabel = new JLabel("<html><i>\"Erfolg ist eine Treppe, keine Tür.\"</i></html>");
        quoteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        quoteLabel.setForeground(cTextMuted);
        quoteLabel.setBorder(new EmptyBorder(10, 5, 10, 5));
        container.add(quoteLabel);

        sidebar.add(container, BorderLayout.CENTER);
        return sidebar;
    }

    private JLabel navLabel(String text) {
        JLabel l = new JLabel(text) {
             @Override protected void paintComponent(Graphics g) {
                 setForeground(cAccent2);
                 super.paintComponent(g);
             }
        };
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setBorder(new EmptyBorder(0, 8, 10, 0));
        return l;
    }

    private ModernNavButton createNavBtn(String text, String card, boolean active) {
        ModernNavButton b = new ModernNavButton(text);
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
    // Views
    // =========================================================

    private JPanel createDashboardView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false); // Transparenz für Hintergrundfarbe
        root.setBorder(new EmptyBorder(10, 25, 25, 25));

        JPanel topGrid = new JPanel(new GridLayout(1, 2, 25, 0));
        topGrid.setOpaque(false);

        // Links: Performance Ring
        JPanel ringCard = createCard("LEISTUNGS-INDEX");
        ring = new PerformanceRing();
        
        kpiAvg = new JLabel("0.0") {
             @Override protected void paintComponent(Graphics g) {
                 setForeground(cTextMain);
                 super.paintComponent(g);
             }
        };
        kpiAvg.setFont(new Font("Segoe UI", Font.BOLD, 48));
        kpiAvg.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel ringWrapper = new JPanel(new BorderLayout());
        ringWrapper.setOpaque(false);
        ringWrapper.add(ring, BorderLayout.CENTER);
        ringWrapper.add(kpiAvg, BorderLayout.SOUTH);
        
        badgesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        badgesPanel.setOpaque(false);
        badgesPanel.setBorder(new EmptyBorder(15,0,0,0));
        
        ringCard.add(ringWrapper, BorderLayout.CENTER);
        ringCard.add(badgesPanel, BorderLayout.SOUTH);

        // Rechts: KPIs
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 25));
        rightPanel.setOpaque(false);

        JPanel kpiGrid = new JPanel(new GridLayout(1, 2, 15, 15));
        kpiGrid.setOpaque(false);
        kpiCritical = kpiValue("0");
        kpiCount = kpiValue("0");
        kpiGrid.add(createTile("KRITISCH (>4.0)", kpiCritical, cDanger)); // Wir übergeben hier Colors, aber Achtung bei Update
        kpiGrid.add(createTile("GESAMT", kpiCount, cAccent1));

        JPanel kpiContainer = createCard("METRIKEN");
        kpiContainer.add(kpiGrid, BorderLayout.CENTER);
        
        rightPanel.add(kpiContainer);
        JPanel spacer = createCard("STATUS");
        JLabel l = new JLabel("System läuft stabil.");
        l.setForeground(cSuccess);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        spacer.add(l, BorderLayout.CENTER);
        rightPanel.add(spacer);
        
        topGrid.add(ringCard);
        topGrid.add(rightPanel);
        root.add(topGrid, BorderLayout.CENTER);
        return root;
    }
    
    private JPanel createTodoView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 25, 25, 25));
        
        JPanel todoCard = createCard("AUFGABEN-PLANER (TO-DO LISTE)");
        todoModel = new DefaultListModel<>();
        todoList = new JList<>(todoModel);
        todoList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        todoList.setFixedCellHeight(30);
        todoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JTextField todoField = createStyledTextField("Neue Aufgabe eingeben...");
        JButton addBtn = new ModernButton("HINZUFÜGEN", cAccent1, cTextMain);
        
        addBtn.addActionListener(e -> {
            if(!todoField.getText().isEmpty()) {
                todoModel.addElement("☐  " + todoField.getText());
                todoField.setText("");
            }
        });
        
        todoList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = todoList.locationToIndex(e.getPoint());
                if(index >= 0) {
                    String item = todoModel.getElementAt(index);
                    if(item.startsWith("☐")) {
                        todoModel.set(index, "☑  " + item.substring(3)); 
                    } else if (e.getClickCount() == 2) {
                        todoModel.remove(index); 
                    }
                }
            }
        });
        
        JLabel hint = new JLabel("Tipp: Doppelklick zum Löschen, Einfacher Klick zum Abhaken.");
        hint.setForeground(cTextMuted);
        
        inputPanel.add(todoField, BorderLayout.CENTER);
        inputPanel.add(addBtn, BorderLayout.EAST);
        
        JPanel bottomWrap = new JPanel(new BorderLayout());
        bottomWrap.setOpaque(false);
        bottomWrap.add(hint, BorderLayout.NORTH);
        bottomWrap.add(inputPanel, BorderLayout.SOUTH);
        
        todoCard.add(new JScrollPane(todoList), BorderLayout.CENTER);
        todoCard.add(bottomWrap, BorderLayout.SOUTH);
        
        root.add(todoCard, BorderLayout.CENTER);
        return root;
    }
    
    private JPanel createFocusView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 25, 25, 25));
        
        JPanel card = createCard("DEEP WORK SESSION");
        focusTimerPanel = new FocusTimerPanel();
        card.add(focusTimerPanel, BorderLayout.CENTER);
        
        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private JPanel createNotesView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 25, 25, 25));

        JPanel inputCard = createCard("NEUEN EINTRAG ERSTELLEN");
        
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        formPanel.setOpaque(false);
        
        fachField = createStyledTextField("");
        fachField.setPreferredSize(new Dimension(250, 35));
        
        noteField = createStyledTextField("");
        noteField.setPreferredSize(new Dimension(150, 35));
        
        formPanel.add(createInputGroup("Fachbezeichnung:", fachField));
        formPanel.add(createInputGroup("Note (1.0 - 6.0):", noteField));
        
        JButton btnSave = new ModernButton("SPEICHERN", cSuccess, cBgDark);
        btnSave.setPreferredSize(new Dimension(120, 35));
        JButton btnReset = new ModernButton("RESET DB", cDanger, cTextMain);
        btnReset.setPreferredSize(new Dimension(120, 35));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(22, 0, 0, 0)); 
        btnPanel.add(btnSave);
        btnPanel.add(btnReset);
        
        formPanel.add(btnPanel);
        
        btnSave.addActionListener(e -> actionAddNote());
        btnReset.addActionListener(e -> actionReset());

        inputCard.add(formPanel, BorderLayout.CENTER);

        JPanel tableCard = createCard("DATENREGISTER");
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
        sp.getViewport().setBackground(cBgPanel);
        tableCard.add(sp, BorderLayout.CENTER);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(inputCard, BorderLayout.NORTH);
        topContainer.add(Box.createVerticalStrut(20), BorderLayout.CENTER);
        
        root.add(topContainer, BorderLayout.NORTH);
        root.add(tableCard, BorderLayout.CENTER);
        return root;
    }
    
    private JPanel createInputGroup(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel l = new JLabel(labelText);
        l.setForeground(cTextMuted);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel createStatsView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(10, 25, 25, 25));
        
        JPanel card = createCard("3D NOTENVERTEILUNG");
        statsChart = new StatsChart();
        card.add(statsChart, BorderLayout.CENTER);
        
        card.add(createCalculatorTile(), BorderLayout.SOUTH);
        
        root.add(card, BorderLayout.CENTER);
        return root;
    }
    
    private JPanel createExportView() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);
        
        JPanel card = createCard("DATEN-EXPORT");
        card.setPreferredSize(new Dimension(500, 250));
        
        JTextArea txt = new JTextArea("Generiert ein Abbild der aktuellen Datenbank.\nFormat: Plain Text (.txt)\nSpeicherort: Projektverzeichnis");
        txt.setOpaque(false);
        txt.setForeground(cTextMuted);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setEditable(false);
        txt.setBorder(new EmptyBorder(10, 10, 20, 10));
        
        JButton btn = new ModernButton("EXPORT STARTEN", cAccent1, cTextMain);
        btn.addActionListener(e -> actionExport());
        
        card.add(txt, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        
        root.add(card);
        return root;
    }
    
    private JPanel createCalculatorTile() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JLabel title = new JLabel("ZIEL-RECHNER");
        title.setForeground(cAccent2);
        title.setFont(new Font("Segoe UI", Font.BOLD, 10));
        
        JPanel body = new JPanel(new FlowLayout(FlowLayout.LEFT));
        body.setOpaque(false);
        
        targetField = createStyledTextField("Wunsch-Ø");
        targetField.setPreferredSize(new Dimension(90, 30));
        
        JButton calcBtn = new ModernButton("Berechnen", cAccent1, cTextMain);
        calcBtn.setPreferredSize(new Dimension(100, 30));
        
        targetResultLabel = new JLabel(" -> ?");
        targetResultLabel.setForeground(cTextMain);
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
            toastHost.showToast("FEHLER: Ungültige Eingabe", ToastKind.INFO);
            return;
        }
        
        verwaltung.noteHinzufuegen(new Note(new Fach(f), val));
        fachField.setText("");
        noteField.setText("");
        fachField.requestFocus();
        
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
            toastHost.showToast("Update fehlgeschlagen", ToastKind.INFO);
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
        
        String col = (needed < 1.0) ? "#A3BE8C" : (needed > 4.0 ? "#BF616A" : "#ECEFF4");
        targetResultLabel.setText("<html>-> <span style='color:"+col+"'>" + needed + "</span></html>");
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
            toastHost.showToast("Schreibfehler", ToastKind.INFO);
        }
    }

    private void refreshAll() {
        notenModel.setRowCount(0);
        List<Note> list = getInternalNotenListe();
        for(Note n : list) notenModel.addRow(new Object[]{ n.getFach().getFachName(), n.getWert(), getBewertung(n.getWert()) });
        
        double avg = list.isEmpty() ? 0.0 : verwaltung.durchschnittBerechnen();
        ring.setValue(avg);
        kpiAvg.setText(TWO_DEC.format(avg));
        kpiCount.setText(String.valueOf(list.size()));
        
        long crit = list.stream().filter(n -> n.getWert() > 4.0).count();
        kpiCritical.setText(String.valueOf(crit));
        
        badgesPanel.removeAll();
        if(list.size() > 0) {
            if(avg < 1.5) badgesPanel.add(createBadge("ELITE", cSuccess));
            else if(avg < 2.5) badgesPanel.add(createBadge("GUT", cAccent1));
            if(crit == 0 && list.size() > 2) badgesPanel.add(createBadge("SAUBER", cWarning));
            if(list.size() >= 5) badgesPanel.add(createBadge("VETERAN", cAccent2));
        }
        badgesPanel.revalidate();
        badgesPanel.repaint();
        
        if(statsChart != null) statsChart.updateData(list);
    }

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
    // Custom UI Klassen
    // =========================================================

    private class ConfettiLayer extends JComponent {
        private final List<Particle> particles = new ArrayList<>();
        private Timer timer;

        ConfettiLayer() { setOpaque(false); setBounds(0,0,3000,3000); }
        void explode() {
            Random r = new Random();
            int cx = getWidth()/2; int cy = getHeight()/2;
            for(int i=0; i<50; i++) particles.add(new Particle(cx, cy, r));
            if(timer == null || !timer.isRunning()) {
                timer = new Timer(20, e -> update());
                timer.start();
            }
        }
        private void update() {
            Iterator<Particle> it = particles.iterator();
            while(it.hasNext()) {
                Particle p = it.next(); p.update();
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
        private class Particle {
            double x, y, vx, vy, life = 1.0, size; Color color;
            Particle(int startX, int startY, Random r) {
                x = startX; y = startY;
                double angle = r.nextDouble() * Math.PI * 2;
                double speed = r.nextDouble() * 10 + 5;
                vx = Math.cos(angle) * speed; vy = Math.sin(angle) * speed;
                size = r.nextDouble() * 8 + 4;
                Color[] cols = {cAccent1, cAccent2, cSuccess, cWarning};
                color = cols[r.nextInt(cols.length)];
            }
            void update() { x += vx; y += vy; vy += 0.5; life -= 0.02; }
        }
    }

    private class FocusTimerPanel extends JPanel {
        private Timer timer;
        private int totalSeconds = 25 * 60; 
        private int remaining = totalSeconds;
        private boolean running = false;
        private JLabel timeLbl;
        private float angle = 360f;

        FocusTimerPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);
            
            timeLbl = new JLabel("25:00") {
                 @Override protected void paintComponent(Graphics g) {
                     setForeground(cTextMain); super.paintComponent(g);
                 }
            };
            timeLbl.setFont(new Font("Monospaced", Font.BOLD, 60));
            timeLbl.setHorizontalAlignment(SwingConstants.CENTER);
            
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            controls.setOpaque(false);
            
            JButton toggle = new ModernButton("START / STOP", cAccent1, cTextMain);
            toggle.addActionListener(e -> toggleTimer());
            
            JButton reset = new ModernButton("RESET", cDanger, cTextMain);
            reset.addActionListener(e -> resetTimer());
            
            controls.add(toggle);
            controls.add(reset);
            
            JPanel center = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int s = Math.min(getWidth(), getHeight()) - 40;
                    int x = (getWidth()-s)/2, y = (getHeight()-s)/2;
                    
                    g2.setStroke(new BasicStroke(8));
                    g2.setColor(cBgInput);
                    g2.drawOval(x, y, s, s);
                    
                    g2.setColor(running ? cSuccess : cAccent1);
                    g2.drawArc(x, y, s, s, 90, (int)angle);
                }
            };
            center.setLayout(new GridBagLayout());
            center.setOpaque(false);
            center.add(timeLbl);
            
            add(center, BorderLayout.CENTER);
            add(controls, BorderLayout.SOUTH);
            
            timer = new Timer(1000, e -> tick());
        }
        
        void startAnimation() { repaint(); }
        
        private void toggleTimer() {
            if(running) timer.stop(); else timer.start();
            running = !running; repaint();
        }
        
        private void resetTimer() {
            timer.stop(); running = false; remaining = totalSeconds; angle = 360f;
            timeLbl.setText("25:00"); repaint();
        }
        
        private void tick() {
            if(remaining > 0) {
                remaining--;
                int m = remaining / 60; int s = remaining % 60;
                timeLbl.setText(String.format("%02d:%02d", m, s));
                angle = 360f * ((float)remaining / totalSeconds);
                repaint();
            } else {
                timer.stop(); running = false; timeLbl.setText("FERTIG");
            }
        }
    }

    private JPanel createCard(String title) {
        JPanel p = new JPanel(new BorderLayout()) {
             @Override protected void paintComponent(Graphics g) {
                 setBackground(cBgPanel); super.paintComponent(g);
             }
        };
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(cBgInput),
            new EmptyBorder(20, 20, 20, 20)
        ));
        JLabel l = new JLabel(title) {
             @Override protected void paintComponent(Graphics g) {
                 setForeground(cTextMuted); super.paintComponent(g);
             }
        };
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setBorder(new EmptyBorder(0, 0, 15, 0));
        p.add(l, BorderLayout.NORTH);
        return p;
    }
    
    private JLabel createBadge(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
        l.setForeground(c);
        l.setFont(new Font("Monospaced", Font.BOLD, 12));
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(c),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return l;
    }

    private JPanel createTile(String title, JLabel valLabel, Color c) { // Color nicht mehr im paint fix
        JPanel p = new JPanel(new BorderLayout()) {
             @Override protected void paintComponent(Graphics g) {
                 setBackground(cBgInput); super.paintComponent(g);
             }
        };
        p.setBorder(BorderFactory.createLineBorder(cBgDark));
        JLabel t = new JLabel(title) {
             @Override protected void paintComponent(Graphics g) {
                 setForeground(cTextMuted); super.paintComponent(g);
             }
        };
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

    private JTextField createStyledTextField(String ph) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(cTextMuted);
                    g.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    g.drawString(ph, 5, getHeight()/2+5);
                }
            }
        };
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(cBgInput.darker()),
            new EmptyBorder(10, 10, 10, 10)
        ));
        tf.setBackground(cBgInput);
        tf.setForeground(cTextMain);
        tf.setCaretColor(cAccent1);
        return tf;
    }
    
    // --- 3D STATS CHART ---
    private class StatsChart extends JComponent {
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
            
            // Boden
            g2.setColor(new Color(0,0,0,30));
            g2.drawLine(0, h-30, w, h-30);
            
            if(maxCount == 0) {
                g2.setColor(cTextMuted);
                g2.drawString("KEINE DATEN", w/2 - 40, h/2);
                return;
            }
            
            int depth = 10; // Tiefe für 3D

            for(int i=0; i<6; i++) {
                int count = distribution[i];
                if(count == 0) continue;
                int barH = (int) ((double)count / maxCount * (h-60));
                int x = i * (w/6) + 15;
                int y = (h - 30) - barH;
                
                // 3D Effekt: Seite und Oben zeichnen
                // Oben
                g2.setColor(cAccent1.brighter());
                Polygon top = new Polygon();
                top.addPoint(x, y);
                top.addPoint(x + depth, y - depth);
                top.addPoint(x + barWidth + depth, y - depth);
                top.addPoint(x + barWidth, y);
                g2.fillPolygon(top);
                
                // Seite (Rechts)
                g2.setColor(cAccent1.darker());
                Polygon side = new Polygon();
                side.addPoint(x + barWidth, y);
                side.addPoint(x + barWidth + depth, y - depth);
                side.addPoint(x + barWidth + depth, y - depth + barH);
                side.addPoint(x + barWidth, y + barH);
                g2.fillPolygon(side);
                
                // Front
                g2.setColor(cAccent1);
                g2.fillRect(x, y, barWidth, barH);
                
                // Text
                g2.setColor(cTextMain);
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2.drawString((i+1) + ".0", x + barWidth/2 - 10, h - 10);
                
                g2.setColor(cTextMuted);
                g2.drawString(String.valueOf(count), x + barWidth/2 - 4, y - depth - 5);
            }
        }
    }

    private class PerformanceRing extends JComponent {
        private double value = 0;
        void setValue(double v) { this.value = v; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight()) - 20;
            int x = (getWidth()-s)/2, y = (getHeight()-s)/2;
            g2.setStroke(new BasicStroke(15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(cBgInput);
            g2.drawArc(x, y, s, s, 0, 360);
            double normalized = 1.0 - ((value - 1.0) / 5.0); 
            if(normalized < 0) normalized = 0;
            Color c1 = value <= 2.0 ? cSuccess : (value <= 4.0 ? cWarning : cDanger);
            g2.setColor(c1);
            g2.drawArc(x, y, s, s, 90, -(int)(normalized * 360));
        }
    }

    private class ModernButton extends JButton {
        private final Color c1, fg;
        ModernButton(String t, Color start, Color text) {
            super(t);
            this.c1 = start; this.fg = text;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(10,20,10,20));
            setForeground(fg);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c1);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
            super.paintComponent(g);
        }
    }

    private class ModernNavButton extends JButton {
        boolean selected = false;
        ModernNavButton(String t) {
            super(t);
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(12, 20, 12, 20));
            setForeground(cTextMuted);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        void setSelectedState(boolean b) { selected = b; setForeground(b ? cTextMain : cTextMuted); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            if(selected) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(cAccent1.getRed(), cAccent1.getGreen(), cAccent1.getBlue(), 30));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(cAccent1);
                g2.fillRect(0, 0, 4, getHeight());
            }
            super.paintComponent(g);
        }
    }
    
    private JPanel createStatusBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT)) {
             @Override protected void paintComponent(Graphics g) {
                 setBackground(cBgDark); super.paintComponent(g);
             }
        };
        status.setForeground(cTextMuted);
        status.setFont(new Font("Monospaced", Font.PLAIN, 10));
        p.add(status);
        return p;
    }

    private class ToastHost extends JPanel {
        ToastHost() { setLayout(null); setOpaque(false); }
        void showToast(String msg, ToastKind k) {
            JLabel l = new JLabel(msg);
            l.setOpaque(true);
            l.setBackground(k == ToastKind.INFO ? cDanger : (k == ToastKind.SUCCESS ? cSuccess : cBgPanel));
            l.setForeground(k == ToastKind.SUCCESS ? cBgDark : cTextMain);
            l.setFont(new Font("Monospaced", Font.BOLD, 12));
            l.setBorder(new EmptyBorder(12, 25, 12, 25));
            int w = 300; int h = 45;
            l.setBounds(getWidth()/2 - w/2, getHeight() - 120, w, h);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            add(l); repaint();
            new Timer(3000, e -> { remove(l); repaint(); ((Timer)e.getSource()).stop(); }).start();
        }
    }
    
    private class RatingRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
            String s = String.valueOf(v);
            if(s.equals("Sehr Gut") || s.equals("Gut")) comp.setForeground(cSuccess);
            else if(s.contains("Mangel") || s.contains("Ungenügend")) comp.setForeground(cDanger);
            else comp.setForeground(cTextMain);
            return comp;
        }
    }
}
