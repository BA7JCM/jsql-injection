package com.test.vendor.derby;

import com.test.AbstractTestSuite;
import org.hibernate.cfg.Environment;
import spring.SpringTargetApplication;

public abstract class ConcreteDerbySuiteIT extends AbstractTestSuite {

    public ConcreteDerbySuiteIT() {

        this.jdbcURL = SpringTargetApplication.propsDerby.getProperty(Environment.URL);
        this.jdbcUser = SpringTargetApplication.propsDerby.getProperty(Environment.USER);
        this.jdbcPass = SpringTargetApplication.propsDerby.getProperty(Environment.PASS);

        this.jsqlDatabaseName = "ADMIN";
        this.jsqlTableName = "STUDENT";
        this.jsqlColumnName = "FIRST_NAME";
        
        this.jdbcColumnForDatabaseName = "schemaname";
        this.jdbcColumnForTableName = "tablename";
        this.jdbcColumnForColumnName = "columnname";
        
        this.jdbcQueryForDatabaseNames = "select schemaname from SYS.SYSSCHEMAS";
        this.jdbcQueryForTableNames = "select tablename from sys.systables t inner join sys.sysschemas s on t.schemaid = s.schemaid where schemaname='"+ this.jsqlDatabaseName +"'";
        this.jdbcQueryForColumnNames =
            "select columnname from sys.systables t inner join sys.sysschemas s on t.schemaid = s.schemaid inner join sys.syscolumns c on t.tableid = c.referenceid"
            + " where schemaname = '"+ this.jsqlDatabaseName +"' and tablename = '"+ this.jsqlTableName +"'"
            + " and columndatatype || '' not like 'DOUBLE%'"
            + " and columndatatype || '' not like 'INTEGER%'"
            + " and columndatatype || '' not like 'DECIMAL%'"
            + " and columndatatype || '' not like 'BLOB%'";
        this.jdbcQueryForValues = "select "+ this.jsqlColumnName +" from "+ this.jsqlDatabaseName +"."+ this.jsqlTableName;
    }
}
