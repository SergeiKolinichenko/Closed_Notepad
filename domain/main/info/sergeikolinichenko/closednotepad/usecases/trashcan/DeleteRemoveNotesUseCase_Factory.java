package info.sergeikolinichenko.closednotepad.usecases.trashcan;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepository;
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
public final class DeleteRemoveNotesUseCase_Factory implements Factory<DeleteRemoveNotesUseCase> {
  private final Provider<RemovedNoteRepository> removedNoteRepositoryProvider;

  public DeleteRemoveNotesUseCase_Factory(
      Provider<RemovedNoteRepository> removedNoteRepositoryProvider) {
    this.removedNoteRepositoryProvider = removedNoteRepositoryProvider;
  }

  @Override
  public DeleteRemoveNotesUseCase get() {
    return newInstance(removedNoteRepositoryProvider.get());
  }

  public static DeleteRemoveNotesUseCase_Factory create(
      Provider<RemovedNoteRepository> removedNoteRepositoryProvider) {
    return new DeleteRemoveNotesUseCase_Factory(removedNoteRepositoryProvider);
  }

  public static DeleteRemoveNotesUseCase newInstance(RemovedNoteRepository removedNoteRepository) {
    return new DeleteRemoveNotesUseCase(removedNoteRepository);
  }
}
