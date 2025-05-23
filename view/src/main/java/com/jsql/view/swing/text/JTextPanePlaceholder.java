package com.jsql.view.swing.text;

import com.jsql.util.LogLevelUtil;
import com.jsql.view.swing.util.UiUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;

import javax.swing.*;
import java.awt.*;
import java.util.ConcurrentModificationException;

/**
 * Textfield with information text displayed when empty.
 */
public class JTextPanePlaceholder extends JTextPane implements JPlaceholder {
    
    private static final Logger LOGGER = LogManager.getRootLogger();
    
    /**
     * Text to display when empty.
     */
    private String placeholderText;

    /**
     * Create a textfield with hint.
     * @param placeholder Text displayed when empty
     */
    public JTextPanePlaceholder(String placeholder) {
        this.placeholderText = placeholder;
        UiUtil.init(this);
    }

    @Override
    public void paint(Graphics g) {
        // Fix #4012: ArrayIndexOutOfBoundsException on paint()
        // Fix #38546: ConcurrentModificationException on getText()
        // Fix #37872: IndexOutOfBoundsException on getText()
        // Fix #48915: ClassCastException on paint()
        // Unhandled IllegalArgumentException #91471 on paint()
        try {
            super.paint(g);
            if (StringUtils.isEmpty(Jsoup.parse(this.getText()).text().trim())) {
                UiUtil.drawPlaceholder(this, g, this.placeholderText);
            }
        } catch (IllegalArgumentException | ConcurrentModificationException | IndexOutOfBoundsException | ClassCastException e) {
            LOGGER.log(LogLevelUtil.CONSOLE_JAVA, e, e);
        }
    }

    @Override
    public void setPlaceholderText(String placeholderText) {
        this.placeholderText = placeholderText;
    }
}