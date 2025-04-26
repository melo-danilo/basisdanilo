package com.draccoapp.basisnordestetest.ui.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.draccoapp.basisnordestetest.R;
import com.draccoapp.basisnordestetest.databinding.DialogAddressFormBinding;
import com.draccoapp.basisnordestetest.databinding.FragmentPersonFormBinding;
import com.draccoapp.basisnordestetest.model.AddressType;
import com.draccoapp.basisnordestetest.model.PersonType;
import com.draccoapp.basisnordestetest.model.dto.AddressDTO;
import com.draccoapp.basisnordestetest.model.response.CepResponse;
import com.draccoapp.basisnordestetest.repository.CepRepository;
import com.draccoapp.basisnordestetest.service.LocationService;
import com.draccoapp.basisnordestetest.ui.adapters.AddressAdapter;
import com.draccoapp.basisnordestetest.util.MaskUtil;
import com.draccoapp.basisnordestetest.util.ValidationUtil;
import com.draccoapp.basisnordestetest.viewmodel.PersonFormViewModel;
import com.draccoapp.basisnordestetest.viewmodel.PersonListViewModel;

import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class PersonFormFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FragmentPersonFormBinding binding;
    private PersonFormViewModel viewModel;
    private AddressAdapter addressAdapter;

    @Inject
    CepRepository cepRepository;

    @Inject
    LocationService locationService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPersonFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PersonFormViewModel.class);

        setupRecyclerView();
        setupFormFields();
        setupListeners();
        observeViewModel();

        // Solicitar permissões de localização
        requestLocationPermission();

        // Verificar se está editando uma pessoa existente
        String personId = null;
        if (getArguments() != null) {
            PersonFormFragmentArgs args = PersonFormFragmentArgs.fromBundle(getArguments());
            personId = args.getPersonId();
        }

        if (personId != null && !personId.isEmpty()) {
            viewModel.loadPerson(personId);
        } else {
            // Se for uma nova pessoa, obter a localização atual
            if (hasLocationPermission()) {
                viewModel.requestLocation(requireContext());
            }
        }
    }

    private void setupRecyclerView() {
        addressAdapter = new AddressAdapter();
        addressAdapter.setOnAddressActionListener(new AddressAdapter.OnAddressActionListener() {
            @Override
            public void onAddressRemove(int position) {
                viewModel.removeAddress(position);
            }

            @Override
            public void onAddressEdit(AddressDTO address, int position) {
                showAddressDialog(address, position);
            }
        });

        binding.recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewAddresses.setAdapter(addressAdapter);
    }

    private void setupFormFields() {
        // Configurar spinner de tipo de pessoa com os nomes amigáveis
        ArrayAdapter<String> personTypeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                PersonType.getDisplayNames()
        );
        personTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPersonType.setAdapter(personTypeAdapter);

        // Adicionar máscaras aos campos
        MaskUtil.addPhoneMask(binding.editTextPhone);
        MaskUtil.addCpfMask(binding.editTextCpf);
        MaskUtil.addCnpjMask(binding.editTextCnpj);

        // Adicionar validação em tempo real para CPF
        binding.editTextCpf.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String cpf = s.toString();
                if (!cpf.isEmpty() && cpf.replaceAll("[^0-9]", "").length() == 11) {
                    if (!ValidationUtil.isValidCpf(cpf)) {
                        binding.editTextCpf.setError("CPF inválido");
                    } else {
                        binding.editTextCpf.setError(null);
                    }
                }
            }
        });

        // Adicionar validação em tempo real para CNPJ
        binding.editTextCnpj.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String cnpj = s.toString();
                if (!cnpj.isEmpty() && cnpj.replaceAll("[^0-9]", "").length() == 14) {
                    if (!ValidationUtil.isValidCnpj(cnpj)) {
                        binding.editTextCnpj.setError("CNPJ inválido");
                    } else {
                        binding.editTextCnpj.setError(null);
                    }
                }
            }
        });

        // Adicionar validação em tempo real para email
        binding.editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString();
                if (!email.isEmpty()) {
                    if (!ValidationUtil.isValidEmail(email)) {
                        binding.editTextEmail.setError("Email inválido");
                    } else {
                        binding.editTextEmail.setError(null);
                    }
                }
            }
        });

        // Adicionar validação em tempo real para telefone
        binding.editTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String phone = s.toString();
                if (!phone.isEmpty()) {
                    String digits = phone.replaceAll("[^0-9]", "");
                    if (digits.length() > 0 && !ValidationUtil.isValidPhone(phone)) {
                        binding.editTextPhone.setError("Telefone inválido");
                    } else {
                        binding.editTextPhone.setError(null);
                    }
                }
            }
        });
    }

    private void setupListeners() {
        binding.spinnerPersonType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PersonType type = PersonType.fromSpinnerPosition(position);
                viewModel.getPersonType().setValue(type);
                updateFormVisibility(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.buttonAddAddress.setOnClickListener(v -> {
            // Criar um novo endereço e mostrar o diálogo para edição
            AddressDTO newAddress = new AddressDTO();
            newAddress.setId(UUID.randomUUID().toString());
            newAddress.setAddressType(AddressType.RESIDENTIAL);
            showAddressDialog(newAddress, -1);
        });

        binding.buttonSave.setOnClickListener(v -> {
            // Obter valores dos campos do formulário
            if (viewModel.getPersonType().getValue() == PersonType.PHYSICAL) {
                viewModel.getName().setValue(binding.editTextName.getText().toString());
                viewModel.getCpf().setValue(binding.editTextCpf.getText().toString());
            } else {
                viewModel.getCompanyName().setValue(binding.editTextCompanyName.getText().toString());
                viewModel.getCnpj().setValue(binding.editTextCnpj.getText().toString());
            }

            viewModel.getPhoneNumber().setValue(binding.editTextPhone.getText().toString());
            viewModel.getEmail().setValue(binding.editTextEmail.getText().toString());

            // Salvar pessoa
            viewModel.savePerson(requireContext());
        });

//        // Botão para atualizar localização
//        binding.buttonUpdateLocation.setOnClickListener(v -> {
//            if (hasLocationPermission()) {
//                viewModel.requestLocation(requireContext());
//                Toast.makeText(requireContext(), "Atualizando localização...", Toast.LENGTH_SHORT).show();
//            } else {
//                requestLocationPermission();
//            }
//        });
    }

    private void observeViewModel() {
        viewModel.getPerson().observe(getViewLifecycleOwner(), person -> {
            if (person != null) {
                // Set person type spinner
                PersonType type = person.getPersonType();
                if (type != null) {
                    binding.spinnerPersonType.setSelection(type.toSpinnerPosition());
                } else {
                    binding.spinnerPersonType.setSelection(PersonType.PHYSICAL.toSpinnerPosition());
                }

                // Set form fields
                binding.editTextName.setText(person.getName() != null ? person.getName() : "");
                binding.editTextCpf.setText(person.getCpf() != null ? person.getCpf() : "");
                binding.editTextCompanyName.setText(person.getCompanyName() != null ? person.getCompanyName() : "");
                binding.editTextCnpj.setText(person.getCnpj() != null ? person.getCnpj() : "");
                binding.editTextPhone.setText(person.getPhoneNumber() != null ? person.getPhoneNumber() : "");
                binding.editTextEmail.setText(person.getEmail() != null ? person.getEmail() : "");
            }
        });

        viewModel.getAddresses().observe(getViewLifecycleOwner(), addresses -> {
            addressAdapter.updateList(addresses);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        viewModel.getSaved().observe(getViewLifecycleOwner(), saved -> {
            if (saved != null && saved) {
                Toast.makeText(requireContext(), "Salvo com sucesso", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Erro")
                        .setMessage(errorMessage)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });

        // Observar mudanças na localização
        viewModel.getLatitude().observe(getViewLifecycleOwner(), latitude -> {
            if (latitude != null) {
//                binding.textViewLocation.setText(String.format("Localização: %.6f, %.6f",
//                        latitude, viewModel.getLongitude().getValue() != null ? viewModel.getLongitude().getValue() : 0.0));
            }
        });

        viewModel.getLongitude().observe(getViewLifecycleOwner(), longitude -> {
            if (longitude != null) {
//                binding.textViewLocation.setText(String.format("Localização: %.6f, %.6f",
//                        viewModel.getLatitude().getValue() != null ? viewModel.getLatitude().getValue() : 0.0, longitude));
            }
        });

        // Observar nome do dispositivo
        viewModel.getDeviceName().observe(getViewLifecycleOwner(), deviceName -> {
            if (deviceName != null && !deviceName.isEmpty()) {
//                binding.textViewDevice.setText("Dispositivo: " + deviceName);
            }
        });
    }

    private void updateFormVisibility(PersonType personType) {
        if (personType == PersonType.PHYSICAL) {
            binding.layoutPhysicalPerson.setVisibility(View.VISIBLE);
            binding.layoutLegalPerson.setVisibility(View.GONE);
        } else {
            binding.layoutPhysicalPerson.setVisibility(View.GONE);
            binding.layoutLegalPerson.setVisibility(View.VISIBLE);
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, obter localização
                viewModel.requestLocation(requireContext());
            } else {
                Toast.makeText(requireContext(), "Permissão de localização negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAddressDialog(AddressDTO address, int position) {
        // Criar binding para o diálogo
        DialogAddressFormBinding dialogBinding = DialogAddressFormBinding.inflate(getLayoutInflater());

        // Configurar spinner de tipo de endereço com os nomes amigáveis
        ArrayAdapter<String> addressTypeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                AddressType.getDisplayNames()
        );
        addressTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerAddressType.setAdapter(addressTypeAdapter);

        // Preencher campos com dados do endereço
        if (address.getAddressType() != null) {
            dialogBinding.spinnerAddressType.setSelection(address.getAddressType().toSpinnerPosition());
        } else {
            dialogBinding.spinnerAddressType.setSelection(AddressType.RESIDENTIAL.toSpinnerPosition());
        }

        dialogBinding.editTextStreet.setText(address.getStreet());
        dialogBinding.editTextNumber.setText(address.getNumber());
        dialogBinding.editTextComplement.setText(address.getComplement());
        dialogBinding.editTextNeighborhood.setText(address.getNeighborhood());

        // Adicionar máscara ao CEP antes de definir o texto
        MaskUtil.addZipCodeMask(dialogBinding.editTextZipCode);

        // Formatar o CEP se necessário
        String zipCode = address.getZipCode();
        if (zipCode != null && !zipCode.isEmpty()) {
            // Remover qualquer formatação existente
            zipCode = zipCode.replaceAll("[^0-9]", "");

            // Aplicar formatação se tiver 8 dígitos
            if (zipCode.length() == 8) {
                zipCode = zipCode.substring(0, 5) + "-" + zipCode.substring(5);
            }

            dialogBinding.editTextZipCode.setText(zipCode);
        }

        dialogBinding.editTextCity.setText(address.getCity());
        dialogBinding.editTextState.setText(address.getState());

        // Adicionar listener para buscar CEP automaticamente
        dialogBinding.editTextZipCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String cep = s.toString();
                // Verificar se o CEP tem o formato correto (8 dígitos + hífen opcional)
                if (cep.replaceAll("[^0-9]", "").length() == 8) {
                    searchCep(cep, dialogBinding);
                }
            }
        });

        // Criar e mostrar o diálogo
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(position >= 0 ? "Editar Endereço" : "Adicionar Endereço")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Salvar", (dialogInterface, i) -> {
                    // Atualizar dados do endereço
                    address.setAddressType(
                            AddressType.fromSpinnerPosition(dialogBinding.spinnerAddressType.getSelectedItemPosition())
                    );
                    address.setStreet(dialogBinding.editTextStreet.getText().toString());
                    address.setNumber(dialogBinding.editTextNumber.getText().toString());
                    address.setComplement(dialogBinding.editTextComplement.getText().toString());
                    address.setNeighborhood(dialogBinding.editTextNeighborhood.getText().toString());
                    address.setZipCode(dialogBinding.editTextZipCode.getText().toString().replaceAll("[^0-9]", ""));
                    address.setCity(dialogBinding.editTextCity.getText().toString());
                    address.setState(dialogBinding.editTextState.getText().toString());

                    // Adicionar ou atualizar endereço
                    if (position >= 0) {
                        viewModel.updateAddress(address, position);
                    } else {
                        viewModel.addAddress();
                        viewModel.updateAddress(address, viewModel.getAddresses().getValue().size() - 1);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();
    }

    private void searchCep(String cep, DialogAddressFormBinding dialogBinding) {

        cepRepository.getCepInfo(cep, new Callback<CepResponse>() {
            @Override
            public void onResponse(Call<CepResponse> call, Response<CepResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    CepResponse cepResponse = response.body();

                    // Verificar se houve erro na busca
                    if (cepResponse.isError()) {
                        Toast.makeText(requireContext(), "CEP não encontrado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Preencher os campos com os dados do CEP
                    dialogBinding.editTextStreet.setText(cepResponse.getStreet());
                    dialogBinding.editTextComplement.setText(cepResponse.getComplement());
                    dialogBinding.editTextNeighborhood.setText(cepResponse.getNeighborhood());
                    dialogBinding.editTextCity.setText(cepResponse.getCity());
                    dialogBinding.editTextState.setText(cepResponse.getState());

                    // Focar no campo de número
                    dialogBinding.editTextNumber.requestFocus();
                } else {
                    Toast.makeText(requireContext(), "Erro ao buscar CEP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CepResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Falha na conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

