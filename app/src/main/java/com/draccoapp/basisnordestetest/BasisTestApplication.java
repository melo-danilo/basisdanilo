package com.draccoapp.basisnordestetest;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.draccoapp.basisnordestetest.model.Person;

import java.util.UUID;

import dagger.hilt.android.HiltAndroidApp;
import io.realm.Realm;
import io.realm.RealmConfiguration;

@HiltAndroidApp
public class BasisTestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializar o Realm
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("basistest.realm")
                .schemaVersion(1)
                .allowWritesOnUiThread(true)  // Permite escritas na UI thread
                .deleteRealmIfMigrationNeeded() // Apenas para desenvolvimento
                .build();
        Realm.setDefaultConfiguration(config);

    }

}