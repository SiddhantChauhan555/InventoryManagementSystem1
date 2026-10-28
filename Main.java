
import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;

    public Main() {
        setTitle("Inventory Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JLabel heading = new JLabel("INVENTORY MANAGEMENT SYSTEM");
        heading.setFont(new Font("Arial", Font.BOLD, 23));
        heading.setForeground(Color.WHITE);
        heading.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(35, 47, 62));
        header.add(heading, BorderLayout.CENTER);

        // Sidebar
        JPanel sidebar = new JPanel(new GridLayout(6, 1, 8, 8));
        sidebar.setBackground(new Color(45, 58, 75));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 12, 15, 12));
        sidebar.setPreferredSize(new Dimension(210, 0));

        String[] pages = {
            "Dashboard", "Products", "Stock Management",
            "Purchases", "Sales", "Reports"
        };

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(245, 247, 250));

        // Dashboard page
        contentPanel.add(new DashboardPanel(), "Dashboard");

        // Other pages: placeholders until we implement each module
        for (int i = 1; i < pages.length; i++) {
            
            if (pages[i].equals("Products")) {
                contentPanel.add(new ProductPanel(), "Products");
            }
            else if (pages[i].equals("Stock Management")) {
                contentPanel.add(new StockPanel(), "Stock Management");
            }
            else if (pages[i].equals("Purchases")) {
                contentPanel.add(new PurchasePanel(), "Purchases");
            } else if (pages[i].equals("Sales")) {
                contentPanel.add(new SalesPanel(), "Sales");
            } else if (pages[i].equals("Purchases")) {
                contentPanel.add(new PurchasePanel(), "Purchases");
             } else if (pages[i].equals("Sales")) {
                contentPanel.add(new SalesPanel(), "Sales");
            } else if (pages[i].equals("Reports")) {
                contentPanel.add(new ReportPanel(), "Reports");
            } else {
                contentPanel.add(createPlaceholder(pages[i]), pages[i]);
            }
        }

        for (String page : pages) {
            JButton button = new JButton(page);
            button.setFont(new Font("Arial", Font.PLAIN, 15));
            button.setFocusPainted(false);
            button.setBackground(new Color(230, 235, 242));
            button.addActionListener(e ->
                cardLayout.show(contentPanel, page)
            );
            sidebar.add(button);
        }

        add(header, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createPlaceholder(String pageName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel(pageName);
        title.setFont(new Font("Arial", Font.BOLD, 26));

        JLabel message = new JLabel(
            "The " + pageName + " interface will be implemented next."
        );
        message.setFont(new Font("Arial", Font.PLAIN, 16));

        panel.add(title, BorderLayout.NORTH);
        panel.add(message, BorderLayout.CENTER);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
