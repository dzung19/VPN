package com.example.androidvpn.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class ServerRepository_Factory implements Factory<ServerRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<CloudflareService> cloudflareServiceProvider;

  public ServerRepository_Factory(Provider<Context> contextProvider,
      Provider<CloudflareService> cloudflareServiceProvider) {
    this.contextProvider = contextProvider;
    this.cloudflareServiceProvider = cloudflareServiceProvider;
  }

  @Override
  public ServerRepository get() {
    return newInstance(contextProvider.get(), cloudflareServiceProvider.get());
  }

  public static ServerRepository_Factory create(Provider<Context> contextProvider,
      Provider<CloudflareService> cloudflareServiceProvider) {
    return new ServerRepository_Factory(contextProvider, cloudflareServiceProvider);
  }

  public static ServerRepository newInstance(Context context, CloudflareService cloudflareService) {
    return new ServerRepository(context, cloudflareService);
  }
}
