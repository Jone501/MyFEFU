package ru.jone501.myfefu.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.FileStorage
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import ru.jone501.myfefu.data.repository.EncryptedSessionManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val coreModule = module {
    singleOf(::EncryptedSessionManager)
    single<DataStore<Preferences>> {
        createDataStore()
    }
    single<Gson> {
        GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, object :
                TypeAdapter<LocalDateTime>() {
                private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                override fun write(
                    out: JsonWriter?,
                    value: LocalDateTime?
                ) {
                    out?.value(value?.format(formatter))
                }

                override fun read(input: JsonReader?): LocalDateTime? {
                    return LocalDateTime.parse(input?.nextString(), formatter)
                }
            })
            .registerTypeAdapter(LocalDate::class.java, object :
                TypeAdapter<LocalDate>() {
                private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                override fun write(
                    out: JsonWriter?,
                    value: LocalDate?
                ) {
                    out?.value(value?.format(formatter))
                }

                override fun read(input: JsonReader?): LocalDate? {
                    return LocalDate.parse(input?.nextString(), formatter)
                }
            })
            .create()
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