
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SalesDAO {

    public void recordSale(int productId, int quantity) {

        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }

        String checkStock =
                "SELECT stock_quantity, selling_price " +
                "FROM products WHERE product_id = ?";

        String insertSale =
                "INSERT INTO sales (product_id, quantity, unit_price) " +
                "VALUES (?, ?, ?)";

        String reduceStock =
                "UPDATE products SET stock_quantity = stock_quantity - ? " +
                "WHERE product_id = ? AND stock_quantity >= ?";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                double sellingPrice;
                int availableStock;

                try (PreparedStatement ps =
                             con.prepareStatement(checkStock)) {
                    ps.setInt(1, productId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Product not found.");
                        }

                        availableStock = rs.getInt("stock_quantity");
                        sellingPrice = rs.getDouble("selling_price");
                    }
                }

                if (availableStock < quantity) {
                    throw new SQLException("Insufficient stock.");
                }

                try (PreparedStatement ps =
                             con.prepareStatement(insertSale)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, quantity);
                    ps.setDouble(3, sellingPrice);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps =
                             con.prepareStatement(reduceStock)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, productId);
                    ps.setInt(3, quantity);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException(
                                "Stock update failed. Please retry.");
                    }
                }

                con.commit();

                double total = sellingPrice * quantity;

                System.out.println("Sale recorded successfully!");
                System.out.println("----------------------------------------");
                System.out.println("Quantity sold: " + quantity);
                System.out.printf("Total amount: Rs. %.2f%n", total);
                System.out.println("Remaining stock: " +
                        (availableStock - quantity));

            
            } catch (SQLException | RuntimeException e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }

                System.out.println("Sale failed: " + e.getMessage());
                System.out.println("Transaction rollback attempted.");
            } finally {
                con.setAutoCommit(true);
            }


        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
