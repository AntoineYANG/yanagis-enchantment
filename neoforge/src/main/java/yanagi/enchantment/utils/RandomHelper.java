package yanagi.enchantment.utils;

import java.util.Random;

public abstract class RandomHelper {

    private static Random random = new Random();

    public static final double clampGaussian(double mean, double sigma, double lo, double hi) {
        // Box-Muller
        double u = Math.max(1e-6, Math.min(0.999999, random.nextDouble()));
        double v = Math.max(1e-6, Math.min(0.999999, random.nextDouble()));
        double z = Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2*Math.PI*v);
        double x = mean + sigma * z;
        return Math.clamp(x, lo, hi);
    }

}
