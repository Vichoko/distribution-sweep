package test;

import algorithm.DistributionSweep;
import segment.SegmentGenerator;
import utils.Constants;

import java.io.*;

import static utils.Constants.TOTAL_SEGMENTS;

/**
 * primera prueba
 */
public class first {
    public static void main(String[] args) throws IOException {
        // Variables
        // TODO: Parsing?
        int m_segmentQuantity = TOTAL_SEGMENTS;
        Constants.EDistribution m_segmentDistribution = Constants.EDistribution.NORMAL;
        double m_segmentClassBalance = 0.50;

        SegmentGenerator sg = new SegmentGenerator(m_segmentQuantity,
                m_segmentDistribution, m_segmentClassBalance);
        String originalSegmentsFileName = sg.generateSegments();
        File originalSegmentsFile = new File(originalSegmentsFileName);

        DistributionSweep ds = new DistributionSweep(originalSegmentsFile);
        String outFileName = "t_first#" +   System.currentTimeMillis() + ".txt";
        ds.getIntersections(outFileName);

        // check outFileName for intersections
    }

}
