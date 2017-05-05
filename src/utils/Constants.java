package utils;

/**
 * Created by vicen on 01-05-2017.
 */
public final class Constants {

    public enum EDistribution{
        UNIFORM, NORMAL
    }

    // segment.Segment Generator
    public static final int Y_MAX = 100;
    public static final int X_MAX = 100;
    public static final double NORMAL_MEAN = X_MAX/2;
    public static final double NORMAL_DEVIATION = X_MAX/7;

    // Algorithms
    public enum EAxis{
        X,Y}

	// Bytes of RAM
    public static final int M = 1024;
    // Bytes of page
    public static final int B = 128;

    // Total segments
    public static final int TOTAL_SEGMENTS = (int) Math.pow(2,9);
}
