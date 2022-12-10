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
public final class RecoveryRemovedNoteUseCase_Factory implements Factory<RecoveryRemovedNoteUseCase> {
  private final Provider<RemovedNoteRepository> removedNoteRepositoryProvider;

  public RecoveryRemovedNoteUseCase_Factory(
      Provider<RemovedNoteRepository> removedNoteRepositoryProvider) {
    this.removedNoteRepositoryProvider = removedNoteRepositoryProvider;
  }

  @Override
  public RecoveryRemovedNoteUseCase get() {
    return newInstance(removedNoteRepositoryProvider.get());
  }

  public static RecoveryRemovedNoteUseCase_Factory create(
      Provider<RemovedNoteRepository> removedNoteRepositoryProvider) {
    return new RecoveryRemovedNoteUseCase_Factory(removedNoteRepositoryProvider);
  }

  public static RecoveryRemovedNoteUseCase newInstance(
      RemovedNoteRepository removedNoteRepository) {
    return new RecoveryRemovedNoteUseCase(removedNoteRepository);
  }
}
