
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class DashboardPanel extends JPanel {
    private JLabel productsValue;
    private JLabel inventoryValue;
    private JLabel revenueValue;
    private JLabel lowStockValue;
    private ChartsPanel chartsPanel;

    public DashboardPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        setBackground(new Color(245, 247, 250));

        JLabel heading = new JLabel("Dashboard");
        heading.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel subtitle = new JLabel(
            "Overview of your inventory and business performance");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 5, 5));
        titlePanel.setOpaque(false);
        titlePanel.add(heading);
        titlePanel.add(subtitle);

        JButton refreshButton = new JButton("Refresh Dashboard");
        refreshButton.addActionListener(e -> {
            loadStatistics();
            chartsPanel.loadCharts();
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(titlePanel, BorderLayout.CENTER);
        topPanel.add(refreshButton, BorderLayout.EAST);

        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));

        productsValue = new JLabel("0");
        inventoryValue = new JLabel("₹0.00");
        revenueValue = new JLabel("₹0.00");
        lowStockValue = new JLabel("0");

        cardsPanel.add(createCard("Total Products", productsValue,
            new Color(52, 120, 190)));
        cardsPanel.add(createCard("Inventory Value", inventoryValue,
            new Color(39, 150, 105)));
        cardsPanel.add(createCard("Total Sales Revenue", revenueValue,
            new Color(220, 140, 45)));
        cardsPanel.add(createCard("Low-Stock Products", lowStockValue,
            new Color(190, 75, 75)));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        chartsPanel = new ChartsPanel();
        centerPanel.add(chartsPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        loadStatistics();
    }

    private JPanel createCard(String title, JLabel value, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 235)),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 17));

        value.setFont(new Font("Arial", Font.BOLD, 28));
        value.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);

        return card;
    }

    private void loadStatistics() {
        String sql =
            "SELECT " +
            "(SELECT COUNT(*) FROM products) AS total_products, " +
            "(SELECT COALESCE(SUM(stock_quantity * purchase_price), 0) " +
            "FROM products) AS inventory_value, " +
            "(SELECT COALESCE(SUM(total_amount), 0) FROM sales) " +
            "AS total_revenue, " +
            "(SELECT COUNT(*) FROM products " +
            "WHERE stock_quantity <= low_stock_limit) AS low_stock_count";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                productsValue.setText(
                    String.valueOf(rs.getInt("total_products")));

                inventoryValue.setText(String.format("₹%,.2f",
                    rs.getDouble("inventory_value")));

                revenueValue.setText(String.format("₹%,.2f",
                    rs.getDouble("total_revenue")));

                lowStockValue.setText(
                    String.valueOf(rs.getInt("low_stock_count")));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Could not load dashboard: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
