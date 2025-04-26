package com.draccoapp.basisnordestetest.viewmodel;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.draccoapp.basisnordestetest.R;
import com.draccoapp.basisnordestetest.model.Address;
import com.draccoapp.basisnordestetest.model.AddressType;
import com.draccoapp.basisnordestetest.model.Person;
import com.draccoapp.basisnordestetest.model.PersonType;
import com.draccoapp.basisnordestetest.model.dto.AddressDTO;
import com.draccoapp.basisnordestetest.model.dto.PersonDTO;
import com.draccoapp.basisnordestetest.model.mapper.AddressMapper;
import com.draccoapp.basisnordestetest.model.mapper.PersonMapper;
import com.draccoapp.basisnordestetest.repository.PersonRepository;
import com.draccoapp.basisnordestetest.service.FirestoreService;
import com.draccoapp.basisnordestetest.service.LocationService;
import com.draccoapp.basisnordestetest.ui.activities.MainActivity;
import com.draccoapp.basisnordestetest.util.LocationUtil;
import com.draccoapp.basisnordestetest.util.NotificationUtil;
import com.draccoapp.basisnordestetest.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.realm.RealmList;

@HiltViewModel
public class PersonFormViewModel extends ViewModel {

    private final PersonRepository repository;
    private final PersonMapper personMapper;
    private final AddressMapper addressMapper;
    private final LocationService locationService;
    private final FirestoreService firestoreService;

    private final MutableLiveData<PersonDTO> person = new MutableLiveData<>(new PersonDTO());
    private final MutableLiveData<List<AddressDTO>> addresses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saved = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    // Campos individuais para binding
    private final MutableLiveData<PersonType> personType = new MutableLiveData<>(PersonType.PHYSICAL);
    private final MutableLiveData<String> name = new MutableLiveData<>("");
    private final MutableLiveData<String> cpf = new MutableLiveData<>("");
    private final MutableLiveData<String> companyName = new MutableLiveData<>("");
    private final MutableLiveData<String> cnpj = new MutableLiveData<>("");
    private final MutableLiveData<String> phoneNumber = new MutableLiveData<>("");
    private final MutableLiveData<String> email = new MutableLiveData<>("");

    // Campos para localização
    private final MutableLiveData<Double> latitude = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> longitude = new MutableLiveData<>(0.0);
    private final MutableLiveData<String> deviceName = new MutableLiveData<>("");

    @Inject
    public PersonFormViewModel(PersonRepository repository, PersonMapper personMapper,
                               AddressMapper addressMapper, LocationService locationService,
                               FirestoreService firestoreService) {
        this.repository = repository;
        this.personMapper = personMapper;
        this.addressMapper = addressMapper;
        this.locationService = locationService;
        this.firestoreService = firestoreService;

        // Inicializar com valores padrão
        PersonDTO defaultPerson = new PersonDTO();
        defaultPerson.setPersonType(PersonType.PHYSICAL); // Definir tipo padrão
        person.setValue(defaultPerson);

        // Definir nome do dispositivo
        deviceName.setValue(obtainDeviceName());
    }

    public LiveData<PersonDTO> getPerson() {
        return person;
    }

    public LiveData<List<AddressDTO>> getAddresses() {
        return addresses;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getSaved() {
        return saved;
    }

    public LiveData<String> getError() {
        return error;
    }

    // Getters para campos individuais
    public MutableLiveData<PersonType> getPersonType() {
        return personType;
    }

    public MutableLiveData<String> getName() {
        return name;
    }

    public MutableLiveData<String> getCpf() {
        return cpf;
    }

    public MutableLiveData<String> getCompanyName() {
        return companyName;
    }

    public MutableLiveData<String> getCnpj() {
        return cnpj;
    }

    public MutableLiveData<String> getPhoneNumber() {
        return phoneNumber;
    }

    public MutableLiveData<String> getEmail() {
        return email;
    }

    public MutableLiveData<Double> getLatitude() {
        return latitude;
    }

    public MutableLiveData<Double> getLongitude() {
        return longitude;
    }

    public MutableLiveData<String> getDeviceName() {
        return deviceName;
    }

    public void loadPerson(String id) {
        if (id == null || id.isEmpty()) {
            // Se não tiver ID, criar uma nova pessoa com valores padrão
            PersonDTO newPerson = new PersonDTO();
            newPerson.setPersonType(PersonType.PHYSICAL);
            person.setValue(newPerson);
            addresses.setValue(new ArrayList<>());
            return;
        }

        loading.setValue(true);
        try {
            Person realmPerson = repository.getPersonDetached(id);
            if (realmPerson != null) {
                PersonDTO personDTO = personMapper.fromRealm(realmPerson);
                // Garantir que personType não seja nulo
                if (personDTO.getPersonType() == null) {
                    personDTO.setPersonType(PersonType.PHYSICAL);
                }
                person.setValue(personDTO);
                addresses.setValue(personDTO.getAddresses() != null ? personDTO.getAddresses() : new ArrayList<>());

                // Atualizar campos individuais
                personType.setValue(personDTO.getPersonType());
                name.setValue(personDTO.getName() != null ? personDTO.getName() : "");
                cpf.setValue(personDTO.getCpf() != null ? personDTO.getCpf() : "");
                companyName.setValue(personDTO.getCompanyName() != null ? personDTO.getCompanyName() : "");
                cnpj.setValue(personDTO.getCnpj() != null ? personDTO.getCnpj() : "");
                phoneNumber.setValue(personDTO.getPhoneNumber() != null ? personDTO.getPhoneNumber() : "");
                email.setValue(personDTO.getEmail() != null ? personDTO.getEmail() : "");
                latitude.setValue(personDTO.getLatitude());
                longitude.setValue(personDTO.getLongitude());
                deviceName.setValue(personDTO.getDeviceName() != null ? personDTO.getDeviceName() : obtainDeviceName());
            } else {
                // Se não encontrar a pessoa, criar uma nova
                PersonDTO newPerson = new PersonDTO();
                newPerson.setPersonType(PersonType.PHYSICAL);
                person.setValue(newPerson);
                addresses.setValue(new ArrayList<>());
            }
        } catch (Exception e) {
            error.setValue("Error loading person: " + e.getMessage());
            // Em caso de erro, criar uma nova pessoa
            PersonDTO newPerson = new PersonDTO();
            newPerson.setPersonType(PersonType.PHYSICAL);
            person.setValue(newPerson);
            addresses.setValue(new ArrayList<>());
        } finally {
            loading.setValue(false);
        }
    }

    public void addAddress() {
        List<AddressDTO> currentAddresses = addresses.getValue();
        if (currentAddresses == null) {
            currentAddresses = new ArrayList<>();
        }

        AddressDTO newAddress = new AddressDTO();
        newAddress.setId(UUID.randomUUID().toString());
        newAddress.setAddressType(AddressType.RESIDENTIAL); // Default type usando o enum

        currentAddresses.add(newAddress);
        addresses.setValue(currentAddresses);
    }

    public void updateAddress(AddressDTO address, int position) {
        List<AddressDTO> currentAddresses = addresses.getValue();
        if (currentAddresses != null && position >= 0 && position < currentAddresses.size()) {
            currentAddresses.set(position, address);
            addresses.setValue(currentAddresses);
        }
    }

    public void removeAddress(int position) {
        List<AddressDTO> currentAddresses = addresses.getValue();
        if (currentAddresses != null && position >= 0 && position < currentAddresses.size()) {
            currentAddresses.remove(position);
            addresses.setValue(currentAddresses);
        }
    }

    public void requestLocation(Context context) {
        if (locationService.hasLocationPermission(context)) {
            locationService.getCurrentLocation(context, new LocationService.LocationListener() {
                @Override
                public void onLocationReceived(double lat, double lng) {
                    latitude.setValue(lat);
                    longitude.setValue(lng);
                }

                @Override
                public void onLocationError(String errorMsg) {
                    error.setValue("Erro ao obter localização: " + errorMsg);
                }
            });
        }
    }

    // Modificar o método savePerson para garantir que as notificações sejam enviadas
    public void savePerson(Context context) {
        loading.setValue(true);

        try {
            // Validar campos obrigatórios
            if (!validateFields()) {
                loading.setValue(false);
                return;
            }

            // Obter a pessoa atual ou criar uma nova
            PersonDTO currentPerson = person.getValue();
            if (currentPerson == null) {
                currentPerson = new PersonDTO();
            }

            // Se não tiver ID, gerar um novo
            if (currentPerson.getId() == null || currentPerson.getId().isEmpty()) {
                currentPerson.setId(UUID.randomUUID().toString());
            }

            // Atualizar campos da pessoa
            currentPerson.setPersonType(personType.getValue());
            currentPerson.setName(name.getValue());
            currentPerson.setCpf(cpf.getValue());
            currentPerson.setCompanyName(companyName.getValue());
            currentPerson.setCnpj(cnpj.getValue());
            currentPerson.setPhoneNumber(phoneNumber.getValue());
            currentPerson.setEmail(email.getValue());
            currentPerson.setCreatedAt(System.currentTimeMillis());
            currentPerson.setLatitude(latitude.getValue() != null ? latitude.getValue() : 0.0);
            currentPerson.setLongitude(longitude.getValue() != null ? longitude.getValue() : 0.0);
            currentPerson.setDeviceName(deviceName.getValue());

            // Adicionar endereços
            currentPerson.setAddresses(addresses.getValue());

            // Converter para objeto Realm e salvar
            final Person realmPerson = personMapper.toRealm(currentPerson);
            final PersonDTO finalPerson = currentPerson;
            final Context appContext = context.getApplicationContext();

            repository.savePerson(realmPerson, success -> {
                if (success) {
                    // Salvar no Firestore
                    firestoreService.savePerson(finalPerson, new FirestoreService.FirestoreCallback() {
                        @Override
                        public void onSuccess(String documentId) {
                            // Enviar notificação
                            String title = "Nova Pessoa Cadastrada";
                            String message = "A pessoa " +
                                    (finalPerson.getPersonType() == PersonType.PHYSICAL ?
                                            finalPerson.getName() : finalPerson.getCompanyName()) +
                                    " foi cadastrada com sucesso.";

                            // Enviar notificação diretamente
                            showDirectNotification(appContext, title, message);

                            // Registrar no Firestore (sem tentar enviar notificação local do FirestoreService)
                            firestoreService.sendNotification(finalPerson.getId(), title, message);

                            loading.postValue(false);
                            saved.postValue(true);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Mesmo com falha no Firestore, consideramos salvo no Realm
                            loading.postValue(false);
                            saved.postValue(true);
                            error.postValue("Aviso: Salvo localmente, mas houve erro ao sincronizar: " + e.getMessage());
                        }
                    });
                } else {
                    loading.postValue(false);
                    error.postValue("Erro ao salvar pessoa");
                }
            });

        } catch (Exception e) {
            loading.setValue(false);
            error.setValue("Erro: " + e.getMessage());
        }
    }

    // Adicionar método para mostrar notificação diretamente
    private void showDirectNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Criar canal de notificação para Android O e superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "person_notifications",
                    "Person Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        // Criar intent para abrir o app quando a notificação for clicada
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Construir a notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "person_notifications")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Mostrar a notificação
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());

        Log.d("PersonFormViewModel", "Notification displayed with ID: " + notificationId);
    }

    private boolean validateFields() {
        // Limpar erro anterior
        error.setValue(null);

        // Validar tipo de pessoa
        PersonType type = personType.getValue();
        if (type == null) {
            error.setValue("Tipo de pessoa é obrigatório");
            return false;
        }

        // Validar campos específicos por tipo de pessoa
        if (type == PersonType.PHYSICAL) {
            if (TextUtils.isEmpty(name.getValue())) {
                error.setValue("Nome é obrigatório");
                return false;
            }

            String cpfValue = cpf.getValue();
            if (TextUtils.isEmpty(cpfValue)) {
                error.setValue("CPF é obrigatório");
                return false;
            }

            // Validar CPF
            if (!ValidationUtil.isValidCpf(cpfValue)) {
                error.setValue("CPF inválido. Deve conter 11 dígitos válidos");
                return false;
            }
        } else {
            if (TextUtils.isEmpty(companyName.getValue())) {
                error.setValue("Nome da empresa é obrigatório");
                return false;
            }

            String cnpjValue = cnpj.getValue();
            if (TextUtils.isEmpty(cnpjValue)) {
                error.setValue("CNPJ é obrigatório");
                return false;
            }

            // Validar CNPJ
            if (!ValidationUtil.isValidCnpj(cnpjValue)) {
                error.setValue("CNPJ inválido. Deve conter 14 dígitos válidos");
                return false;
            }
        }

        // Validar campos comuns
        String phoneValue = phoneNumber.getValue();
        if (TextUtils.isEmpty(phoneValue)) {
            error.setValue("Telefone é obrigatório");
            return false;
        }

        // Validar telefone
        if (!ValidationUtil.isValidPhone(phoneValue)) {
            error.setValue("Telefone inválido. Deve conter 10 ou 11 dígitos");
            return false;
        }

        String emailValue = email.getValue();
        if (TextUtils.isEmpty(emailValue)) {
            error.setValue("Email é obrigatório");
            return false;
        }

        // Validar email
        if (!ValidationUtil.isValidEmail(emailValue)) {
            error.setValue("Email inválido");
            return false;
        }

        // Validar endereços - agora opcional no início, mas será validado no momento do salvamento
        List<AddressDTO> addressList = addresses.getValue();
        if (addressList == null) {
            addresses.setValue(new ArrayList<>());
        }

        return true;
    }

    private String obtainDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
