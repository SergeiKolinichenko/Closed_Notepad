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
public final class GetListRemovedNoteUseCase_Factory implements Factory<GetListRemovedNoteUseCase> {
  private final Provider<RemovedNoteRepository> removedNoteRepositoryProvider;

  public GetListRemovedNoteUseCase_Factory(
      Provider<RemovedNoteRepository> removedNoteRepositoryProvider) {
    this.removedNoteRepositoryProvider = removedNoteRepositoryProvider;
  }

  @Override
  public GetListRemovedNoteUseCase get() {
    return newInstance(removedNoteRepositoryProvider.get());
  }

  public static GetListRemovedNoteUseCase_Factory create(
      Provider<RemovedNoteRepository> removedNoteRepositoryProvider) {
    return new GetListRemovedNoteUseCase_Factory(removedNoteRepositoryProvider);
  }

  public static GetListRemovedNoteUseCase newInstance(RemovedNoteRepository removedNoteRepository) {
    return new GetListRemovedNoteUseCase(removedNoteRepository);
  }
}
