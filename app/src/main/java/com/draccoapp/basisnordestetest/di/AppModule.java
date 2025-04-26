package com.draccoapp.basisnordestetest.di;

import android.content.Context;

import com.draccoapp.basisnordestetest.api.CepService;
import com.draccoapp.basisnordestetest.repository.PersonRepository;
import com.draccoapp.basisnordestetest.service.FirestoreService;
import com.draccoapp.basisnordestetest.service.LocationService;
import com.draccoapp.basisnordestetest.util.LocationUtil;
import com.draccoapp.basisnordestetest.util.NotificationUtil;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import io.realm.Realm;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    private static final String CEP_BASE_URL = "https://viacep.com.br/ws/";

    @Provides
    @Singleton
    public Realm provideRealm() {
        return Realm.getDefaultInstance();
    }

    @Provides
    @Singleton
    public LocationService provideLocationService(@ApplicationContext Context context) {
        return new LocationService(context);
    }

    @Provides
    @Singleton
    public FirestoreService provideFirestoreService(@ApplicationContext Context context) {
        return new FirestoreService(context);
    }

    @Provides
    @Singleton
    public PersonRepository providePersonRepository(Realm realm, FirestoreService firestoreService) {
        return new PersonRepository(realm, firestoreService);
    }

    @Provides
    @Singleton
    public LocationUtil provideLocationUtil(@ApplicationContext Context context) {
        return new LocationUtil(context);
    }

    @Provides
    @Singleton
    public NotificationUtil provideNotificationUtil() {
        return new NotificationUtil();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(CEP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public CepService provideCepService(Retrofit retrofit) {
        return retrofit.create(CepService.class);
    }

}
