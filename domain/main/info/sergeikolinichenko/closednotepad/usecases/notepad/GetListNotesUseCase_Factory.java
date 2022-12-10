package info.sergeikolinichenko.closednotepad.usecases.notepad;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository;
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
public final class GetListNotesUseCase_Factory implements Factory<GetListNotesUseCase> {
  private final Provider<NotesRepository> noteRepositoryProvider;

  public GetListNotesUseCase_Factory(Provider<NotesRepository> noteRepositoryProvider) {
    this.noteRepositoryProvider = noteRepositoryProvider;
  }

  @Override
  public GetListNotesUseCase get() {
    return newInstance(noteRepositoryProvider.get());
  }

  public static GetListNotesUseCase_Factory create(
      Provider<NotesRepository> noteRepositoryProvider) {
    return new GetListNotesUseCase_Factory(noteRepositoryProvider);
  }

  public static GetListNotesUseCase newInstance(NotesRepository noteRepository) {
    return new GetListNotesUseCase(noteRepository);
  }
}
