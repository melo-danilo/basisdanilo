package com.draccoapp.basisnordestetest.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.draccoapp.basisnordestetest.model.dto.PersonDTO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirestoreService {
    private static final String TAG = "FirestoreService";
    private static final String COLLECTION_PERSONS = "persons";
    private static final String COLLECTION_TOKENS = "tokens";

    private final FirebaseFirestore db;
    private String currentUserToken;
    private final Context applicationContext;

    public interface FirestoreCallback {
        void onSuccess(String documentId);
        void onFailure(Exception e);
    }

    @Inject
    public FirestoreService(Context applicationContext) {
        this.applicationContext = applicationContext;
        db = FirebaseFirestore.getInstance();

        // Obter o token FCM atual
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Obter o novo token FCM
                        currentUserToken = task.getResult();
                        Log.d(TAG, "FCM Token: " + currentUserToken);

                        // Salvar o token no Firestore
                        saveTokenToFirestore(currentUserToken);
                    }
                });
    }

    private void saveTokenToFirestore(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", token);
        tokenData.put("createdAt", System.currentTimeMillis());
        tokenData.put("platform", "android");

        db.collection(COLLECTION_TOKENS)
                .document(token)
                .set(tokenData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Token saved to Firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error saving token to Firestore", e);
                    }
                });
    }

    public void savePerson(PersonDTO person, FirestoreCallback callback) {
        Map<String, Object> personData = new HashMap<>();
        personData.put("id", person.getId());
        personData.put("personType", person.getPersonType().name());
        personData.put("name", person.getName());
        personData.put("cpf", person.getCpf());
        personData.put("companyName", person.getCompanyName());
        personData.put("cnpj", person.getCnpj());
        personData.put("phoneNumber", person.getPhoneNumber());
        personData.put("email", person.getEmail());
        personData.put("createdAt", person.getCreatedAt());
        personData.put("latitude", person.getLatitude());
        personData.put("longitude", person.getLongitude());
        personData.put("deviceName", person.getDeviceName());
        personData.put("createdByToken", currentUserToken);

        // Adicionar endereços como subcoleção ou array
        if (person.getAddresses() != null && !person.getAddresses().isEmpty()) {
            personData.put("hasAddresses", true);
            personData.put("addressCount", person.getAddresses().size());
        } else {
            personData.put("hasAddresses", false);
            personData.put("addressCount", 0);
        }

        db.collection(COLLECTION_PERSONS)
                .document(person.getId())
                .set(personData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Person saved to Firestore with ID: " + person.getId());

                        // Se houver endereços, salvá-los como subcoleção
                        if (person.getAddresses() != null && !person.getAddresses().isEmpty()) {
                            saveAddresses(person, callback);
                        } else {
                            callback.onSuccess(person.getId());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error saving person to Firestore", e);
                        callback.onFailure(e);
                    }
                });
    }

    private void saveAddresses(PersonDTO person, FirestoreCallback callback) {
        final int[] addressesSaved = {0};
        final int totalAddresses = person.getAddresses().size();

        for (int i = 0; i < person.getAddresses().size(); i++) {
            Map<String, Object> addressData = new HashMap<>();
            addressData.put("id", person.getAddresses().get(i).getId());
            addressData.put("addressType", person.getAddresses().get(i).getAddressType().name());
            addressData.put("street", person.getAddresses().get(i).getStreet());
            addressData.put("number", person.getAddresses().get(i).getNumber());
            addressData.put("complement", person.getAddresses().get(i).getComplement());
            addressData.put("neighborhood", person.getAddresses().get(i).getNeighborhood());
            addressData.put("zipCode", person.getAddresses().get(i).getZipCode());
            addressData.put("city", person.getAddresses().get(i).getCity());
            addressData.put("state", person.getAddresses().get(i).getState());

            db.collection(COLLECTION_PERSONS)
                    .document(person.getId())
                    .collection("addresses")
                    .document(person.getAddresses().get(i).getId())
                    .set(addressData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            addressesSaved[0]++;
                            if (addressesSaved[0] == totalAddresses) {
                                callback.onSuccess(person.getId());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error saving address to Firestore", e);
                            callback.onFailure(e);
                        }
                    });
        }
    }

    public void deletePerson(String personId, FirestoreCallback callback) {
        db.collection(COLLECTION_PERSONS)
                .document(personId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Person deleted from Firestore with ID: " + personId);
                        callback.onSuccess(personId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting person from Firestore", e);
                        callback.onFailure(e);
                    }
                });
    }

    public void sendNotification(String personId, String title, String message) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("personId", personId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("read", false);

        // Salvar a notificação no Firestore
        db.collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Notification saved to Firestore with ID: " + documentReference.getId());

                        // Enviar notificação local
                        sendLocalNotification(title, message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error saving notification to Firestore", e);
                    }
                });
    }

    // Adicionar método para enviar notificações locais
    private void sendLocalNotification(String title, String message) {
        if (applicationContext != null) {
            Intent intent = new Intent("com.draccoapp.basisnordestetest.NOTIFICATION");
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            intent.putExtra("timestamp", System.currentTimeMillis());
            applicationContext.sendBroadcast(intent);
            Log.d(TAG, "Local notification broadcast sent: " + title);
        } else {
            Log.e(TAG, "Cannot send local notification: application context is null");
        }
    }
}