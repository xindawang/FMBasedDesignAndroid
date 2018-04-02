package com.fengmap.indoorPosition.utils;


import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/8/4.
 */

public class JsonTool {

    //json to list
    public static List<Map<String, String>> jsonToListMap(String json) throws JsonParseException, JsonMappingException, IOException {


        final ObjectMapper om = new ObjectMapper();
        return om.readValue(json, new TypeReference<List<Map<String, String>>>() {
        });

    }

    public static String objectToJson(Object o) {
        final ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //list to json String
    public static <T> String listToJson(List<T> list) {
        final OutputStream out = new ByteArrayOutputStream();
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(out, list);
            return out.toString();
        } catch (JsonGenerationException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            return null;
        } catch (JsonMappingException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            return null;
        }
    }




}
