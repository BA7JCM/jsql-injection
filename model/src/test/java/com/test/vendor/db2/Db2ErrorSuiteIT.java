package com.test.vendor.db2;

import com.jsql.model.InjectionModel;
import com.jsql.model.exception.JSqlException;
import com.jsql.view.terminal.SystemOutTerminal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junitpioneer.jupiter.RetryingTest;

public class Db2ErrorSuiteIT extends ConcreteDb2SuiteIT {
    //Unstable

    @Override
    public void setupInjection() throws Exception {
        
        InjectionModel model = new InjectionModel();
        this.injectionModel = model;

        model.subscribe(new SystemOutTerminal());

        // Slow fingerprinting => star
        model.getMediatorUtils().getParameterUtil().initializeQueryString(
            "http://localhost:8080/errors?tenant=db2&name=0'*"
        );
        
        model
        .getMediatorUtils()
        .getConnectionUtil()
        .withMethodInjection(model.getMediatorMethod().getQuery())
        .withTypeRequest("GET");

        model.beginInjection();
    }
    
    @Override
    @RetryingTest(3)
    public void listDatabases() throws JSqlException {
        super.listDatabases();
    }

    @Override
    @RetryingTest(3)
    public void listTables() throws JSqlException {
        super.listTables();
    }

    @Override
    @RetryingTest(3)
    public void listColumns() throws JSqlException {
        super.listColumns();
    }

    @Override
    @RetryingTest(3)
    public void listValues() throws JSqlException {
        super.listValues();
    }

    @AfterEach
    public void afterEach() {
        Assertions.assertEquals(
            this.injectionModel.getMediatorStrategy().getError(),
            this.injectionModel.getMediatorStrategy().getStrategy()
        );
    }
}