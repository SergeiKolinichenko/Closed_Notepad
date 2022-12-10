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
public final class AddRemovedNoteUseCase_Factory implements Factory<AddRemovedNoteUseCase> {
  private final Provider<RemovedNoteRepository> removedNoteRepositoryProvider;

  public AddRemovedNoteUseCase_Factory(
      Provider<RemovedNoteRepository> removedNoteRepositoryProvider) {
    this.removedNoteRepositoryProvider = removedNoteRepositoryProvider;
  }

  @Override
  public AddRemovedNoteUseCase get() {
    return newInstance(removedNoteRepositoryProvider.get());
  }

  public static AddRemovedNoteUseCase_Factory create(
      Provider<RemovedNoteRepository> removedNoteRepositoryProvider) {
    return new AddRemovedNoteUseCase_Factory(removedNoteRepositoryProvider);
  }

  public static AddRemovedNoteUseCase newInstance(RemovedNoteRepository removedNoteRepository) {
    return new AddRemovedNoteUseCase(removedNoteRepository);
  }
}
