package com.jsql.util;

import com.jsql.model.InjectionModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;

import java.net.http.HttpRequest.Builder;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.stream.Stream;

public class CsrfUtil {
    
    private static final Logger LOGGER = LogManager.getRootLogger();

    private SimpleEntry<String, String> tokenCsrf = null;
    
    private static final String SET_COOKIE_RESPONSE = "set-cookie";
    private static final String INPUT_ATTR_VALUE = "value";
    private static final String MSG_ENABLE_CSRF = "Try with option CSRF processing enabled in preferences";

    private final InjectionModel injectionModel;
    
    public CsrfUtil(InjectionModel injectionModel) {
        this.injectionModel = injectionModel;
    }

    public void parseForCsrfToken(String pageSource, Map<String, String> headers) {
        this.parseCsrfFromCookie(headers);
        this.parseCsrfFromHtml(pageSource);
    }

    private void parseCsrfFromHtml(String pageSource) {
        // Change immutable list to mutable for adding user tag
        List<String> tags = new ArrayList<>(
            Arrays.asList(
                "[name=_csrf]",
                "[name=_token]",
                "[name=csrf-token]",
                "[name=_csrf_header]",
                "[name=csrf_token]",
                "[name=csrfToken]",
                "[name=user_token]",
                "[name=csrfmiddlewaretoken]",
                "[name=form_build_id]"
            )
        );
        
        if (this.injectionModel.getMediatorUtils().getPreferencesUtil().isCsrfUserTag()) {
            tags.add(
                String.format(
                    "[name='%s']",
                    this.injectionModel.getMediatorUtils().getPreferencesUtil().csrfUserTag()
                )
            );
        }
        
        Optional<SimpleEntry<String, String>> optionalTokenCsrf = Jsoup.parse(pageSource)
            .select("input")
            .select(String.join(",", tags))
            .stream()
            .findFirst()
            .map(input ->
                new SimpleEntry<>(
                    input.attr("name"),
                    input.attr(CsrfUtil.INPUT_ATTR_VALUE)
                )
            );
        
        if (optionalTokenCsrf.isPresent()) {
            SimpleEntry<String, String> tokenCsrfFound = optionalTokenCsrf.get();
            LOGGER.log(
                LogLevelUtil.CONSOLE_INFORM,
                "Found Csrf token from HTML body: {}={}",
                tokenCsrfFound::getKey,
                tokenCsrfFound::getValue
            );
            if (
                !this.injectionModel.getMediatorUtils().getPreferencesUtil().isNotProcessingCookies()
                && this.injectionModel.getMediatorUtils().getPreferencesUtil().isProcessingCsrf()
            ) {
                this.tokenCsrf = tokenCsrfFound;
                LOGGER.log(
                    LogLevelUtil.CONSOLE_SUCCESS,
                    "Csrf token added to query and header: {}",
                    tokenCsrfFound::getValue
                );
            } else {
                LOGGER.log(LogLevelUtil.CONSOLE_INFORM, CsrfUtil.MSG_ENABLE_CSRF);
            }
        }
    }

    private void parseCsrfFromCookie(Map<String, String> mapResponse) {
        Optional<SimpleEntry<String, String>> optionalCookieCsrf = Optional.empty();
        
        if (mapResponse.containsKey(CsrfUtil.SET_COOKIE_RESPONSE)) {
            // Spring: Cookie XSRF-TOKEN => Header X-XSRF-TOKEN, GET/POST parameter _csrf
            // Laravel, Zend, Symfony
            String[] cookieValues = StringUtils.split(mapResponse.get(CsrfUtil.SET_COOKIE_RESPONSE), ";");
            optionalCookieCsrf = Stream.of(cookieValues)
                .filter(cookie -> cookie.trim().toLowerCase().startsWith("xsrf-token"))
                .map(cookie -> {
                    String[] cookieEntry = StringUtils.split(cookie, "=");
                    return new SimpleEntry<>(
                        cookieEntry[0].trim(),
                        cookieEntry[1].trim()
                    );
                })
                .findFirst();
        }
        
        if (optionalCookieCsrf.isPresent()) {
            SimpleEntry<String, String> cookieCsrf = optionalCookieCsrf.get();
            LOGGER.log(
                LogLevelUtil.CONSOLE_ERROR,
                "Found CSRF token from Cookie: {}={}",
                cookieCsrf::getKey,
                cookieCsrf::getValue
            );
            SimpleEntry<String, String> headerCsrf = new SimpleEntry<>(
                cookieCsrf.getKey(),
                cookieCsrf.getValue()
            );
            if (
                !this.injectionModel.getMediatorUtils().getPreferencesUtil().isNotProcessingCookies()
                && this.injectionModel.getMediatorUtils().getPreferencesUtil().isProcessingCsrf()
            ) {
                this.tokenCsrf = headerCsrf;
            } else {
                LOGGER.log(LogLevelUtil.CONSOLE_INFORM, CsrfUtil.MSG_ENABLE_CSRF);
            }
        }
    }

    public void addHeaderToken(Builder httpRequest) {
        if (this.tokenCsrf == null) {
             return;
        }

        httpRequest.setHeader("X-XSRF-TOKEN", this.tokenCsrf.getValue());
        httpRequest.setHeader("X-CSRF-TOKEN", this.tokenCsrf.getValue());
        
        if (this.injectionModel.getMediatorUtils().getPreferencesUtil().isCsrfUserTag()) {
            httpRequest.setHeader(
                this.injectionModel.getMediatorUtils().getPreferencesUtil().csrfUserTagOutput(),
                this.tokenCsrf.getValue()
            );
        }
    }

    public void addRequestToken(StringBuilder httpRequest) {
        if (this.tokenCsrf == null) {
            return;
        }
        httpRequest.append(
            String.format(
                "%s=%s&",
                this.tokenCsrf.getKey(),
                this.tokenCsrf.getValue()
            )
        );
        httpRequest.append(
            String.format(
                "_csrf=%s&",
                this.tokenCsrf.getValue()
            )
        );
        if (this.injectionModel.getMediatorUtils().getPreferencesUtil().isCsrfUserTag()) {
            httpRequest.append(
                String.format(
                    "%s=%s&",
                    this.injectionModel.getMediatorUtils().getPreferencesUtil().csrfUserTagOutput(),
                    this.tokenCsrf.getValue()
                )
            );
        }
    }
    
    public String addQueryStringToken(String urlInjection) {
        String urlInjectionFixed = urlInjection;
        if (this.tokenCsrf == null) {
            return urlInjectionFixed;
        }
        urlInjectionFixed += String.format(
            "&%s=%s",
            this.tokenCsrf.getKey(),
            this.tokenCsrf.getValue()
        );
        urlInjectionFixed += String.format(
            "&_csrf=%s",
            this.tokenCsrf.getValue()
        );
        if (this.injectionModel.getMediatorUtils().getPreferencesUtil().isCsrfUserTag()) {
            urlInjectionFixed += String.format(
                "&%s=%s",
                this.injectionModel.getMediatorUtils().getPreferencesUtil().csrfUserTagOutput(),
                this.tokenCsrf.getValue()
            );
        }
        return urlInjectionFixed;
    }
    
    
    // Getter / Setter

    public boolean isCsrf() {
        return this.tokenCsrf != null;
    }

    public SimpleEntry<String, String> getTokenCsrf() {
        return this.tokenCsrf;
    }

    public void setTokenCsrf(SimpleEntry<String, String> tokenCsrf) {
        this.tokenCsrf = tokenCsrf;
    }
}
