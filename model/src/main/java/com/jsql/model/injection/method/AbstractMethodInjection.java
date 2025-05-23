package com.jsql.model.injection.method;

import com.jsql.model.InjectionModel;
import com.jsql.model.exception.JSqlException;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.util.I18nUtil;
import com.jsql.util.JsonUtil;
import com.jsql.util.LogLevelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractMethodInjection implements Serializable {
    
    private static final Logger LOGGER = LogManager.getRootLogger();
    
    protected final InjectionModel injectionModel;
    
    protected AbstractMethodInjection(InjectionModel injectionModel) {
        this.injectionModel = injectionModel;
    }
    
    public abstract boolean isCheckingAllParam();
    public abstract String getParamsAsString();
    public abstract List<SimpleEntry<String, String>> getParams();
    public abstract String name();
    
    public boolean testParameters(boolean hasFoundInjection) throws JSqlException {
        if (!hasFoundInjection) {
            LOGGER.log(
                LogLevelUtil.CONSOLE_DEFAULT,
                "{} {} params...",
                () -> I18nUtil.valueByKey("LOG_CHECKING"),
                () -> this.name().toLowerCase()
            );
            return this.testParameters();
        }
        return true;
    }

    /**
     * Verify if injection works for specific Method using 3 modes: standard (last param), injection point
     * and full params injection. Special injections like JSON and SOAP are checked.
     * @return true if injection didn't fail
     * @throws JSqlException when no params integrity, process stopped by user, or injection failure
     */
    public boolean testParameters() throws JSqlException {
        var hasFoundInjection = false;
        
        // Injects URL, Request or Header params only if user tests every params
        // or method is selected by user.
        if (
            !this.injectionModel.getMediatorUtils().getPreferencesUtil().isCheckingAllParam()
            && this.injectionModel.getMediatorUtils().getConnectionUtil().getMethodInjection() != this
        ) {
            return false;
        }
        
        // Force injection method of model to current running method
        this.injectionModel.getMediatorUtils().getConnectionUtil().setMethodInjection(this);
        
        // Injection by injection point in params or in path
        if (
            this.getParamsAsString().contains(InjectionModel.STAR)
            || this.injectionModel.getMediatorUtils().getConnectionUtil().getUrlBase().contains(InjectionModel.STAR)
        ) {
            hasFoundInjection = this.checkParamWithStar();
        } else if (!this.isCheckingAllParam()) {
            hasFoundInjection = this.checkLastParam();
        } else {
            hasFoundInjection = this.checkAllParams();
        }
        return hasFoundInjection;
    }

    private boolean checkParamWithStar() throws JSqlException {
        SimpleEntry<String, String> parameterToInject = this.getParams().stream()
            .filter(entry -> entry.getValue().contains("*"))
            .findFirst()
            .orElse(null);
        return this.injectionModel.getMediatorStrategy().testStrategies(parameterToInject);
    }

    /**
     *  Default injection: last param tested only
     */
    private boolean checkLastParam() throws JSqlException {
        // Will check param value by user.
        // Notice options 'Inject each URL params' and 'inject JSON' must be checked both
        // for JSON injection of last param
        SimpleEntry<String, String> parameterToInject = this.getParams().stream()
            .reduce((a, b) -> b)
            .orElseThrow(() -> new JSqlException("Missing last parameter"));
        return this.injectionModel.getMediatorStrategy().testStrategies(parameterToInject);
    }

    /**
     * Injection of every params: isCheckingAllParam() == true.
     * Params are tested one by one in two loops:
     * - inner loop erases * from previous param
     * - outer loop adds * to current param
     */
    private boolean checkAllParams() throws StoppedByUserSlidingException {
        // This param will be marked by * if injection is found,
        // inner loop will erase mark * otherwise
        for (SimpleEntry<String, String> paramBase: this.getParams()) {
            // This param is the current tested one.
            // For JSON value attributes are traversed one by one to test every value.
            // For standard value mark * is simply added to the end of its value.
            for (SimpleEntry<String, String> paramStar: this.getParams()) {
                if (paramStar == paramBase) {
                    try {
                        if (this.isParamInjectable(paramStar)) {
                            return true;
                        }
                    } catch (JSONException e) {
                        LOGGER.log(LogLevelUtil.CONSOLE_JAVA, e, e);
                    }
                }
            }
        }
        return false;
    }

    private boolean isParamInjectable(SimpleEntry<String, String> paramStar) throws StoppedByUserSlidingException {
        boolean hasFoundInjection;
        
        // Will test if current value is a JSON entity
        Object jsonEntity = JsonUtil.getJson(paramStar.getValue());
        
        // Define a tree of JSON attributes with path as the key: root.a => value of a
        List<SimpleEntry<String, String>> attributesJson = JsonUtil.createEntries(jsonEntity, "root", null);
        
        // When option 'Inject JSON' is selected and there's a JSON entity to inject
        // then loop through each path to add * at the end of value and test each strategy.
        // Marks * are erased between each test.
        if (!attributesJson.isEmpty() && this.injectionModel.getMediatorUtils().getPreferencesUtil().isCheckingAllJsonParam()) {
            hasFoundInjection = this.injectionModel.getMediatorUtils().getJsonUtil().testJsonParam(this, paramStar);
        } else {
            hasFoundInjection = this.testJsonlessParam(paramStar);  // Standard non JSON injection
        }
        return hasFoundInjection;
    }
    
    public boolean testJsonlessParam(SimpleEntry<String, String> paramStar) throws StoppedByUserSlidingException {
        var hasFoundInjection = false;

        paramStar.setValue(paramStar.getValue() + InjectionModel.STAR);
        
        try {
            LOGGER.log(
                LogLevelUtil.CONSOLE_INFORM,
                "{} {} parameter {}={}",
                () -> I18nUtil.valueByKey("LOG_CHECKING"),
                this::name,
                paramStar::getKey,
                () -> paramStar.getValue().replace(InjectionModel.STAR, StringUtils.EMPTY)
            );
            
            // Test current standard value marked with * for injection
            // Keep original param
            hasFoundInjection = this.injectionModel.getMediatorStrategy().testStrategies(paramStar);
            
        } catch (StoppedByUserSlidingException e) { // Break all params processing in upper methods
            throw e;
        } catch (JSqlException e) {  // Injection failure
            LOGGER.log(
                LogLevelUtil.CONSOLE_ERROR,
                "No {} injection found for parameter {}={} ({})",
                this.name(),
                paramStar.getKey(),
                paramStar.getValue().replaceAll("\\+.?$|\\" + InjectionModel.STAR, StringUtils.EMPTY),
                e.getMessage()
            );
        } finally {
            if (!hasFoundInjection) {  // Erase * from JSON if failure
                
                // Erase * at the end of each params
                this.getParams().forEach(e ->
                    e.setValue(
                        e.getValue().replaceAll(Pattern.quote(InjectionModel.STAR) +"$", StringUtils.EMPTY)
                    )
                );
                
                // TODO It erases STAR from value => * can't be used in parameter
                paramStar.setValue(paramStar.getValue().replace(InjectionModel.STAR, StringUtils.EMPTY));
            }
        }
        return hasFoundInjection;
    }
}