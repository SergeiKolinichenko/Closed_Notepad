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
public final class GetPrefAutoDelReNoteUseCase_Factory implements Factory<GetPrefAutoDelReNoteUseCase> {
  private final Provider<PreferencesRepository> prefRepositoryProvider;

  public GetPrefAutoDelReNoteUseCase_Factory(
      Provider<PreferencesRepository> prefRepositoryProvider) {
    this.prefRepositoryProvider = prefRepositoryProvider;
  }

  @Override
  public GetPrefAutoDelReNoteUseCase get() {
    return newInstance(prefRepositoryProvider.get());
  }

  public static GetPrefAutoDelReNoteUseCase_Factory create(
      Provider<PreferencesRepository> prefRepositoryProvider) {
    return new GetPrefAutoDelReNoteUseCase_Factory(prefRepositoryProvider);
  }

  public static GetPrefAutoDelReNoteUseCase newInstance(PreferencesRepository prefRepository) {
    return new GetPrefAutoDelReNoteUseCase(prefRepository);
  }
}
