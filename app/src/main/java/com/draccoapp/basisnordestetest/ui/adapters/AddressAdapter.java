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
import com.draccoapp.basisnordestetest.databinding.ItemAddressBinding;
import com.draccoapp.basisnordestetest.model.Address;
import com.draccoapp.basisnordestetest.model.AddressType;
import com.draccoapp.basisnordestetest.model.dto.AddressDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    private final DiffUtil.ItemCallback<AddressDTO> diffCallback = new DiffUtil.ItemCallback<AddressDTO>() {
        @Override
        public boolean areItemsTheSame(@NonNull AddressDTO oldItem, @NonNull AddressDTO newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull AddressDTO oldItem, @NonNull AddressDTO newItem) {
            // Comparação mais detalhada para determinar se o conteúdo mudou
            return (oldItem.getStreet() == null ? newItem.getStreet() == null : oldItem.getStreet().equals(newItem.getStreet())) &&
                    (oldItem.getNumber() == null ? newItem.getNumber() == null : oldItem.getNumber().equals(newItem.getNumber())) &&
                    (oldItem.getComplement() == null ? newItem.getComplement() == null : oldItem.getComplement().equals(newItem.getComplement())) &&
                    (oldItem.getNeighborhood() == null ? newItem.getNeighborhood() == null : oldItem.getNeighborhood().equals(newItem.getNeighborhood())) &&
                    (oldItem.getZipCode() == null ? newItem.getZipCode() == null : oldItem.getZipCode().equals(newItem.getZipCode())) &&
                    (oldItem.getCity() == null ? newItem.getCity() == null : oldItem.getCity().equals(newItem.getCity())) &&
                    (oldItem.getState() == null ? newItem.getState() == null : oldItem.getState().equals(newItem.getState())) &&
                    (oldItem.getAddressType() == newItem.getAddressType());
        }
    };

    private final AsyncListDiffer<AddressDTO> differ = new AsyncListDiffer<>(this, diffCallback);
    private final List<AddressDTO> addresses = new ArrayList<>();
    private OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onAddressRemove(int position);
        void onAddressEdit(AddressDTO address, int position);
    }

    public void setOnAddressActionListener(OnAddressActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<AddressDTO> newList) {
        addresses.clear();
        if (newList != null) {
            addresses.addAll(newList);
        }
        differ.submitList(new ArrayList<>(addresses));
    }

    public void removeAddress(int position) {
        if (position >= 0 && position < addresses.size()) {
            addresses.remove(position);
            differ.submitList(new ArrayList<>(addresses));
        }
    }

    public void addAddress(AddressDTO address) {
        addresses.add(address);
        differ.submitList(new ArrayList<>(addresses));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAddressBinding binding;

        public ViewHolder(ItemAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AddressDTO address, OnAddressActionListener listener, int position) {
            if (address == null) return;

            // Configurar tipo de endereço
            AddressType addressType = address.getAddressType();
            if (addressType != null) {
                binding.textViewAddressType.setText(addressType.getDisplayName());

                // Definir cor de fundo com base no tipo de endereço
                int bgColor = addressType == AddressType.COMMERCIAL
                        ? R.color.commercial_address_bg
                        : R.color.orange;
                binding.textViewAddressType.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), bgColor)
                );
            } else {
                binding.textViewAddressType.setText(AddressType.RESIDENTIAL.getDisplayName());
                binding.textViewAddressType.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.orange)
                );
            }

            // Configurar rua e número
            String street = address.getStreet() != null ? address.getStreet() : "";
            String number = address.getNumber() != null ? address.getNumber() : "";
            binding.textViewStreetAndNumber.setText(street + (number.isEmpty() ? "" : ", " + number));

            // Configurar complemento
            String complement = address.getComplement();
            if (complement != null && !complement.isEmpty()) {
                binding.textViewComplement.setText(complement);
                binding.textViewComplement.setVisibility(View.VISIBLE);
            } else {
                binding.textViewComplement.setVisibility(View.GONE);
            }

            // Configurar bairro
            binding.textViewNeighborhood.setText(address.getNeighborhood() != null ? address.getNeighborhood() : "");

            // Configurar cidade e estado
            String city = address.getCity() != null ? address.getCity() : "";
            String state = address.getState() != null ? address.getState() : "";
            String cityState = "";
            if (!city.isEmpty()) {
                cityState = city;
                if (!state.isEmpty()) {
                    cityState += "/" + state;
                }
            } else if (!state.isEmpty()) {
                cityState = state;
            }
            binding.textViewCityState.setText(cityState);

            // Configurar CEP
            String zipCode = address.getZipCode();
            if (zipCode != null && !zipCode.isEmpty()) {
                if (zipCode.length() == 8) {
                    zipCode = zipCode.substring(0, 5) + "-" + zipCode.substring(5);
                }
                binding.textViewZipCode.setText("CEP: " + zipCode);
                binding.textViewZipCode.setVisibility(View.VISIBLE);
            } else {
                binding.textViewZipCode.setVisibility(View.GONE);
            }

            // Configurar listeners
            if (listener != null) {
                binding.buttonRemoveAddress.setOnClickListener(v ->
                        listener.onAddressRemove(position)
                );

                itemView.setOnClickListener(v ->
                        listener.onAddressEdit(address, position)
                );
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAddressBinding binding = ItemAddressBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(differ.getCurrentList().get(position), listener, position);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    // Método para obter endereço em uma posição específica
    public AddressDTO getAddress(int position) {
        if (position >= 0 && position < differ.getCurrentList().size()) {
            return differ.getCurrentList().get(position);
        }
        return null;
    }

    // Método para obter a lista atual de endereços
    public List<AddressDTO> getCurrentAddresses() {
        return new ArrayList<>(differ.getCurrentList());
    }
}