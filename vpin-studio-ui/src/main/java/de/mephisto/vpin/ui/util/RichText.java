package de.mephisto.vpin.ui.util;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.collection.ListModification;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class RichText {


  private static final String[] KEYWORDS = new String[] {
      "AddHandler", "AddressOf", "Alias", "And", "AndAlso",
      "As", "Boolean", "ByRef", "Byte", "ByVal",
      "Call", "Case", "Catch", "CBool", "CByte",
      "CChar", "CDate", "CDbl", "CDec", "Char",
      "CInt", "Class", "CLng", "CObj", "Const",
      "Continue", "CSByte", "CShort", "CSng", "CStr",
      "CType", "CUInt", "CULng", "CUShort", "Date",
      "Decimal", "Declare", "Default", "Delegate", "Dim", "dim",
      "DirectCast", "Do", "Double", "Each", "Else",
      "ElseIf", "End", "EndIf", "end", "endIf","Enum", "Erase",
      "Error", "Event", "Exit", "False", "Finally",
      "for", "For Each…Next", "Friend", "Function", "Get",
      "GetType", "GetXMLNamespace", "Global", "GoSub", "GoTo",
      "Handles", "If", "if", "Implements", "Imports", "In",
      "Inherits", "Integer", "Interface", "Is", "IsNot",
      "Let", "Lib", "Like", "Long", "Loop",
      "Me", "Mod", "Module", "MustInherit", "MustOverride",
      "MyBase", "MyClass", "Namespace", "Narrowing", "New",
      "Next", "next", "Not", "Nothing", "NotInheritable", "NotOverridable",
      "Object", "Of", "On", "Operator", "Option",
      "Optional", "Or", "OrElse", "Out", "Overloads",
      "Overridable", "Overrides", "ParamArray", "Partial", "Private",
      "Property", "Protected", "Public", "RaiseEvent", "ReadOnly",
      "ReDim", "REM", "RemoveHandler", "Resume", "Return",
      "SByte", "Select", "Set", "Shadows", "Shared",
      "Short", "Single", "Static", "Step", "Stop",
      "String", "Structure", "Sub", "SyncLock", "Then", "then",
      "Throw", "To", "True", "Try", "TryCast",
      "TypeOf…Is", "UInteger", "ULong", "UShort", "Using",
      "Variant", "Wend", "When", "While", "Widening",
      "With", "WithEvents", "WriteOnly", "Xor", "#Else"
  };

  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String PAREN_PATTERN = "\\(|\\)";
  private static final String BRACE_PATTERN = "\\{|\\}";
  private static final String BRACKET_PATTERN = "\\[|\\]";
  private static final String SEMICOLON_PATTERN = "\\;";
  private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
  private static final String COMMENT_PATTERN = "^(\\'[^\\r\\n]+)$|(''[^\\r\\n]+)$\n";
//  private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"   // for whole text processing (text blocks)
//      + "|" + "/\\*[^\\v]*" + "|" + "^\\h*\\*([^\\v]*|/)";  // for visible paragraph processing (line by line)

  private static final Pattern PATTERN = Pattern.compile(
      "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
          + "|(?<PAREN>" + PAREN_PATTERN + ")"
          + "|(?<BRACE>" + BRACE_PATTERN + ")"
          + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
          + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
          + "|(?<STRING>" + STRING_PATTERN + ")"
          + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  );

  private final CodeArea codeArea;

  public CodeArea getCodeArea() {
    return codeArea;
  }

  public RichText(String source) {
    codeArea = new CodeArea();

    // add line numbers to the left of area
    codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
    codeArea.setContextMenu(new DefaultContextMenu());
/*
        // recompute the syntax highlighting for all text, 500 ms after user stops editing area
        // Note that this shows how it can be done but is not recommended for production with
        // large files as it does a full scan of ALL the text every time there is a change !
        Subscription cleanupWhenNoLongerNeedIt = codeArea

                // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
                // multi plain changes = save computation by not rerunning the code multiple times
                //   when making multiple changes (e.g. renaming a method at multiple parts in file)
                .multiPlainChanges()

                // do not emit an event until 500 ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(500))

                // run the following code block when previous stream emits an event
                .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));

        // when no longer need syntax highlighting and wish to clean up memory leaks
        // run: `cleanupWhenNoLongerNeedIt.unsubscribe();`
*/
    // recompute syntax highlighting only for visible paragraph changes
    // Note that this shows how it can be done but is not recommended for production where multi-
    // line syntax requirements are needed, like comment blocks without a leading * on each line.
    codeArea.getVisibleParagraphs().addModificationObserver
        (
            new VisibleParagraphStyler<>(codeArea, this::computeHighlighting)
        );

    // auto-indent: insert previous line's indents on enter
    final Pattern whiteSpace = Pattern.compile("^\\s+");
    codeArea.addEventHandler(KeyEvent.KEY_PRESSED, KE ->
    {
      if (KE.getCode() == KeyCode.ENTER) {
        int caretPosition = codeArea.getCaretPosition();
        int currentParagraph = codeArea.getCurrentParagraph();
        Matcher m0 = whiteSpace.matcher(codeArea.getParagraph(currentParagraph - 1).getSegments().get(0));
        if (m0.find()) Platform.runLater(() -> codeArea.insertText(caretPosition, m0.group()));
      }
    });


    codeArea.replaceText(0, 0, source);
  }

  private StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder
        = new StyleSpansBuilder<>();
    while(matcher.find()) {
      String styleClass =
          matcher.group("KEYWORD") != null ? "keyword" :
              matcher.group("PAREN") != null ? "paren" :
                  matcher.group("BRACE") != null ? "brace" :
                      matcher.group("BRACKET") != null ? "bracket" :
                          matcher.group("SEMICOLON") != null ? "semicolon" :
                              matcher.group("STRING") != null ? "string" :
                                  matcher.group("COMMENT") != null ? "comment" :
                                      null; /* never happens */ assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  private class VisibleParagraphStyler<PS, SEG, S> implements Consumer<ListModification<? extends Paragraph<PS, SEG, S>>>
  {
    private final GenericStyledArea<PS, SEG, S> area;
    private final Function<String,StyleSpans<S>> computeStyles;
    private int prevParagraph, prevTextLength;

    public VisibleParagraphStyler( GenericStyledArea<PS, SEG, S> area, Function<String,StyleSpans<S>> computeStyles )
    {
      this.computeStyles = computeStyles;
      this.area = area;
    }

    @Override
    public void accept( ListModification<? extends Paragraph<PS, SEG, S>> lm )
    {
      if ( lm.getAddedSize() > 0 ) Platform.runLater( () ->
      {
        int paragraph = Math.min( area.firstVisibleParToAllParIndex() + lm.getFrom(), area.getParagraphs().size()-1 );
        String text = area.getText( paragraph, 0, paragraph, area.getParagraphLength( paragraph ) );

        if ( paragraph != prevParagraph || text.length() != prevTextLength )
        {
          if ( paragraph < area.getParagraphs().size()-1 )
          {
            int startPos = area.getAbsolutePosition( paragraph, 0 );
            area.setStyleSpans( startPos, computeStyles.apply( text ) );
          }
          prevTextLength = text.length();
          prevParagraph = paragraph;
        }
      });
    }
  }


  private class DefaultContextMenu extends ContextMenu {
    private MenuItem fold, unfold;

    public DefaultContextMenu() {
      fold = new MenuItem("Fold selected text");
      fold.setOnAction(AE -> {
        hide();
        fold();
      });

      unfold = new MenuItem("Unfold from cursor");
      unfold.setOnAction(AE -> {
        hide();
        unfold();
      });

      getItems().addAll(fold, unfold);
    }

    /**
     * Folds multiple lines of selected text, only showing the first line and hiding the rest.
     */
    private void fold() {
      ((CodeArea) getOwnerNode()).foldSelectedParagraphs();
    }

    /**
     * Unfold the CURRENT line/paragraph if it has a fold.
     */
    private void unfold() {
      CodeArea area = (CodeArea) getOwnerNode();
      area.unfoldParagraphs(area.getCurrentParagraph());
    }

    private void print() {
      System.out.println(((CodeArea) getOwnerNode()).getText());
    }
  }
}
