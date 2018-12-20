package ba.unsa.etf.rpr;

import java.sql.*;
import java.util.ArrayList;

public class GeografijaDAO {

    private static GeografijaDAO instance = null;

    private Connection conn;

    private static void initialize() throws SQLException { // TODO: zabiljezi: staticna metoda, kreira intancu
        instance = new GeografijaDAO();
    }

    private GeografijaDAO() throws SQLException {
        String url = "jdbc:sqlite:baza.db";
        conn = DriverManager.getConnection(url);
        try {
            generirajTabeleAkoNePostoje();
        } catch (SQLException e) {
            // tabele baze vec postoje
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

    private void generirajTabeleAkoNePostoje() throws SQLException { // TODO: kad se koji bacaju izuzetci
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

    private void popuniTabele() throws SQLException {
        //Statement stmt = conn.createStatement();

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

            PreparedStatement stmt = conn.prepareStatement("DELETE FROM drzave WHERE naziv = ?");
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
            PreparedStatement stmt = conn.prepareStatement("SELECT grad.id, grad.naziv, brojStanovnika, drzava, drzava.naziv, " +
                    "drzava.glavniGrad FROM grad, drzava WHERE grad.drzava = drzava.id ORDER BY brojStanovnika DESC");
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Grad grad = new Grad();
                Drzava d = new Drzava();
                grad.setNaziv(resultSet.getString(2));
                grad.setBrojStanovnika(resultSet.getInt(3));
                d.setNaziv(resultSet.getString(6));
                // Prolazeci sve gradove u bazi ovom while petljom, sigurno cemo proci i kroz sve gradove koji su ujedno
                // i glavni gradovi svojih drzava, to provjeravamo ovdje
                if (resultSet.getInt(1) == resultSet.getInt(6)) {
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

    public Drzava nadjiDrzavu(String drzava) {
        Drzava d = new Drzava();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT naziv, glavni_grad FROM drzave WHERE naziv = ?");
            stmt.setString(1, drzava);
            ResultSet resultSet = stmt.executeQuery();
            d.setNaziv(resultSet.getString(1));
            return d;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
