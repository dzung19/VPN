package com.example.androidvpn.di;

import com.example.androidvpn.data.CloudflareApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class NetworkModule_ProvideCloudflareApiFactory implements Factory<CloudflareApi> {
  private final NetworkModule module;

  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideCloudflareApiFactory(NetworkModule module,
      Provider<Retrofit> retrofitProvider) {
    this.module = module;
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public CloudflareApi get() {
    return provideCloudflareApi(module, retrofitProvider.get());
  }

  public static NetworkModule_ProvideCloudflareApiFactory create(NetworkModule module,
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideCloudflareApiFactory(module, retrofitProvider);
  }

  public static CloudflareApi provideCloudflareApi(NetworkModule instance, Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(instance.provideCloudflareApi(retrofit));
  }
}
