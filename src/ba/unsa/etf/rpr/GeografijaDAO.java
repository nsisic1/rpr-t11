package ba.unsa.etf.rpr;

import java.sql.*;
import java.util.ArrayList;

public class GeografijaDAO {

    private static GeografijaDAO instance = null;

    private static Connection conn;

    private static PreparedStatement nadjiDrzavu;
    private static PreparedStatement nadjiGlavniGradDrzave;
    private static PreparedStatement obrisiDrzavuNeIGradove;
    private static PreparedStatement obrisiGradoveDrzave;
    private static PreparedStatement nadjiGradoveSortBrStanovnika;
    private static PreparedStatement unesiGrad;
    private static PreparedStatement unesiDrzavu;
    private static PreparedStatement promijeniGrad;





    private static void initialize() throws SQLException { // TODO: zabiljezi: staticna metoda, kreira intancu
        instance = new GeografijaDAO();
    }

    private GeografijaDAO() throws SQLException {
        String url = "jdbc:sqlite:baza.db";
        System.out.println("HEHEHEHH");

        conn = DriverManager.getConnection(url);
        try {
            pripremiUpite();
            generirajTabeleAkoNePostoje();

        } catch (SQLException e) {
            System.out.println("ACACACCA");
        }
    }

    public static GeografijaDAO getInstance() {
        try {
            if (instance == null) {
                initialize();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public static void removeInstance() {
        if (conn == null) {
            return; // Izgleda da je potrebno
        }
        try {
            conn.close();
            conn = null;
        } catch (SQLException ex) {
            System.out.println("BWBWBBWBWBBW");
            ex.printStackTrace();
        }
        instance = null;
    }

    private void generirajTabeleAkoNePostoje() throws SQLException { // TODO: kad se koji bacaju izuzetci

        // TODO ovako uraditi: provjeriti je li postoji BAZA (try baza* nesto catch), ako postoji nista ne raditi
        // * bilo koji upit (?)

        Statement stmt = conn.createStatement();
        String generirajDrzave = "CREATE TABLE \"drzava\" ( `id` INTEGER, `naziv` TEXT, `glavni_grad` INTEGER," +
                "FOREIGN KEY(`glavni_grad`) REFERENCES `grad`, PRIMARY KEY(`id`) )";
        try {
            stmt.executeQuery(generirajDrzave);
        } catch (SQLException e) {
            // Drzave vec postoje
        }
        String generirajGradove = "CREATE TABLE \"grad\" ( `id` INTEGER, `naziv` TEXT, `broj_stanovnika` INTEGER," +
                " `drzava` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`drzava`) REFERENCES `drzava` )";
        try {
            stmt.executeQuery(generirajGradove);
        } catch (SQLException e) {
            // Gradovi vec postoje
        }

        popuniTabele();
    }


    Grad glavniGrad(String drzava) {
        if (nadjiDrzavu(drzava) == null) {
            return null;
        }
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT grad.naziv, broj_stanovnika, drzava.naziv," +
                    "FROM grad, drzava WHERE drzava.naziv = ? AND grad.id = drzava.id");
            stmt.setString(1, drzava);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                if (resultSet.getString(3).equals(drzava)) {
                    Grad grad = new Grad();
                    grad.setNaziv(resultSet.getString(1));
                    grad.setBrojStanovnika(resultSet.getInt(2));
                    Drzava d = new Drzava();
                    d.setNaziv(resultSet.getString(3));
                    d.setGlavniGrad(grad);
                    grad.setDrzava(d);
                    return grad;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // u slucaju da ne postoji glavni grad ?
    }

    public void obrisiDrzavu(String drzava) {
        Drzava d = nadjiDrzavu(drzava);
        try {
            if (d == null) {
                return;
            }
            // Prvo brisemo gradove, potreban je id drzave
            izbrisiGradove(drzava);

            PreparedStatement stmt = conn.prepareStatement("DELETE FROM drzava WHERE naziv = ?");
            stmt.setString(1, drzava);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void izbrisiGradove(String drzava) throws SQLException {
        int dID; // ID drzave u bazi
        String nadjiId = "SELECT id FROM drzava WHERE naziv = ?";
        PreparedStatement stmt = conn.prepareStatement(nadjiId);
        stmt.setString(1, drzava);
        ResultSet resultSet = stmt.executeQuery();
        resultSet.next();
        dID = resultSet.getInt(1);

        stmt = conn.prepareStatement("DELETE FROM grad WHERE drzava = ?");
        stmt.setInt(1, dID);
        stmt.executeUpdate();
    }

    public ArrayList<Grad> gradovi() {
        ArrayList<Grad> retval = new ArrayList<Grad>();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT grad.id, grad.naziv, broj_stanovnika, drzava, drzava.naziv, " +
                    "drzava.glavniGrad FROM grad, drzava WHERE grad.drzava = drzava.id ORDER BY broj_stanovnika DESC");
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Grad grad = new Grad();
                Drzava d = new Drzava();
                grad.setNaziv(resultSet.getString(2));
                grad.setBrojStanovnika(resultSet.getInt(3));
                d.setNaziv(resultSet.getString(6));
                // Prolazeci sve gradove u bazi ovom while petljom, sigurno cemo proci i kroz sve gradove koji su ujedno
                // i glavni gradovi svojih drzava, to provjeravamo ovdje*
                if (resultSet.getInt(1) == resultSet.getInt(6)) {
                    d.setGlavniGrad(grad);
                }
                // * TODO: NE; trebamo iterirati kroz retval svaki put kad nadjemo na drzavu da vidimo je li vec kreirana

                grad.setDrzava(d);
                retval.add(grad);
            }
            return retval;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void dodajGrad(Grad grad) {
        try {
            // return ako grad vec postoji?

            // Nadjemo id drzave
            int dID;
            String nadjiId = "SELECT id FROM drzava WHERE naziv = ?";
            PreparedStatement stmt = conn.prepareStatement(nadjiId);
            System.out.println(grad.getNaziv());
            System.out.println(grad.getDrzava().getNaziv());
            System.out.println("BBBBBBBBBBBBBBBBBBBBBB");

            stmt.setString(1, grad.getDrzava().getNaziv());
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            dID = resultSet.getInt(1);

            stmt = conn.prepareStatement("INSERT OR REPLACE INTO gradovi(naziv, brojStanovnika, drzava) VALUES(?, ?, ?)");
            stmt.setString(1, grad.getNaziv());
            stmt.setInt(2, grad.getBrojStanovnika());
            stmt.setInt(3, dID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void dodajDrzavu(Drzava drzava) {
        // TODO: uradi
    }

    public void izmijeniGrad(Grad grad) {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE grad SET naziv = ?, brojStanovnika = ?, drzava = ? WHERE id = ?");
            stmt.setString(1, grad.getNaziv());
            stmt.setInt(2, grad.getBrojStanovnika());
            // TODO naci id drzave
            // TODO: naci id grada
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Drzava nadjiDrzavu(String drzava) {
        Drzava d = new Drzava();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT naziv, glavni_grad FROM drzava WHERE naziv = ?");
            stmt.setString(1, drzava);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.isClosed()) {
                return null;
            }
            d.setNaziv(resultSet.getString(1));
            return d;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void dodajNovuDrzava(String nazivDrzave, String nazivGrada, int brojStanovnika, boolean glavni) {
        Grad noviGrad = new Grad();
        noviGrad.setNaziv(nazivGrada);
        noviGrad.setBrojStanovnika(brojStanovnika);
        Drzava novaDrzava = new Drzava();
        novaDrzava.setNaziv(nazivDrzave);
        if (glavni) {
            novaDrzava.setGlavniGrad(noviGrad);
        }
        dodajDrzavu(novaDrzava);
        dodajGrad(noviGrad);
    }

    private static void pripremiUpite() {
        try {
            nadjiGlavniGradDrzave = conn.prepareStatement("SELECT grad.naziv, broj_stanovnika, drzava.naziv, " +
                    "FROM grad, drzava WHERE drzava.naziv = ? AND drzava.grad = grad.id");
            obrisiDrzavuNeIGradove = conn.prepareStatement("DELETE FROM drzava WHERE naziv = ?");
            obrisiGradoveDrzave = conn.prepareStatement("DELETE FROM grad WHERE drzava = ?");
            /* nadjiGradoveSortBrStanovnika = conn.prepareStatement(
                    "SELECT grad.id, grad.naziv, broj_stanovnika, drzava, drzava.naziv, drzava.glavniGrad " +
                    "FROM grad, drzava WHERE grad.drzava = drzava.id ORDER BY broj_stanovnika DESC"); */ // TODO: zabiljezi: mozda??? je bolje ne ovu veliku nego ovu obicnu a za nalazenje drzave ono za drzavu
            nadjiGradoveSortBrStanovnika = conn.prepareStatement("SELECT id, naziv, brojStanovnika, drzava " +
                    " FROM grad ORDER BY broj_stanovnika DESC");
            unesiGrad = conn.prepareStatement("INSERT OR REPLACE INTO gradovi(naziv, brojStanovnika, drzava) VALUES(?, ?, ?)");
            unesiDrzavu = conn.prepareStatement(""); // TODO
            promijeniGrad = conn.prepareStatement("UPDATE grad SET naziv = ?, brojStanovnika = ?, drzava = ? WHERE id = ?");
            nadjiDrzavu = conn.prepareStatement("SELECT id, naziv, grad FROM drzava WHERE naziv = ?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void popuniTabele() {
        // Pariz
        Grad pariz = new Grad();
        pariz.setNaziv("Pariz");
        pariz.setBrojStanovnika(2200000);
        Drzava francuska = new Drzava();
        francuska.setNaziv("Francuska");
        francuska.setGlavniGrad(pariz);
        pariz.setDrzava(francuska);
        dodajDrzavu(francuska);
        dodajGrad(pariz);

        Grad london = new Grad();
        london.setNaziv("London");
        london.setBrojStanovnika(8136000);
        Drzava velikaBritanija = new Drzava();
        velikaBritanija.setNaziv("Velika Britanija");
        velikaBritanija.setGlavniGrad(london);
        london.setDrzava(velikaBritanija);
        dodajDrzavu(velikaBritanija);
        dodajGrad(london);

        Grad bec = new Grad();
        bec.setNaziv("Beč");
        bec.setBrojStanovnika(1867000);
        Drzava austrija = new Drzava();
        austrija.setNaziv("Austrija");
        austrija.setGlavniGrad(bec);
        bec.setDrzava(austrija);
        dodajDrzavu(austrija);
        dodajGrad(bec);

        Grad mancester = new Grad();
        mancester.setNaziv("Manchester");
        mancester.setBrojStanovnika(510746);
        mancester.setDrzava(velikaBritanija);
        dodajGrad(mancester);

        Grad graz = new Grad();
        graz.setNaziv("Graz");
        graz.setBrojStanovnika(283869);
        graz.setDrzava(austrija);
        dodajGrad(graz);
    }

}


// TODO nakon iteriranja zatvoriti kursor kojim iteriramo kroz resultSet; mada ce se vjerovatno sam zatvoriti
// moze se i desiti da baza ostane zauzeta (neko vrijeme?) ako neki upit nije kako treba
// getGradFromResultSet