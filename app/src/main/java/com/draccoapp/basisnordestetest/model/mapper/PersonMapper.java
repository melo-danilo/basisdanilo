package com.draccoapp.basisnordestetest.model.mapper;

import com.draccoapp.basisnordestetest.model.Address;
import com.draccoapp.basisnordestetest.model.Person;
import com.draccoapp.basisnordestetest.model.PersonType;
import com.draccoapp.basisnordestetest.model.dto.AddressDTO;
import com.draccoapp.basisnordestetest.model.dto.PersonDTO;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.realm.RealmList;

@Singleton
public class PersonMapper {

    private final AddressMapper addressMapper;

    @Inject
    public PersonMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public PersonDTO fromRealm(Person person) {
        if (person == null) return null;

        PersonDTO dto = new PersonDTO();
        dto.setId(person.getId());
        // Converter String para enum PersonType
        dto.setPersonType(PersonType.fromString(person.getPersonType()));
        dto.setName(person.getName());
        dto.setCpf(person.getCpf());
        dto.setCompanyName(person.getCompanyName());
        dto.setCnpj(person.getCnpj());
        dto.setPhoneNumber(person.getPhoneNumber());
        dto.setEmail(person.getEmail());
        dto.setCreatedAt(person.getCreatedAt());
        dto.setLatitude(person.getLatitude());
        dto.setLongitude(person.getLongitude());
        dto.setDeviceName(person.getDeviceName());

        // Mapear endereços
        if (person.getAddresses() != null) {
            dto.setAddresses(addressMapper.fromRealm(person.getAddresses()));
        }

        return dto;
    }

    public List<PersonDTO> fromRealm(List<Person> persons) {
        if (persons == null) return null;

        List<PersonDTO> dtos = new ArrayList<>(persons.size());
        for (Person person : persons) {
            dtos.add(fromRealm(person));
        }
        return dtos;
    }

    public Person toRealm(PersonDTO dto) {
        if (dto == null) return null;

        Person person = new Person();
        person.setId(dto.getId());
        // Converter enum PersonType para String
        person.setPersonType(dto.getPersonType() != null ? dto.getPersonType().name() : PersonType.PHYSICAL.name());
        person.setName(dto.getName());
        person.setCpf(dto.getCpf());
        person.setCompanyName(dto.getCompanyName());
        person.setCnpj(dto.getCnpj());
        person.setPhoneNumber(dto.getPhoneNumber());
        person.setEmail(dto.getEmail());
        person.setCreatedAt(dto.getCreatedAt());
        person.setLatitude(dto.getLatitude());
        person.setLongitude(dto.getLongitude());
        person.setDeviceName(dto.getDeviceName());

        // Mapear endereços
        if (dto.getAddresses() != null) {
            RealmList<Address> addresses = new RealmList<>();
            for (AddressDTO addressDTO : dto.getAddresses()) {
                addresses.add(addressMapper.toRealm(addressDTO));
            }
            person.setAddresses(addresses);
        }

        return person;
    }
}
