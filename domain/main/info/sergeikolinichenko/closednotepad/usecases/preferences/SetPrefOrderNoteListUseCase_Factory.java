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
public final class SetPrefOrderNoteListUseCase_Factory implements Factory<SetPrefOrderNoteListUseCase> {
  private final Provider<PreferencesRepository> prefRepositoryProvider;

  public SetPrefOrderNoteListUseCase_Factory(
      Provider<PreferencesRepository> prefRepositoryProvider) {
    this.prefRepositoryProvider = prefRepositoryProvider;
  }

  @Override
  public SetPrefOrderNoteListUseCase get() {
    return newInstance(prefRepositoryProvider.get());
  }

  public static SetPrefOrderNoteListUseCase_Factory create(
      Provider<PreferencesRepository> prefRepositoryProvider) {
    return new SetPrefOrderNoteListUseCase_Factory(prefRepositoryProvider);
  }

  public static SetPrefOrderNoteListUseCase newInstance(PreferencesRepository prefRepository) {
    return new SetPrefOrderNoteListUseCase(prefRepository);
  }
}
