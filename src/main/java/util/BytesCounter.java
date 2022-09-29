package util;

public class BytesCounter {

    private static final int BYTE_INTERVAL = 1024;
    private long counter;
    private long startTimeMark;

    private double currentBytesPerSecond;
    private double prevBytesPerSecond;

    public BytesCounter() {
        counter = 0;
        currentBytesPerSecond = 0;
    }

    private void resetCounter() {
        counter = 0;
    }

    public void setMark() {
        if (counter == 0) {
            startTimeMark = System.currentTimeMillis();
            counter++;
        } else if (counter == BYTE_INTERVAL) {
            long endTimeMark = System.currentTimeMillis();
            calculateBitrate(endTimeMark);
            resetCounter();
        } else {
            counter++;
        }
    }

    private void calculateBitrate(long endTimeMark) {
        long tempTime = endTimeMark - startTimeMark;
        long tempRate = (tempTime > 0) ? Math.round((BYTE_INTERVAL * 1000d) / tempTime) : 0;
        currentBytesPerSecond = (prevBytesPerSecond != 0) ? (tempRate + prevBytesPerSecond) / 2 : tempRate;
    }

    public double getBitrate() {
        return currentBytesPerSecond;
    }

}
