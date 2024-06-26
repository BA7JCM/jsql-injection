/*******************************************************************************
 * Copyhacked (H) 2012-2020.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 *
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 *******************************************************************************/
package com.jsql.view.swing.tab;

import com.jsql.util.I18nUtil;
import com.jsql.view.swing.manager.*;
import com.jsql.view.swing.util.MediatorHelper;
import com.jsql.view.swing.util.UiUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import java.awt.*;

/**
 * Panel on the left with functionalities like webshell, file reading and admin page finder.
 */
public class TabManagersProxy extends JTabbedPane {
    
    private final ManagerWebShell managerWebShell = new ManagerWebShell();
    private final ManagerFile managerFile = new ManagerFile();
    private final ManagerUpload managerUpload = new ManagerUpload();
    private final ManagerSqlShell managerSqlShell = new ManagerSqlShell();
    
    /**
     * Create manager panel.
     */
    public TabManagersProxy() {
        
        this.setName("tabManagersProxy");
        
        // Hide tab headers
        this.setUI(new MetalTabbedPaneUI() {
            
            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
                
                return 0;
            }
        });
        
        var managerScanList = new ManagerScan();
        var managerDatabase = new ManagerDatabase();
        var managerAdminPage = new ManagerAdminPage();
        var managerBruteForce = new ManagerBruteForce();
        
        MediatorHelper.register(this.managerWebShell);
        MediatorHelper.register(this.managerFile);
        MediatorHelper.register(this.managerUpload);
        MediatorHelper.register(this.managerSqlShell);
        MediatorHelper.register(managerScanList);
        MediatorHelper.register(managerAdminPage);
        MediatorHelper.register(managerBruteForce);
        
        this.setMinimumSize(new Dimension(100, 0));
        
        this.buildI18nTab("DATABASE_TAB", UiUtil.ICON_DATABASE_SERVER, managerDatabase);
        this.buildI18nTab("ADMINPAGE_TAB", UiUtil.ICON_ADMIN_SERVER, managerAdminPage);
        this.buildI18nTab("FILE_TAB", UiUtil.ICON_FILE_SERVER, this.managerFile);
        this.buildI18nTab("WEBSHELL_TAB", UiUtil.ICON_SHELL_SERVER, this.managerWebShell);
        this.buildI18nTab("SQLSHELL_TAB", UiUtil.ICON_SHELL_SERVER, this.managerSqlShell);
        this.buildI18nTab("UPLOAD_TAB", UiUtil.ICON_UPLOAD, this.managerUpload);
        this.buildI18nTab("BRUTEFORCE_TAB", UiUtil.ICON_BRUTER, managerBruteForce);
        this.buildI18nTab("CODER_TAB", UiUtil.ICON_CODER, new ManagerCoder());
        this.buildI18nTab("SCANLIST_TAB", UiUtil.ICON_SCANLIST, managerScanList);
    }
    
    public void addToLists(String path, String name) {
        
        // Add the path String to the list of files only if there is no same StringObject value already
        this.managerWebShell.addToList(path.replace(name, StringUtils.EMPTY));
        this.managerUpload.addToList(path.replace(name, StringUtils.EMPTY));
        this.managerSqlShell.addToList(path.replace(name, StringUtils.EMPTY));
    }
    
    public void markFileSystemInvulnerable() {
        
        this.managerFile.changePrivilegeIcon(UiUtil.ICON_SQUARE_RED);
        this.managerFile.endProcess();
        
        this.managerWebShell.changePrivilegeIcon(UiUtil.ICON_SQUARE_RED);
        this.managerWebShell.endProcess();
        
        this.managerUpload.changePrivilegeIcon(UiUtil.ICON_SQUARE_RED);
        this.managerUpload.endProcess();
        
        this.managerSqlShell.changePrivilegeIcon(UiUtil.ICON_SQUARE_RED);
        this.managerSqlShell.endProcess();
    }
    
    public void endPreparation() {
        
        this.managerFile.setButtonEnable(true);
        this.managerWebShell.setButtonEnable(true);
        this.managerSqlShell.setButtonEnable(true);
        this.managerUpload.setButtonEnable(true);
    }
    
    public void markFileSystemVulnerable() {
        
        this.managerFile.changePrivilegeIcon(UiUtil.ICON_TICK);
        this.managerWebShell.changePrivilegeIcon(UiUtil.ICON_TICK);
        this.managerSqlShell.changePrivilegeIcon(UiUtil.ICON_TICK);
        this.managerUpload.changePrivilegeIcon(UiUtil.ICON_TICK);
    }
    
    private void buildI18nTab(String keyLabel, Icon icon, Component manager) {
        this.addTab(I18nUtil.valueByKey(keyLabel), icon, manager);
    }
}
