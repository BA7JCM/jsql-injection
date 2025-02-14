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
 * Append text to the tab Chunk.
 */
public class MessageChunk implements InteractionCommand {
    
    /**
     * Text to append to the Chunk log area.
     */
    private final String text;

    /**
     * @param interactionParams Text to append
     */
    public MessageChunk(Object[] interactionParams) {
        this.text = (String) interactionParams[0];
    }

    @Override
    public void execute() {
        MediatorHelper.panelConsoles().messageChunk(this.text);
        MediatorHelper.tabConsoles().setBold("Chunk");
    }
}
