package projectdir.models;

public class CancelOrder {
    protected String comment;

    public CancelOrder() {
    }

    public CancelOrder(String commentOrder) {
        this.comment = commentOrder;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
