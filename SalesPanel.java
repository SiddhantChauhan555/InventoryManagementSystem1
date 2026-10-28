
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SalesPanel extends JPanel {
    private JTextField productIdField, quantityField;
    private JTable table;
    private DefaultTableModel model;

    public SalesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel("Sales Management");
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        add(heading, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Record New Sale"));

        productIdField = new JTextField();
        quantityField = new JTextField();

        form.add(new JLabel("Product ID:"));
        form.add(productIdField);
        form.add(new JLabel("Quantity:"));
        form.add(quantityField);

        JButton sellButton = new JButton("Record Sale");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh History");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(sellButton);
        buttons.add(clearButton);
        buttons.add(refreshButton);

        form.add(buttons);
        form.add(new JLabel("Selling price is taken from the product record."));

        model = new DefaultTableModel(
            new String[]{"Sale ID", "Product", "Quantity",
                         "Unit Price (₹)", "Total (₹)", "Sale Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(24);

        mainPanel.add(form, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        sellButton.addActionListener(e -> recordSale());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> loadSales());

        loadSales();
    }

    private void recordSale() {
        int productId, quantity;

        try {
            productId = Integer.parseInt(productIdField.getText().trim());
            quantity = Integer.parseInt(quantityField.getText().trim());

            if (productId <= 0 || quantity <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                "Enter a valid Product ID and a quantity greater than zero.");
            return;
        }

        String stockSQL =
            "SELECT stock_quantity, selling_price FROM products " +
            "WHERE product_id = ? FOR UPDATE";

        String insertSQL =
            "INSERT INTO sales (product_id, quantity, unit_price) " +
            "VALUES (?, ?, ?)";

        String updateSQL =
            "UPDATE products SET stock_quantity = stock_quantity - ? " +
            "WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                int availableStock;
                double sellingPrice;

                try (PreparedStatement ps = con.prepareStatement(stockSQL)) {
                    ps.setInt(1, productId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Product ID not found.");
                        }

                        availableStock = rs.getInt("stock_quantity");
                        sellingPrice = rs.getDouble("selling_price");
                    }
                }

                if (availableStock < quantity) {
                    throw new SQLException(
                        "Insufficient stock. Available quantity: " +
                        availableStock);
                }

                try (PreparedStatement ps = con.prepareStatement(insertSQL)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, quantity);
                    ps.setDouble(3, sellingPrice);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(updateSQL)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, productId);
                    ps.executeUpdate();
                }

                con.commit();

                JOptionPane.showMessageDialog(this,
                    "Sale recorded successfully!\n" +
                    "Quantity: " + quantity +
                    "\nUnit price: ₹" + sellingPrice +
                    "\nTotal: ₹" + (quantity * sellingPrice));

                clearFields();
                loadSales();

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Sale failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSales() {
        model.setRowCount(0);

        String sql =
            "SELECT s.sale_id, p.product_name, s.quantity, " +
            "s.unit_price, s.total_amount, s.sale_date " +
            "FROM sales s " +
            "JOIN products p ON s.product_id = p.product_id " +
            "ORDER BY s.sale_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("sale_id"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_price"),
                    rs.getDouble("total_amount"),
                    rs.getTimestamp("sale_date")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Could not load sales history: " + ex.getMessage());
        }
    }

    private void clearFields() {
        productIdField.setText("");
        quantityField.setText("");
    }
}
