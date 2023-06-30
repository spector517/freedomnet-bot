package ru.borun.freedomnet.bot.stages.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ru.borun.freedomnet.bot.data.StageData;

import java.io.IOException;
import java.util.HashMap;

public class StageFactory {

    public static final String INIT_STAGE_NAME = "start";

    public static final String STAGES_RES_PATH = "/stages.yaml";
    public static final HashMap<String, StageData> STAGES_DATA_MAP;

    static {
        try (var stageRes = StageFactory.class.getResourceAsStream(STAGES_RES_PATH)) {
            var stagesTypeRef = new TypeReference<HashMap<String, StageData>>(){};
            STAGES_DATA_MAP = new ObjectMapper(new YAMLFactory()).readValue(stageRes, stagesTypeRef);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
