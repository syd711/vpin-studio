package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateMergerTest {

  private final TemplateMerger merger = new TemplateMerger();

  // ---- helper ----

  /** A card with no parentId is a template (parentId==null means isTemplate()==true). */
  private CardTemplate template() {
    CardTemplate c = new CardTemplate();
    // parentId remains null => isTemplate() == true
    return c;
  }

  /** A card with a parentId set is NOT a template (isTemplate()==false). */
  private CardTemplate childCard() {
    CardTemplate c = new CardTemplate();
    c.setParentId(1L);
    return c;
  }

  // ---- _merge — card is a template ----

  @Test
  void merge_returnsCardUnchanged_whenCardIsTemplate() {
    CardTemplate parent = template();
    parent.setLockBackground(true);
    parent.setRenderBackground(true);

    CardTemplate card = template();
    card.setRenderBackground(false);

    CardTemplate result = merger._merge(card, parent);

    assertSame(card, result);
    assertFalse(result.isRenderBackground());
  }

  // ---- _merge — parent lock overrides child ----

  @Test
  void merge_overridesChildBackground_whenParentLockBackgroundTrue() {
    CardTemplate parent = childCard();
    parent.setLockBackground(true);
    parent.setRenderBackground(true);

    CardTemplate card = childCard();
    card.setRenderBackground(false);

    CardTemplate result = merger._merge(card, parent);

    assertSame(card, result);
    assertTrue(result.isRenderBackground());
    assertTrue(result.isLockBackground());
  }

  // ---- _merge — unlocked parent leaves child unchanged ----

  @Test
  void merge_doesNotOverrideChild_whenParentLockBackgroundFalse() {
    CardTemplate parent = childCard();
    parent.setLockBackground(false);
    parent.setRenderBackground(true);

    CardTemplate card = childCard();
    card.setRenderBackground(false);

    CardTemplate result = merger._merge(card, parent);

    assertSame(card, result);
    assertFalse(result.isRenderBackground());
    assertFalse(result.isLockBackground());
  }

  // ---- _merge — multiple lock groups ----

  @Test
  void merge_overridesFrame_whenParentLockFrameTrue() {
    CardTemplate parent = childCard();
    parent.setLockFrame(true);
    parent.setRenderFrame(true);
    parent.setBorderWidth(5);

    CardTemplate card = childCard();
    card.setRenderFrame(false);
    card.setBorderWidth(0);

    CardTemplate result = merger._merge(card, parent);

    assertTrue(result.isRenderFrame());
    assertEquals(5, result.getBorderWidth());
  }

  @Test
  void merge_doesNotOverrideFrame_whenParentLockFrameFalse() {
    CardTemplate parent = childCard();
    parent.setLockFrame(false);
    parent.setRenderFrame(true);

    CardTemplate card = childCard();
    card.setRenderFrame(false);

    CardTemplate result = merger._merge(card, parent);

    assertFalse(result.isRenderFrame());
  }
}
