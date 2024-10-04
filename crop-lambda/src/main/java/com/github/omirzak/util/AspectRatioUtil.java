package com.github.omirzak.util;

public final class AspectRatioUtil {
    private AspectRatioUtil() {
    }

    /**
     * Calculates the aspect ratio for given width and height using LCM.
     *
     * @param width  The width in pixels
     * @param height The height in pixels
     * @return A string representation of the aspect ratio in the format "width:height"
     */
    public static double[] calculateAspectRatio(int width, int height) {
        int gcd = gcd(width, height);
        return new double[]{(double) width / gcd, (double) height / gcd};
    }

    /**
     * Calculates the greatest common divisor (GCD) of two numbers using the Euclidean algorithm.
     *
     * @param a First number
     * @param b Second number
     * @return The greatest common divisor of a and b
     */
    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
