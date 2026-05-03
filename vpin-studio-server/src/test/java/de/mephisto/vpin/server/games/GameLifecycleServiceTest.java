package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameLifecycleServiceTest {

  private GameLifecycleService service;
  private GameLifecycleListener lifecycleListener;
  private GameDataChangedListener dataChangedListener;

  @BeforeEach
  void setUp() {
    service = new GameLifecycleService();
    lifecycleListener = mock(GameLifecycleListener.class);
    dataChangedListener = mock(GameDataChangedListener.class);
  }

  // ---- lifecycle listener registration and dispatch ----

  @Test
  void notifyGameCreated_callsAllRegisteredListeners() {
    GameLifecycleListener second = mock(GameLifecycleListener.class);
    service.addGameLifecycleListener(lifecycleListener);
    service.addGameLifecycleListener(second);

    service.notifyGameCreated(42);

    verify(lifecycleListener).gameCreated(42);
    verify(second).gameCreated(42);
  }

  @Test
  void notifyGameUpdated_callsAllRegisteredListeners() {
    service.addGameLifecycleListener(lifecycleListener);

    service.notifyGameUpdated(7);

    verify(lifecycleListener).gameUpdated(7);
  }

  @Test
  void notifyGameDeleted_callsAllRegisteredListeners() {
    service.addGameLifecycleListener(lifecycleListener);

    service.notifyGameDeleted(3);

    verify(lifecycleListener).gameDeleted(3);
  }

  @Test
  void notifyGameCreated_noListeners_doesNotThrow() {
    service.notifyGameCreated(1);
  }

  // ---- data-changed listener ----

  @Test
  void notifyGameDataChanged_callsListener_whenDataDiffers() {
    service.addGameDataChangedListener(dataChangedListener);

    TableDetails oldData = new TableDetails();
    oldData.setGameDisplayName("Old Name");
    TableDetails newData = new TableDetails();
    newData.setGameDisplayName("New Name");

    service.notifyGameDataChanged(10, oldData, newData);

    verify(dataChangedListener).gameDataChanged(any(GameDataChangedEvent.class));
  }

  @Test
  void notifyGameDataChanged_skips_whenDataIsEqual() {
    service.addGameDataChangedListener(dataChangedListener);

    TableDetails data = new TableDetails();
    service.notifyGameDataChanged(10, data, data);

    verifyNoInteractions(dataChangedListener);
  }

  @Test
  void notifyGameAssetsChanged_withGameId_callsListener() {
    service.addGameDataChangedListener(dataChangedListener);

    service.notifyGameAssetsChanged(5, AssetType.DIRECTB2S, null);

    verify(dataChangedListener).gameAssetChanged(any(GameAssetChangedEvent.class));
  }

  @Test
  void notifyGameAssetsChanged_withoutGameId_callsListener() {
    service.addGameDataChangedListener(dataChangedListener);

    service.notifyGameAssetsChanged(AssetType.DIRECTB2S, new Object());

    verify(dataChangedListener).gameAssetChanged(any(GameAssetChangedEvent.class));
  }

  @Test
  void notifyGameScreenAssetsChanged_callsListener() {
    service.addGameDataChangedListener(dataChangedListener);

    service.notifyGameScreenAssetsChanged(8, VPinScreen.Wheel, null);

    verify(dataChangedListener).gameScreenAssetChanged(any(GameScreenAssetChangedEvent.class));
  }

  @Test
  void multipleDataListeners_allNotified() {
    GameDataChangedListener second = mock(GameDataChangedListener.class);
    service.addGameDataChangedListener(dataChangedListener);
    service.addGameDataChangedListener(second);

    service.notifyGameAssetsChanged(1, AssetType.DIRECTB2S, null);

    verify(dataChangedListener).gameAssetChanged(any());
    verify(second).gameAssetChanged(any());
  }
}
