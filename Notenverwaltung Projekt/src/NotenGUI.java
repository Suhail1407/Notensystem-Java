// Wichtig: Imports für die Swing-GUI
import javax.swing.JFrame;
import javax.swing.JLabel;

// Platzhalter für deine GUI-Klasse (Person 4)
public class NotenGUI {

    // Konstruktor, wie in 4.4 beschrieben
    public NotenGUI(NotenVerwaltung verwaltung) {
        
        System.out.println("DEBUG: NotenGUI wurde gestartet!");
        
        // Erstelle ein minimales Test-Fenster
        // JFrame ist die Klasse für ein Fenster
        JFrame frame = new JFrame("Notenverwaltung (GUI-Test)"); 
        
        // Größe des Fensters festlegen
        frame.setSize(400, 300);
        
        // Wichtig: Programm beenden, wenn Fenster geschlossen wird
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 

        // Ein Text-Label erstellen
        JLabel label = new JLabel("GUI gestartet. Zugriff auf " + 
                                  verwaltung.getNoten().size() + " Noten.");
        
        // Das Label zum Fenster hinzufügen
        frame.add(label);
        
        // Mach das Fenster sichtbar
        frame.setVisible(true); 
    }
}