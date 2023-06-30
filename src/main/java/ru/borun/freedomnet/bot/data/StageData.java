package ru.borun.freedomnet.bot.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StageData {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Choice {
        private String display;
        private String to;
    }

    private String text;
    @JsonProperty(value = "parse_mode")
    private String parseMode;
    private String next;
    private String fail;
    private boolean through;
    private List<Choice> chooses;
}
