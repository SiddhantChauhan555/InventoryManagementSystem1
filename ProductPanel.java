
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ProductPanel extends JPanel {

    private JTextField idField, nameField, categoryField;
    private JTextField purchaseField, sellingField;
    private JTextField stockField, limitField;

    private JTextField searchField;
    private JComboBox<String> categoryBox;

    private JTable table;
    private DefaultTableModel model;

    public ProductPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Product Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        // Product input form
        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));

        idField = new JTextField();
        nameField = new JTextField();
        categoryField = new JTextField();
        purchaseField = new JTextField();
        sellingField = new JTextField();
        stockField = new JTextField();
        limitField = new JTextField();

        addField(form, "Product ID (for search/update/delete)", idField);
        addField(form, "Product Name", nameField);
        addField(form, "Category", categoryField);
        addField(form, "Purchase Price", purchaseField);
        addField(form, "Selling Price", sellingField);
        addField(form, "Stock Quantity", stockField);
        addField(form, "Low Stock Limit", limitField);

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton searchIdButton = new JButton("Search by ID");
        JButton refreshButton = new JButton("Refresh");
        JButton clearButton = new JButton("Clear");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(searchIdButton);
        buttons.add(refreshButton);
        buttons.add(clearButton);

        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        formPanel.add(form, BorderLayout.CENTER);
        formPanel.add(buttons, BorderLayout.SOUTH);

        // Search and category filters
        searchField = new JTextField(15);

        categoryBox = new JComboBox<>();
        categoryBox.addItem("All Categories");

        JButton filterButton = new JButton("Filter");
        JButton resetButton = new JButton("Reset Filters");

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Product Name:"));
        filterPanel.add(searchField);
        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(categoryBox);
        filterPanel.add(filterButton);
        filterPanel.add(resetButton);

        // Product table
        model = new DefaultTableModel(
            new String[]{
                "Product ID", "Product Name", "Category",
                "Purchase Price", "Selling Price",
                "Stock Quantity", "Low Stock Limit"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(formPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(topPanel, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        // Product operation buttons
        addButton.addActionListener(e -> addProduct());
        updateButton.addActionListener(e -> updateProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        searchIdButton.addActionListener(e -> searchProduct());
        refreshButton.addActionListener(e -> loadProducts());
        clearButton.addActionListener(e -> clearFields());

        // Search and filtering buttons
        filterButton.addActionListener(e -> filterProducts());

        resetButton.addActionListener(e -> {
            searchField.setText("");
            categoryBox.setSelectedIndex(0);
            loadProducts();
        });

        // Apply filter when Enter is pressed in search field
        searchField.addActionListener(e -> filterProducts());

        // Load category list and products
        loadCategories();
        loadProducts();
    }

    private void addField(JPanel panel, String label, JTextField field) {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    // Load all products
    private void loadProducts() {
        model.setRowCount(0);

        String sql = "SELECT * FROM products ORDER BY product_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                addProductRow(rs);
            }

        } catch (SQLException ex) {
            showError("Could not load products: " + ex.getMessage());
        }
    }

    // Add a database result to the table
    private void addProductRow(ResultSet rs) throws SQLException {
        model.addRow(new Object[]{
            rs.getInt("product_id"),
            rs.getString("product_name"),
            rs.getString("category"),
            rs.getDouble("purchase_price"),
            rs.getDouble("selling_price"),
            rs.getInt("stock_quantity"),
            rs.getInt("low_stock_limit")
        });
    }

    // Load unique categories
    private void loadCategories() {
        String selected = (String) categoryBox.getSelectedItem();

        categoryBox.removeAllItems();
        categoryBox.addItem("All Categories");

        String sql =
            "SELECT DISTINCT category FROM products " +
            "WHERE category IS NOT NULL AND category <> '' " +
            "ORDER BY category";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categoryBox.addItem(rs.getString("category"));
            }

        } catch (SQLException ex) {
            showError("Could not load categories: " + ex.getMessage());
        }

        if (selected != null) {
            categoryBox.setSelectedItem(selected);
        }
    }

    // Filter by product name and category
    private void filterProducts() {
        model.setRowCount(0);

        String name = searchField.getText().trim();
        String category = (String) categoryBox.getSelectedItem();

        StringBuilder sql = new StringBuilder(
            "SELECT * FROM products WHERE 1=1"
        );

        if (!name.isEmpty()) {
            sql.append(" AND product_name LIKE ?");
        }

        if (category != null && !category.equals("All Categories")) {
            sql.append(" AND category = ?");
        }

        sql.append(" ORDER BY product_id");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;

            if (!name.isEmpty()) {
                ps.setString(index++, "%" + name + "%");
            }

            if (category != null && !category.equals("All Categories")) {
                ps.setString(index, category);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addProductRow(rs);
                }
            }

        } catch (SQLException ex) {
            showError("Filtering failed: " + ex.getMessage());
        }
    }

    // Add a new product
    private void addProduct() {
        try {
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();

            double purchase = Double.parseDouble(purchaseField.getText().trim());
            double selling = Double.parseDouble(sellingField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());
            int limit = Integer.parseInt(limitField.getText().trim());

            if (name.isEmpty() || category.isEmpty()
                    || purchase < 0 || selling < 0
                    || stock < 0 || limit < 0) {
                showError("Enter valid product details and non-negative values.");
                return;
            }

            String sql =
                "INSERT INTO products " +
                "(product_name, category, purchase_price, selling_price, " +
                "stock_quantity, low_stock_limit) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, name);
                ps.setString(2, category);
                ps.setDouble(3, purchase);
                ps.setDouble(4, selling);
                ps.setInt(5, stock);
                ps.setInt(6, limit);

                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Product added successfully!");
            clearFields();
            refreshData();

        } catch (NumberFormatException ex) {
            showError("Enter valid numbers for prices, stock, and limit.");
        } catch (SQLException ex) {
            showError("Could not add product: " + ex.getMessage());
        }
    }

    // Update product details
    private void updateProduct() {
        try {
            int id = Integer.parseInt(idField.getText().trim());

            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();

            double purchase = Double.parseDouble(purchaseField.getText().trim());
            double selling = Double.parseDouble(sellingField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());
            int limit = Integer.parseInt(limitField.getText().trim());

            if (id <= 0 || name.isEmpty() || category.isEmpty()
                    || purchase < 0 || selling < 0
                    || stock < 0 || limit < 0) {
                showError("Enter valid product details.");
                return;
            }

            String sql =
                "UPDATE products SET product_name=?, category=?, " +
                "purchase_price=?, selling_price=?, low_stock_limit=? " +
                "WHERE product_id=?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, name);
                ps.setString(2, category);
                ps.setDouble(3, purchase);
                ps.setDouble(4, selling);
                ps.setInt(5, limit);
                ps.setInt(6, id);

                int rows = ps.executeUpdate();

                if (rows == 0) {
                    showError("Product ID not found.");
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Product updated successfully!");
            clearFields();
            refreshData();

        } catch (NumberFormatException ex) {
            showError("Enter a valid product ID and numeric values.");
        } catch (SQLException ex) {
            showError("Could not update product: " + ex.getMessage());
        }
    }

    // Delete product
    private void deleteProduct() {
        try {
            int id = Integer.parseInt(idField.getText().trim());

            int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete product ID " + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            String sql = "DELETE FROM products WHERE product_id=?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, id);
                int rows = ps.executeUpdate();

                if (rows == 0) {
                    showError("Product ID not found.");
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Product deleted successfully!");
            clearFields();
            refreshData();

        } catch (NumberFormatException ex) {
            showError("Enter a valid Product ID.");
        } catch (SQLException ex) {
            showError("Could not delete product. It may have purchase or sales history.\n"
                + ex.getMessage());
        }
    }

    // Search by product ID
    private void searchProduct() {
        try {
            int id = Integer.parseInt(idField.getText().trim());

            String sql = "SELECT * FROM products WHERE product_id=?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        nameField.setText(rs.getString("product_name"));
                        categoryField.setText(rs.getString("category"));
                        purchaseField.setText(
                            String.valueOf(rs.getDouble("purchase_price")));
                        sellingField.setText(
                            String.valueOf(rs.getDouble("selling_price")));
                        stockField.setText(
                            String.valueOf(rs.getInt("stock_quantity")));
                        limitField.setText(
                            String.valueOf(rs.getInt("low_stock_limit")));

                        JOptionPane.showMessageDialog(this, "Product found.");
                    } else {
                        showError("Product not found.");
                    }
                }
            }

        } catch (NumberFormatException ex) {
            showError("Enter a valid Product ID.");
        } catch (SQLException ex) {
            showError("Search failed: " + ex.getMessage());
        }
    }

    // Refresh products and categories
    private void refreshData() {
        loadCategories();
        filterProducts();
    }

    // Clear input fields
    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        categoryField.setText("");
        purchaseField.setText("");
        sellingField.setText("");
        stockField.setText("");
        limitField.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this, message, "Error", JOptionPane.ERROR_MESSAGE
        );
    }
}
