
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChartsPanel extends JPanel {
    private BarChart revenueChart;
    private BarChart productsChart;
    private BarChart profitChart;
    private JComboBox<String> periodBox;

    public ChartsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        revenueChart = new BarChart("Daily Sales Revenue", "Revenue (Rs.)");
        productsChart = new BarChart("Best-Selling Products", "Quantity Sold");
        profitChart = new BarChart("Daily Gross Profit", "Profit (Rs.)");

        periodBox = new JComboBox<>(new String[]{
            "Today",
            "Last 7 Days",
            "Last 30 Days",
            "All Time"
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Sales Period:"));
        topPanel.add(periodBox);

        add(topPanel, BorderLayout.NORTH);

        JPanel chartsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        chartsPanel.setOpaque(false);

        chartsPanel.add(revenueChart);
        chartsPanel.add(productsChart);
        chartsPanel.add(profitChart);

        JScrollPane scrollPane = new JScrollPane(chartsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        periodBox.addActionListener(e -> loadCharts());

        loadCharts();
    }

    public void loadCharts() {
        loadRevenueChart();
        loadProductsChart();
        loadProfitChart();
    }

    
    private void loadRevenueChart() {
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        String period = (String) periodBox.getSelectedItem();

        String sql;

        if ("Last 7 Days".equals(period)) {
            sql = "SELECT DATE_FORMAT(d.day, '%d %b') AS sale_day, " +
                  "COALESCE(SUM(s.total_amount), 0) AS revenue " +
                  "FROM (" +
                  "SELECT CURDATE() - INTERVAL 6 DAY AS day " +
                  "UNION ALL SELECT CURDATE() - INTERVAL 5 DAY " +
                  "UNION ALL SELECT CURDATE() - INTERVAL 4 DAY " +
                  "UNION ALL SELECT CURDATE() - INTERVAL 3 DAY " +
                  "UNION ALL SELECT CURDATE() - INTERVAL 2 DAY " +
                  "UNION ALL SELECT CURDATE() - INTERVAL 1 DAY " +
                  "UNION ALL SELECT CURDATE()" +
                  ") d LEFT JOIN sales s ON DATE(s.sale_date) = d.day " +
                  "GROUP BY d.day ORDER BY d.day";
        } else if ("Last 30 Days".equals(period)) {
            sql = "SELECT DATE(sale_date) AS sale_day, " +
                  "SUM(total_amount) AS revenue FROM sales " +
                  "WHERE sale_date >= CURDATE() - INTERVAL 29 DAY " +
                  "GROUP BY DATE(sale_date) ORDER BY sale_day";
        } else {
            sql = "SELECT DATE(sale_date) AS sale_day, " +
                  "SUM(total_amount) AS revenue FROM sales " +
                  "GROUP BY DATE(sale_date) ORDER BY sale_day";
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                labels.add(rs.getString("sale_day"));
                values.add(rs.getDouble("revenue"));
            }

            revenueChart.setData(labels, values);

        } catch (SQLException ex) {
            showError("Could not load revenue chart: " + ex.getMessage());
        }
    }


    
    
    private void loadProductsChart() {
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        String period = (String) periodBox.getSelectedItem();
        String dateCondition = "";

        if ("Last 7 Days".equals(period)) {
            dateCondition =
                "WHERE s.sale_date >= CURDATE() - INTERVAL 6 DAY ";
        } else if ("Last 30 Days".equals(period)) {
            dateCondition =
                "WHERE s.sale_date >= CURDATE() - INTERVAL 29 DAY ";
        }

        String sql =
            "SELECT p.product_name, SUM(s.quantity) AS quantity_sold " +
            "FROM sales s " +
            "JOIN products p ON s.product_id = p.product_id " +
            dateCondition +
            "GROUP BY p.product_id, p.product_name " +
            "ORDER BY quantity_sold DESC " +
            "LIMIT 5";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

          while (rs.next()) {
              labels.add(rs.getString("product_name"));
              values.add(rs.getDouble("quantity_sold"));
          }

          productsChart.setData(labels, values);

        } catch (SQLException ex) {
            showError("Could not load product chart: " + ex.getMessage());
        }
    }



    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Chart Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class BarChart extends JPanel {
        private final String title;
        private final String axisLabel;
        private List<String> labels = new ArrayList<>();
        private List<Double> values = new ArrayList<>();

        public BarChart(String title, String axisLabel) {
            this.title = title;
            this.axisLabel = axisLabel;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(
                new Color(220, 225, 230)));
            setPreferredSize(new Dimension(600, 320));
            setMinimumSize(new Dimension(400, 280));
        }

        public void setData(List<String> labels, List<Double> values) {
            this.labels = new ArrayList<>(labels);
            this.values = new ArrayList<>(values);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            g2.setColor(new Color(35, 47, 62));
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(title, 15, 25);

            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(axisLabel, 15, 45);

            int left = 55;
            int right = 20;
            int top = 80;
            int bottom = 45;

            int chartWidth = width - left - right;
            int chartHeight = height - top - bottom;

            if (labels.isEmpty()) {
                g2.drawString("No sales data available",
                    left + 10, top + chartHeight / 2);
                g2.dispose();
                return;
            }

            double max = 0;
            for (double value : values) {
                max = Math.max(max, value);
            }
            if (max == 0) max = 1;

            g2.setColor(new Color(225, 230, 235));
            g2.drawLine(left, top, left, top + chartHeight);
            g2.drawLine(left, top + chartHeight,
                left + chartWidth, top + chartHeight);

            int count = labels.size();
            int slotWidth = Math.max(1, chartWidth / count);
            int barWidth = Math.max(5, slotWidth / 2);

            for (int i = 0; i < count; i++) {
                int barHeight = (int)
                    ((values.get(i) / max) * (chartHeight - 15));
                if (values.get(i) == 0) {
                    barHeight = 2;
                }

                int x = left + i * slotWidth +
                    (slotWidth - barWidth) / 2;
                int y = top + chartHeight - barHeight;

                g2.setColor(new Color(55, 130, 200));
                g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);

                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Arial", Font.PLAIN, 10));

                String valueText = String.format("%.0f", values.get(i));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(valueText,
                    x + (barWidth - fm.stringWidth(valueText)) / 2,
                    Math.max(top + 10, y - 5));

                String label = labels.get(i);
                if (label.length() > 9) {
                    label = label.substring(0, 8) + "...";
                }

                int labelWidth = fm.stringWidth(label);
                int labelX = x + (barWidth - labelWidth) / 2;
                int labelY = top + chartHeight + 20;

                g2.drawString(label, labelX, labelY);
            }

            g2.dispose();
        }
    }

    
    private void loadProfitChart() {
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        String period = (String) periodBox.getSelectedItem();
        String sql;

        if ("Today".equals(period)) {
            sql =
                "SELECT DATE_FORMAT(CURDATE(), '%d %b') AS sale_day, " +
                "COALESCE(SUM(s.total_amount - " +
                "(s.quantity * p.purchase_price)), 0) AS profit " +
                "FROM sales s JOIN products p " +
                "ON s.product_id = p.product_id " +
                "WHERE s.sale_date >= CURDATE() " +
                "AND s.sale_date < CURDATE() + INTERVAL 1 DAY";

        } else if ("Last 7 Days".equals(period)) {
            sql =
                "SELECT DATE_FORMAT(d.day, '%d %b') AS sale_day, " +
                "COALESCE(SUM(s.total_amount - " +
                "(s.quantity * p.purchase_price)), 0) AS profit " +
                "FROM (" +
                "SELECT CURDATE() - INTERVAL 6 DAY AS day " +
                "UNION ALL SELECT CURDATE() - INTERVAL 5 DAY " +
                "UNION ALL SELECT CURDATE() - INTERVAL 4 DAY " +
                "UNION ALL SELECT CURDATE() - INTERVAL 3 DAY " +
                "UNION ALL SELECT CURDATE() - INTERVAL 2 DAY " +
                "UNION ALL SELECT CURDATE() - INTERVAL 1 DAY " +
                "UNION ALL SELECT CURDATE()" +
                ") d LEFT JOIN sales s " +
                "ON DATE(s.sale_date) = d.day " +
                "LEFT JOIN products p ON s.product_id = p.product_id " +
                "GROUP BY d.day ORDER BY d.day";

        } else if ("Last 30 Days".equals(period)) {
            sql =
                "SELECT DATE(s.sale_date) AS sale_day, " +
                "SUM(s.total_amount - " +
                "(s.quantity * p.purchase_price)) AS profit " +
                "FROM sales s " +
                "JOIN products p ON s.product_id = p.product_id " +
                "WHERE s.sale_date >= CURDATE() - INTERVAL 29 DAY " +
                "AND s.sale_date < CURDATE() + INTERVAL 1 DAY " +
                "GROUP BY DATE(s.sale_date) " +
                "ORDER BY DATE(s.sale_date)";

        } else {
            sql =
                "SELECT DATE(s.sale_date) AS sale_day, " +
                "SUM(s.total_amount - " +
                "(s.quantity * p.purchase_price)) AS profit " +
                "FROM sales s " +
                "JOIN products p ON s.product_id = p.product_id " +
                "GROUP BY DATE(s.sale_date) " +
                "ORDER BY DATE(s.sale_date)";
        }


        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                labels.add(rs.getString("sale_day"));
                values.add(rs.getDouble("profit"));
            }

            profitChart.setData(labels, values);

        } catch (SQLException ex) {
            showError("Could not load profit chart: " + ex.getMessage());
        }
    }


}
