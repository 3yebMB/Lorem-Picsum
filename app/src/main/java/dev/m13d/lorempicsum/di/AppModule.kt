package dev.m13d.lorempicsum.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.m13d.lorempicsum.api.PicsumApi
import dev.m13d.lorempicsum.data.PicsumDatabase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(PicsumApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun providePicsumApi(retrofit: Retrofit): PicsumApi =
        retrofit.create(PicsumApi::class.java)


    @Provides
    @Singleton
    fun provideDatabase(app: Application): PicsumDatabase =
        Room.databaseBuilder(app, PicsumDatabase::class.java, "picsum_photo_database")
            .fallbackToDestructiveMigration()
            .build()
}