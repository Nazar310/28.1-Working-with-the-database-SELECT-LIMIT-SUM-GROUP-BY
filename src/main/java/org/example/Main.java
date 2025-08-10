package org.example;

import java.sql.*;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:sales_database.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("Підключення до БД успішне!");

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS sales (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            product TEXT,
                            price REAL,
                            quantity INTEGER
                        );
                        """);
                stmt.execute("DELETE FROM sales");
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO sales(product, price, quantity) VALUES (?, ?, ?)")) {
                Object[][] data = {
                        {"Laptop", 1000.0, 5},
                        {"Phone", 700.0, 3},
                        {"Tablet", 500.0, 2},
                        {"Printer", 300.0, 4}
                };
                for (Object[] row : data) {
                    pstmt.setString(1, (String) row[0]);
                    pstmt.setDouble(2, (Double) row[1]);
                    pstmt.setInt(3, (Integer) row[2]);
                    pstmt.executeUpdate();
                }
            }

            System.out.println("\nВсі записи:");
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM sales")) {
                printResultSet(rs);
            }

            System.out.println("\nПерші два записи:");
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM sales LIMIT 2")) {
                printResultSet(rs);
            }

            System.out.println("\nЗагальна вартість усіх продуктів:");
            try (ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT SUM(price * quantity) AS total FROM sales")) {
                if (rs.next()) {
                    System.out.println("Total: " + rs.getDouble("total"));
                }
            }

            System.out.println("\nГрупування за продуктом:");
            try (ResultSet rs = conn.createStatement().executeQuery("""
                    SELECT product,
                           SUM(quantity) AS total_quantity,
                           AVG(price) AS avg_price
                    FROM sales
                    GROUP BY product
                    """)) {
                while (rs.next()) {
                    System.out.printf("%-10s | Кількість: %-3d | Середня ціна: %.2f%n",
                            rs.getString("product"),
                            rs.getInt("total_quantity"),
                            rs.getDouble("avg_price"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Помилка роботи з БД: " + e.getMessage());
        }
    }

    private static void printResultSet(ResultSet rs) throws SQLException {
        while (rs.next()) {
            System.out.printf("%-3d | %-10s | %-7.2f | %-3d%n",
                    rs.getInt("id"),
                    rs.getString("product"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"));
        }
    }
}