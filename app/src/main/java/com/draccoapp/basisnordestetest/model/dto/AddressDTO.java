package com.draccoapp.basisnordestetest.model.dto;

import com.draccoapp.basisnordestetest.model.AddressType;

public class AddressDTO {
    private String id;
    private AddressType addressType; // Agora usando o enum
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String zipCode;
    private String city;
    private String state;

    // Construtores
    public AddressDTO() {
        this.addressType = AddressType.RESIDENTIAL; // Valor padrão
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    // Para compatibilidade com códigos existentes
    public String getAddressTypeString() {
        return addressType != null ? addressType.name() : AddressType.RESIDENTIAL.name();
    }

    // Para compatibilidade com códigos existentes
    public void setAddressType(String addressType) {
        this.addressType = AddressType.fromString(addressType);
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
