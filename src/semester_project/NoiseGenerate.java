package semester_project;

import java.util.Random;

/**
 *
 * @author Victor, Tai Ji Chen
 * GROUP: 404 Bug Not Found
 * CS4450 Spring 2026
 * 
 */

public class NoiseGenerate {

    public static final int CHUNK_SIZE = 30;

    private final int largestFeature;
    private final double persistence;
    private final int seed;

    private final SimplexNoiseOctave[] octaves;
    private final double[] frequencies;
    private final double[] amplitudes;

    public NoiseGenerate(int largestFeature, double persistence, int seed) {
        this.largestFeature = largestFeature;
        this.persistence = persistence;
        this.seed = seed;

        int numberOfOctaves = (int) Math.ceil(Math.log(largestFeature) / Math.log(2));

        octaves = new SimplexNoiseOctave[numberOfOctaves];
        frequencies = new double[numberOfOctaves];
        amplitudes = new double[numberOfOctaves];

        Random random = new Random(seed);

        for (int i = 0; i < numberOfOctaves; i++) {
            octaves[i] = new SimplexNoiseOctave(random.nextInt());
            frequencies[i] = Math.pow(2, i);
            amplitudes[i] = Math.pow(persistence, numberOfOctaves - i);
        }
    }

    public double getNoise(double x, double z) {
        double result = 0.0;

        for (int i = 0; i < octaves.length; i++) {
            double frequency = frequencies[i];
            double amplitude = amplitudes[i];
            result += octaves[i].noise(x / frequency, z / frequency) * amplitude;
        }

        return result;
    }

    public int generateHeight(int worldX, int worldZ, int minY, int maxY) {
        double noiseValue = getNoise(worldX, worldZ);

        double normalized = (noiseValue + 1.0) / 2.0;

        if (normalized < 0) normalized = 0;
        if (normalized > 1) normalized = 1;

        return minY + (int) (normalized * (maxY - minY));
    }

    public int[][] generateChunkHeights(int startX, int startZ, int minY, int maxY) {
        int[][] heights = new int[CHUNK_SIZE][CHUNK_SIZE];

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;
                heights[x][z] = generateHeight(worldX, worldZ, minY, maxY);
            }
        }

        return heights;
    }

    private static class SimplexNoiseOctave {
        private static final int[][] GRAD3 = {
            {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
            {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
            {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}
        };

        private final short[] perm = new short[512];

        private static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
        private static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;

        public SimplexNoiseOctave(int seed) {
            short[] p = new short[256];
            for (short i = 0; i < 256; i++) {
                p[i] = i;
            }

            Random rand = new Random(seed);
            for (int i = 255; i >= 0; i--) {
                int index = rand.nextInt(i + 1);
                short temp = p[i];
                p[i] = p[index];
                p[index] = temp;
            }

            for (int i = 0; i < 512; i++) {
                perm[i] = p[i & 255];
            }
        }

        private static int fastFloor(double x) {
            return x > 0 ? (int) x : (int) x - 1;
        }

        private static double dot(int[] g, double x, double y) {
            return g[0] * x + g[1] * y;
        }

        public double noise(double xin, double yin) {
            double n0, n1, n2;

            double s = (xin + yin) * F2;
            int i = fastFloor(xin + s);
            int j = fastFloor(yin + s);

            double t = (i + j) * G2;
            double X0 = i - t;
            double Y0 = j - t;
            double x0 = xin - X0;
            double y0 = yin - Y0;

            int i1, j1;
            if (x0 > y0) {
                i1 = 1;
                j1 = 0;
            } else {
                i1 = 0;
                j1 = 1;
            }

            double x1 = x0 - i1 + G2;
            double y1 = y0 - j1 + G2;
            double x2 = x0 - 1.0 + 2.0 * G2;
            double y2 = y0 - 1.0 + 2.0 * G2;

            int ii = i & 255;
            int jj = j & 255;
            int gi0 = perm[ii + perm[jj]] % 12;
            int gi1 = perm[ii + i1 + perm[jj + j1]] % 12;
            int gi2 = perm[ii + 1 + perm[jj + 1]] % 12;

            double t0 = 0.5 - x0 * x0 - y0 * y0;
            if (t0 < 0) {
                n0 = 0.0;
            } else {
                t0 *= t0;
                n0 = t0 * t0 * dot(GRAD3[gi0], x0, y0);
            }

            double t1 = 0.5 - x1 * x1 - y1 * y1;
            if (t1 < 0) {
                n1 = 0.0;
            } else {
                t1 *= t1;
                n1 = t1 * t1 * dot(GRAD3[gi1], x1, y1);
            }

            double t2 = 0.5 - x2 * x2 - y2 * y2;
            if (t2 < 0) {
                n2 = 0.0;
            } else {
                t2 *= t2;
                n2 = t2 * t2 * dot(GRAD3[gi2], x2, y2);
            }

            return 70.0 * (n0 + n1 + n2);
        }
    }
}