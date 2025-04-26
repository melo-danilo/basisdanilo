package com.draccoapp.basisnordestetest.model.dto;

import com.draccoapp.basisnordestetest.model.PersonType;

import java.util.ArrayList;
import java.util.List;

public class PersonDTO {
    private String id;
    private PersonType personType; // Agora usando o enum
    private String name;
    private String cpf;
    private String companyName;
    private String cnpj;
    private String phoneNumber;
    private String email;
    private List<AddressDTO> addresses;
    private long createdAt;
    private double latitude;
    private double longitude;
    private String deviceName;

    // Construtores
    public PersonDTO() {
        this.addresses = new ArrayList<>();
        this.personType = PersonType.PHYSICAL; // Definir tipo padrão
        this.name = "";
        this.cpf = "";
        this.companyName = "";
        this.cnpj = "";
        this.phoneNumber = "";
        this.email = "";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PersonType getPersonType() {
        return personType;
    }

    public void setPersonType(PersonType personType) {
        this.personType = personType;
    }

    // Para compatibilidade com códigos existentes
    public String getPersonTypeString() {
        return personType != null ? personType.name() : PersonType.PHYSICAL.name();
    }

    // Para compatibilidade com códigos existentes
    public void setPersonType(String personType) {
        this.personType = PersonType.fromString(personType);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<AddressDTO> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressDTO> addresses) {
        this.addresses = addresses;
    }

    public void addAddress(AddressDTO address) {
        this.addresses.add(address);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}