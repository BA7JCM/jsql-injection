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
package com.jsql.view.terminal.interaction;

import com.jsql.util.AnsiColorUtil;
import com.jsql.view.interaction.InteractionCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * Create a new tab for the values.
 */
public class CreateValuesTab implements InteractionCommand {
    
    private static final Logger LOGGER = LogManager.getRootLogger();

    /**
     * 2D array of values.
     */
    private final String[][] data;

    /**
     * @param interactionParams Names of columns, table's values and corresponding table
     */
    public CreateValuesTab(Object[] interactionParams) {
        this.data = (String[][]) interactionParams[1];  // 2D array of values
    }

    @Override
    public void execute() {
        LOGGER.debug(() -> AnsiColorUtil.addGreenColor(this.getClass().getSimpleName()));
        LOGGER.debug(() -> Arrays.deepToString(this.data));
    }
}
