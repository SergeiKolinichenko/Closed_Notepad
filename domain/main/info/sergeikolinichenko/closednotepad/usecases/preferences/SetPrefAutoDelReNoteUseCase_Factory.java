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
public final class SetPrefAutoDelReNoteUseCase_Factory implements Factory<SetPrefAutoDelReNoteUseCase> {
  private final Provider<PreferencesRepository> prefRepositoryProvider;

  public SetPrefAutoDelReNoteUseCase_Factory(
      Provider<PreferencesRepository> prefRepositoryProvider) {
    this.prefRepositoryProvider = prefRepositoryProvider;
  }

  @Override
  public SetPrefAutoDelReNoteUseCase get() {
    return newInstance(prefRepositoryProvider.get());
  }

  public static SetPrefAutoDelReNoteUseCase_Factory create(
      Provider<PreferencesRepository> prefRepositoryProvider) {
    return new SetPrefAutoDelReNoteUseCase_Factory(prefRepositoryProvider);
  }

  public static SetPrefAutoDelReNoteUseCase newInstance(PreferencesRepository prefRepository) {
    return new SetPrefAutoDelReNoteUseCase(prefRepository);
  }
}
