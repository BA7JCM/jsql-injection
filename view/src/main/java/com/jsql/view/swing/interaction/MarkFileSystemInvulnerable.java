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

import com.jsql.view.interaction.InteractionCommand;
import com.jsql.view.swing.util.MediatorHelper;

/**
 * Mark the injection as using a user profile invulnerable to file I/O.
 */
public class MarkFileSystemInvulnerable implements InteractionCommand {
    
    public MarkFileSystemInvulnerable(Object[] interactionParams) {
        // Do nothing
    }

    @Override
    public void execute() {
        MediatorHelper.tabManagersCards().markFileSystemInvulnerable();
    }
}
