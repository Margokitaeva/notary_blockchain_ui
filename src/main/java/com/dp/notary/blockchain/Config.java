package com.dp.notary.blockchain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Config {
    private JsonNode config;

    private static Config instance = new Config();
    public void loadConfig() throws IOException {
        InputStream inputStream = App.class.getResourceAsStream("/config.json");
        if (inputStream == null) {
            throw new FileNotFoundException();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        config = objectMapper.readTree(inputStream);
    }

    public String getString(String key) {
        return config.get(key).asText();
    }

    public boolean getBoolean(String key) {
        return config.get(key).asBoolean();
    }
    public static Config getInstance(){
        return instance;
    }
}

