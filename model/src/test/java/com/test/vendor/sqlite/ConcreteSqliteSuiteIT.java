package com.test.vendor.sqlite;

import com.jsql.model.accessible.DataAccess;
import com.test.AbstractTestSuite;
import org.hibernate.cfg.JdbcSettings;
import spring.SpringApp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ConcreteSqliteSuiteIT extends AbstractTestSuite {

    public ConcreteSqliteSuiteIT() {
        this.config();
    }
    
    public void config() {

        this.jdbcURL = SpringApp.propsSqlite.getProperty(JdbcSettings.JAKARTA_JDBC_URL);
        this.jdbcUser = SpringApp.propsSqlite.getProperty(JdbcSettings.JAKARTA_JDBC_USER);
        this.jdbcPass = SpringApp.propsSqlite.getProperty(JdbcSettings.JAKARTA_JDBC_PASSWORD);

        this.jsqlDatabaseName = "musicstore";
        this.jsqlTableName = "Student";
        this.jsqlColumnName = "Student_Id";
        
        this.jdbcColumnForDatabaseName = "sqlite_master";
        this.jdbcColumnForTableName = "name";
        this.jdbcColumnForColumnName = "sql";
        
        this.jdbcQueryForDatabaseNames = "select '"+ this.jdbcColumnForDatabaseName +"' "+ this.jdbcColumnForDatabaseName +" from "+ this.jdbcColumnForDatabaseName +" WHERE type = 'table'";
        this.jdbcQueryForTableNames =    "select "+ this.jdbcColumnForTableName +" from sqlite_master WHERE type = 'table'";
        this.jdbcQueryForColumnNames =   "select "+ this.jdbcColumnForColumnName +" from "+ this.jdbcColumnForDatabaseName +" where tbl_name = '"+ this.jsqlTableName +"' and type = 'table'";
        this.jdbcQueryForValues =    "select "+ this.jsqlColumnName +" from "+ this.jsqlTableName;
    }
    
    @Override
    protected Collection<String> parse(List<String> rawColumns) {
        
        String modelColumns = rawColumns.stream().findFirst().orElseThrow();
        String columnsToParse = this.injectionModel.getMediatorVendor().getSqlite().transformSqlite(modelColumns);
        
        Matcher regexSearch = Pattern.compile(
                DataAccess.MODE
                + DataAccess.ENCLOSE_VALUE_RGX
                + DataAccess.CELL_TABLE
                + DataAccess.ENCLOSE_VALUE_RGX
            )
            .matcher(columnsToParse);

        List<String> columns = new ArrayList<>();
        
        while (regexSearch.find()) {
            columns.add(regexSearch.group(1));
        }
        
        return columns;
    }
}
