package com.hrv.sampledata;

import java.io.IOException;
import java.nio.file.Path;

import com.hrv.core.RRi;
import com.hrv.io.HrvIO;

/** 样例数据加载器。 */
public final class SampleDataLoader {
    private SampleDataLoader() {
    }

    public static RRi loadSampleData(String filename) throws IOException {
        String base = "hrv/sampledata";
        Path path = Path.of(base, filename);
        if (filename.endsWith(".txt")) {
            return HrvIO.readFromText(path.toString());
        }
        if (filename.endsWith(".hrm")) {
            return HrvIO.readFromHrm(path.toString());
        }
        throw new IllegalArgumentException("仅支持 .txt/.hrm");
    }

    public static RRi loadRestRri() throws IOException {
        return loadSampleData("rest_rri.txt");
    }

    public static RRi loadExerciseRri() throws IOException {
        return loadSampleData("exercise_rri.hrm");
    }

    public static RRi loadNoisyRri() throws IOException {
        return loadSampleData("noisy_rri.hrm");
    }
}
