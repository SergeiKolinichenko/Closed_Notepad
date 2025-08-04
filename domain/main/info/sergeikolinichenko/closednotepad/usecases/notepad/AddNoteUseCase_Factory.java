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
public final class AddNoteUseCase_Factory implements Factory<AddNoteUseCase> {
  private final Provider<NotesRepository> noteRepositoryProvider;

  public AddNoteUseCase_Factory(Provider<NotesRepository> noteRepositoryProvider) {
    this.noteRepositoryProvider = noteRepositoryProvider;
  }

  @Override
  public AddNoteUseCase get() {
    return newInstance(noteRepositoryProvider.get());
  }

  public static AddNoteUseCase_Factory create(Provider<NotesRepository> noteRepositoryProvider) {
    return new AddNoteUseCase_Factory(noteRepositoryProvider);
  }

  public static AddNoteUseCase newInstance(NotesRepository noteRepository) {
    return new AddNoteUseCase(noteRepository);
  }
}
