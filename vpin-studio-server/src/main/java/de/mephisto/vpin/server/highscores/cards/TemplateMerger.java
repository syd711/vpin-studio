package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TemplateMerger {

  @Autowired
  private TemplateMappingRepository templateMappingRepository;

  private static Map<String, List<String>> lock2properties = new HashMap<>();

  static {
    lock2properties.put("lockBackground", Arrays.asList("renderBackground", "backgroundX", "backgroundY", "zoom", "useDmdPositions", "fullScreen", "transparentPercentage", "alphaBlack", "alphaWhite", "blur", "grayScale", "useDefaultBackground", "background", "useColoredBackground", "backgroundColor"));
    lock2properties.put("lockFrame", Arrays.asList("renderFrame", "borderWidth", "borderRadius", "borderColor", "marginTop", "marginRight", "marginBottom", "marginLeft"));
    lock2properties.put("lockTableName", Arrays.asList("renderTableName", "tableUseVpsName", "tableRenderManufacturer", "tableRenderYear", "tableFontName", "tableFontSize", "tableFontStyle", "tableUseDefaultColor", "tableColor", "tableX", "tableY", "tableWidth", "tableHeight"));
    lock2properties.put("lockTitle", Arrays.asList("renderTitle", "title", "titleFontName", "titleFontSize", "titleFontStyle", "titleUseDefaultColor", "titleColor", "titleX", "titleY", "titleWidth", "titleHeight"));
    lock2properties.put("lockWheelIcon", Arrays.asList("renderWheelIcon", "wheelX", "wheelY", "wheelSize"));
    lock2properties.put("lockManufacturerLogo", Arrays.asList("renderManufacturerLogo", "manufacturerLogoKeepAspectRatio", "manufacturerLogoUseYear", "manufacturerLogoX", "manufacturerLogoY", "manufacturerLogoWidth", "manufacturerLogoHeight"));
    lock2properties.put("lockOtherMedia", Arrays.asList("renderOtherMedia", "otherMediaKeepAspectRatio", "otherMediaScreen", "otherMediaX", "otherMediaY", "otherMediaWidth", "otherMediaHeight"));
    lock2properties.put("lockCanvas", Arrays.asList("renderCanvas", "canvasX", "canvasY", "canvasWidth", "canvasHeight", "canvasBackground", "canvasAlphaPercentage", "canvasBorderRadius"));
    lock2properties.put("lockScores", Arrays.asList("renderScores", "scoresX", "scoresY", "scoresWidth", "scoresHeight", "rowMargin", "rawScore", "maxScores", "fontColor", "friendsFontColor", "scoreFontName", "scoreFontSize", "scoreFontStyle", "renderFriends", "renderPositions", "renderScoreDates"));
    lock2properties.put("lockOverlay", Arrays.asList("overlayMode", "overlayScreen"));
  }


  public CardTemplate merge(@NonNull CardTemplate card, @NonNull CardTemplate defaultTemplate) {
    if (card.isTemplate()) {
      return card;
    }

    CardTemplate parent = getParent(card);
    if (parent == null) {
      parent = defaultTemplate;
    }

    mergeProperties(parent, card);
    return card;
  }

  private void mergeProperties(@NonNull CardTemplate parent, @NonNull CardTemplate card) {
    for (Map.Entry<String, List<String>> entries : lock2properties.entrySet()) {
      String lockProperty = entries.getKey();
      List<String> fields = entries.getValue();

      BeanWrapper parentWrapper = new BeanWrapperImpl(parent);
      BeanWrapper cardWrapper = new BeanWrapperImpl(card);

      boolean locked = (boolean) parentWrapper.getPropertyValue(lockProperty);
      if (locked) {
        for (String field : fields) {
          cardWrapper.setPropertyValue(field, parentWrapper.getPropertyValue(field));
        }
      }
    }
  }

  @Nullable
  private CardTemplate getParent(CardTemplate card) {
    if (card.getParentId() != null) {
      Optional<TemplateMapping> byId = templateMappingRepository.findById(card.getParentId());
      if (byId.isPresent()) {
        return byId.get().getTemplate();
      }
    }

    return null;
  }
}
