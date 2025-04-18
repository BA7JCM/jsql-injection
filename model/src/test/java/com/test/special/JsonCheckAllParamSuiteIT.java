package com.test.special;

import com.jsql.model.InjectionModel;
import com.jsql.model.exception.JSqlException;
import com.jsql.view.terminal.SystemOutTerminal;
import com.test.vendor.mysql.ConcreteMySqlSuiteIT;
import org.junitpioneer.jupiter.RetryingTest;

class JsonCheckAllParamSuiteIT extends ConcreteMySqlSuiteIT {
    
    @Override
    public void setupInjection() throws Exception {
        InjectionModel model = new InjectionModel();
        this.injectionModel = model;

        model.subscribe(new SystemOutTerminal());

        model.getMediatorUtils().getParameterUtil().initQueryString("http://localhost:8080/json");
        model.getMediatorUtils().getParameterUtil().initRequest(
            "tenant=mysql&name={\"c\": 1, \"b\": {\"b\": [1, true, null, {\"a\": {\"a\": \"0'\"}}]}}"
        );

        model.setIsScanning(true);
        
        model
        .getMediatorUtils()
        .getPreferencesUtil()
        .withIsCheckingAllRequestParam(true)
        .withIsCheckingAllJsonParam(true)
        .withIsStrategyBlindBitDisabled(true)
        .withIsStrategyBlindBinDisabled(true)
        .withIsStrategyTimeDisabled(true)
        .withIsStrategyMultibitDisabled(true);

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
