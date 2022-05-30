package util;

import jip.JIPImage;
import jipfunc.FRGBToYCbCr;

/**
 * Created by IntelliJ IDEA.
 * User: rmartony
 * Date: 21/08/2009
 * Time: 10:09:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class ImageUtil {
    public static final int MAX_INTENSITY = 255;
    private static FRGBToYCbCr convertToYUV = new FRGBToYCbCr();
    private static int mat[][] = {
            {0, 1, 0},
            {1, 1, 1},
            {0, 1, 0}};
    private static int mx = mat[0].length;
    private static int my = mat.length;

    /**
     * substract returns
     * 1 if abs(a-b) > 1
     * abs(a-b) if abs(a-b) <=1
     *
     * @param a
     * @param b
     * @return
     */
    public static float subtract(float a, float b) {
        float abs = Math.abs(a - b);
        return abs > 1.0f ? 1.0f : abs;
    }

    /**
     * Subtraction b to a
     *
     * @param a minuend image
     * @param b substrahend image
     * @return result image from subtraction
     */
    public static JIPImage subtract(JIPImage a, JIPImage b) {
        float[] bmpA = a.getAllPixelFlo(0);
        float[] bmpB = a.getAllPixelFlo(0);
        float[] resultBmp = new float[bmpA.length];

        for (int i = 0; i < bmpA.length; i++) {
            // keeps diff between 0f and 1.0f
            resultBmp[i] = subtract(bmpA[i], bmpB[i]);
        }

        return new JIPImage(a.getWidth(), a.getHeight(), resultBmp);
    }

    public static float[] substract(float[] bmpA, float[] bmpB) {
        float[] resultBmp = new float[bmpA.length];
        for (int i = 0; i < bmpA.length; i++) {
            resultBmp[i] = subtract(bmpA[i], bmpB[i]);
        }
        return resultBmp;
    }

    public static JIPImage substract(int w, int h, float[] bmpA, float[] bmpB) {
        float[] resultBmp = new float[bmpA.length];
        for (int i = 0; i < bmpA.length; i++) {
            resultBmp[i] = subtract(bmpA[i], bmpB[i]);
        }
        return new JIPImage(w, h, resultBmp);
    }

    public static JIPImage substractWithThreshold(int w, int h, float[] bmpA, float[] bmpB, float threshold) {
        float[] resultBmp = new float[bmpA.length];
        for (int i = 0; i < bmpA.length; i++) {
            resultBmp[i] = subtract(bmpA[i], bmpB[i]);
            if (resultBmp[i] < threshold) {
                resultBmp[i] = 0f;
            }
        }
        return new JIPImage(w, h, resultBmp);
    }

    public static void merge(float[] bmpA, float[] bmpB) {
        for (int i = 0; i < bmpA.length; i++) {
            if (bmpB[i] > bmpA[i]) {
                bmpA[i] = bmpB[i];
            }
        }
    }


    /**
     * Transforms values in [-1,1] to [0,1]
     *
     * @param img original image
     * @return transformed image
     */
    public static float[] adjustImage(float[] img) {
        for (int i = 0; i < img.length; i++) {
            img[i] = (img[i] + 1f) / 2f;
        }
        return img;
    }

    public static int[] getAllByteFromReal(final float[] pix) {
        int[] res = new int[pix.length];
        for (int i = 0; i < pix.length; i++) {
            if ((pix[i] >= 0.8f)) {
                res[i] = 255;
            } else {
                res[i] = 0;
            }

            // res[i] = (int) Math.round(pix[i] * 127.0f + 127f);
        }
        return res;
    }

    public static float[] binarize(final float[] img, float threshold) {
        float[] res = new float[img.length];
        for (int i = 0; i < img.length; i++) {
            res[i] = (img[i] + 1f) / 2f;
            if ((img[i] >= threshold)) {
                res[i] = 1f;
            } else {
                res[i] = 0f;
            }

            // res[i] = (int) Math.round(pix[i] * 127.0f + 127f);
        }
        return res;
    }

    /**
     * Binarize an image
     * @param img float image with values [-1, 1]
     * @param threshold threshold value in [-1, 1]
     * @return binary image
     */
    public static boolean[] binarize2(final float[] img, float threshold) {
        boolean[] res = new boolean[img.length];
        for (int i = 0; i < img.length; i++) {
            //res[i] = ((img[i] + 1f) / 2f) >= threshold;
            res[i] = img[i] >= threshold;
        }
        return res;
    }

    public static JIPImage extractYFromYUVImage(JIPImage jipImage) {
        JIPImage yImage = new JIPImage(jipImage.getWidth(), jipImage.getHeight(), jipImage.getAllPixelFlo(0));
        return yImage;
    }

    public static JIPImage convertToYUV(JIPImage jipImage) {
        return convertToYUV.processImg(jipImage);
    }

    public static int max(int n1, int n2) {
        if (n1 < n2)
            return n2;
        else
            return n1;
    }

    public static int min(int n1, int n2) {
        if (n1 > n2)
            return n2;
        else
            return n1;
    }

    public static JIPImage grow(JIPImage img) {

        img = dilateByK(img, 2);
        img = erodeByK(img, 3);
        img = dilateByK(img, 1);

        /*
        img = dilateByK(img, 3);
        img = erodeByK(img, 4);
        img = dilateByK(img, 2);
        */
        return img;
    }

    public static JIPImage dilateByK(JIPImage image, int k) {
        int[] result = manhattan(image, 1);
        int pos;

        int width = image.getWidth();
        int height = image.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pos = i + width * j;
                result[pos] = ((result[pos] <= k) ? 1 : 0);
            }
        }
        return new JIPImage(width, height, image.getType(), result);
    }

    public static JIPImage erodeByK(JIPImage image, int k) {
        int[] result = manhattan(image, 0);
        int pos;

        int width = image.getWidth();
        int height = image.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pos = i + width * j;
                result[pos] = ((result[pos] <= k) ? 0 : 1);
            }
        }
        return new JIPImage(width, height, image.getType(), result);
    }

    // O(n^2) solution to find the Manhattan distance to "on" pixels in a two dimension array
    private static int[] manhattan(JIPImage image, int pixel_on) {
        int width = image.getWidth();
        int height = image.getHeight();
        int pos;

        int[] pixels = image.getAllPixel();

        // traverse from top left to bottom right
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pos = i + width * j;
                if (pixels[pos] == pixel_on) {
                    // first pass and pixel was on, it gets a zero
                    pixels[pos] = 0;
                } else {
                    // pixel was off
                    // It is at most the sum of the lengths of the array
                    // away from a pixel that is on
                    pixels[pos] = width + height;
                    // or one more than the pixel to the north
                    if (i > 0) pixels[pos] = Math.min(pixels[pos], pixels[i - 1 + width * j] + 1);
                    // or one more than the pixel to the west
                    if (j > 0) pixels[pos] = Math.min(pixels[pos], pixels[i + width * (j - 1)] + 1);
                }
            }
        }
        // traverse from bottom right to top left
        for (int i = width - 1; i >= 0; i--) {
            for (int j = height - 1; j >= 0; j--) {
                pos = i + width * j;
                // either what we had on the first pass
                // or one more than the pixel to the south
                if (i + 1 < width) pixels[pos] = Math.min(pixels[pos], pixels[pos + 1] + 1);
                // or one more than the pixel to the east
                if (j + 1 < height) pixels[pos] = Math.min(pixels[pos], pixels[pos + width] + 1);
            }
        }
        return pixels;
    }

}
