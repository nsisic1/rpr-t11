package ba.unsa.etf.rpr;

import java.sql.*;

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

}
