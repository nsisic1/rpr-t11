package ba.unsa.etf.rpr;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        GeografijaDAO geo = GeografijaDAO.getInstance();
        glavniGrad();
    }

    public static String ispisiGradove(){
        ArrayList<Grad> gradovi = GeografijaDAO.getInstance().gradovi();
        String retval = "";
        for (Grad grad: gradovi) {
            retval += grad.getNaziv() + " (" + grad.getDrzava().getNaziv() + ")" + " - " +
                    grad.getBrojStanovnika() + "\n";
        }
        return retval;
    }

    public static void glavniGrad(){
        System.out.println("Unesite drzavu: ");
        Scanner scanner = new Scanner(System.in);
        String drzava = scanner.nextLine();
        Grad grad = GeografijaDAO.getInstance().glavniGrad(drzava);
        if (grad != null) {
            System.out.println("Glavni grad države " + drzava + " je " + grad.getNaziv());
        }
        else {
            System.out.println("Nepostojeca država");
        }

    }
}

// morao sam prebaciti (smanjiti) verziju jave na 8

// https://stackoverflow.com/questions/52960935/android-studio-database-file-loaded-in-the-wrong-encoding-utf-8

// Maven: rucno sam promijenio package sa "main.java.ba.unsa.etf.rpr;"  na "ba.unsa.etf.rpr;"

// promijenio modul Modul name u "rpr-t11"

// radio i sa obicnim jar-m, nije trebao fat jar
// baza se ne prikaze u idea ali radi program, ako pokrenem iz windows explorera jar onda se prikaze baza i u njemu,
// i u idea-i ako zatvorim i otvorim folder u stablu