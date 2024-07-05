package com.test.vendor.informix;

import com.test.AbstractTestSuite;
import org.hibernate.cfg.Environment;
import spring.SpringTargetApplication;

public abstract class ConcreteInformixSuiteIT extends AbstractTestSuite {

    public ConcreteInformixSuiteIT() {

        this.jdbcURL = SpringTargetApplication.propsInformix.getProperty(Environment.URL);
        this.jdbcUser = SpringTargetApplication.propsInformix.getProperty(Environment.USER);
        this.jdbcPass = SpringTargetApplication.propsInformix.getProperty(Environment.PASS);

        this.jsqlDatabaseName = "sysutils";
        this.jsqlTableName = "student";
        this.jsqlColumnName = "student_id";
        
        this.jdbcColumnForDatabaseName = "name";
        this.jdbcColumnForTableName = "tabname";
        this.jdbcColumnForColumnName = "colname";
        
        this.jdbcQueryForDatabaseNames = "select distinct trim(name)name from sysmaster:informix.sysdatabases";
        this.jdbcQueryForTableNames =    "select distinct trim(tabname)tabname from "+ this.jsqlDatabaseName +":informix.systables";
        this.jdbcQueryForColumnNames =   "select distinct colname from "+ this.jsqlDatabaseName +":informix.syscolumns c join "+ this.jsqlDatabaseName +":informix.systables t on c.tabid = t.tabid where tabname = '"+ this.jsqlTableName +"'";
        this.jdbcQueryForValues =        "select distinct "+ this.jsqlColumnName +" from "+ this.jsqlDatabaseName +":"+ this.jsqlTableName;
    }
}
