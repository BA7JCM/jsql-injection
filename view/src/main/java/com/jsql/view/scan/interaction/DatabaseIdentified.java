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
package com.jsql.view.scan.interaction;

import com.jsql.model.bean.util.Header;
import com.jsql.model.injection.vendor.model.Vendor;
import com.jsql.view.interaction.InteractionCommand;
import com.jsql.view.swing.util.MediatorHelper;

import java.util.Map;

/**
 * End the refreshing of administration page search button.
 */
public class DatabaseIdentified implements InteractionCommand {
    
    private final String url;
    private final Vendor vendor;
    
    @SuppressWarnings("unchecked")
    public DatabaseIdentified(Object[] interactionParams) {
        Map<Header, Object> params = (Map<Header, Object>) interactionParams[0];
        this.url = (String) params.get(Header.URL);
        this.vendor = (Vendor) params.get(Header.VENDOR);
    }

    @Override
    public void execute() {
        MediatorHelper.managerScan().highlight(this.url, this.vendor.toString());
    }
}
