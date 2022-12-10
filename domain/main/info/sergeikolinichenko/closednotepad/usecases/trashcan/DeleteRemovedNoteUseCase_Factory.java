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
public final class DeleteRemovedNoteUseCase_Factory implements Factory<DeleteRemovedNoteUseCase> {
  private final Provider<RemovedNoteRepository> removedNoteRepositoryProvider;

  public DeleteRemovedNoteUseCase_Factory(
      Provider<RemovedNoteRepository> removedNoteRepositoryProvider) {
    this.removedNoteRepositoryProvider = removedNoteRepositoryProvider;
  }

  @Override
  public DeleteRemovedNoteUseCase get() {
    return newInstance(removedNoteRepositoryProvider.get());
  }

  public static DeleteRemovedNoteUseCase_Factory create(
      Provider<RemovedNoteRepository> removedNoteRepositoryProvider) {
    return new DeleteRemovedNoteUseCase_Factory(removedNoteRepositoryProvider);
  }

  public static DeleteRemovedNoteUseCase newInstance(RemovedNoteRepository removedNoteRepository) {
    return new DeleteRemovedNoteUseCase(removedNoteRepository);
  }
}
