
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;

public class ReportPanel extends JPanel {
    private JComboBox<String> reportBox;
    private JTable table;
    private DefaultTableModel model;
    private JLabel statusLabel;
    private JTextField startDateField;
    private JTextField endDateField;
    private JButton exportButton;

    private final String[] reports = {
        "Inventory Value",
        "Sales Revenue",
        "Best-Selling Products",
        "Purchase History",
        "Low-Stock Report",
        "Sales History",
        "Profit Analysis",
        "Profit by Product"
    };

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel("Reports and Analytics");
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        add(heading, BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        reportBox = new JComboBox<>(reports);

        startDateField = new JTextField(10);
        endDateField = new JTextField(10);

        startDateField.setText(LocalDate.now().withDayOfMonth(1).toString());
        endDateField.setText(LocalDate.now().toString());

        JButton viewButton = new JButton("View Report");
        exportButton = new JButton("Export CSV");

        statusLabel = new JLabel("Select a report.");

        topPanel.add(new JLabel("Report:"));
        topPanel.add(reportBox);
        topPanel.add(new JLabel("Start (YYYY-MM-DD):"));
        topPanel.add(startDateField);
        topPanel.add(new JLabel("End:"));
        topPanel.add(endDateField);
        topPanel.add(viewButton);
        topPanel.add(exportButton);
        topPanel.add(statusLabel);

        exportButton.addActionListener(e -> exportCSV());

        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(26);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        add(topPanel, BorderLayout.PAGE_START);
        add(new JScrollPane(table), BorderLayout.CENTER);

        viewButton.addActionListener(e -> loadReport());

        loadReport();
    }

    private void loadReport() {
        String selected = (String) reportBox.getSelectedItem();
        if ((selected.equals("Sales Revenue")|| selected.equals("Best-Selling Products")|| selected.equals("Sales History"))&& !isValidDateRange()) {
           return;
        }
        String sql;

        switch (selected) {
            case "Inventory Value":
                sql = "SELECT COUNT(*) AS total_products, " +
                      "COALESCE(SUM(stock_quantity * purchase_price), 0) " +
                      "AS inventory_value FROM products";
                break;

            case "Sales Revenue":
                sql = "SELECT COUNT(*) AS total_sales, " + "COALESCE(SUM(total_amount), 0) AS total_revenue " + "FROM sales WHERE sale_date >= ? " + "AND sale_date < DATE_ADD(?, INTERVAL 1 DAY)";
                break;

            case "Best-Selling Products":
                sql = "SELECT p.product_id, p.product_name, " +
                      "SUM(s.quantity) AS quantity_sold, " +
                      "SUM(s.total_amount) AS revenue " +
                      "FROM sales s JOIN products p ON s.product_id = p.product_id " +
                      "WHERE s.sale_date >= ? " +
                      "AND s.sale_date < DATE_ADD(?, INTERVAL 1 DAY) " +
                      "GROUP BY p.product_id, p.product_name " +
                      "ORDER BY quantity_sold DESC";
                break;

            case "Purchase History":
                sql = "SELECT pu.purchase_id, p.product_name, " +
                      "s.supplier_name, pu.quantity, pu.unit_cost, " +
                      "(pu.quantity * pu.unit_cost) AS total_cost, " +
                      "pu.purchase_date " +
                      "FROM purchases pu " +
                      "JOIN products p ON pu.product_id = p.product_id " +
                      "LEFT JOIN suppliers s ON pu.supplier_id = s.supplier_id " +
                      "ORDER BY pu.purchase_id DESC";
                break;

            case "Low-Stock Report":
                sql = "SELECT product_id, product_name, category, " +
                      "stock_quantity, low_stock_limit " +
                      "FROM products " +
                      "WHERE stock_quantity <= low_stock_limit " +
                      "ORDER BY stock_quantity ASC";
                break;

            case "Sales History":
                sql = "SELECT s.sale_id, p.product_name, s.quantity, " +
                      "s.unit_price, s.total_amount, s.sale_date " +
                      "FROM sales s JOIN products p ON s.product_id = p.product_id " +
                      "WHERE s.sale_date >= ? " +
                      "AND s.sale_date < DATE_ADD(?, INTERVAL 1 DAY) " +
                      "ORDER BY s.sale_id DESC";
                break;

            
            case "Profit Analysis":
                sql = "SELECT " +
                      "COALESCE(SUM(s.total_amount), 0) AS total_revenue, " +
                      "COALESCE(SUM(s.quantity * p.purchase_price), 0) AS estimated_cogs, " +
                      "COALESCE(SUM(s.total_amount - " +
                      "(s.quantity * p.purchase_price)), 0) AS gross_profit " +
                      "FROM sales s " +
                      "JOIN products p ON s.product_id = p.product_id" + 
                      "WHERE s.sale_date >= ? " +
                      "AND s.sale_date < DATE_ADD(?, INTERVAL 1 DAY)";
                break;

            case "Profit by Product":
                sql = "SELECT p.product_id, p.product_name, " +
                      "SUM(s.quantity) AS quantity_sold, " +
                      "SUM(s.total_amount) AS revenue, " +
                      "SUM(s.quantity * p.purchase_price) AS estimated_cogs, " +
                      "SUM(s.total_amount - " +
                      "(s.quantity * p.purchase_price)) AS gross_profit " +
                      "FROM sales s " +
                      "JOIN products p ON s.product_id = p.product_id " +
                      "GROUP BY p.product_id, p.product_name " +
                      "WHERE s.sale_date >= ? " +
                      "AND s.sale_date < DATE_ADD(?, INTERVAL 1 DAY) " +
                      "ORDER BY gross_profit DESC";
                break;


            default:
                return;
        }

        model.setRowCount(0);
        model.setColumnCount(0);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (selected.equals("Sales Revenue") || selected.equals("Best-Selling Products") || selected.equals("Sales History") || selected.equals("Profit Analysis") || selected.equals("Profit Analysis") ) {
               ps.setDate(1, java.sql.Date.valueOf(
                   LocalDate.parse(startDateField.getText().trim())));
               ps.setDate(2, java.sql.Date.valueOf(
                   LocalDate.parse(endDateField.getText().trim())));
            }

        try (ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(meta.getColumnLabel(i));
            }

            int rowCount = 0;

            while (rs.next()) {
                Object[] row = new Object[columnCount];

                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }

                model.addRow(row);
                rowCount++;
            }

            statusLabel.setText(rowCount + " row(s) found.");

            if (rowCount == 0) {
                statusLabel.setText("No records found.");
            }
        }
        } catch (SQLException ex) {
            statusLabel.setText("Report failed.");
            JOptionPane.showMessageDialog(this,
                "Could not load report: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void exportCSV() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "There is no report data to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("inventory_report.csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (int column = 0; column < model.getColumnCount(); column++) {
                if (column > 0) writer.print(",");
                writer.print(escapeCSV(model.getColumnName(column)));
            }
            writer.println();

            for (int row = 0; row < model.getRowCount(); row++) {
                for (int column = 0;
                        column < model.getColumnCount(); column++) {
                    if (column > 0) writer.print(",");
                    Object value = model.getValueAt(row, column);
                    writer.print(escapeCSV(
                        value == null ? "" : value.toString()));
                }
                writer.println();
            }

            JOptionPane.showMessageDialog(this,
                "CSV exported successfully:\n" + file.getAbsolutePath());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Export failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeCSV(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }


    private boolean isValidDateRange() {
        try {
            LocalDate start = LocalDate.parse(startDateField.getText().trim());
            LocalDate end = LocalDate.parse(endDateField.getText().trim());

            if (start.isAfter(end)) {
                JOptionPane.showMessageDialog(this,
                    "Start date must not be after end date.");
                return false;
            }

            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Enter dates in YYYY-MM-DD format.");
            return false;
        }
    }

    

}
