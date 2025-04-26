package com.draccoapp.basisnordestetest.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.draccoapp.basisnordestetest.R;
import com.draccoapp.basisnordestetest.databinding.FragmentPersonListBinding;
import com.draccoapp.basisnordestetest.model.Person;
import com.draccoapp.basisnordestetest.model.dto.PersonDTO;
import com.draccoapp.basisnordestetest.ui.adapters.PersonAdapter;
import com.draccoapp.basisnordestetest.viewmodel.PersonListViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PersonListFragment extends Fragment {

    private FragmentPersonListBinding binding;
    private PersonListViewModel viewModel;
    private PersonAdapter adapter;
    private String currentQuery = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPersonListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PersonListViewModel.class);
        setupRecyclerView();
        observeViewModel();
        setupListeners();
    }

    private void setupRecyclerView() {
        adapter = new PersonAdapter();
        binding.recyclerViewPersons.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewPersons.setAdapter(adapter);

        adapter.setOnItemClickListener(new PersonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(PersonDTO person, int position) {
                // Navegar para tela de detalhes
                if (person != null && person.getId() != null) {
                    Navigation.findNavController(requireView()).navigate(
                            PersonListFragmentDirections.actionPersonListFragmentToPersonFormFragment(person.getId())
                    );
                }
            }

            @Override
            public void onItemLongClick(PersonDTO person, int position) {
                // Mostrar opções (editar, excluir, etc.)
                showOptionsDialog(person, position);
            }

            @Override
            public void onEditClick(PersonDTO person, int position) {
                // Navegar para tela de edição
                if (person != null && person.getId() != null) {
                    Navigation.findNavController(requireView()).navigate(
                            PersonListFragmentDirections.actionPersonListFragmentToPersonFormFragment(person.getId())
                    );
                }
            }

            @Override
            public void onDeleteClick(PersonDTO person, int position) {
                // Mostrar diálogo de confirmação para excluir
                showDeleteConfirmation(person, position);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getDetachedPersons().observe(getViewLifecycleOwner(), this::updatePersonList);

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePersonList(List<PersonDTO> persons) {
        adapter.updateList(persons);

        // Atualizar estado vazio com mensagem específica para pesquisa
        if (persons == null || persons.isEmpty()) {
            binding.textViewEmpty.setVisibility(View.VISIBLE);
            binding.recyclerViewPersons.setVisibility(View.GONE);

            if (!currentQuery.isEmpty()) {
                binding.textViewEmpty.setText("Nenhum resultado encontrado para \"" + currentQuery + "\"");
            } else {
                binding.textViewEmpty.setText("Nenhuma pessoa cadastrada");
            }
        } else {
            binding.textViewEmpty.setVisibility(View.GONE);
            binding.recyclerViewPersons.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        binding.fabAdd.setOnClickListener(v -> {
            // Navegar para tela de adicionar pessoa
            Navigation.findNavController(requireView()).navigate(
                    PersonListFragmentDirections.actionPersonListFragmentToPersonFormFragment(null)
            );
        });

        // Corrigir o tipo do listener para SearchView da biblioteca androidx
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.isEmpty()) {
                    currentQuery = "";
                    viewModel.loadAllPersons();
                }
                return true;
            }
        });

        binding.buttonSearch.setOnClickListener(v -> {
            String query = binding.searchView.getQuery().toString();
            performSearch(query);
        });
    }

    private void performSearch(String query) {
        Log.d("PersonListFragment", "Performing search: " + query);
        currentQuery = query;

        if (query != null && !query.isEmpty()) {
            viewModel.searchPersons(query);
        } else {
            currentQuery = "";
            viewModel.loadAllPersons();
        }
    }

    private void showOptionsDialog(PersonDTO person, int position) {
        String[] options = {"Editar", "Excluir"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Opções")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Editar
                            if (person != null && person.getId() != null) {
                                Navigation.findNavController(requireView()).navigate(
                                        PersonListFragmentDirections.actionPersonListFragmentToPersonFormFragment(person.getId())
                                );
                            }
                            break;
                        case 1: // Excluir
                            showDeleteConfirmation(person, position);
                            break;
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showDeleteConfirmation(PersonDTO person, int position) {
        String displayName = person.getPersonType() == null || person.getPersonType().name().equals("PHYSICAL")
                ? person.getName()
                : person.getCompanyName();

        if (displayName == null || displayName.isEmpty()) {
            displayName = "esta pessoa";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Excluir Pessoa")
                .setMessage("Tem certeza que deseja excluir " + displayName + "?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    deletePerson(person.getId(), position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletePerson(String personId, int position) {
        viewModel.deletePerson(personId, success -> {
            if (success) {
                adapter.removePerson(position);
                Toast.makeText(requireContext(), "Pessoa excluída com sucesso", Toast.LENGTH_SHORT).show();

                // Verificar se a lista está vazia após a exclusão
                if (adapter.getItemCount() == 0) {
                    updateEmptyState(false);
                }
            }
        });
    }

    private void updateEmptyState(boolean hasData) {
        binding.textViewEmpty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        binding.recyclerViewPersons.setVisibility(hasData ? View.VISIBLE : View.GONE);

        if (!hasData) {
            if (!currentQuery.isEmpty()) {
                binding.textViewEmpty.setText("Nenhum resultado encontrado para \"" + currentQuery + "\"");
            } else {
                binding.textViewEmpty.setText("Nenhuma pessoa cadastrada");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}