package projectdir.models;

import java.sql.Timestamp;

public class CreateOrder extends Event{
    protected String customer;
    protected Timestamp expectedTime;
    protected String product;
    protected int priceOrder;

    public CreateOrder() {
    }



    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Timestamp getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(Timestamp expectedTime) {
        this.expectedTime = expectedTime;
    }

    public String getProduct() {return product;}

    public void setProduct(String product) {this.product = product;}

    public int getPriceOrder() {
        return priceOrder;
    }

    public void setPriceOrder(int priceOrder) {
        this.priceOrder = priceOrder;
    }
}
