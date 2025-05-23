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
package com.jsql.model.bean.database;

import com.jsql.util.LogLevelUtil;
import com.jsql.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Define a Table, e.g. is sent to the view by the model after injection.
 * Allow to traverse upward to its corresponding database.
 */
public class Table extends AbstractElementDatabase {
    
    private static final Logger LOGGER = LogManager.getRootLogger();
    
    // The database that contains the current column.
    private final Database parentDatabase;
    
    // The number of rows in the table.
    // TODO to int and move to abstract class
    private String rowCount;

    /**
     * Define the table label, number of rows and parent database.
     */
    public Table(String tableName, String rowCount, Database parentDatabase) {
        this.elementValue = tableName;
        this.rowCount = rowCount;
        this.parentDatabase = parentDatabase;
    }
    
    // Return the parent database.
    @Override
    public AbstractElementDatabase getParent() {
        return this.parentDatabase;
    }
    
    // Return the number of rows in the table.
    @Override
    public int getChildCount() {
        return Integer.parseInt(this.rowCount);
    }

    /**
     * A readable label for the table, with number of rows, displayed
     * by the view. If parent database is the system information_schema, number
     * of rows is unknown, e.g. my_table (7 rows).
     */
    @Override
    public String getLabelWithCount() {
        String nbRow = StringUtil.INFORMATION_SCHEMA.equals(this.parentDatabase.toString())
            ? "?"
            : this.rowCount;
        
        // Report #138: detect incorrect number of rows
        String sPlural = StringUtils.EMPTY;
        try {
            if (Integer.parseInt(this.rowCount) > 1) {
                sPlural = "s";
            }
        } catch (NumberFormatException e) {
            this.rowCount = "0";
            nbRow = "0";
            LOGGER.log(LogLevelUtil.CONSOLE_ERROR, "Incorrect number of rows.");
        }
        
        return String.format(
             "%s (%s row%s)",
             this.elementValue,
             nbRow,
             sPlural
        );
    }
}
