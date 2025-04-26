package com.draccoapp.basisnordestetest.model.mapper;

import com.draccoapp.basisnordestetest.model.Address;
import com.draccoapp.basisnordestetest.model.AddressType;
import com.draccoapp.basisnordestetest.model.dto.AddressDTO;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AddressMapper {

    @Inject
    public AddressMapper() {
        // Construtor vazio para injeção
    }

    public AddressDTO fromRealm(Address address) {
        if (address == null) return null;

        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        // Converter String para enum AddressType
        dto.setAddressType(AddressType.fromString(address.getAddressType()));
        dto.setStreet(address.getStreet());
        dto.setNumber(address.getNumber());
        dto.setComplement(address.getComplement());
        dto.setNeighborhood(address.getNeighborhood());
        dto.setZipCode(address.getZipCode());
        dto.setCity(address.getCity());
        dto.setState(address.getState());

        return dto;
    }

    public List<AddressDTO> fromRealm(Iterable<Address> addresses) {
        if (addresses == null) return null;

        List<AddressDTO> dtos = new ArrayList<>();
        for (Address address : addresses) {
            dtos.add(fromRealm(address));
        }
        return dtos;
    }

    public Address toRealm(AddressDTO dto) {
        if (dto == null) return null;

        Address address = new Address();
        address.setId(dto.getId());
        // Converter enum AddressType para String
        address.setAddressType(dto.getAddressType() != null ? dto.getAddressType().name() : AddressType.RESIDENTIAL.name());
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setComplement(dto.getComplement());
        address.setNeighborhood(dto.getNeighborhood());
        address.setZipCode(dto.getZipCode());
        address.setCity(dto.getCity());
        address.setState(dto.getState());

        return address;
    }
}
