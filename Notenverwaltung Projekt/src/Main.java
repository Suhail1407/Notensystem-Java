public class Main {

    public static void main (String[] args) {

    Schueler schueler1 = new Schueler("Anas", 18);

    Fach Mathe = new Fach("Mathematik");
    Fach Deutsch = new Fach("Deutsch");

    Note noteMathe = new Note(Mathe, 1.2);
    Note noteDeutsch = new Note(Deutsch, 1.3);

    NotenVerwaltung verwaltung = new NotenVerwaltung();
    verwaltung.noteHinzufuegen(noteDeutsch);
    verwaltung.noteHinzufuegen(noteMathe);

    System.out.println("---Notenverwaltungssystem---");
    System.out.println("Schüler: " + schueler1.getName());
    System.out.println("Anzahl der Noten: " + verwaltung.getNoten().size());
    System.out.println("Durchschnitt: " + verwaltung.durchschnittBerechnen());
    System.out.println("---------------");

    System.out.println("Starte grafische Oberfläche...");
    new NotenGUI(verwaltung);





    }
}