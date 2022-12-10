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
public final class GetRemovedNoteUseCase_Factory implements Factory<GetRemovedNoteUseCase> {
  private final Provider<RemovedNoteRepository> removedNoteRepositoryProvider;

  public GetRemovedNoteUseCase_Factory(
      Provider<RemovedNoteRepository> removedNoteRepositoryProvider) {
    this.removedNoteRepositoryProvider = removedNoteRepositoryProvider;
  }

  @Override
  public GetRemovedNoteUseCase get() {
    return newInstance(removedNoteRepositoryProvider.get());
  }

  public static GetRemovedNoteUseCase_Factory create(
      Provider<RemovedNoteRepository> removedNoteRepositoryProvider) {
    return new GetRemovedNoteUseCase_Factory(removedNoteRepositoryProvider);
  }

  public static GetRemovedNoteUseCase newInstance(RemovedNoteRepository removedNoteRepository) {
    return new GetRemovedNoteUseCase(removedNoteRepository);
  }
}
