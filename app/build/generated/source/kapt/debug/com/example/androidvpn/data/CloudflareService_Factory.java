package com.example.androidvpn.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class CloudflareService_Factory implements Factory<CloudflareService> {
  private final Provider<CloudflareApi> apiProvider;

  public CloudflareService_Factory(Provider<CloudflareApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public CloudflareService get() {
    return newInstance(apiProvider.get());
  }

  public static CloudflareService_Factory create(Provider<CloudflareApi> apiProvider) {
    return new CloudflareService_Factory(apiProvider);
  }

  public static CloudflareService newInstance(CloudflareApi api) {
    return new CloudflareService(api);
  }
}
