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
package com.jsql.view.swing.interaction;

import com.jsql.model.bean.database.Database;
import com.jsql.view.interaction.InteractionCommand;
import com.jsql.view.swing.util.MediatorHelper;

import java.util.List;

/**
 * Add the databases to current injection panel.
 */
public class AddDatabases implements InteractionCommand {
    
    /**
     * Databases retrieved by the view.
     */
    private final List<Database> databases;

    /**
     * @param interactionParams List of databases retrieved by the Model
     */
    @SuppressWarnings("unchecked")
    public AddDatabases(Object[] interactionParams) {
        this.databases = (List<Database>) interactionParams[0];
    }

    @Override
    public void execute() {
        MediatorHelper.treeDatabase().addDatabases(this.databases);
    }
}
