
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PurchaseDAO {

    public void recordPurchase(
            int productId, int supplierId, int quantity, double unitCost) {

        if (quantity <= 0 || unitCost < 0) {
            System.out.println("Invalid quantity or purchase cost.");
            return;
        }

        String checkProduct =
                "SELECT product_id FROM products WHERE product_id = ?";
        String checkSupplier =
                "SELECT supplier_id FROM suppliers WHERE supplier_id = ?";
        String insertPurchase =
                "INSERT INTO purchases " +
                "(product_id, supplier_id, quantity, unit_cost) " +
                "VALUES (?, ?, ?, ?)";
        String updateStock =
                "UPDATE products SET stock_quantity = stock_quantity + ? " +
                "WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                // Verify that the product exists
                try (PreparedStatement ps =
                             con.prepareStatement(checkProduct)) {
                    ps.setInt(1, productId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Product ID not found.");
                        }
                    }
                }

                // Verify that the supplier exists
                try (PreparedStatement ps =
                             con.prepareStatement(checkSupplier)) {
                    ps.setInt(1, supplierId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Supplier ID not found.");
                        }
                    }
                }

                // Save the purchase record
                try (PreparedStatement ps =
                             con.prepareStatement(insertPurchase)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, supplierId);
                    ps.setInt(3, quantity);
                    ps.setDouble(4, unitCost);
                    ps.executeUpdate();
                }

                // Increase product stock
                try (PreparedStatement ps =
                             con.prepareStatement(updateStock)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, productId);
                    ps.executeUpdate();
                }

                con.commit();
                System.out.println("Purchase recorded successfully!");
                System.out.println("Stock increased by " + quantity + ".");

            } catch (SQLException | RuntimeException e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }

                System.out.println("Purchase failed: " + e.getMessage());
                System.out.println("Transaction rollback attempted.");
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
