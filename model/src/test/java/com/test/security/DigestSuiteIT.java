package com.test.security;

import com.jsql.model.InjectionModel;
import com.jsql.model.exception.JSqlException;
import com.jsql.view.terminal.SystemOutTerminal;
import com.test.vendor.mysql.ConcreteMySqlErrorSuiteIT;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junitpioneer.jupiter.RetryingTest;
import spring.security.DigestSecurityConfig;

class DigestSuiteIT extends ConcreteMySqlErrorSuiteIT {

    /**
     * https://en.wikipedia.org/wiki/Digest_access_authentication
     * https://gist.github.com/usamadar/2912088
     * https://stackoverflow.com/questions/2152573/java-client-program-to-send-digest-authentication-request-using-httpclient-api
     * https://stackoverflow.com/questions/73264239/digest-authentication-java-net-http-httpclient/74903645#74903645
     * &lt; Set-Cookie: JSESSIONID=D7F3C20D780A2FD2552EA30990E891DC; Path=/; HttpOnly
     * &lt; WWW-Authenticate: Digest realm="Digest Realm", qop="auth", nonce="MTY5MDA4NjM0MDUxMzozZTljM2M5NjU1NTgzZWQwMDI2OGVhNjc4ZTNhNTgxZA=="
     * HA1 = MD5(username:realm:password)
     * HA2 = MD5(method:digestURI)
     * response = MD5(HA1:nonce:nonceCount:cnonce:qop:HA2)
     * > Authorization: Digest username="login-digest",realm="Digest Realm",nonce="MTY5MDA4NjM0MDUxMzozZTljM2M5NjU1NTgzZWQwMDI2OGVhNjc4ZTNhNTgxZA==",uri="/digest?tenant=mysql&amp;name=",cnonce="2ecb0e39da79fcb5aa6ffb1bd45cb3bb",nc=00000001,response="e5703ca7433d944525a40f49c5501155",qop="auth"
     */

    @Override
    public void setupInjection() throws Exception {
        InjectionModel model = new InjectionModel();
        this.injectionModel = model;

        model.subscribe(new SystemOutTerminal());

        model.getMediatorUtils().getParameterUtil().initQueryString(
            "http://localhost:8080/digest?tenant=mysql-error&name="
        );

        model
        .getMediatorUtils()
        .getPreferencesUtil()
        .withIsStrategyBlindBitDisabled(true)
        .withIsStrategyBlindBinDisabled(true)
        .withIsStrategyTimeDisabled(true);

        model.setIsScanning(true);

        model
        .getMediatorUtils()
        .getAuthenticationUtil()
        .set(
            false,
            DigestSecurityConfig.DIGEST_USERNAME,
            DigestSecurityConfig.DIGEST_PASSWORD,
            false,
            StringUtils.EMPTY,
            StringUtils.EMPTY
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

    @AfterEach
    void afterEach() {
        Assertions.assertEquals(
            this.injectionModel.getMediatorStrategy().getError(),
            this.injectionModel.getMediatorStrategy().getStrategy()
        );
    }

    @AfterAll
    @Order(Order.DEFAULT)
    synchronized void assertResult() {
        Assertions.assertTrue(DigestSecurityConfig.FILTER.count > 0);
        LOGGER.info("DigestSecurityConfig.filter.count: {}", DigestSecurityConfig.FILTER.count);
    }
}
