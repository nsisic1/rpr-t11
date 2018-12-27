package ba.unsa.etf.rpr;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        //System.out.println("Gradovi su:\n" + ispisiGradove());
        //glavniGrad();
        GeografijaDAO geo = GeografijaDAO.getInstance();
        System.out.println("Tntntnnt");
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
        System.out.println("Drzavu: ");
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