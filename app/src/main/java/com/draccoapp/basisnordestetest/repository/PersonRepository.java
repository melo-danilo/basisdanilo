package com.draccoapp.basisnordestetest.repository;

import com.draccoapp.basisnordestetest.model.Address;
import com.draccoapp.basisnordestetest.model.Person;
import com.draccoapp.basisnordestetest.service.FirestoreService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

@Singleton
public class PersonRepository {

    private final Realm realm;
    private final FirestoreService firestoreService;

    @Inject
    public PersonRepository(Realm realm, FirestoreService firestoreService) {
        this.realm = realm;
        this.firestoreService = firestoreService;
    }

    public RealmResults<Person> getAllPersons() {
        return realm.where(Person.class)
                .sort("name", Sort.ASCENDING)
                .findAll();
    }

    // Método adicional que retorna cópias desvinculadas do Realm
    public List<Person> getAllPersonsDetached() {
        RealmResults<Person> results = realm.where(Person.class)
                .sort("name", Sort.ASCENDING)
                .findAll();
        return realm.copyFromRealm(results);
    }

    public RealmResults<Person> getPersonsByName(String query) {
        // Melhorar a pesquisa para ser case-insensitive e pesquisar em ambos os campos
        return realm.where(Person.class)
                .beginGroup()
                .contains("name", query, Case.INSENSITIVE)
                .or()
                .contains("companyName", query, Case.INSENSITIVE)
                .endGroup()
                .sort("name", Sort.ASCENDING)
                .findAll();
    }

    // Método adicional que retorna cópias desvinculadas do Realm
    public List<Person> getPersonsByNameDetached(String query) {
        RealmResults<Person> results = realm.where(Person.class)
                .beginGroup()
                .contains("name", query, Case.INSENSITIVE)
                .or()
                .contains("companyName", query, Case.INSENSITIVE)
                .endGroup()
                .sort("name", Sort.ASCENDING)
                .findAll();
        return realm.copyFromRealm(results);
    }

    public Person getPerson(String id) {
        return realm.where(Person.class).equalTo("id", id).findFirst();
    }

    // Método adicional que retorna uma cópia desvinculada do Realm
    public Person getPersonDetached(String id) {
        Person person = realm.where(Person.class).equalTo("id", id).findFirst();
        return person != null ? realm.copyFromRealm(person) : null;
    }

    public void savePerson(final Person person, final Callback callback) {
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    // Garantir que os IDs dos endereços estão definidos
                    if (person.getAddresses() != null) {
                        for (Address address : person.getAddresses()) {
                            if (address.getId() == null || address.getId().isEmpty()) {
                                address.setId(UUID.randomUUID().toString());
                            }
                        }
                    }

                    realm.insertOrUpdate(person);
                }
            });

            // Não precisamos mais salvar no Firebase aqui, pois isso é feito no ViewModel
            callback.onResult(true);

        } catch (Exception e) {
            callback.onResult(false);
        }
    }

    public void addAddressToPerson(final String personId, final Address address, final Callback callback) {
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Person person = realm.where(Person.class).equalTo("id", personId).findFirst();
                    if (person != null) {
                        // Garantir que o ID do endereço está definido
                        if (address.getId() == null || address.getId().isEmpty()) {
                            address.setId(UUID.randomUUID().toString());
                        }

                        person.getAddresses().add(address);
                    }
                }
            });
            callback.onResult(true);
        } catch (Exception e) {
            callback.onResult(false);
        }
    }

    public void updateAddress(final String personId, final Address address, final Callback callback) {
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Person person = realm.where(Person.class).equalTo("id", personId).findFirst();
                    if (person != null) {
                        RealmList<Address> addresses = person.getAddresses();
                        for (int i = 0; i < addresses.size(); i++) {
                            if (addresses.get(i).getId().equals(address.getId())) {
                                addresses.set(i, address);
                                break;
                            }
                        }
                    }
                }
            });
            callback.onResult(true);
        } catch (Exception e) {
            callback.onResult(false);
        }
    }

    public void removeAddress(final String personId, final String addressId, final Callback callback) {
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Person person = realm.where(Person.class).equalTo("id", personId).findFirst();
                    if (person != null) {
                        RealmList<Address> addresses = person.getAddresses();
                        for (int i = 0; i < addresses.size(); i++) {
                            if (addresses.get(i).getId().equals(addressId)) {
                                addresses.remove(i);
                                break;
                            }
                        }
                    }
                }
            });
            callback.onResult(true);
        } catch (Exception e) {
            callback.onResult(false);
        }
    }

    public void deletePerson(final String id, final Callback callback) {
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Person person = realm.where(Person.class).equalTo("id", id).findFirst();
                    if (person != null) {
                        // Remover todos os endereços associados
                        person.getAddresses().deleteAllFromRealm();
                        // Remover a pessoa
                        person.deleteFromRealm();
                    }
                }
            });

            // Excluir do Firestore
            firestoreService.deletePerson(id, new FirestoreService.FirestoreCallback() {
                @Override
                public void onSuccess(String documentId) {
                    callback.onResult(true);
                }

                @Override
                public void onFailure(Exception e) {
                    // Mesmo com falha no Firestore, consideramos excluído no Realm
                    callback.onResult(true);
                }
            });

        } catch (Exception e) {
            callback.onResult(false);
        }
    }

    // Método para operações em lote
    public void deleteMultiplePersons(final List<String> ids, final Callback callback) {
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (String id : ids) {
                        Person person = realm.where(Person.class).equalTo("id", id).findFirst();
                        if (person != null) {
                            // Remover todos os endereços associados
                            person.getAddresses().deleteAllFromRealm();
                            // Remover a pessoa
                            person.deleteFromRealm();
                        }
                    }
                }
            });

            // Excluir do Firestore
            for (String id : ids) {
                firestoreService.deletePerson(id, new FirestoreService.FirestoreCallback() {
                    @Override
                    public void onSuccess(String documentId) {
                        // Não fazemos nada aqui, pois o callback final será chamado após o loop
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Não fazemos nada aqui, pois o callback final será chamado após o loop
                    }
                });
            }

            callback.onResult(true);
        } catch (Exception e) {
            callback.onResult(false);
        }
    }

    public void close() {
        realm.close();
    }

    public interface Callback {
        void onResult(boolean success);
    }
}
