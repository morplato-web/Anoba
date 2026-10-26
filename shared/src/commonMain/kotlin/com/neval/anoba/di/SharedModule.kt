package com.neval.anoba.di

import org.koin.core.module.Module
import org.koin.dsl.module

val sharedModule = module {
    // Shared KMP dependency injection definitions
}

fun appModules(): List<Module> = listOf(sharedModule)
