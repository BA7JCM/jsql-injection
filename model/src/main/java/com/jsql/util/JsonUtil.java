package com.jsql.util;

import com.jsql.model.InjectionModel;
import com.jsql.model.exception.JSqlException;
import com.jsql.model.injection.method.AbstractMethodInjection;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class JsonUtil {
    
    private static final Logger LOGGER = LogManager.getRootLogger();

    private final InjectionModel injectionModel;
    
    public JsonUtil(InjectionModel injectionModel) {
        this.injectionModel = injectionModel;
    }

    public static Object getJson(String param) {
        Object jsonEntity;  // Will test if current value is a JSON entity
        try {
            jsonEntity = new JSONObject(param);  // Test for JSON Object
        } catch (JSONException exceptionJSONObject) {
            try {
                jsonEntity = new JSONArray(param);  // Test for JSON Array
            } catch (JSONException exceptionJSONArray) {
                jsonEntity = new Object();  // Not a JSON entity
            }
        }
        return jsonEntity;
    }

    public static List<SimpleEntry<String, String>> createEntries(Object jsonEntity, String parentName, SimpleEntry<String, String> parentXPath) {
        List<SimpleEntry<String, String>> attributesXPath = new ArrayList<>();
        if (jsonEntity instanceof JSONObject) {
            JsonUtil.scanJsonObject(jsonEntity, parentName, parentXPath, attributesXPath);
        } else if (jsonEntity instanceof JSONArray) {
            JsonUtil.scanJsonArray(jsonEntity, parentName, parentXPath, attributesXPath);
        }
        return attributesXPath;
    }

    private static void scanJsonArray(Object jsonEntity, String parentName, SimpleEntry<String, String> parentXPath, List<SimpleEntry<String, String>> attributesXPath) {
        var jsonArrayEntity = (JSONArray) jsonEntity;
        for (var i = 0 ; i < jsonArrayEntity.length() ; i++) {
            Object value = jsonArrayEntity.get(i);
            String xpath = parentName +"["+ i +"]";
            
            // Not possible to make generic with scanJsonObject() because of JSONArray.put(int) != JSONObject.put(String)
            if (value instanceof JSONArray || value instanceof JSONObject) {
                attributesXPath.addAll(JsonUtil.createEntries(value, xpath, parentXPath));
            } else if (value instanceof String) {
                SimpleEntry<String, String> stringValue = new SimpleEntry<>(xpath, (String) value);
                attributesXPath.add(stringValue);
                
                if (parentXPath == null) {
                    jsonArrayEntity.put(i, value.toString().replaceAll(Pattern.quote(InjectionModel.STAR) +"$", StringUtils.EMPTY));
                } else if (stringValue.equals(parentXPath)) {
                    jsonArrayEntity.put(i, value + InjectionModel.STAR);
                }
            }
        }
    }

    private static void scanJsonObject(Object jsonEntity, String parentName, SimpleEntry<String, String> parentXPath, List<SimpleEntry<String, String>> attributesXPath) {
        var jsonObjectEntity = (JSONObject) jsonEntity;
        Iterator<?> keys = jsonObjectEntity.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            var value = jsonObjectEntity.get(key);
            String xpath = parentName +"."+ key;
            
            // Not possible to make generic with scanJsonObject() because of JSONArray.put(int) != JSONObject.put(String)
            if (value instanceof JSONArray || value instanceof JSONObject) {
                attributesXPath.addAll(JsonUtil.createEntries(value, xpath, parentXPath));
            } else if (value instanceof String) {
                
                SimpleEntry<String, String> stringValue = new SimpleEntry<>(xpath, (String) value);
                attributesXPath.add(stringValue);
                
                if (parentXPath == null) {
                    jsonObjectEntity.put(key, value.toString().replaceAll(Pattern.quote(InjectionModel.STAR) +"$", StringUtils.EMPTY));
                } else if (stringValue.equals(parentXPath)) {
                    jsonObjectEntity.put(key, value + InjectionModel.STAR);
                }
            }
        }
    }
    
    public boolean testJsonParam(AbstractMethodInjection methodInjection, SimpleEntry<String, String> paramStar) {
        var hasFoundInjection = false;
        
        // Remove STAR at the end of parameter, STAR will be added inside json data instead
        paramStar.setValue(paramStar.getValue().replace(InjectionModel.STAR, StringUtils.EMPTY));
        
        // Will test if current value is a JSON entity
        Object jsonEntity = JsonUtil.getJson(paramStar.getValue());
        
        // Define a tree of JSON attributes with path as the key: root.a => value of a
        List<SimpleEntry<String, String>> attributesJson = JsonUtil.createEntries(jsonEntity, "root", null);
        
        // Loop through each JSON values
        for (SimpleEntry<String, String> parentXPath: attributesJson) {
            JsonUtil.createEntries(jsonEntity, "root", null);  // Erase previously defined *
            JsonUtil.createEntries(jsonEntity, "root", parentXPath);  // Add * to current parameter's value

            paramStar.setValue(jsonEntity.toString());  // Replace param value by marked one
            
            try {
                LOGGER.log(
                    LogLevelUtil.CONSOLE_INFORM,
                    "{} JSON {}={} with {}",
                    () -> I18nUtil.valueByKey("LOG_CHECKING"),
                    parentXPath::getKey,
                    () -> parentXPath.getValue().replace(InjectionModel.STAR, StringUtils.EMPTY),
                    methodInjection::name
                );
                
                // Test current JSON value marked with * for injection
                // Keep original param
                hasFoundInjection = this.injectionModel.getMediatorStrategy().testStrategies(paramStar);
                
                // Injection successful
                break;
            } catch (JSqlException e) {
                // Injection failure
                LOGGER.log(
                    LogLevelUtil.CONSOLE_ERROR,
                    String.format(
                        "No injection found for JSON %s parameter %s=%s",
                        methodInjection.name(),
                        parentXPath.getKey(),
                        parentXPath.getValue().replace(InjectionModel.STAR, StringUtils.EMPTY)
                    )
                );
            } finally {
                // Erase * at the end of each params
                // TODO useless
                methodInjection.getParams()
                    .forEach(e -> e.setValue(
                        e.getValue().replaceAll(Pattern.quote(InjectionModel.STAR) +"$", StringUtils.EMPTY)
                    ));
                
                // Erase * from JSON if failure
                if (!hasFoundInjection) {
                    paramStar.setValue(
                        paramStar.getValue().replace(InjectionModel.STAR, StringUtils.EMPTY)
                    );
                }
            }
        }
        return hasFoundInjection;
    }
}
