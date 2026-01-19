package model;

public class PaymentMethod {
    private String cardNumber;
    private String cvc;
    private String expiryDate;
    private String cardHolderName;
    private String bankCardName;

    public PaymentMethod(String cardNumber, String cvc, String expiryDate, String cardHolderName, String bankCardName) {
        this.cardNumber = cardNumber;
        this.cvc = cvc;
        this.expiryDate = expiryDate;
        this.cardHolderName = cardHolderName;
        this.bankCardName = bankCardName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCVC() {
        return cvc;
    }

    public void setCVC(String cvc) {
        this.cvc = cvc;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getBankCardName() {
        return bankCardName;
    }

    public void setBankCardName(String bankCardName) {
        this.bankCardName = bankCardName;
    }
}
