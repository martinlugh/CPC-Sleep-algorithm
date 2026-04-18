package com.hrv.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hrv.core.RRi;
import com.hrv.exception.EmptyFileException;

/** RRi 文件读取工具。 */
public final class HrvIO {
    private HrvIO() {
    }

    public static RRi readFromText(String pathname) throws IOException {
        String content = Files.readString(Path.of(pathname), StandardCharsets.UTF_8);
        if (content.isEmpty()) throw new EmptyFileException("empty file!");

        Matcher m = Pattern.compile("\\d\\.?[0-9]+")
                .matcher(content);
        List<Double> vals = new ArrayList<>();
        while (m.find()) vals.add(Double.parseDouble(m.group()));
        return new RRi(vals.stream().mapToDouble(Double::doubleValue).toArray());
    }

    public static RRi readFromHrm(String pathname) throws IOException {
        String content = Files.readString(Path.of(pathname), StandardCharsets.UTF_8);
        int idx = content.indexOf("[HRData]");
        if (idx < 0) throw new EmptyFileException("empty file!");

        Matcher m = Pattern.compile("\\d+").matcher(content.substring(idx));
        List<Double> vals = new ArrayList<>();
        while (m.find()) vals.add(Double.parseDouble(m.group()));
        if (vals.isEmpty()) throw new EmptyFileException("empty file!");
        return new RRi(vals.stream().mapToDouble(Double::doubleValue).toArray());
    }

    public static RRi readFromCsv(
            String pathname,
            int rriColIndex,
            Integer timeColIndex,
            int rowOffset,
            String sep
    ) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(pathname), StandardCharsets.UTF_8);
        if (sep == null) {
            sep = lines.get(0).contains(";") ? ";" : ",";
        }

        List<Double> rri = new ArrayList<>();
        List<Double> time = new ArrayList<>();
        for (int i = rowOffset; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;
            String[] cols = lines.get(i).split(Pattern.quote(sep));
            rri.add(Double.parseDouble(cols[rriColIndex].trim()));
            if (timeColIndex != null) time.add(Double.parseDouble(cols[timeColIndex].trim()));
        }

        double[] rv = rri.stream().mapToDouble(Double::doubleValue).toArray();
        if (timeColIndex == null) return new RRi(rv);
        return new RRi(rv, time.stream().mapToDouble(Double::doubleValue).toArray());
    }
}
