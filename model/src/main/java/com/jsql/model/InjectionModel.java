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
package com.jsql.model;

import com.jsql.model.accessible.DataAccess;
import com.jsql.model.accessible.ResourceAccess;
import com.jsql.model.bean.util.Header;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.JSqlException;
import com.jsql.model.exception.JSqlRuntimeException;
import com.jsql.model.injection.method.AbstractMethodInjection;
import com.jsql.model.injection.method.MediatorMethod;
import com.jsql.model.injection.strategy.MediatorStrategy;
import com.jsql.model.injection.strategy.blind.callable.AbstractCallableBit;
import com.jsql.model.injection.vendor.MediatorVendor;
import com.jsql.model.injection.vendor.model.VendorYaml;
import com.jsql.util.*;
import com.jsql.util.GitUtil.ShowOnConsole;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Model class of MVC pattern for processing SQL injection automatically.<br>
 * Different views can be attached to this observable, like Swing or command line, in order to separate
 * the functional job from the graphical processing.<br>
 * The Model has a specific database vendor and strategy which run an automatic injection to get name of
 * databases, tables, columns and values, and it can also retrieve resources like files and shell.<br>
 * Tasks are run in multi-threads in general to speed the process.
 */
public class InjectionModel extends AbstractModelObservable implements Serializable {
    
    private static final Logger LOGGER = LogManager.getRootLogger();
    
    private final transient MediatorVendor mediatorVendor = new MediatorVendor(this);
    private final transient MediatorMethod mediatorMethod = new MediatorMethod(this);
    private final transient MediatorUtils mediatorUtils;
    private final transient MediatorStrategy mediatorStrategy;
    private final transient PropertiesUtil propertiesUtil = new PropertiesUtil();
    private final transient DataAccess dataAccess = new DataAccess(this);
    private final transient ResourceAccess resourceAccess = new ResourceAccess(this);
    
    public static final String STAR = "*";
    public static final String BR = "<br>&#10;";

    /**
     * initialUrl transformed to a correct injection url.
     */
    private String indexesInUrl = StringUtils.EMPTY;
    private String analysisReport = StringUtils.EMPTY;

    /**
     * Allow to directly start an injection after a failed one
     * without asking the user 'Start a new injection?'.
     */
    private boolean shouldErasePreviousInjection = false;
    private boolean isScanning = false;

    public InjectionModel() {
        this.mediatorStrategy = new MediatorStrategy(this);
        this.mediatorUtils = new MediatorUtils();
        this.mediatorUtils.setCertificateUtil(new CertificateUtil());
        this.mediatorUtils.setPropertiesUtil(this.propertiesUtil);
        this.mediatorUtils.setConnectionUtil(new ConnectionUtil(this));
        this.mediatorUtils.setAuthenticationUtil(new AuthenticationUtil());
        this.mediatorUtils.setGitUtil(new GitUtil(this));
        this.mediatorUtils.setHeaderUtil(new HeaderUtil(this));
        this.mediatorUtils.setParameterUtil(new ParameterUtil(this));
        this.mediatorUtils.setExceptionUtil(new ExceptionUtil(this));
        this.mediatorUtils.setSoapUtil(new SoapUtil(this));
        this.mediatorUtils.setMultipartUtil(new MultipartUtil(this));
        this.mediatorUtils.setCookiesUtil(new CookiesUtil(this));
        this.mediatorUtils.setJsonUtil(new JsonUtil(this));
        this.mediatorUtils.setPreferencesUtil(new PreferencesUtil());
        this.mediatorUtils.setProxyUtil(new ProxyUtil());
        this.mediatorUtils.setThreadUtil(new ThreadUtil(this));
        this.mediatorUtils.setTamperingUtil(new TamperingUtil());
        this.mediatorUtils.setUserAgentUtil(new UserAgentUtil());
        this.mediatorUtils.setCsrfUtil(new CsrfUtil(this));
        this.mediatorUtils.setFormUtil(new FormUtil(this));
        this.mediatorUtils.setDigestUtil(new DigestUtil(this));
    }

    /**
     * Reset each injection attributes: Database metadata, General Thread status, Strategy.
     */
    public void resetModel() {
        this.mediatorStrategy.getSpecificUnion().setVisibleIndex(null);
        
        this.mediatorStrategy.getUnion().setApplicable(false);
        this.mediatorStrategy.getError().setApplicable(false);
        this.mediatorStrategy.getBlindBit().setApplicable(false);
        this.mediatorStrategy.getBlindBin().setApplicable(false);
        this.mediatorStrategy.getMultibit().setApplicable(false);
        this.mediatorStrategy.getTime().setApplicable(false);
        this.mediatorStrategy.getStack().setApplicable(false);
        this.mediatorStrategy.setStrategy(null);

        this.indexesInUrl = StringUtils.EMPTY;
        this.analysisReport = StringUtils.EMPTY;
        this.isStoppedByUser = false;
        this.shouldErasePreviousInjection = false;

        this.mediatorUtils.getCsrfUtil().setTokenCsrf(null);
        this.mediatorUtils.getDigestUtil().setTokenDigest(null);
        this.mediatorUtils.getThreadUtil().reset();
    }

    /**
     * Prepare the injection process, can be interrupted by the user (via shouldStopAll).
     * Erase all attributes eventually defined in a previous injection.
     * Run by Scan, Standard and TU.
     */
    public void beginInjection() {
        this.resetModel();
        try {
            if (this.mediatorUtils.getProxyUtil().isNotLive(ShowOnConsole.YES)) {
                return;
            }
            LOGGER.log(
                LogLevelUtil.CONSOLE_INFORM,
                "{}: {}",
                () -> I18nUtil.valueByKey("LOG_START_INJECTION"),
                () -> this.mediatorUtils.getConnectionUtil().getUrlByUser()
            );
            
            // Check general integrity if user's parameters
            this.mediatorUtils.getParameterUtil().checkParametersFormat();
            this.mediatorUtils.getConnectionUtil().testConnection();

            // TODO Check all path params
            boolean hasFoundInjection = this.mediatorMethod.getQuery().testParameters(false);
            hasFoundInjection = this.mediatorUtils.getMultipartUtil().testParameters(hasFoundInjection);
            hasFoundInjection = this.mediatorUtils.getSoapUtil().testParameters(hasFoundInjection);
            hasFoundInjection = this.mediatorMethod.getRequest().testParameters(hasFoundInjection);
            hasFoundInjection = this.mediatorMethod.getHeader().testParameters(hasFoundInjection);
            hasFoundInjection = this.mediatorUtils.getCookiesUtil().testParameters(hasFoundInjection);

            if (hasFoundInjection && !this.isScanning) {
                if (!this.getMediatorUtils().getPreferencesUtil().isNotShowingVulnReport()) {
                    var requestSetVendor = new Request();
                    requestSetVendor.setMessage(Interaction.CREATE_ANALYSIS_REPORT);
                    requestSetVendor.setParameters(this.analysisReport);
                    this.sendToViews(requestSetVendor);
                }
                if (this.getMediatorUtils().getPreferencesUtil().isZipStrategy()) {
                    LOGGER.log(LogLevelUtil.CONSOLE_INFORM, "Using Zip mode for reduced query size");
                } else if (this.getMediatorUtils().getPreferencesUtil().isDiosStrategy()) {
                    LOGGER.log(LogLevelUtil.CONSOLE_INFORM, "Using Dump In One Shot strategy for single query dump");
                }
                if (!this.mediatorUtils.getPreferencesUtil().isNotInjectingMetadata()) {
                    this.dataAccess.getDatabaseInfos();
                }
                this.dataAccess.listDatabases();
            }
            
            LOGGER.log(LogLevelUtil.CONSOLE_DEFAULT, () -> I18nUtil.valueByKey("LOG_DONE"));
            
            this.shouldErasePreviousInjection = true;
        } catch (InterruptedException e) {
            LOGGER.log(LogLevelUtil.IGNORE, e, e);
            Thread.currentThread().interrupt();
        } catch (JSqlRuntimeException | JSqlException | IOException e) {  // Catch expected exceptions only
            if (e.getMessage() == null) {
                LOGGER.log(LogLevelUtil.CONSOLE_ERROR, "Interruption: {}", InjectionModel.getImplicitReason(e));
            } else {
                LOGGER.log(LogLevelUtil.CONSOLE_ERROR, "Interruption: {}", e.getMessage());
            }
        } finally {
            var request = new Request();
            request.setMessage(Interaction.END_PREPARATION);
            this.sendToViews(request);
        }
    }
    
    public static String getImplicitReason(Throwable e) {
        String message = e.getClass().getSimpleName();
        if (e.getMessage() != null) {
            message += ": "+ e.getMessage();
        }
        if (e.getCause() != null && !e.equals(e.getCause())) {
            message += " > "+ InjectionModel.getImplicitReason(e.getCause());
        }
        return message;
    }
    
    /**
     * Run an HTTP connection to the web server.
     * @param dataInjection SQL query
     * @return source code of current page
     */
    @Override
    public String inject(
        String dataInjection,
        boolean isUsingIndex,
        String metadataInjectionProcess,
        AbstractCallableBit<?> callableBoolean,
        boolean isReport
    ) {
        // Temporary url, we go from "select 1,2,3,4..." to "select 1,([complex query]),2...", but keep initial url
        String urlInjection = this.mediatorUtils.getConnectionUtil().getUrlBase();
        urlInjection = this.mediatorStrategy.buildPath(urlInjection, isUsingIndex, dataInjection);
        urlInjection = StringUtil.cleanSql(urlInjection.trim());

        URL urlObject;
        try {  // TODO Keep only a single check
            urlObject = new URI(urlInjection).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.log(LogLevelUtil.CONSOLE_ERROR, String.format("Incorrect Query Url: %s", e.getMessage()));
            return StringUtils.EMPTY;
        }

        Map<Header, Object> msgHeader = new EnumMap<>(Header.class);
        urlObject = this.initQueryString(  // TODO useless as urlInjection == urlObject
            isUsingIndex,
            urlInjection,
            dataInjection,
            urlObject,
            msgHeader
        );
        
        String pageSource = StringUtils.EMPTY;
        
        // Define the connection
        try {
            var httpRequestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(urlObject.toString()))
                .setHeader(HeaderUtil.CONTENT_TYPE_REQUEST, "text/plain")
                .timeout(Duration.ofSeconds(15));
            
            this.mediatorUtils.getCsrfUtil().addHeaderToken(httpRequestBuilder);
            this.mediatorUtils.getDigestUtil().addHeaderToken(httpRequestBuilder);
            this.mediatorUtils.getConnectionUtil().setCustomUserAgent(httpRequestBuilder);

            String body = this.initRequest(isUsingIndex, dataInjection, httpRequestBuilder, msgHeader);
            this.initHeader(isUsingIndex, dataInjection, httpRequestBuilder);
            
            var httpRequest = httpRequestBuilder.build();

            if (isReport) {
                Color colorReport = UIManager.getColor("TextArea.inactiveForeground");
                String report = InjectionModel.BR + StringUtil.formatReport(colorReport, "Method: ") + httpRequest.method();
                report += InjectionModel.BR + StringUtil.formatReport(colorReport, "Path: ") + httpRequest.uri().getPath();
                if (httpRequest.uri().getQuery() != null) {
                    report += InjectionModel.BR + StringUtil.formatReport(colorReport, "Query: ") + httpRequest.uri().getQuery();
                }
                if (
                    !(this.mediatorUtils.getParameterUtil().getListRequest().isEmpty()
                    && this.mediatorUtils.getCsrfUtil().getTokenCsrf() == null)
                ) {
                    report += InjectionModel.BR + StringUtil.formatReport(colorReport, "Body: ") + body;
                }
                report += InjectionModel.BR + StringUtil.formatReport(colorReport, "Header: ") + httpRequest.headers().map().entrySet().stream()
                    .map(entry -> String.format("%s: %s", entry.getKey(), String.join(StringUtils.EMPTY, entry.getValue())))
                    .collect(Collectors.joining(InjectionModel.BR));
                return report;
            }
            
            HttpResponse<String> response = this.getMediatorUtils().getConnectionUtil().getHttpClient().build().send(
                httpRequestBuilder.build(),
                BodyHandlers.ofString()
            );
            if (this.mediatorUtils.getParameterUtil().isRequestSoap()) {
                // Invalid XML control chars like \x04 requires urlencoding from server
                pageSource = URLDecoder.decode(response.body(), StandardCharsets.UTF_8);
            } else {
                pageSource = response.body();
            }

            Map<String, String> headersResponse = ConnectionUtil.getHeadersMap(response);
            msgHeader.put(Header.RESPONSE, headersResponse);
            msgHeader.put(Header.HEADER, ConnectionUtil.getHeadersMap(httpRequest.headers()));
            
            int sizeHeaders = headersResponse.keySet()
                .stream()
                .map(key -> headersResponse.get(key).length() + key.length())
                .mapToInt(Integer::intValue)
                .sum();
            float size = (float) (pageSource.length() + sizeHeaders) / 1024;
            var decimalFormat = new DecimalFormat("0.000");
            msgHeader.put(Header.PAGE_SIZE, decimalFormat.format(size));
            
            if (this.mediatorUtils.getParameterUtil().isRequestSoap()) {
                pageSource = StringUtil.fromHtml(pageSource);
            }
            
            msgHeader.put(
                Header.SOURCE,
                pageSource
                .replaceAll("("+ VendorYaml.CALIBRATOR_SQL +"){60,}", "$1...")  // Remove ranges of # created by calibration
                .replaceAll("(jIyM){60,}", "$1...")  // Remove batch of chars created by Dios
            );
            msgHeader.put(Header.METADATA_PROCESS, metadataInjectionProcess);
            msgHeader.put(Header.METADATA_STRATEGY, this.mediatorStrategy.getMeta());
            msgHeader.put(Header.METADATA_BOOLEAN, callableBoolean);
            
            // Send data to Views
            var request = new Request();
            request.setMessage(Interaction.MESSAGE_HEADER);
            request.setParameters(msgHeader);
            this.sendToViews(request);
        } catch (IOException e) {
            LOGGER.log(
                LogLevelUtil.CONSOLE_ERROR,
                String.format("Error during connection: %s", e.getMessage())
            );
        } catch (InterruptedException e) {
            LOGGER.log(LogLevelUtil.IGNORE, e, e);
            Thread.currentThread().interrupt();
        }

        // return the source code of the page
        return pageSource;
    }

    private URL initQueryString(
        boolean isUsingIndex,
        String urlInjection,
        String dataInjection,
        URL urlObject,
        Map<Header, Object> msgHeader
    ) {
        String urlInjectionFixed = urlInjection;
        var urlObjectFixed = urlObject;
        if (
            this.mediatorUtils.getParameterUtil().getListQueryString().isEmpty()
            && !this.mediatorUtils.getPreferencesUtil().isProcessingCsrf()
        ) {
            msgHeader.put(Header.URL, urlInjectionFixed);
            return urlObjectFixed;
        }
            
        // URL without query string like Request and Header can receive
        // new params from <form> parsing, in that case add the '?' to URL
        if (!urlInjectionFixed.contains("?")) {
            urlInjectionFixed += "?";
        }
        urlInjectionFixed += this.buildQuery(
            this.mediatorMethod.getQuery(),
            this.mediatorUtils.getParameterUtil().getQueryStringFromEntries(),
            isUsingIndex,
            dataInjection
        );
        urlInjectionFixed = this.mediatorUtils.getCsrfUtil().addQueryStringToken(urlInjectionFixed);
        
        // TODO Keep single check
        try {
            urlObjectFixed = new URI(urlInjectionFixed).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.log(
                LogLevelUtil.CONSOLE_ERROR,
                String.format("Incorrect Url: %s", e.getMessage())
            );
        }

        msgHeader.put(Header.URL, urlInjectionFixed);
        return urlObjectFixed;
    }

    private void initHeader(
        boolean isUsingIndex,
        String dataInjection,
        Builder httpRequest
    ) {
        if (!this.mediatorUtils.getParameterUtil().getListHeader().isEmpty()) {
            Stream.of(
                this.buildQuery(
                    this.mediatorMethod.getHeader(),
                    this.mediatorUtils.getParameterUtil().getHeaderFromEntries(),
                    isUsingIndex,
                    dataInjection
                )
                .split("\\\\r\\\\n")
            )
            .forEach(header -> {
                if (header.split(":").length == 2) {
                    try {  // TODO Should not catch, rethrow or use runtime exception
                        HeaderUtil.sanitizeHeaders(
                            httpRequest,
                            new SimpleEntry<>(
                                header.split(":")[0],
                                header.split(":")[1]
                            )
                        );
                    } catch (JSqlException e) {
                        LOGGER.log(LogLevelUtil.CONSOLE_ERROR, "Headers sanitizing issue caught already during connection, ignoring", e);
                    }
                }
            });
        }
    }

    private String initRequest(
        boolean isUsingIndex,
        String dataInjection,
        Builder httpRequest,
        Map<Header, Object> msgHeader
    ) {
        if (
            this.mediatorUtils.getParameterUtil().getListRequest().isEmpty()
            && this.mediatorUtils.getCsrfUtil().getTokenCsrf() == null
        ) {
            return dataInjection;
        }
            
        // Set connection method
        // Active for query string injection too, in that case inject query string still with altered method
        
        if (this.mediatorUtils.getParameterUtil().isRequestSoap()) {
            httpRequest.setHeader(HeaderUtil.CONTENT_TYPE_REQUEST, "text/xml");
        } else {
            httpRequest.setHeader(HeaderUtil.CONTENT_TYPE_REQUEST, "application/x-www-form-urlencoded");
        }

        var body = new StringBuilder();
        this.mediatorUtils.getCsrfUtil().addRequestToken(body);
            
        if (this.mediatorUtils.getConnectionUtil().getTypeRequest().matches("PUT|POST")) {
            if (this.mediatorUtils.getParameterUtil().isRequestSoap()) {
                body.append(
                    this.buildQuery(
                        this.mediatorMethod.getRequest(),
                        this.mediatorUtils.getParameterUtil().getRawRequest(),
                        isUsingIndex,
                        dataInjection
                    )
                    // Invalid XML characters in recent Spring version
                    // Server needs to urldecode, or stop using out of range chars
                    .replace("\u0001", "&#01;")
                    .replace("\u0003", "&#03;")
                    .replace("\u0004", "&#04;")
                    .replace("\u0005", "&#05;")
                    .replace("\u0006", "&#06;")
                    .replace("\u0007", "&#07;")
                    .replace("+", "%2B")  // Prevent replace '+' into 'space' on server side urldecode
                );
            } else {
                body.append(
                    this.buildQuery(
                        this.mediatorMethod.getRequest(),
                        this.mediatorUtils.getParameterUtil().getRequestFromEntries(),
                        isUsingIndex,
                        dataInjection
                    )
                );
            }
        }
        
        var bodyPublisher = BodyPublishers.ofString(body.toString());
        httpRequest.method(
            this.mediatorUtils.getConnectionUtil().getTypeRequest(),
            bodyPublisher
        );
        
        msgHeader.put(Header.POST, body.toString());
        return body.toString();
    }
    
    private String buildQuery(AbstractMethodInjection methodInjection, String paramLead, boolean isUsingIndex, String sqlTrail) {
        String query;
        String paramLeadFixed = paramLead.replace(
            InjectionModel.STAR,
            TamperingUtil.TAG_OPENED + InjectionModel.STAR + TamperingUtil.TAG_CLOSED
        );
        if (
            // No parameter transformation if method is not selected by user
            this.mediatorUtils.getConnectionUtil().getMethodInjection() != methodInjection
            // No parameter transformation if injection point in URL
            || this.mediatorUtils.getConnectionUtil().getUrlBase().contains(InjectionModel.STAR)
        ) {
            // Just pass parameters without any transformation
            query = paramLeadFixed;
        } else if (
            // If method is selected by user and URL does not contain injection point
            // but parameters contain an injection point
            // then replace injection point by SQL expression in this parameter
            paramLeadFixed.contains(InjectionModel.STAR)
        ) {
            query = this.initStarInjection(paramLeadFixed, isUsingIndex, sqlTrail);
        } else {
            query = this.initRawInjection(paramLeadFixed, isUsingIndex, sqlTrail);
        }
        // Remove comments except empty /**/
        query = this.cleanQuery(methodInjection, query);
        // Add empty comments with space=>/**/
        if (this.mediatorUtils.getConnectionUtil().getMethodInjection() == methodInjection) {
            query = this.mediatorUtils.getTamperingUtil().tamper(query);
        }
        return this.applyEncoding(methodInjection, query);
    }

    private String initRawInjection(String paramLead, boolean isUsingIndex, String sqlTrail) {
        String query;
        // Method is selected by user and there's no injection point
        if (!isUsingIndex) {
            // Several SQL expressions does not use indexes in SELECT,
            // like Boolean, Error, Shell and search for character insertion,
            // in that case concat SQL expression to the end of param.
            query = paramLead + sqlTrail;
        } else {
            // Concat indexes found for Union strategy to params
            // and use visible Index for injection
            query = paramLead + this.indexesInUrl.replaceAll(
                String.format(VendorYaml.FORMAT_INDEX, this.mediatorStrategy.getSpecificUnion().getVisibleIndex()),
                // Oracle column often contains $, which is reserved for regex.
                // => need to be escape with quoteReplacement()
                Matcher.quoteReplacement(sqlTrail)
            );
        }
        // Add ending line comment by vendor
        return query + this.mediatorVendor.getVendor().instance().endingComment();
    }

    private String initStarInjection(String paramLead, boolean isUsingIndex, String sqlTrail) {
        String query;
        // Several SQL expressions does not use indexes in SELECT,
        // like Boolean, Error, Shell and search for character insertion,
        // in that case replace injection point by SQL expression.
        // Injection point is always at the end?
        if (!isUsingIndex) {
            query = paramLead.replace(
                InjectionModel.STAR,
                sqlTrail + this.mediatorVendor.getVendor().instance().endingComment()
            );
        } else {
            // Replace injection point by indexes found for Union strategy
            // and use visible Index for injection
            query = paramLead.replace(
                InjectionModel.STAR,
                this.indexesInUrl.replace(
                    String.format(VendorYaml.FORMAT_INDEX, this.mediatorStrategy.getSpecificUnion().getVisibleIndex()),
                    sqlTrail
                )
                + this.mediatorVendor.getVendor().instance().endingComment()
            );
        }
        return query;
    }

    /**
     * Dependency:
     * - Tamper space=>comment
     */
    private String cleanQuery(AbstractMethodInjection methodInjection, String query) {
        String queryFixed = query;
        if (
            methodInjection == this.mediatorMethod.getRequest()
            && (
                this.mediatorUtils.getParameterUtil().isRequestSoap()
                || this.mediatorUtils.getParameterUtil().isMultipartRequest()
            )
        ) {
            queryFixed = StringUtil.removeSqlComment(queryFixed)
                .replace("+", " ")
                .replace("%2b", "+")  // Failsafe
                .replace("%23", "#");  // End comment
            if (this.mediatorUtils.getParameterUtil().isMultipartRequest()) {
                // restore linefeed from textfield
                queryFixed = queryFixed.replaceAll("(?s)\\\\n", "\r\n");
            }
        } else {
            queryFixed = StringUtil.cleanSql(queryFixed);
        }
        return queryFixed;
    }

    private String applyEncoding(AbstractMethodInjection methodInjection, String query) {
        String queryFixed = query;
        if (!this.mediatorUtils.getParameterUtil().isRequestSoap()) {
            if (methodInjection == this.mediatorMethod.getQuery()) {
                // URL encode each character because no query parameter context
                if (!this.mediatorUtils.getPreferencesUtil().isUrlEncodingDisabled()) {
                    queryFixed = queryFixed.replace("'", "%27");
                    queryFixed = queryFixed.replace("(", "%28");
                    queryFixed = queryFixed.replace(")", "%29");
                    queryFixed = queryFixed.replace("{", "%7b");
                    queryFixed = queryFixed.replace("[", "%5b");
                    queryFixed = queryFixed.replace("]", "%5d");
                    queryFixed = queryFixed.replace("}", "%7d");
                    queryFixed = queryFixed.replace(">", "%3e");
                    queryFixed = queryFixed.replace("<", "%3c");
                    queryFixed = queryFixed.replace("?", "%3f");
                    queryFixed = queryFixed.replace("_", "%5f");
                    queryFixed = queryFixed.replace(",", "%2c");
                }
                // HTTP forbidden characters
                queryFixed = queryFixed.replace(StringUtils.SPACE, "+");
                queryFixed = queryFixed.replace("`", "%60");  // from `${database}`.`${table}`
                queryFixed = queryFixed.replace("\"", "%22");
                queryFixed = queryFixed.replace("|", "%7c");
                queryFixed = queryFixed.replace("\\", "%5c");
            } else if (methodInjection != this.mediatorMethod.getRequest()) {
                // For cookies in Spring (confirmed, covered by integration tests)
                queryFixed = queryFixed.replace("+", "%20");
                queryFixed = queryFixed.replace(",", "%2c");
                try {  // fix #95709: IllegalArgumentException on decode()
                    queryFixed = URLDecoder.decode(queryFixed, StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    LOGGER.log(LogLevelUtil.CONSOLE_ERROR, "Incorrect values in [{}], please check the parameters", methodInjection.name());
                    throw new JSqlRuntimeException(e);
                }
            }
        }
        return queryFixed;
    }
    
    /**
     * Display source code in console.
     * @param message Error message
     * @param source Text to display in console
     */
    public void sendResponseFromSite(String message, String source) {
        LOGGER.log(LogLevelUtil.CONSOLE_ERROR, "{}, response from site:", message);
        LOGGER.log(LogLevelUtil.CONSOLE_ERROR, ">>>{}", source);
    }
    
    
    // Getters and setters

    public String getIndexesInUrl() {
        return this.indexesInUrl;
    }

    public void setIndexesInUrl(String indexesInUrl) {
        this.indexesInUrl = indexesInUrl;
    }

    public boolean shouldErasePreviousInjection() {
        return this.shouldErasePreviousInjection;
    }

    public void setIsScanning(boolean isScanning) {
        this.isScanning = isScanning;
    }

    public PropertiesUtil getPropertiesUtil() {
        return this.propertiesUtil;
    }

    public MediatorUtils getMediatorUtils() {
        return this.mediatorUtils;
    }

    public MediatorVendor getMediatorVendor() {
        return this.mediatorVendor;
    }

    public MediatorMethod getMediatorMethod() {
        return this.mediatorMethod;
    }

    public DataAccess getDataAccess() {
        return this.dataAccess;
    }

    public ResourceAccess getResourceAccess() {
        return this.resourceAccess;
    }

    public MediatorStrategy getMediatorStrategy() {
        return this.mediatorStrategy;
    }

    public void appendAnalysisReport(String analysisReport) {
        this.appendAnalysisReport(analysisReport, false);
    }

    public void appendAnalysisReport(String analysisReport, boolean isInit) {
        this.analysisReport += (isInit ? StringUtils.EMPTY : "<br>&#10;<br>&#10;") + analysisReport;
    }
}
