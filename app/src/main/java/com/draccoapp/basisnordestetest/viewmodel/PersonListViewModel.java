package com.draccoapp.basisnordestetest.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.draccoapp.basisnordestetest.model.Address;
import com.draccoapp.basisnordestetest.model.Person;
import com.draccoapp.basisnordestetest.model.dto.PersonDTO;
import com.draccoapp.basisnordestetest.model.mapper.PersonMapper;
import com.draccoapp.basisnordestetest.repository.PersonRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

@HiltViewModel
public class PersonListViewModel extends ViewModel {

    private final PersonRepository repository;
    private final PersonMapper personMapper;

    private final MutableLiveData<RealmResults<Person>> persons = new MutableLiveData<>();
    // Adicionar LiveData para lista desvinculada
    private final MutableLiveData<List<PersonDTO>> detachedPersons = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    // Listener para mudanças no Realm
    private RealmChangeListener<RealmResults<Person>> realmChangeListener;
    private RealmResults<Person> currentResults;

    @Inject
    public PersonListViewModel(PersonRepository repository, PersonMapper personMapper) {
        this.repository = repository;
        this.personMapper = personMapper;

        // Inicializar o listener
        realmChangeListener = results -> {
            persons.setValue(results);
            detachedPersons.setValue(personMapper.fromRealm(repository.getAllPersonsDetached()));
        };

        loadAllPersons();
    }

    public LiveData<RealmResults<Person>> getPersons() {
        return persons;
    }

    // Getter para a lista desvinculada
    public LiveData<List<PersonDTO>> getDetachedPersons() {
        return detachedPersons;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadAllPersons() {
        loading.setValue(true);
        try {
            // Remover o listener anterior se existir
            if (currentResults != null) {
                currentResults.removeChangeListener(realmChangeListener);
            }

            // Obter novos resultados e adicionar o listener
            RealmResults<Person> result = repository.getAllPersons();
            currentResults = result;
            result.addChangeListener(realmChangeListener);

            persons.setValue(result);
            detachedPersons.setValue(personMapper.fromRealm(repository.getAllPersonsDetached()));
            loading.setValue(false);
        } catch (Exception e) {
            error.setValue("Error loading persons: " + e.getMessage());
            loading.setValue(false);
        }
    }

    public void searchPersons(String query) {
        loading.setValue(true);
        try {
            // Log para debug
            Log.d("PersonListViewModel", "Searching for: " + query);

            // Remover o listener anterior se existir
            if (currentResults != null) {
                currentResults.removeChangeListener(realmChangeListener);
            }

            // Obter novos resultados e adicionar o listener
            RealmResults<Person> result = repository.getPersonsByName(query);
            currentResults = result;
            result.addChangeListener(realmChangeListener);

            // Log para debug
            Log.d("PersonListViewModel", "Search results count: " + result.size());

            persons.setValue(result);
            List<Person> detachedResults = repository.getPersonsByNameDetached(query);
            List<PersonDTO> dtos = personMapper.fromRealm(detachedResults);

            // Log para debug
            Log.d("PersonListViewModel", "Mapped DTOs count: " + (dtos != null ? dtos.size() : 0));

            detachedPersons.setValue(dtos);
            loading.setValue(false);
        } catch (Exception e) {
            Log.e("PersonListViewModel", "Error searching persons", e);
            error.setValue("Error searching persons: " + e.getMessage());
            loading.setValue(false);
        }
    }

    public void deletePerson(String id, final PersonRepository.Callback callback) {
        loading.setValue(true);
        repository.deletePerson(id, success -> {
            if (!success) {
                error.setValue("Error deleting person");
            }
            loading.setValue(false);
            callback.onResult(success);
        });
    }

    // Método para excluir múltiplas pessoas
    public void deleteMultiplePersons(List<String> ids, final PersonRepository.Callback callback) {
        loading.setValue(true);
        repository.deleteMultiplePersons(ids, success -> {
            if (!success) {
                error.setValue("Error deleting persons");
            }
            loading.setValue(false);
            callback.onResult(success);
        });
    }

    // Métodos para gerenciar endereços
    public void addAddressToPerson(String personId, Address address, final PersonRepository.Callback callback) {
        loading.setValue(true);
        repository.addAddressToPerson(personId, address, success -> {
            if (!success) {
                error.setValue("Error adding address");
            }
            loading.setValue(false);
            callback.onResult(success);
        });
    }

    public void updateAddress(String personId, Address address, final PersonRepository.Callback callback) {
        loading.setValue(true);
        repository.updateAddress(personId, address, success -> {
            if (!success) {
                error.setValue("Error updating address");
            }
            loading.setValue(false);
            callback.onResult(success);
        });
    }

    public void removeAddress(String personId, String addressId, final PersonRepository.Callback callback) {
        loading.setValue(true);
        repository.removeAddress(personId, addressId, success -> {
            if (!success) {
                error.setValue("Error removing address");
            }
            loading.setValue(false);
            callback.onResult(success);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Remover o listener quando o ViewModel for destruído
        if (currentResults != null) {
            currentResults.removeChangeListener(realmChangeListener);
        }
    }
}