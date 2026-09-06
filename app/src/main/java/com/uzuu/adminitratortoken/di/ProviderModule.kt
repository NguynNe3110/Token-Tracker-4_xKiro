package com.uzuu.adminitratortoken.di

import com.uzuu.adminitratortoken.data.provider.TokenProvider
import com.uzuu.adminitratortoken.data.provider.xkiro.XkiroTokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
@InstallIn(SingletonComponent::class)
abstract class ProviderModule {

    @Binds
    @IntoMap
    @StringKey("XKIRO")
    abstract fun bindXkiroProvider(provider: XkiroTokenProvider): TokenProvider
}