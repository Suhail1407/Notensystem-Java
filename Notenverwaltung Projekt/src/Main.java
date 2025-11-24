public class Main {
    public static void main(String[] args) {

        Schueler s1 = new Schueler("JohnIstSchwul", 16);

        // Mehrere Fächer
        Fach mathe = new Fach("Mathe");
        Fach deutsch = new Fach("Deutsch");
        Fach englisch = new Fach("Englisch");
        Fach physik = new Fach("Physik");
        Fach informatik = new Fach("Informatik");

        // Ausgabe
        System.out.println("Schueler: " + s1);
        System.out.println("Fächer:");
        System.out.println(mathe);
        System.out.println(deutsch);
        System.out.println(englisch);
        System.out.println(physik);
        System.out.println(informatik);
    }
}


