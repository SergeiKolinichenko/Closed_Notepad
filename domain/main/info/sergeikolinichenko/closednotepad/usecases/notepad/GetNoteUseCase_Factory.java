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
public final class GetNoteUseCase_Factory implements Factory<GetNoteUseCase> {
  private final Provider<NotesRepository> noteRepositoryProvider;

  public GetNoteUseCase_Factory(Provider<NotesRepository> noteRepositoryProvider) {
    this.noteRepositoryProvider = noteRepositoryProvider;
  }

  @Override
  public GetNoteUseCase get() {
    return newInstance(noteRepositoryProvider.get());
  }

  public static GetNoteUseCase_Factory create(Provider<NotesRepository> noteRepositoryProvider) {
    return new GetNoteUseCase_Factory(noteRepositoryProvider);
  }

  public static GetNoteUseCase newInstance(NotesRepository noteRepository) {
    return new GetNoteUseCase(noteRepository);
  }
}
