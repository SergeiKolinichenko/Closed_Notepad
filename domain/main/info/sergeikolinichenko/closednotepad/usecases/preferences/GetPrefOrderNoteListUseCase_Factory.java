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
public final class GetPrefOrderNoteListUseCase_Factory implements Factory<GetPrefOrderNoteListUseCase> {
  private final Provider<PreferencesRepository> prefRepositoryProvider;

  public GetPrefOrderNoteListUseCase_Factory(
      Provider<PreferencesRepository> prefRepositoryProvider) {
    this.prefRepositoryProvider = prefRepositoryProvider;
  }

  @Override
  public GetPrefOrderNoteListUseCase get() {
    return newInstance(prefRepositoryProvider.get());
  }

  public static GetPrefOrderNoteListUseCase_Factory create(
      Provider<PreferencesRepository> prefRepositoryProvider) {
    return new GetPrefOrderNoteListUseCase_Factory(prefRepositoryProvider);
  }

  public static GetPrefOrderNoteListUseCase newInstance(PreferencesRepository prefRepository) {
    return new GetPrefOrderNoteListUseCase(prefRepository);
  }
}
