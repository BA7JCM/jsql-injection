package com.test.method;

import com.jsql.model.InjectionModel;
import com.jsql.model.exception.JSqlException;
import com.jsql.view.terminal.SystemOutTerminal;
import com.test.vendor.mysql.ConcreteMySqlSuiteIT;
import org.junitpioneer.jupiter.RetryingTest;

class PostSuiteIT extends ConcreteMySqlSuiteIT {
    
    @Override
    public void setupInjection() throws Exception {
        InjectionModel model = new InjectionModel();
        this.injectionModel = model;

        model.subscribe(new SystemOutTerminal());

        model.getMediatorUtils().getParameterUtil().initQueryString("http://localhost:8080/post");
        model.getMediatorUtils().getParameterUtil().initRequest("tenant=mysql&name=");

        model.setIsScanning(true);

        model
        .getMediatorUtils()
        .getPreferencesUtil()
        .withIsStrategyBlindBitDisabled(true)
        .withIsStrategyBlindBinDisabled(true)
        .withIsStrategyTimeDisabled(true);
        
        model
        .getMediatorUtils()
        .getConnectionUtil()
        .withMethodInjection(model.getMediatorMethod().getRequest())
        .withTypeRequest("POST");
        
        model.beginInjection();
    }
    
    @Override
    @RetryingTest(3)
    public void listDatabases() throws JSqlException {
        super.listDatabases();
    }
}
