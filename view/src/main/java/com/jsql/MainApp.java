package com.jsql;

import com.jsql.model.InjectionModel;
import com.jsql.util.LogLevelUtil;
import com.jsql.view.swing.JFrameView;
import com.jsql.view.swing.util.MediatorHelper;
import com.jsql.view.swing.util.UiUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

/**
 * Main class of the application and called from the .jar.
 * This class set the general environment of execution and start the software.
 */
public class MainApp {
    
    private static final Logger LOGGER = LogManager.getRootLogger();
    private static final InjectionModel INJECTION_MODEL;  // required to load preferences first
    
    static {
        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "connection,content-length,expect,host,upgrade");

        if (GraphicsEnvironment.isHeadless()) {
            LOGGER.log(Level.ERROR, "Headless runtime not supported, use default Java runtime instead");
            System.exit(1);
        }

        INJECTION_MODEL = new InjectionModel();
        MainApp.INJECTION_MODEL.getMediatorUtils().getPreferencesUtil().loadSavedPreferences();

        var nameTheme = MainApp.INJECTION_MODEL.getMediatorUtils().getPreferencesUtil().getThemeFlatLafName();
        UiUtil.applyTheme(nameTheme);  // required init but not enough, reapplied next
        MainApp.apply4K();
    }

    private MainApp() {
        // nothing
    }
    
    /**
     * Application starting point.
     * @param args CLI parameters (not used)
     */
    public static void main(String[] args) {
        MediatorHelper.register(MainApp.INJECTION_MODEL);

        MainApp.INJECTION_MODEL.getMediatorUtils().getExceptionUtil().setUncaughtExceptionHandler();
        MainApp.INJECTION_MODEL.getMediatorUtils().getProxyUtil().initializeProxy();
        MainApp.INJECTION_MODEL.getMediatorUtils().getAuthenticationUtil().setKerberosCifs();

        try {
            var view = new JFrameView(MainApp.INJECTION_MODEL);
            MediatorHelper.register(view);
            MainApp.INJECTION_MODEL.subscribe(view.getSubscriber());
        } catch (HeadlessException e) {
            LOGGER.log(LogLevelUtil.CONSOLE_JAVA, "HeadlessException, command line execution not supported: %s", e);
        } catch (AWTError e) {
            // Fix #22668: Assistive Technology not found
            LOGGER.log(LogLevelUtil.CONSOLE_JAVA, String.format(
                "Java Access Bridge missing or corrupt, check your access bridge definition in JDK_HOME/jre/lib/accessibility.properties: %s",
                e.getMessage()
            ), e);
        }
    }

    private static void apply4K() {  // required not in UiUtil before frame is set
        if (MainApp.INJECTION_MODEL.getMediatorUtils().getPreferencesUtil().is4K()) {
            System.setProperty("sun.java2d.uiScale", "2.5");  // jdk >= 9
        }
    }
}
