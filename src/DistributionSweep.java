import segment.Segment;
import segment.segment_dispatcher.SegmentDispatcher;
import segment.segment_dispatcher.SegmentDispatcherPermanent;
import segment.segment_dispatcher.SegmentDispatcherTemporary;
import sortalgorithm.MergeSort;
import utils.Slab;
import utils.UtilsIOSegments;

import static utils.Constants.*;

import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;

import static java.lang.System.exit;

/**
 * Created by constanzafierro on 07-05-17.
 */
public class DistributionSweep {
    private RandomAccessFile accessFile;
    private File inputFile;
    private SegmentDispatcher answerFile;
    private ArrayList<Segment> answerList;

    public DistributionSweep(File inputFile) {
        this.inputFile = inputFile;
    }
    public void getIntersections(String filename){
        answerFile = new SegmentDispatcherPermanent(filename);
        answerList = new ArrayList<>();
        String xSortFilename = new MergeSort(EAxis.X, inputFile).sort();
        String ySortFilename = new MergeSort(EAxis.Y, inputFile).sort();
        try {
            recursiveDistributionSweep(xSortFilename, 0, (int)inputFile.length(), ySortFilename);
        } catch (IOException e) {
            System.err.println("DistributionSweep:: Error reading file");
            System.err.println(e.toString());
            exit(-2);
        }
        answerFile.close();
    }

    private void recursiveDistributionSweep(String xSortedFilename, int beginOffset, int endOffset,
                                            String ySortedFilename) throws IOException {
        // it fits in RAM
        if(endOffset-beginOffset < M){
            // TODO
        }
        else{
            // Obs. max bytes offset 2^21 * 10*4 (each point is a double, 8 bytes + comas and points = 10 bytes)
            // => 8*10^7, it fit in an integer
            Slab[] slabs = generateSlabs(xSortedFilename, beginOffset, endOffset);
            ArrayDeque<Segment>[] activeVerticals = new ArrayDeque[slabs.length];
            RandomAccessFile[] activeVerticalFile = new RandomAccessFile[slabs.length];
            SegmentDispatcher[] yHorizontalFiles = new SegmentDispatcher[slabs.length];
            boolean[] horizontalNotComplete = new boolean[slabs.length];
            for (int i = 0; i < slabs.length; i++) {
                activeVerticals[i] = new ArrayDeque<>();
                activeVerticalFile[i] = new RandomAccessFile(new File("activeVertical_"+i+".txt"), "rw");
                yHorizontalFiles[i] = new SegmentDispatcherTemporary("yHorizontal_"+i);
                horizontalNotComplete[i] = false;
            }
            RandomAccessFile ySegmentsSorted = new RandomAccessFile(ySortedFilename, "r");
            int offsetY = 0;
            // sweep line
            while (true){
                UtilsIOSegments.ArrayBytesRead page = UtilsIOSegments.readPage(ySegmentsSorted, offsetY);
                ArrayList<Segment> segments = page.segments;
                offsetY += page.bytesRead;
                if (segments.size()==0) break;
                for (Segment segment: segments){
                    if (segment.isVertical()) {
                        int index = getIndexSlab(segment.x1, slabs);
                        addToActiveVerticals(activeVerticals[index], activeVerticalFile[index], segment);
                    }
                    // horizontal
                    else{
                        // search first and last slab where the segment is complete
                        int[] index = getIndexSegment(segment, slabs, yHorizontalFiles, horizontalNotComplete);
                        if (index != null)
                            writeIntersections(activeVerticals, activeVerticalFile, index[0], index[1]);
                    }
                }
            }
            // recursive call
            for (int i = 0; i < slabs.length; i++)
                yHorizontalFiles[i].close();
            for (int i = 0; i < slabs.length; i++) {
                if (horizontalNotComplete[i])
                    recursiveDistributionSweep(xSortedFilename, slabs[i].initialOffset, slabs[i].finalOffset,
                        yHorizontalFiles[i].getPathname());
            }
        }
    }

    /**
     * Reads all the active verticals (list and file) between these slabs,
     * updates the active list and writes the intersections in the answer file
     * @param activeVerticals       List with active verticals
     * @param activeVerticalFile    File with active verticals
     * @param slab_i                first slab where to check intersections
     * @param slab_j                last slab where to check intersections
     */
    private void writeIntersections(ArrayDeque<Segment>[] activeVerticals, RandomAccessFile[] activeVerticalFile,
                                    int slab_i, int slab_j) {
    }

    /**
     * Adds the segment to the active vertical list, if its full it writes in the file
     * @param activeVertical
     * @param randomAccessFile
     * @param segment
     */
    private void addToActiveVerticals(ArrayDeque<Segment> activeVertical, RandomAccessFile randomAccessFile,
                                      Segment segment) {
        // TODO:
    }

    /**
     * Returns slabs index where the segment is complete
     *
     * @param segment               Segment to place
     * @param slabs                 List of Slab objects
     * @param yHorizontalFiles      Where save the segments that are not complete
     * @param horizontalNotComplete Where set to true the slabs that have segments not complete
     * @return                      first and last index where the segment is complete, if none then null
     */
    private int[] getIndexSegment(Segment segment, Slab[] slabs, SegmentDispatcher[] yHorizontalFiles,
                                  boolean[] horizontalNotComplete) {
        double i = Math.min(segment.x1, segment.x2);
        double j = Math.max(segment.x1, segment.x2);
        int slab_i = getIndexSlab(i, slabs);
        int slab_j = getIndexSlab(j, slabs);
        if (slab_i == slab_j && !(i==slabs[slab_i].initX && j==slabs[slab_j].finalX)){
            yHorizontalFiles[slab_i].saveSegment(segment);
            horizontalNotComplete[slab_i] = true;
            return null;
        }
        if (i > slabs[slab_i].initX && i < slabs[slab_i].finalX){
            yHorizontalFiles[slab_i].saveSegment(segment);
            horizontalNotComplete[slab_i] = true;
            slab_i++;
        }
        if (j > slabs[slab_j].initX && j < slabs[slab_j].finalX){
            yHorizontalFiles[slab_j].saveSegment(segment);
            horizontalNotComplete[slab_j] = true;
            slab_j--;
        }
        return new int[]{slab_i, slab_j};
    }

    /**
     * Search the slab where the x coordinate is placed
     *
     * @param i     x coordinate to be searched
     * @param slabs List of Slab objects
     * @return      The index or -1 if it's not between these slabs
     */
    private int getIndexSlab(double i, Slab[] slabs) {
        for (int j = 0; j < slabs.length; j++) {
            if (i >= slabs[j].initX && i < slabs[j].finalX)
                return j;
        }
        return -1;
    }

    /**
     * Calculates offset (initial and final) for each slab in the file
     * Generates slabs with equal amount of vertical segments
     *
     * @param xSortedFilename   File with segments
     * @param beginOffset       Place in the file to start slabs
     * @param endOffset         Place in the file to end slabs
     * @return                  Array with slabs, each one with initial offset in 0 and final offset in 1
     * @throws IOException      Error reading the file
     */
    private Slab[] generateSlabs(String xSortedFilename, int beginOffset, int endOffset) throws IOException {
        // TODO:
        return null;
    }

    /**
     * Counts the lines in a file
     * @param filename  Name of the file
     * @param start     Place in the file where start counting
     * @param end       Place in the file where stop counting
     * @return          Total count of lines in the file
     * @throws IOException  When reading the file
     */
    private int countLines(String filename, int start, int end) throws IOException {
        RandomAccessFile file = new RandomAccessFile(filename, "r");
        file.seek(start);
        int linesCount = 0;
        boolean empty = true;
        int totalRead = 0;
        int toReadBytes = end-start;
        while (totalRead < toReadBytes){
            byte[] buffer = new byte[M];
            file.read(buffer);
            for (int i = 0; i < M && totalRead < toReadBytes; i++) {
                empty = false;
                byte b = buffer[i];
                if (b == 0) break; // end of file
                totalRead++;
                if((char)b == '\n') linesCount++;
            }
        }
        file.close();
        return (linesCount == 0 && !empty) ? 1 : linesCount;
    }
}
