package com.yausername.youtubedl_android;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YoutubeDLOptions {

    private final Map<String, List<String>> options = new LinkedHashMap<>();

    public YoutubeDLOptions addOption(@NonNull String option, @NonNull String argument) {
        if (!options.containsKey(option)) {
            List<String> arguments = new ArrayList<>();
            arguments.add(argument);
            options.put(option, arguments);
        } else {
            options.get(option).add(argument);
        }
        return this;
    }

    public YoutubeDLOptions addOption(@NonNull String option, @NonNull Number argument) {
        if (!options.containsKey(option)) {
            List<String> arguments = new ArrayList<>();
            arguments.add(argument.toString());
            options.put(option, arguments);
        } else {
            options.get(option).add(argument.toString());
        }
        return this;
    }

    public YoutubeDLOptions addOption(String option) {
        if (!options.containsKey(option)) {
            List<String> arguments = new ArrayList<>();
            arguments.add("");
            options.put(option, arguments);
        } else {
            options.get(option).add("");
        }
        return this;
    }

    public String getArgument(String option) {
        if (!options.containsKey(option))
            return null;
        String argument = options.get(option).get(0);
        if (argument.isEmpty())
            return null;
        else return argument;
    }

    public List<String> getArguments(String option) {
        if (!options.containsKey(option))
            return null;
        return options.get(option);
    }

    public boolean hasOption(String option) {
        return options.containsKey(option);
    }

    public List<String> buildOptions() {
        List<String> commandList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : options.entrySet()) {
            String option = entry.getKey();
            for (String argument : entry.getValue()) {
                commandList.add(option);
                if (!argument.isEmpty())
                    commandList.add(argument);
            }
        }
        return commandList;
    }

}
