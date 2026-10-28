
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StockPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField productIdField;
    private JTextField quantityField;

    public StockPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Stock Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"ID", "Product", "Category",
                    "Current Stock", "Low Stock Limit"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        JPanel controls = new JPanel(new GridLayout(2, 4, 8, 8));

        productIdField = new JTextField();
        quantityField = new JTextField();

        controls.add(new JLabel("Product ID"));
        controls.add(productIdField);
        controls.add(new JLabel("Quantity"));
        controls.add(quantityField);

        JButton addButton = new JButton("Add Stock");
        JButton reduceButton = new JButton("Reduce Stock");
        JButton lowStockButton = new JButton("Low-Stock Alerts");
        JButton refreshButton = new JButton("Refresh");

        controls.add(addButton);
        controls.add(reduceButton);
        controls.add(lowStockButton);
        controls.add(refreshButton);

        add(controls, BorderLayout.SOUTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addButton.addActionListener(e -> changeStock(true));
        reduceButton.addActionListener(e -> changeStock(false));
        lowStockButton.addActionListener(e -> loadLowStock());
        refreshButton.addActionListener(e -> loadStock());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.convertRowIndexToModel(
                        table.getSelectedRow());
                productIdField.setText(
                        model.getValueAt(row, 0).toString());
            }
        });

        loadStock();
    }

    private void loadStock() {
        model.setRowCount(0);

        String sql = "SELECT product_id, product_name, category, " +
                "stock_quantity, low_stock_limit " +
                "FROM products ORDER BY product_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getInt("stock_quantity"),
                    rs.getInt("low_stock_limit")
                });
            }

        } catch (SQLException e) {
            showError("Unable to load stock", e);
        }
    }

    private void loadLowStock() {
        model.setRowCount(0);

        String sql = "SELECT product_id, product_name, category, " +
                "stock_quantity, low_stock_limit FROM products " +
                "WHERE stock_quantity <= low_stock_limit " +
                "ORDER BY stock_quantity";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getInt("stock_quantity"),
                    rs.getInt("low_stock_limit")
                });
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No products currently need restocking.");
            }

        } catch (SQLException e) {
            showError("Unable to load low-stock alerts", e);
        }
    }

    private void changeStock(boolean add) {
        int productId;
        int quantity;

        try {
            productId = Integer.parseInt(
                    productIdField.getText().trim());
            quantity = Integer.parseInt(
                    quantityField.getText().trim());

            if (productId <= 0 || quantity <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Enter a valid product ID and a quantity greater than zero.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql;

        if (add) {
            sql = "UPDATE products SET stock_quantity = " +
                    "stock_quantity + ? WHERE product_id = ?";
        } else {
            sql = "UPDATE products SET stock_quantity = " +
                    "stock_quantity - ? " +
                    "WHERE product_id = ? AND stock_quantity >= ?";
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, productId);

            if (!add) {
                ps.setInt(3, quantity);
            }

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                        add ? "Stock increased successfully!"
                            : "Stock reduced successfully!");

                quantityField.setText("");
                loadStock();

            } else {
                JOptionPane.showMessageDialog(this,
                        "Product not found or insufficient stock.",
                        "Operation Failed", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            showError("Unable to update stock", e);
        }
    }

    private void showError(String message, SQLException e) {
        JOptionPane.showMessageDialog(this,
                message + ":\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
