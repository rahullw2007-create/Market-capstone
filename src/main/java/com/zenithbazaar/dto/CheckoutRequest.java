package com.zenithbazaar.dto;

public class CheckoutRequest {
    private String paymentMethod;
    private String shippingAddress;

    public CheckoutRequest() {}

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}
