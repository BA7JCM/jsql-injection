package com.test.vendor.postgres;

import com.jsql.model.InjectionModel;
import com.jsql.model.exception.JSqlException;
import com.jsql.view.terminal.SystemOutTerminal;
import org.junit.jupiter.api.Assertions;
import org.junitpioneer.jupiter.RetryingTest;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

class PostgresReadFileSuiteIT extends ConcretePostgresSuiteIT {
    
    @Override
    public void setupInjection() throws Exception {
        InjectionModel model = new InjectionModel();
        this.injectionModel = model;

        model.subscribe(new SystemOutTerminal());

        model.getMediatorUtils().getParameterUtil().initQueryString(
            "http://localhost:8080/union?tenant=postgres&name="
        );

        model.setIsScanning(true);

        model
        .getMediatorUtils()
        .getConnectionUtil()
        .withMethodInjection(model.getMediatorMethod().getQuery())
        .withTypeRequest("GET");

        model
        .getMediatorUtils()
        .getPreferencesUtil()
        .withIsStrategyBlindDisabled(true)
        .withIsStrategyTimeDisabled(true)
        .withIsStrategyStackDisabled(true)
        .withIsStrategyErrorDisabled(true);

        model.beginInjection();
    }
    
    @RetryingTest(3)
    public void readFile() throws JSqlException, ExecutionException, InterruptedException {
        List<String> contents = this.injectionModel.getResourceAccess().readFile(Collections.singletonList("PG_VERSION"));
        LOGGER.info("ReadFile: found {}, to find {}", String.join(",", contents).trim(), "9.6");
        Assertions.assertEquals("9.6", String.join(",", contents).trim());
    }
}
