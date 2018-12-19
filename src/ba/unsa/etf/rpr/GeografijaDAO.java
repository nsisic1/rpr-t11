package ba.unsa.etf.rpr;

public class GeografijaDAO {

    private static GeografijaDAO instance = null;

    private static void initialize() { // TODO: zabiljezi: staticna metoda, kreira intancu
        instance = new GeografijaDAO();
    }

    private GeografijaDAO() {

    }

    public static GeografijaDAO getInstance() {
        if (instance == null) {
            initialize();
        }
        return instance;
    }
}
