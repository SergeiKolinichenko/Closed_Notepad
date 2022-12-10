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
public final class SetPrefColorIndexUseCase_Factory implements Factory<SetPrefColorIndexUseCase> {
  private final Provider<PreferencesRepository> prefRepositoryProvider;

  public SetPrefColorIndexUseCase_Factory(Provider<PreferencesRepository> prefRepositoryProvider) {
    this.prefRepositoryProvider = prefRepositoryProvider;
  }

  @Override
  public SetPrefColorIndexUseCase get() {
    return newInstance(prefRepositoryProvider.get());
  }

  public static SetPrefColorIndexUseCase_Factory create(
      Provider<PreferencesRepository> prefRepositoryProvider) {
    return new SetPrefColorIndexUseCase_Factory(prefRepositoryProvider);
  }

  public static SetPrefColorIndexUseCase newInstance(PreferencesRepository prefRepository) {
    return new SetPrefColorIndexUseCase(prefRepository);
  }
}
