package info.sergeikolinichenko.closednotepad.usecases.preferences;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository;
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
    "rawtypes"
})
public final class GetPrefColorIndexUseCase_Factory implements Factory<GetPrefColorIndexUseCase> {
  private final Provider<PreferencesRepository> prefRepositoryProvider;

  public GetPrefColorIndexUseCase_Factory(Provider<PreferencesRepository> prefRepositoryProvider) {
    this.prefRepositoryProvider = prefRepositoryProvider;
  }

  @Override
  public GetPrefColorIndexUseCase get() {
    return newInstance(prefRepositoryProvider.get());
  }

  public static GetPrefColorIndexUseCase_Factory create(
      Provider<PreferencesRepository> prefRepositoryProvider) {
    return new GetPrefColorIndexUseCase_Factory(prefRepositoryProvider);
  }

  public static GetPrefColorIndexUseCase newInstance(PreferencesRepository prefRepository) {
    return new GetPrefColorIndexUseCase(prefRepository);
  }
}
