
public class Product {
    private int productId;
    private String productName;
    private String category;
    private double purchasePrice;
    private double sellingPrice;
    private int stockQuantity;
    private int lowStockLimit;

    public Product(int productId, String productName, String category,
                   double purchasePrice, double sellingPrice,
                   int stockQuantity, int lowStockLimit) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
        this.lowStockLimit = lowStockLimit;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public int getLowStockLimit() {
        return lowStockLimit;
    }
}
