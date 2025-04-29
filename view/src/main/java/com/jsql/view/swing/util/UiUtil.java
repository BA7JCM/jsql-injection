/*******************************************************************************
 * Copyhacked (H) 2012-2025.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.view.swing.util;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme;
import com.jsql.util.I18nUtil;
import com.jsql.util.LogLevelUtil;
import com.jsql.view.swing.console.JTextPaneAppender;
import com.jsql.view.swing.sql.SqlEngine;
import com.jsql.view.swing.text.action.DeleteNextCharAction;
import com.jsql.view.swing.text.action.DeletePrevCharAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Build default component appearance, keyboard shortcuts and icons.
 */
public class UiUtil {
    
    private static final Logger LOGGER = LogManager.getRootLogger();

    public static final Border BORDER_5PX = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    public static final ImageIcon ICON_FLAG_AR = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/ar.png")));
    public static final ImageIcon ICON_FLAG_ZH = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/zh.png")));
    public static final ImageIcon ICON_FLAG_RU = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/ru.png")));
    public static final ImageIcon ICON_FLAG_TR = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/tr.png")));
    public static final ImageIcon ICON_FLAG_EN = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/en.png")));
    public static final ImageIcon ICON_FLAG_FR = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/fr.png")));
    public static final ImageIcon ICON_FLAG_HI = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/hi.png")));
    public static final ImageIcon ICON_FLAG_CS = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/cs.png")));
    public static final ImageIcon ICON_FLAG_DE = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/de.png")));
    public static final ImageIcon ICON_FLAG_NL = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/nl.png")));
    public static final ImageIcon ICON_FLAG_IN = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/in.png")));
    public static final ImageIcon ICON_FLAG_IT = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/it.png")));
    public static final ImageIcon ICON_FLAG_ES = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/es.png")));
    public static final ImageIcon ICON_FLAG_PT = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/pt.png")));
    public static final ImageIcon ICON_FLAG_PL = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/pl.png")));
    public static final ImageIcon ICON_FLAG_JA = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/ja.png")));
    public static final ImageIcon ICON_FLAG_KO = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/ko.png")));
    public static final ImageIcon ICON_FLAG_RO = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/ro.png")));
    public static final ImageIcon ICON_FLAG_LK = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/lk.png")));
    public static final ImageIcon ICON_FLAG_SE = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/se.png")));
    public static final ImageIcon ICON_FLAG_FI = new ImageIcon(Objects.requireNonNull(UiUtil.class.getClassLoader().getResource("swing/images/flags/fi.png")));

    public static final ModelSvgIcon DATABASE_BOLD = new ModelSvgIcon("database-bold", 0x1C274C)
        .withTab("DATABASE_TAB", "DATABASE_TOOLTIP");
    public static final ModelSvgIcon ADMIN = new ModelSvgIcon("admin", 0.02f)
        .withTab("ADMINPAGE_TAB", "ADMINPAGE_TOOLTIP");
    public static final ModelSvgIcon DOWNLOAD = new ModelSvgIcon("download", 0.55f)
        .withTab("FILE_TAB", "FILE_TOOLTIP");
    public static final ModelSvgIcon TERMINAL = new ModelSvgIcon("terminal", 0.50f)
        .withTab("EXPLOIT_TAB", "EXPLOIT_TOOLTIP");
    public static final ModelSvgIcon UPLOAD = new ModelSvgIcon("upload", 0.55f);
    public static final ModelSvgIcon LOCK = new ModelSvgIcon("lock", 0.02f)
        .withTab("BRUTEFORCE_TAB", "BRUTEFORCE_TOOLTIP");
    public static final ModelSvgIcon TEXTFIELD = new ModelSvgIcon("textfield", 0.02f)
        .withTab("CODER_TAB", "CODER_TOOLTIP");
    public static final ModelSvgIcon BATCH = new ModelSvgIcon("batch", 0.02f)
        .withTab("SCANLIST_TAB", "SCANLIST_TOOLTIP");

    public static final ModelSvgIcon TABLE_LINEAR = new ModelSvgIcon("table-linear", 0x212121);
    public static final ModelSvgIcon TABLE_BOLD = new ModelSvgIcon("table-bold", 0x212121);
    public static final ModelSvgIcon NETWORK = new ModelSvgIcon("network", 0.02f);
    public static final ModelSvgIcon DATABASE_LINEAR = new ModelSvgIcon("database-linear", 0x1C274C);
    public static final ModelSvgIcon CUP = new ModelSvgIcon("cup", 0.02f);
    public static final ModelSvgIcon CONSOLE = new ModelSvgIcon("console", 0.02f);
    public static final ModelSvgIcon BINARY = new ModelSvgIcon("binary", 0.02f);
    public static final ModelSvgIcon CHUNK = new ModelSvgIcon("chunk", 0.02f);
    public static final ModelSvgIcon COG = new ModelSvgIcon("cog", 0.02f);

    public static final ModelSvgIcon CROSS_RED = new ModelSvgIcon("cross", new Color(0x0F0F0F), null, LogLevelUtil.COLOR_RED, 0.025f);
    private static final String NAME_ARROW = "arrow";
    public static final ModelSvgIcon ARROW = new ModelSvgIcon(UiUtil.NAME_ARROW, new Color(0x005a96), "ComboBox.buttonArrowColor", 1f);
    public static final ModelSvgIcon ARROW_HOVER = new ModelSvgIcon(UiUtil.NAME_ARROW, new Color(0x005a96), "ComboBox.buttonHoverArrowColor", 1f);
    public static final ModelSvgIcon ARROW_PRESSED = new ModelSvgIcon(UiUtil.NAME_ARROW, new Color(0x005a96), "ComboBox.buttonPressedArrowColor", 1f);
    private static final String NAME_EXPAND = "expand";
    public static final ModelSvgIcon EXPAND = new ModelSvgIcon(UiUtil.NAME_EXPAND, Color.BLACK, "ComboBox.buttonArrowColor", 0.02f);
    public static final ModelSvgIcon EXPAND_HOVER = new ModelSvgIcon(UiUtil.NAME_EXPAND, Color.BLACK, "ComboBox.buttonHoverArrowColor", 0.02f);
    public static final ModelSvgIcon EXPAND_PRESSED = new ModelSvgIcon(UiUtil.NAME_EXPAND, Color.BLACK, "ComboBox.buttonPressedArrowColor", 0.02f);

    public static final ModelSvgIcon HOURGLASS = new ModelSvgIcon("hourglass", 0.02f);
    public static final ModelSvgIcon ARROW_UP = new ModelSvgIcon("arrow-up", 0.02f);
    public static final ModelSvgIcon ARROW_DOWN = new ModelSvgIcon("arrow-down", 0.02f);
    public static final ModelSvgIcon SQUARE = new ModelSvgIcon("square", 0.01f);
    public static final ModelSvgIcon TICK_GREEN = new ModelSvgIcon("tick", Color.BLACK, null, LogLevelUtil.COLOR_GREEN, 0.02f);
    public static final ModelSvgIcon GLOBE = new ModelSvgIcon("globe", 0.025f);
    public static final ModelSvgIcon APP_ICON = new ModelSvgIcon("app", 0.04f);
    public static final ModelSvgIcon APP_BIG = new ModelSvgIcon("app", 0.5f);
    public static final ModelSvgIcon APP_MIDDLE = new ModelSvgIcon("app", 0.25f);

    public static final String PATH_PAUSE = "swing/images/icons/pause.png";

    public static final String FONT_NAME_MONO_NON_ASIAN = "Ubuntu Mono";
    public static final int FONT_SIZE_MONO_NON_ASIAN = 14;
    public static final String FONT_NAME_MONO_ASIAN = "Monospace";
    public static final int FONT_SIZE_MONO_ASIAN = 13;
    
    // Used in Translation Dialog
    // HTML engine considers Monospaced/Monospace to be the same Font
    // Java engine recognizes only Monospaced
    public static final String FONT_NAME_MONOSPACED = "Monospaced";
    public static final String TEXTAREA_FONT = "TextArea.font";
    public static final String TEXTPANE_FONT = "TextPane.font";
    public static final Font FONT_MONO_NON_ASIAN = new Font(
        UiUtil.FONT_NAME_MONO_NON_ASIAN,
        Font.PLAIN,
        UIManager.getDefaults().getFont(UiUtil.TEXTAREA_FONT).getSize() + 2
    );
    
    public static final Font FONT_MONO_ASIAN = new Font(
        UiUtil.FONT_NAME_MONO_ASIAN,
        Font.PLAIN,
        UIManager.getDefaults().getFont(UiUtil.TEXTPANE_FONT).getSize()
    );
    
    public static final Font FONT_MONO_ASIAN_BIG = new Font(
        UiUtil.FONT_NAME_MONO_ASIAN,
        Font.PLAIN,
        UIManager.getDefaults().getFont(UiUtil.TEXTPANE_FONT).getSize() + 2
    );

    public static final Font FONT_NON_MONO = new Font(
        "Segoe UI",
        Font.PLAIN,
        UIManager.getDefaults().getFont(UiUtil.TEXTPANE_FONT).getSize()
    );
    
    public static final Font FONT_NON_MONO_BIG = new Font(
        UIManager.getDefaults().getFont("TextField.font").getName(),
        Font.PLAIN,
        UIManager.getDefaults().getFont("TextField.font").getSize() + 2
    );

    private UiUtil() {
        // Utility class
    }

    /**
     * Change the default style of various components.
     */
    public static void prepareGUI() {
        UiUtil.loadFonts();
        // timer before closing automatically tooltip
        ToolTipManager.sharedInstance().setDismissDelay(3 * ToolTipManager.sharedInstance().getDismissDelay());
        UIManager.put(UiUtil.TEXTAREA_FONT, UiUtil.FONT_MONO_NON_ASIAN);  // required for basic text like chunks tab
        UIManager.put(UiUtil.TEXTPANE_FONT, UIManager.getFont(UiUtil.TEXTAREA_FONT));  // align textpane font
    }

    private static void loadFonts() {
        var graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try (InputStream fontStream = new BufferedInputStream(
            Objects.requireNonNull(UiUtil.class.getClassLoader().getResourceAsStream("swing/font/UbuntuMono-R-ctrlchar.ttf"))
        )) {
            var ubuntuFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            graphicsEnvironment.registerFont(ubuntuFont);
        } catch (FontFormatException | IOException e) {
            LOGGER.log(LogLevelUtil.CONSOLE_ERROR, "Loading Font Ubuntu Mono with control characters failed", e);
        }
    }

    /**
     * Icons for application window.
     * @return List of a 16x16 (default) and 32x32 icon (alt-tab, taskbar)
     */
    public static List<Image> getIcons() {
        List<Image> images = new ArrayList<>();
        // Fix #2154: NoClassDefFoundError on read()
        try {
            images.add(UiUtil.APP_ICON.getIcon().getImage());
            images.add(UiUtil.APP_MIDDLE.getIcon().getImage());
            images.add(UiUtil.APP_BIG.getIcon().getImage());
        } catch (NoClassDefFoundError e) {
            LOGGER.log(LogLevelUtil.CONSOLE_JAVA, e, e);
        }
        return images;
    }
    
    public static void drawPlaceholder(JTextComponent textComponent, Graphics g, String placeholderText) {
        UiUtil.drawPlaceholder(textComponent, g, placeholderText, 0, g.getFontMetrics().getAscent() + 2);
    }
    
    public static void drawPlaceholder(JTextComponent textComponent, Graphics g, String placeholderText, int x, int y) {
        int w = textComponent.getWidth();
        
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        var ins = textComponent.getInsets();
        var fm = g.getFontMetrics();
        
        int c0 = UIManager.getColor("TextArea.background").getRGB();
        int c1 = UIManager.getColor("TextArea.foreground").getRGB();
        var m = 0xfefefefe;
        int c2 = ((c0 & m) >>> 1) + ((c1 & m) >>> 1);
        
        g.setColor(new Color(c2, true));

        var fontNonUbuntu = textComponent.getFont() == UiUtil.FONT_NON_MONO_BIG  // when address bar
            ? UiUtil.FONT_MONO_ASIAN_BIG.deriveFont(Font.ITALIC)  // bigger font
            : UiUtil.FONT_MONO_ASIAN.deriveFont(Font.ITALIC);  // fine for address bar, console, textfield
        g.setFont(
            I18nViewUtil.isNonUbuntu(I18nUtil.getCurrentLocale())
            ? fontNonUbuntu
            : textComponent.getFont().deriveFont(Font.ITALIC)  // same
        );

        g.drawString(
            placeholderText,
            x +
            (ComponentOrientation.RIGHT_TO_LEFT.equals(textComponent.getComponentOrientation())
            ? w - (fm.stringWidth(placeholderText) + ins.left + 2)
            : ins.left + 2),
            y
        );
    }
    
    public static void init(JTextComponent component) {
        component.setCaret(new DefaultCaret() {
            @Override
            public void setSelectionVisible(boolean visible) {
                super.setSelectionVisible(true);
            }
        });
        component.getActionMap().put(DefaultEditorKit.deletePrevCharAction, new DeletePrevCharAction());
        component.getActionMap().put(DefaultEditorKit.deleteNextCharAction, new DeleteNextCharAction());
    }

    public static void applySyntaxTheme(RSyntaxTextArea textArea) {
        try {
            boolean isDark = UIManager.getLookAndFeel().getName().matches(".*(Dark|High contrast).*");
            var xmlTheme = String.format("/org/fife/ui/rsyntaxtextarea/themes/%s.xml", isDark ? "dark" : "default");
            Theme theme = Theme.load(SqlEngine.class.getResourceAsStream(xmlTheme));
            theme.apply(textArea);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void applyTheme(String nameTheme) {
        try {
            Class<?> c = Class.forName(StringUtils.isEmpty(nameTheme) ? FlatLightFlatIJTheme.class.getName() : nameTheme);
            LookAndFeel lookAndFeel = (LookAndFeel) c.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (
            ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
            InvocationTargetException | UnsupportedLookAndFeelException e
        ) {
            throw new IllegalArgumentException(e);
        }
        FlatLaf.updateUI();  // required

        // required ATTRIBUTE_ALL without color for compatibility with dark/light mode as text is white/black
        StyleConstants.setForeground(JTextPaneAppender.ATTRIBUTE_WARN, LogLevelUtil.COLOR_RED);
        StyleConstants.setForeground(JTextPaneAppender.ATTRIBUTE_INFORM, LogLevelUtil.COLOR_BLU);
        StyleConstants.setForeground(JTextPaneAppender.ATTRIBUTE_SUCCESS, LogLevelUtil.COLOR_GREEN);
    }

    public static GridLayout getColumnLayout(int size) {
        return new GridLayout((size + 1) / 2, 2);
    }
}
