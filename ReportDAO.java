
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportDAO {

    // 1. Total inventory value
    public void inventoryValueReport() {
        String sql = "SELECT SUM(stock_quantity * purchase_price) AS value " +
                     "FROM products";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            System.out.printf("Total Inventory Value: Rs. %.2f%n",
                    rs.getDouble("value"));

        } catch (SQLException e) {
            System.out.println("Report error: " + e.getMessage());
        }
    }

    // 2. Total sales revenue
    public void salesRevenueReport() {
        String sql = "SELECT SUM(total_amount) AS revenue FROM sales";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            System.out.printf("Total Sales Revenue: Rs. %.2f%n",
                    rs.getDouble("revenue"));

        } catch (SQLException e) {
            System.out.println("Report error: " + e.getMessage());
        }
    }

    // 3. Best-selling products
    public void bestSellingProductsReport() {
        String sql = "SELECT p.product_name, SUM(s.quantity) AS total_sold " +
                     "FROM sales s JOIN products p " +
                     "ON s.product_id = p.product_id " +
                     "GROUP BY p.product_id, p.product_name " +
                     "ORDER BY total_sold DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== BEST-SELLING PRODUCTS =====");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println("Product: " +
                        rs.getString("product_name"));
                System.out.println("Quantity Sold: " +
                        rs.getInt("total_sold"));
                System.out.println("--------------------------------");
            }

            if (!found) {
                System.out.println("No sales recorded yet.");
            }

        } catch (SQLException e) {
            System.out.println("Report error: " + e.getMessage());
        }
    }

    // 4. Purchase history with product and supplier names
    public void purchaseHistoryReport() {
        String sql = "SELECT pu.purchase_id, p.product_name, " +
                     "s.supplier_name, pu.quantity, pu.unit_cost, " +
                     "pu.purchase_date " +
                     "FROM purchases pu " +
                     "JOIN products p ON pu.product_id = p.product_id " +
                     "LEFT JOIN suppliers s ON pu.supplier_id = s.supplier_id " +
                     "ORDER BY pu.purchase_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== PURCHASE HISTORY =====");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println("Purchase ID: " +
                        rs.getInt("purchase_id"));
                System.out.println("Product: " +
                        rs.getString("product_name"));
                System.out.println("Supplier: " +
                        rs.getString("supplier_name"));
                System.out.println("Quantity: " +
                        rs.getInt("quantity"));
                System.out.printf("Unit Cost: Rs. %.2f%n",
                        rs.getDouble("unit_cost"));
                System.out.println("Date: " +
                        rs.getTimestamp("purchase_date"));
                System.out.println("--------------------------------");
            }

            if (!found) {
                System.out.println("No purchases recorded yet.");
            }

        } catch (SQLException e) {
            System.out.println("Report error: " + e.getMessage());
        }
    }

    // 5. Low-stock report
    public void lowStockReport() {
        String sql = "SELECT product_name, stock_quantity, low_stock_limit " +
                     "FROM products " +
                     "WHERE stock_quantity <= low_stock_limit " +
                     "ORDER BY stock_quantity";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== LOW-STOCK REPORT =====");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println("Product: " +
                        rs.getString("product_name"));
                System.out.println("Current Stock: " +
                        rs.getInt("stock_quantity"));
                System.out.println("Low Stock Limit: " +
                        rs.getInt("low_stock_limit"));
                System.out.println("--------------------------------");
            }

            if (!found) {
                System.out.println("No products need restocking.");
            }

        } catch (SQLException e) {
            System.out.println("Report error: " + e.getMessage());
        }
    }

    
    // Display sales history with product names
    public void salesHistoryReport() {
        String sql = "SELECT s.sale_id, p.product_name, s.quantity, " +
                "s.unit_price, s.total_amount, s.sale_date " +
                "FROM sales s " +
                "JOIN products p ON s.product_id = p.product_id " +
                "ORDER BY s.sale_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n========== SALES HISTORY ==========");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println("Sale ID: " + rs.getInt("sale_id"));
                System.out.println("Product: " + rs.getString("product_name"));
                System.out.println("Quantity: " + rs.getInt("quantity"));
                System.out.printf("Unit Price: Rs. %.2f%n",
                        rs.getDouble("unit_price"));
                System.out.printf("Total: Rs. %.2f%n",
                        rs.getDouble("total_amount"));
                System.out.println("Date: " + rs.getTimestamp("sale_date"));
                System.out.println("-----------------------------------");
            }

            if (!found) {
                System.out.println("No sales recorded yet.");
            }

        } catch (SQLException e) {
            System.out.println("Error displaying sales: " + e.getMessage());
        }
    }

}
