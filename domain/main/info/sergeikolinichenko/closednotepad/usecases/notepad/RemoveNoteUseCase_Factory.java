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
public final class RemoveNoteUseCase_Factory implements Factory<RemoveNoteUseCase> {
  private final Provider<NotesRepository> noteRepositoryProvider;

  public RemoveNoteUseCase_Factory(Provider<NotesRepository> noteRepositoryProvider) {
    this.noteRepositoryProvider = noteRepositoryProvider;
  }

  @Override
  public RemoveNoteUseCase get() {
    return newInstance(noteRepositoryProvider.get());
  }

  public static RemoveNoteUseCase_Factory create(Provider<NotesRepository> noteRepositoryProvider) {
    return new RemoveNoteUseCase_Factory(noteRepositoryProvider);
  }

  public static RemoveNoteUseCase newInstance(NotesRepository noteRepository) {
    return new RemoveNoteUseCase(noteRepository);
  }
}
