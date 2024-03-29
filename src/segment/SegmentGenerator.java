package segment;

import segment.writer.SegmentWriter;
import utils.random.IRandom;
import utils.random.NormalRandom;
import utils.random.UniformRandom;

import java.io.IOException;

import static utils.Constants.*;

/**
 * Segmentos almacenados como tupla (x1,y1, x2,y2)
 *
 * Debe ser capaz de generar segmentos:
 * Cantidades: N = {2^9, ..., 2^21}
 * Distribucion de la coordenada x de los segmentos verticales: Uniforme, normal.
 * Distribucion resto de cooredenadas: Uniforme
 * Balance entre cantidad de segmentos verticales vs horizontales:
 *  aN horizontales, (1-a)N Verticales, a en {0.25, 0.5, 0.75}
 */



public class SegmentGenerator {
    int n;
    EDistribution distribution;
    double a;

    /***
     *
     * @param n             Cantidad de segmentos a generar
     * @param distribution  distribución De X en segmentos verticales
     *                      {EDistribution.UNIFORM, EDistribution.NORMAL}
     * @param a             Balance de segmentos verticales vs horizontales
     */
    public SegmentGenerator(int n, EDistribution distribution, double a){
        this.n = n;
        this.distribution = distribution;
        this.a = a;
    }

    /***
     * Creates the vertical and horizontal segments
     * And uses segment.writer to write them in a file
     */
    public String generateSegments() throws IOException {
        String filename = n+"_a="+a+"_";
        if (distribution==EDistribution.NORMAL) filename += "norm_";
        else filename += "unif_";
        filename += Long.toString(System.currentTimeMillis());
        SegmentWriter dispatcher = new SegmentWriter(filename);
        IRandom uniformXRand = new UniformRandom(X_MAX);
        IRandom uniformYRand = new UniformRandom(Y_MAX);
        IRandom normalRand = new NormalRandom(NORMAL_MEAN, NORMAL_DEVIATION);
        double x1, y1, x2, y2;

        if (distribution == EDistribution.UNIFORM){
            // Generar N segmentos con distribucion de coordenadas uniformes. Balance dependiendo de a.
            //Genero los verticales
            for (int i = 0; i < a*n ; i++){
                x1 = uniformXRand.getRandom();
                y1 = uniformYRand.getRandom();

                x2 = x1;
                y2 = uniformYRand.getRandom();
                dispatcher.saveSegment(x1, y1, x2, y2);
            }
            // Generar Horizontales
            for (int i = 0; i < (1-a)*n; i++){
                x1 = uniformXRand.getRandom();
                y1 = uniformYRand.getRandom();

                x2 = uniformXRand.getRandom();
                y2 = y1;
                dispatcher.saveSegment(x1, y1, x2, y2);
            }

        } else {
            // Generar N segmentos con distribucion de coordenadas uniformes. Excepto la coordenada x de los verticales debe tener normal. Balance dependiendo de a.
            //Genero los verticales
            for (int i = 0; i < a*n ; i++){
                x1 = normalRand.getRandom();
                y1 = uniformYRand.getRandom();

                x2 = x1;
                y2 = uniformYRand.getRandom();
                dispatcher.saveSegment(x1, y1, x2, y2);
            }
            // Generar Horizontales
            for (int i = 0; i < (1-a)*n; i++){
                x1 = uniformXRand.getRandom();
                y1 = uniformYRand.getRandom();

                x2 = uniformXRand.getRandom();
                y2 = y1;
                dispatcher.saveSegment(x1, y1, x2, y2);
            }
        }
        dispatcher.close();
        return filename;
    }

    public static void main(String[] args) throws IOException {
        int exp = Integer.parseInt(args[0]);
        TOTAL_SEGMENTS = (int) Math.pow(2, exp);
        double alpha = Double.parseDouble(args[2]);
        SegmentGenerator generator;
        if(args[1].equals("Normal")) generator = new SegmentGenerator(TOTAL_SEGMENTS,
                                                                      EDistribution.NORMAL,
                                                                      alpha);
        else generator = new SegmentGenerator(TOTAL_SEGMENTS, EDistribution.UNIFORM, alpha);
        String filename = generator.generateSegments();
        System.out.println(exp+" "+filename);
    }
}
