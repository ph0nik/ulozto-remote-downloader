package util;

public class BytesCounter {

    private final int byteInterval;
    private long counter = 0;
    private long startTimeMark;
    private long currentBytesPerSecond = 0;
//    private double prevBytesPerSecond;

    public BytesCounter(int byteInterval) {
        this.byteInterval = byteInterval;
    }

    public void setMark() {
        if (counter == 0) {
            startTimeMark = System.currentTimeMillis();
            counter++;
        } else if (counter == byteInterval) {
            calculateBitrate(System.currentTimeMillis());
            counter = 0;
        } else {
            counter++;
        }
    }

    private void calculateBitrate(long endTimeMark) {
        long tempTime = endTimeMark - startTimeMark;
        long tempRate = (tempTime > 0)
                ? Math.round((byteInterval * 1000d) / tempTime)
                : 0;
        currentBytesPerSecond = (currentBytesPerSecond != 0)
                ? Math.round((tempRate + currentBytesPerSecond) / 2d)
                : tempRate;

//        currentBytesPerSecond = (prevBytesPerSecond != 0)
//                ? (tempRate + prevBytesPerSecond) / 2
//                : tempRate;
    }

    public long getBitrate() {
        return currentBytesPerSecond;
    }

}
