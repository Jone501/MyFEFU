package ru.jone501.myfefu.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.FileStorage
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import ru.jone501.myfefu.data.repository.EncryptedSessionManager

val coreModule = module {
    singleOf(::EncryptedSessionManager)
    single<DataStore<Preferences>> {
        createDataStore()
    }
}

fun Scope.createDataStore(): DataStore<Preferences> = createDataStore(
    storage = FileStorage(
        serializer = PreferencesFileSerializer,
        produceFile = { androidContext().filesDir.resolve("myfefu.preferences_pb") }
    )
)

fun createDataStore(storage: Storage<Preferences>): DataStore<Preferences> =
    DataStoreFactory.create(storage = storage)