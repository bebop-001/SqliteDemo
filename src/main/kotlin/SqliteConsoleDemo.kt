import java.sql.*

class SqliteConsoleDemo {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            var c: Connection? = null
            try {
                Class.forName("org.sqlite.JDBC")
                c = DriverManager.getConnection("jdbc:sqlite:test.db")
                val stmt = c.createStatement();
                val sql = "CREATE TABLE IF NOT EXISTS COMPANY " +
                "(ID INT PRIMARY KEY     NOT NULL," +
                    " NAME           TEXT    NOT NULL, " +
                    " AGE            INT     NOT NULL, " +
                    " ADDRESS        CHAR(50), " +
                    " SALARY         REAL)";
                stmt.executeUpdate(sql);
                stmt.close();
                c.close();
            } catch ( e :Exception) {
                System.err.println( "${e.javaClass.name}:${e.message}");
                System.exit(0);
            }
            System.out.println("Opened database successfully")
        }
    }
}