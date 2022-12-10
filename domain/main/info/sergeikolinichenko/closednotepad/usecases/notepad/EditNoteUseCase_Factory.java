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
public final class EditNoteUseCase_Factory implements Factory<EditNoteUseCase> {
  private final Provider<NotesRepository> noteRepositoryProvider;

  public EditNoteUseCase_Factory(Provider<NotesRepository> noteRepositoryProvider) {
    this.noteRepositoryProvider = noteRepositoryProvider;
  }

  @Override
  public EditNoteUseCase get() {
    return newInstance(noteRepositoryProvider.get());
  }

  public static EditNoteUseCase_Factory create(Provider<NotesRepository> noteRepositoryProvider) {
    return new EditNoteUseCase_Factory(noteRepositoryProvider);
  }

  public static EditNoteUseCase newInstance(NotesRepository noteRepository) {
    return new EditNoteUseCase(noteRepository);
  }
}
