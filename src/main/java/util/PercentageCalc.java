package util;

import model.DownloadElement;

public class PercentageCalc {

    public static double getPercentageProgress(DownloadElement downloadElement) {
        if (downloadElement.getDataTotalSize() >= downloadElement.getDataOffset()) {
            double v = (downloadElement.getDataOffset() * 100d) / downloadElement.getDataTotalSize();
            return Math.round(v * 100.0) / 100d;
        }
        return 0;
    }
}
