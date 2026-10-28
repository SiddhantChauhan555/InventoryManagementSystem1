
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PurchasePanel extends JPanel {
    private JTextField productIdField, supplierIdField;
    private JTextField quantityField, costField;
    private JTable table;
    private DefaultTableModel model;

    public PurchasePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel("Purchase Management");
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        add(heading, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Record New Purchase"));

        productIdField = new JTextField();
        supplierIdField = new JTextField();
        quantityField = new JTextField();
        costField = new JTextField();

        form.add(new JLabel("Product ID:"));
        form.add(productIdField);
        form.add(new JLabel("Supplier ID:"));
        form.add(supplierIdField);
        form.add(new JLabel("Quantity:"));
        form.add(quantityField);
        form.add(new JLabel("Unit Cost (₹):"));
        form.add(costField);

        JButton recordButton = new JButton("Record Purchase");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh History");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(recordButton);
        buttons.add(clearButton);
        buttons.add(refreshButton);

        form.add(buttons);
        form.add(new JLabel("Enter IDs from your database."));

        model = new DefaultTableModel(
            new String[]{"Purchase ID", "Product", "Supplier",
                         "Quantity", "Unit Cost (₹)", "Total (₹)",
                         "Purchase Date"}, 0) {
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

        recordButton.addActionListener(e -> recordPurchase());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> loadPurchases());

        loadPurchases();
    }

    private void recordPurchase() {
        int productId, supplierId, quantity;
        double unitCost;

        try {
            productId = Integer.parseInt(productIdField.getText().trim());
            supplierId = Integer.parseInt(supplierIdField.getText().trim());
            quantity = Integer.parseInt(quantityField.getText().trim());
            unitCost = Double.parseDouble(costField.getText().trim());

            if (productId <= 0 || supplierId <= 0 ||
                quantity <= 0 || unitCost < 0) {
                throw new IllegalArgumentException();
            }
        }
        catch (IllegalArgumentException ex){
            JOptionPane.showMessageDialog(this,
                "Enter valid IDs, a positive quantity, and a non-negative unit cost.");
            return;
        }

        String insertSQL =
            "INSERT INTO purchases " +
            "(product_id, supplier_id, quantity, unit_cost) " +
            "VALUES (?, ?, ?, ?)";

        String updateSQL =
            "UPDATE products SET stock_quantity = stock_quantity + ? " +
            "WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                // Verify that the product exists.
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT product_id FROM products WHERE product_id = ?")) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Product ID not found.");
                        }
                    }
                }

                // Verify that the supplier exists.
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT supplier_id FROM suppliers WHERE supplier_id = ?")) {
                    ps.setInt(1, supplierId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Supplier ID not found.");
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(insertSQL)) {
                    ps.setInt(1, productId);
                    ps.setInt(2, supplierId);
                    ps.setInt(3, quantity);
                    ps.setDouble(4, unitCost);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(updateSQL)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, productId);
                    ps.executeUpdate();
                }

                con.commit();

                JOptionPane.showMessageDialog(this,
                    "Purchase recorded successfully!\nStock has been increased.");
                clearFields();
                loadPurchases();

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Purchase failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPurchases() {
        model.setRowCount(0);

        String sql =
            "SELECT pu.purchase_id, p.product_name, s.supplier_name, " +
            "pu.quantity, pu.unit_cost, " +
            "(pu.quantity * pu.unit_cost) AS total_cost, pu.purchase_date " +
            "FROM purchases pu " +
            "JOIN products p ON pu.product_id = p.product_id " +
            "LEFT JOIN suppliers s ON pu.supplier_id = s.supplier_id " +
            "ORDER BY pu.purchase_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("purchase_id"),
                    rs.getString("product_name"),
                    rs.getString("supplier_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_cost"),
                    rs.getDouble("total_cost"),
                    rs.getTimestamp("purchase_date")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Could not load purchase history: " + ex.getMessage());
        }
    }

    private void clearFields() {
        productIdField.setText("");
        supplierIdField.setText("");
        quantityField.setText("");
        costField.setText("");
    }
}
