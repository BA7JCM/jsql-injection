package com.test.vendor.oracle;

import com.test.AbstractTestSuite;

public abstract class ConcreteOracleSuiteIT extends AbstractTestSuite {

    public ConcreteOracleSuiteIT() {
        this.config();
    }
    
    public void config() {
        /*
            Oracle Database 19c (19.3.0) Enterprise Edition and Standard Edition 2

            - Download LINUX.X64_193000_db_home.zip in folder 19.3.0
            - buildDockerImage.sh -v 19.3.0 -e
            
            docker run --name oracle19ee \
            -p 1521:1521 -p 5500:5500 \
            -e ORACLE_SID=ORCLCDB \
            -e ORACLE_PDB=ORCLPDB1 \
            -e ORACLE_PWD=Password1_One \
            -e ORACLE_CHARACTERSET=AL32UTF8 \
            oracle/database:19.3.0-ee

            - Virtualbox port forward 11521 => 1521

            jdbc:oracle:thin:@localhost:11521:ORCLCDB
            system
            Password1_One
         */
        
        this.jdbcURL = "jdbc:oracle:thin:@localhost:11521:ORCLCDB";
        this.jdbcUser = "system";
        this.jdbcPass = "Password1_One";
        this.jsqlDatabaseName = "SYSTEM";
        this.jsqlTableName = "STUDENT";
        this.jsqlColumnName = "STUDENT_ID";
        
        this.jdbcColumnForDatabaseName = "owner";
        this.jdbcColumnForTableName = "table_name";
        this.jdbcColumnForColumnName = "column_name";
        
        this.jdbcQueryForDatabaseNames = "SELECT owner FROM all_tables";
        this.jdbcQueryForTableNames = "SELECT table_name FROM all_tables where owner='SYSTEM'";
        this.jdbcQueryForColumnNames = "SELECT column_name FROM all_tab_columns where owner='SYSTEM' and table_name='STUDENT'";
        this.jdbcQueryForValues = "SELECT STUDENT_ID FROM SYSTEM.STUDENT";
    }
}
