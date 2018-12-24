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
    private static PreparedStatement nadjiGradoveSortBrStanovnikaD;
    private static PreparedStatement unesiGrad;
    private static PreparedStatement unesiDrzavu;
    private static PreparedStatement promijeniGrad;
    private static PreparedStatement nadjiDrzavuPoID;

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
            nadjiGlavniGradDrzave.setString(1, drzava);
            ResultSet resultSet = nadjiGlavniGradDrzave.executeQuery();

            if (resultSet.next()) { // trebao bi samo jedan
                Grad grad = new Grad();
                grad.setNaziv(resultSet.getString(1));
                grad.setBrojStanovnika(resultSet.getInt(2));
                Drzava d = new Drzava();
                d.setNaziv(resultSet.getString(drzava));
                d.setGlavniGrad(grad);
                grad.setDrzava(d);
                return grad;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // u slucaju da ne postoji glavni grad ?
    }

    public void obrisiDrzavu(String drzava) throws SQLException {
        nadjiDrzavu.setString(1, drzava);
        ResultSet resultSet = nadjiDrzavu.executeQuery();
        if (!resultSet.next()) {
            return; // nema drzave
        }
        Drzava d = nadjiDrzavu(drzava);
        try {
            izbrisiGradove(drzava); // Prvo brisemo sve gradove ove drzave
            obrisiDrzavuNeIGradove.setString(1, drzava);
            obrisiDrzavuNeIGradove.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void izbrisiGradove(String drzava) throws SQLException {
        nadjiDrzavu.setString(1, drzava);
        ResultSet resultSet = nadjiDrzavu.executeQuery();
        int drzavaId;
        if (resultSet.next()) {
            drzavaId = resultSet.getInt(1);
        } else {
            return;
        }
        obrisiGradoveDrzave.setInt(1, drzavaId);
        obrisiGradoveDrzave.executeUpdate();
    }

    public ArrayList<Grad> gradovi() {
        ArrayList<Grad> retval = new ArrayList<Grad>();

        try {

            //nadjiGradoveSortBrStanovnikaD = conn.prepareStatement("SELECT id, naziv, broj_stanovnika, drzava FROM grad ORDER BY broj_stanovnika DESC");
            ResultSet resultSet = nadjiGradoveSortBrStanovnikaD.executeQuery();
            ResultSet rSetDrzava;
            while (resultSet.next()) {
                Grad grad = new Grad();
                Drzava d = new Drzava();
                grad.setNaziv(resultSet.getString(2));
                grad.setBrojStanovnika(resultSet.getInt(3));

                // Nadjemo drzavu ovog grada, preko id-a
                nadjiDrzavuPoID.setInt(1, resultSet.getInt(4));
                rSetDrzava = nadjiDrzavuPoID.executeQuery();
                d.setNaziv(rSetDrzava.getString(2));
                // Prolazeci sve gradove u bazi ovom while petljom, sigurno cemo proci i kroz sve gradove koji su ujedno
                // i glavni gradovi svojih drzava, to provjeravamo ovdje*
                if (resultSet.getInt(1) == rSetDrzava.getInt(3)) {
                    d.setGlavniGrad(grad);
                }
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
            int drzavaId = 0;
            if (grad.getDrzava() != null) {
                nadjiDrzavu.setString(1, grad.getDrzava().getNaziv());
                ResultSet resultSet = nadjiDrzavu.executeQuery();
                if (resultSet.next()) {
                    drzavaId = resultSet.getInt(1);
                } else {
                    return;
                }
            }

            // INSERT OR REPLACE INTO grad(naziv, broj_stanovnika, drzava) VALUES(?, ?, ?)
            unesiGrad.setString(1, grad.getNaziv());
            unesiGrad.setInt(2, grad.getBrojStanovnika());

            if (grad.getDrzava() != null) {
                unesiGrad.setInt(3, drzavaId);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void dodajDrzavu(Drzava drzava) throws SQLException {
        unesiDrzavu.setString(1, drzava.getNaziv());
        // trazimo id glavnog grada
        nadjiGlavniGradDrzave.setString(1, drzava.getNaziv());
        ResultSet resultSet = nadjiGlavniGradDrzave.executeQuery();
        int glavniGradID = 0;
        if (resultSet.next()) {
            glavniGradID = resultSet.getInt(1);
            unesiDrzavu.setInt(2, glavniGradID);
        }
        unesiDrzavu.executeUpdate();
    }

    public void izmijeniGrad(Grad grad) {
        //             promijeniGrad = conn.prepareStatement("UPDATE grad SET naziv = ?, broj_stanovnika = ?, drzava = ? WHERE id = ?");
        try {
            promijeniGrad.setString(1, grad.getNaziv());
            promijeniGrad.setInt(2, grad.getBrojStanovnika());
            nadjiDrzavu.setString(1, grad.getDrzava().getNaziv());
            ResultSet resultSet = nadjiDrzavu.executeQuery();
            if (resultSet.next()) {
                promijeniGrad.setInt(3, resultSet.getInt(3));
            }
            promijeniGrad.executeUpdate();
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

    /*private void dodajNovuDrzava(String nazivDrzave, String nazivGrada, int brojStanovnika, boolean glavni) {
        Grad noviGrad = new Grad();
        noviGrad.setNaziv(nazivGrada);
        noviGrad.setBrojStanovnika(brojStanovnika);
        Drzava novaDrzava = new Drzava();
        novaDrzava.setNaziv(nazivDrzave);
        if (glavni) {
            novaDrzava.setGlavniGrad(noviGrad);
        }
        //dodajDrzavu(novaDrzava);
        dodajGrad(noviGrad);
    }*/

    private static void pripremiUpite() {
        try {
            nadjiGlavniGradDrzave = conn.prepareStatement("SELECT id, grad.naziv, broj_stanovnika FROM grad, drzava WHERE drzava.naziv = ? AND drzava.glavni_grad = grad.id;"); // TODO prepravi metode, dodao sam id ovdje
            obrisiDrzavuNeIGradove = conn.prepareStatement("DELETE FROM drzava WHERE naziv = ?");
            obrisiGradoveDrzave = conn.prepareStatement("DELETE FROM grad WHERE drzava = ?");
            nadjiGradoveSortBrStanovnikaD = conn.prepareStatement("SELECT id, naziv, broj_stanovnika, drzava " +
                    " FROM grad ORDER BY broj_stanovnika DESC");
            unesiGrad = conn.prepareStatement("INSERT OR REPLACE INTO grad(naziv, broj_stanovnika, drzava) VALUES(?, ?, ?)");
            unesiDrzavu = conn.prepareStatement("INSERT OR REPLACE INTO drzava(naziv, glavni_grad) VALUES(?, ?)");
            promijeniGrad = conn.prepareStatement("UPDATE grad SET naziv = ?, broj_stanovnika = ?, drzava = ? WHERE id = ?");
            nadjiDrzavu = conn.prepareStatement("SELECT id, naziv, glavni_grad FROM drzava WHERE naziv = ?");
            nadjiDrzavuPoID = conn.prepareStatement("SELECT id, naziv, glavni_grad FROM drzava WHERE id = ?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void popuniTabele() throws SQLException {
        PreparedStatement gradovi = conn.prepareStatement("INSERT INTO grad(naziv, broj_stanovnika, drzava) VALUES " +
                " (Pariz, 2200000), (London, 8136000), (Bec, 1867000), (Manchester, 510746), (Graz, 283869);");
        gradovi.executeUpdate();
        // TODO: preko settera, izbrisati ctor?
        Grad pariz = new Grad("Pariz", 2200000, null);
        Grad london = new Grad("London", 8136000, null);
        Grad bec = new Grad("Bec", 1867000, null);
        Grad manchester = new Grad("Manchester", 510746, null);
        Grad graz = new Grad("Graz", 283869, null);

        Drzava francuska = new Drzava();
        francuska.setNaziv("Francuska");
        francuska.setGlavniGrad(pariz);

        Drzava velikaB = new Drzava();
        velikaB.setNaziv("Velika Britanija");
        velikaB.setGlavniGrad(london);

        Drzava austrija = new Drzava();
        austrija.setNaziv("Austrija");
        austrija.setGlavniGrad(bec);

        dodajDrzavu(francuska);
        dodajDrzavu(velikaB);
        dodajDrzavu(austrija);
    }

}


// TODO nakon iteriranja zatvoriti kursor kojim iteriramo kroz resultSet; mada ce se vjerovatno sam zatvoriti
// moze se i desiti da baza ostane zauzeta (neko vrijeme?) ako neki upit nije kako treba
// getGradFromResultSet
// kroz gui da se moze vidjeti da li rade (ispravno) metode

// sqlite ne dozvoljava naknadno dodavanje foreign key-eva?
// izbriso not null restrikcije jer su one mozda uzrokovale "foreign key mismatch"