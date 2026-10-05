package ru.jone501.myfefu.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.jone501.myfefu.data.viewmodel.LessonsViewModel
import ru.jone501.myfefu.data.viewmodel.ProfileInfoViewModel

val viewModelModule = module {
    singleOf(::ProfileInfoViewModel)
    singleOf(::LessonsViewModel)
}