package com.example.androidvpn.ui;

import android.app.Application;
import com.example.androidvpn.data.ServerRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<ServerRepository> repositoryProvider;

  private final Provider<Application> applicationProvider;

  public HomeViewModel_Factory(Provider<ServerRepository> repositoryProvider,
      Provider<Application> applicationProvider) {
    this.repositoryProvider = repositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(repositoryProvider.get(), applicationProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<ServerRepository> repositoryProvider,
      Provider<Application> applicationProvider) {
    return new HomeViewModel_Factory(repositoryProvider, applicationProvider);
  }

  public static HomeViewModel newInstance(ServerRepository repository, Application application) {
    return new HomeViewModel(repository, application);
  }
}
