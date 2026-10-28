
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDAO {

    // Add a new product
    public void addProduct(Product product) {
        String sql = "INSERT INTO products " +
                "(product_name, category, purchase_price, selling_price, " +
                "stock_quantity, low_stock_limit) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getCategory());
            ps.setDouble(3, product.getPurchasePrice());
            ps.setDouble(4, product.getSellingPrice());
            ps.setInt(5, product.getStockQuantity());
            ps.setInt(6, product.getLowStockLimit());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product added successfully!");
            }

        } catch (SQLException e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
    }

    
    // Search for a product by ID
    public void searchProduct(int id) {
        String sql = "SELECT * FROM products WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\nProduct Found!");
                    System.out.println("ID: " + rs.getInt("product_id"));
                    System.out.println("Name: " + rs.getString("product_name"));
                    System.out.println("Category: " + rs.getString("category"));
                    System.out.println("Purchase Price: " +
                            rs.getDouble("purchase_price"));
                    System.out.println("Selling Price: " +
                            rs.getDouble("selling_price"));
                    System.out.println("Stock: " +
                            rs.getInt("stock_quantity"));
                    System.out.println("Low Stock Limit: " +
                            rs.getInt("low_stock_limit"));
                } else {
                    System.out.println("Product not found!");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching product: " + e.getMessage());
        }
    }

    // Update an existing product
    public void updateProduct(Product product) {
        String sql = "UPDATE products SET product_name = ?, category = ?, " +
                "purchase_price = ?, selling_price = ?, stock_quantity = ?, " +
                "low_stock_limit = ? WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getCategory());
            ps.setDouble(3, product.getPurchasePrice());
            ps.setDouble(4, product.getSellingPrice());
            ps.setInt(5, product.getStockQuantity());
            ps.setInt(6, product.getLowStockLimit());
            ps.setInt(7, product.getProductId());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product updated successfully!");
            } else {
                System.out.println("Product ID not found!");
            }

        } catch (SQLException e) {
            System.out.println("Error updating product: " + e.getMessage());
        }
    }

    // Delete a product by ID
    public void deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println("Product ID not found!");
            }

        } catch (SQLException e) {
            System.out.println("Error deleting product: " + e.getMessage());
            System.out.println(
                    "The product may be referenced by purchase or sales records.");
        }
    }

    
    // Display products that need restocking
    public void viewLowStockProducts() {
        String sql = "SELECT product_id, product_name, stock_quantity, " +
                "low_stock_limit FROM products " +
                "WHERE stock_quantity <= low_stock_limit " +
                "ORDER BY stock_quantity";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n========== LOW-STOCK ALERTS ==========");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println("ID: " + rs.getInt("product_id"));
                System.out.println("Product: " + rs.getString("product_name"));
                System.out.println("Current Stock: " +
                        rs.getInt("stock_quantity"));
                System.out.println("Low Stock Limit: " +
                        rs.getInt("low_stock_limit"));
                System.out.println("--------------------------------------");
            }

            if (!found) {
                System.out.println("No products currently need restocking.");
            }

        } catch (SQLException e) {
            System.out.println("Error checking stock: " + e.getMessage());
        }
    }

    // Increase stock when new stock arrives
    public void addStock(int productId, int quantity) {
        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }

        String sql = "UPDATE products SET stock_quantity = " +
                "stock_quantity + ? WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, productId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Stock increased successfully!");
            } else {
                System.out.println("Product ID not found!");
            }

        } catch (SQLException e) {
            System.out.println("Error adding stock: " + e.getMessage());
        }
    }

    // Reduce stock, provided enough stock is available
    public void reduceStock(int productId, int quantity) {
        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }

        String sql = "UPDATE products SET stock_quantity = " +
                "stock_quantity - ? " +
                "WHERE product_id = ? AND stock_quantity >= ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Stock reduced successfully!");
            } else {
                System.out.println(
                        "Product not found or insufficient stock.");
            }

        } catch (SQLException e) {
            System.out.println("Error reducing stock: " + e.getMessage());
        }
    }


    // Display all products
    public void viewProducts() {
        String sql = "SELECT * FROM products ORDER BY product_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n========== PRODUCT LIST ==========");

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("product_id"));
                System.out.println("Name: " + rs.getString("product_name"));
                System.out.println("Category: " + rs.getString("category"));
                System.out.println("Purchase Price: " +
                        rs.getDouble("purchase_price"));
                System.out.println("Selling Price: " +
                        rs.getDouble("selling_price"));
                System.out.println("Stock: " +
                        rs.getInt("stock_quantity"));
                System.out.println("Low Stock Limit: " +
                        rs.getInt("low_stock_limit"));
                System.out.println("----------------------------------");
            }

        } catch (SQLException e) {
            System.out.println("Error viewing products: " + e.getMessage());
        }
    }
}
