package com.draccoapp.basisnordestetest.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.draccoapp.basisnordestetest.R;
import com.draccoapp.basisnordestetest.databinding.ItemPersonBinding;
import com.draccoapp.basisnordestetest.model.AddressType;
import com.draccoapp.basisnordestetest.model.Person;
import com.draccoapp.basisnordestetest.model.PersonType;
import com.draccoapp.basisnordestetest.model.dto.AddressDTO;
import com.draccoapp.basisnordestetest.model.dto.PersonDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.ViewHolder> {

    private final DiffUtil.ItemCallback<PersonDTO> diffCallback = new DiffUtil.ItemCallback<PersonDTO>() {
        @Override
        public boolean areItemsTheSame(@NonNull PersonDTO oldItem, @NonNull PersonDTO newItem) {
            return oldItem.getId() != null && newItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PersonDTO oldItem, @NonNull PersonDTO newItem) {
            // Comparação mais detalhada para determinar se o conteúdo mudou
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getEmail().equals(newItem.getEmail()) &&
                    oldItem.getPhoneNumber().equals(newItem.getPhoneNumber()) &&
                    oldItem.getPersonType().equals(newItem.getPersonType()) &&
                    ((oldItem.getCpf() == null && newItem.getCpf() == null) ||
                            (oldItem.getCpf() != null && oldItem.getCpf().equals(newItem.getCpf()))) &&
                    ((oldItem.getCnpj() == null && newItem.getCnpj() == null) ||
                            (oldItem.getCnpj() != null && oldItem.getCnpj().equals(newItem.getCnpj()))) &&
                    ((oldItem.getCompanyName() == null && newItem.getCompanyName() == null) ||
                            (oldItem.getCompanyName() != null && oldItem.getCompanyName().equals(newItem.getCompanyName())));
        }
    };

    private final AsyncListDiffer<PersonDTO> differ = new AsyncListDiffer<>(this, diffCallback);
    private final List<PersonDTO> persons = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(PersonDTO person, int position);
        void onItemLongClick(PersonDTO person, int position);
        void onEditClick(PersonDTO person, int position);
        void onDeleteClick(PersonDTO person, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<PersonDTO> newList) {
        persons.clear();
        if (newList != null) {
            persons.addAll(newList);
        }
        differ.submitList(new ArrayList<>(persons)); // Enviar uma cópia imutável para o differ
    }

    public void removePerson(int position) {
        if (position >= 0 && position < persons.size()) {
            persons.remove(position);
            differ.submitList(new ArrayList<>(persons));
        }
    }

    public void addPerson(PersonDTO person) {
        persons.add(person);
        differ.submitList(new ArrayList<>(persons));
    }

    public PersonDTO getPersonAt(int position) {
        if (position >= 0 && position < differ.getCurrentList().size()) {
            return differ.getCurrentList().get(position);
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPersonBinding binding;

        public ViewHolder(ItemPersonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PersonDTO person, OnItemClickListener listener) {
            if (person == null) return;

            // Configurar tipo de pessoa
            PersonType personType = person.getPersonType();
            if (personType != null) {
                binding.textViewPersonType.setText(personType.getDisplayName());

                // Definir cor de fundo com base no tipo de pessoa
                int bgColor = personType == PersonType.LEGAL
                        ? R.color.legal_person_bg
                        : R.color.person_type_bg;
                binding.textViewPersonType.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), bgColor)
                );
            } else {
                binding.textViewPersonType.setText(PersonType.PHYSICAL.getDisplayName());
                binding.textViewPersonType.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.person_type_bg)
                );
            }

            // Definir nome principal com base no tipo de pessoa
            if (personType == PersonType.PHYSICAL) {
                binding.textViewName.setText(person.getName() != null ? person.getName() : "");
                binding.textViewCompanyName.setVisibility(View.GONE);
                binding.textViewDocument.setText("CPF: " + (person.getCpf() != null ? formatCpf(person.getCpf()) : ""));
            } else {
                binding.textViewName.setText(person.getCompanyName() != null ? person.getCompanyName() : "");
                if (person.getName() != null && !person.getName().isEmpty()) {
                    binding.textViewCompanyName.setText("Contato: " + person.getName());
                    binding.textViewCompanyName.setVisibility(View.VISIBLE);
                } else {
                    binding.textViewCompanyName.setVisibility(View.GONE);
                }
                binding.textViewDocument.setText("CNPJ: " + (person.getCnpj() != null ? formatCnpj(person.getCnpj()) : ""));
            }

            // Configurar telefone e email
            binding.textViewPhone.setText("Tel: " + (person.getPhoneNumber() != null ? formatPhone(person.getPhoneNumber()) : ""));
            binding.textViewEmail.setText("Email: " + (person.getEmail() != null ? person.getEmail() : ""));

            // Configurar endereços
            StringBuilder addressesText = new StringBuilder();
            if (person.getAddresses() != null && !person.getAddresses().isEmpty()) {
                for (int i = 0; i < person.getAddresses().size(); i++) {
                    AddressDTO address = person.getAddresses().get(i);
                    if (i > 0) {
                        addressesText.append("\n\n");
                    }

                    // Adicionar tipo de endereço
                    AddressType addressType = address.getAddressType();
                    if (addressType != null) {
                        addressesText.append(addressType.getDisplayName()).append(": ");
                    }

                    // Adicionar rua e número
                    String street = address.getStreet();
                    String number = address.getNumber();
                    if (street != null && !street.isEmpty()) {
                        addressesText.append(street);
                        if (number != null && !number.isEmpty()) {
                            addressesText.append(", ").append(number);
                        }
                    }

                    // Adicionar bairro
                    String neighborhood = address.getNeighborhood();
                    if (neighborhood != null && !neighborhood.isEmpty()) {
                        addressesText.append(" - ").append(neighborhood);
                    }

                    // Adicionar cidade e estado
                    String city = address.getCity();
                    String state = address.getState();
                    if ((city != null && !city.isEmpty()) || (state != null && !state.isEmpty())) {
                        addressesText.append(", ");
                        if (city != null && !city.isEmpty()) {
                            addressesText.append(city);
                            if (state != null && !state.isEmpty()) {
                                addressesText.append("/").append(state);
                            }
                        } else if (state != null && !state.isEmpty()) {
                            addressesText.append(state);
                        }
                    }
                }
            } else {
                addressesText.append("Nenhum endereço cadastrado");
            }
            binding.textViewAddresses.setText(addressesText.toString());

            // Configurar listeners de clique
            if (listener != null) {
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(person, position);
                    }
                });

                itemView.setOnLongClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemLongClick(person, position);
                        return true;
                    }
                    return false;
                });

                binding.buttonEdit.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEditClick(person, position);
                    }
                });

                binding.buttonDelete.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(person, position);
                    }
                });
            }
        }

        private String formatCpf(String cpf) {
            if (cpf == null || cpf.length() != 11) return cpf;
            return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." +
                    cpf.substring(6, 9) + "-" + cpf.substring(9);
        }

        private String formatCnpj(String cnpj) {
            if (cnpj == null || cnpj.length() != 14) return cnpj;
            return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." +
                    cnpj.substring(5, 8) + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12);
        }

        private String formatPhone(String phone) {
            if (phone == null) return "";
            // Remove caracteres não numéricos
            String numbers = phone.replaceAll("[^0-9]", "");

            if (numbers.length() == 11) {
                // Formato (XX) XXXXX-XXXX
                return "(" + numbers.substring(0, 2) + ") " +
                        numbers.substring(2, 7) + "-" +
                        numbers.substring(7);
            } else if (numbers.length() == 10) {
                // Formato (XX) XXXX-XXXX
                return "(" + numbers.substring(0, 2) + ") " +
                        numbers.substring(2, 6) + "-" +
                        numbers.substring(6);
            }
            return phone; // Retorna o original se não conseguir formatar
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPersonBinding binding = ItemPersonBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(differ.getCurrentList().get(position), listener);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }
}
