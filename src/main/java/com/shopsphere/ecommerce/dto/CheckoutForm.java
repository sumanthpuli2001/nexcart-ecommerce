package com.shopsphere.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CheckoutForm {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be at least 2 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format (e.g. name@domain.com)")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+91-?)?[0-9]{10}$", message = "Please enter a valid mobile number")
    private String phone;

    @NotBlank(message = "Shipping address is required")
    @Size(min = 5, message = "Address must be at least 5 characters long")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Zip code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Please enter a valid 6-digit PIN code")
    private String zipCode;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod = "Cash On Delivery";

    public CheckoutForm() {
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
