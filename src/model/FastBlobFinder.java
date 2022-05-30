package model;// Blob Finder Demo
// A.Greensted
// http://www.labbookpages.co.uk
// Please use however you like. I'd be happy to hear any feedback or comments.

import jip.JIPImage;

import java.util.ArrayList;
import java.util.List;

public class FastBlobFinder {
    private short[][] COLOUR_ARRAY = {
            {(short) 255, 0, 0},
            {0, (short) 255, 0},
            {0, 0, (short) 255},
            {0, (short) 255, (short) 255},
            {(short) 255, 0, (short) 255},
            {(short) 255, (short) 255, 0},
            {(short) 255, (short) 255, (short) 255},
            {(short) 103, (short) 121, (short) 255},
            {(short) 249, (short) 255, (short) 139},
            {(short) 140, (short) 255, (short) 127},
            {(short) 167, (short) 254, (short) 255},
            {(short) 255, (short) 111, (short) 71}};

    private int width;
    private int height;

    private int[] labelBuffer;

    private int[] labelTable;
    private int[] xMinTable;
    private int[] xMaxTable;
    private int[] yMinTable;
    private int[] yMaxTable;
    private int[] massTable;

    public static class Blob {
        public int xMin;
        public int xMax;
        public int yMin;
        public int yMax;
        public int mass;

        public Blob(int xMin, int xMax, int yMin, int yMax, int mass) {
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
            this.mass = mass;
        }

        public String toString() {
            return String.format("X: %4d -> %4d, Y: %4d -> %4d, mass: %6d", xMin, xMax, yMin, yMax, mass);
        }
    }

    public FastBlobFinder(int width, int height) {
        this.width = width;
        this.height = height;

        labelBuffer = new int[width * height];

        // The maximum number of blobs is given by an image filled with equally spaced single pixel
        // blobs. For images with less blobs, memory will be wasted, but this approach is simpler and
        // probably quicker than dynamically resizing arrays
        int tableSize = width * height / 4;

        labelTable = new int[tableSize];
        xMinTable = new int[tableSize];
        xMaxTable = new int[tableSize];
        yMinTable = new int[tableSize];
        yMaxTable = new int[tableSize];
        massTable = new int[tableSize];
    }

    //public List<Blob> detectBlobs(boolean[] srcData, JIPImage dstdata, int minBlobMass, int maxBlobMass, boolean matchVal, List<Blob> blobList) {
    public List<Blob> detectBlobs(boolean[] srcData, int minBlobMass, int maxBlobMass, boolean matchVal) {
        List<Blob> blobList = new ArrayList<Blob>();

        // This is the neighbouring pixel pattern. For position X, A, B, C & D are checked
        // A B C
        // D X

        int srcPtr = 0;
        int aPtr = -width - 1;
        int bPtr = -width;
        int cPtr = -width + 1;
        int dPtr = -1;

        int label = 1;

        //float[] srcData = srcImage.getAllPixelFlo();

        // Iterate through pixels looking for connected regions. Assigning labels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                labelBuffer[srcPtr] = 0;

                // Check if on foreground pixel
                if (srcData[srcPtr] == matchVal) {
                    // Find label for neighbours (0 if out of range)
                    int aLabel = (x > 0 && y > 0) ? labelTable[labelBuffer[aPtr]] : 0;
                    int bLabel = (y > 0) ? labelTable[labelBuffer[bPtr]] : 0;
                    int cLabel = (x < width - 1 && y > 0) ? labelTable[labelBuffer[cPtr]] : 0;
                    int dLabel = (x > 0) ? labelTable[labelBuffer[dPtr]] : 0;

                    // Look for label with least value
                    int min = Integer.MAX_VALUE;
                    if (aLabel != 0 && aLabel < min) min = aLabel;
                    if (bLabel != 0 && bLabel < min) min = bLabel;
                    if (cLabel != 0 && cLabel < min) min = cLabel;
                    if (dLabel != 0 && dLabel < min) min = dLabel;

                    // If no neighbours in foreground
                    if (min == Integer.MAX_VALUE) {
                        labelBuffer[srcPtr] = label;
                        labelTable[label] = label;

                        // Initialise min/max x,y for label
                        yMinTable[label] = y;
                        yMaxTable[label] = y;
                        xMinTable[label] = x;
                        xMaxTable[label] = x;
                        massTable[label] = 1;

                        label++;
                    }

                    // Neighbour found
                    else {
                        // Label pixel with lowest label from neighbours
                        labelBuffer[srcPtr] = min;

                        // Update min/max x,y for label
                        yMaxTable[min] = y;
                        massTable[min]++;
                        if (x < xMinTable[min]) xMinTable[min] = x;
                        if (x > xMaxTable[min]) xMaxTable[min] = x;

                        if (aLabel != 0) labelTable[aLabel] = min;
                        if (bLabel != 0) labelTable[bLabel] = min;
                        if (cLabel != 0) labelTable[cLabel] = min;
                        if (dLabel != 0) labelTable[dLabel] = min;
                    }
                }

                srcPtr++;
                aPtr++;
                bPtr++;
                cPtr++;
                dPtr++;
            }
        }

        // Iterate through labels pushing min/max x,y values towards minimum label
        //if (blobList == null) blobList = new ArrayList<Blob>();

        for (int i = label - 1; i > 0; i--) {
            if (labelTable[i] != i) {
                if (xMaxTable[i] > xMaxTable[labelTable[i]]) xMaxTable[labelTable[i]] = xMaxTable[i];
                if (xMinTable[i] < xMinTable[labelTable[i]]) xMinTable[labelTable[i]] = xMinTable[i];
                if (yMaxTable[i] > yMaxTable[labelTable[i]]) yMaxTable[labelTable[i]] = yMaxTable[i];
                if (yMinTable[i] < yMinTable[labelTable[i]]) yMinTable[labelTable[i]] = yMinTable[i];
                massTable[labelTable[i]] += massTable[i];

                int l = i;
                while (l != labelTable[l]) {
                    l = labelTable[l];
                }
                labelTable[i] = l;
            } else {
                // Ignore blobs that butt against corners
                if (i == labelBuffer[0]) continue;                                    // Top Left
                if (i == labelBuffer[width]) continue;                                // Top Right
                int i1 = width * height;
                if (i == labelBuffer[i1 - width + 1]) continue;    // Bottom Left
                if (i == labelBuffer[i1 - 1]) continue;            // Bottom Right

                if (massTable[i] >= minBlobMass && (massTable[i] <= maxBlobMass || maxBlobMass == -1)) {
                    Blob blob = new Blob(xMinTable[i], xMaxTable[i], yMinTable[i], yMaxTable[i], massTable[i]);
                    blobList.add(blob);
                }
            }
        }

        // take de union of blobs
        //blobList = simplifyBlobs(blobList);

        // If dst buffer provided, fill with coloured blobs
        //if (dstData != null) {
            for (int i = label - 1; i > 0; i--) {
                if (labelTable[i] != i) {
                    int l = i;
                    while (l != labelTable[l]) {
                        l = labelTable[l];
                    }
                    labelTable[i] = l;
                }
            }

            // Renumber labels into sequential numbers, starting with 0
            int newLabel = 0;
            for (int i = 1; i < label; i++) {
                if (labelTable[i] == i) {
                    labelTable[i] = newLabel++;
                } else {
                    labelTable[i] = labelTable[labelTable[i]];
                }
            }

            /*
            srcPtr = 0;
            while (srcPtr < srcData.length) {
                if (srcData[srcPtr] == matchVal) {

                    //dstData.setIndexPixel(0, srcPtr, 0);
                    //dstData.setIndexPixel(1, srcPtr, 255);
                    //dstData.setIndexPixel(2, srcPtr, 0);

                    int c = labelTable[labelBuffer[srcPtr]] % COLOUR_ARRAY.length;
                    dstData.setIndexPixel(0, srcPtr, COLOUR_ARRAY[c][0]);
                    dstData.setIndexPixel(1, srcPtr, COLOUR_ARRAY[c][1]);
                    dstData.setIndexPixel(2, srcPtr, COLOUR_ARRAY[c][2]);

                }// else {
                //    dstData.setIndexPixel(0, srcPtr, 0);
                //    dstData.setIndexPixel(1, srcPtr, 0);
                //    dstData.setIndexPixel(2, srcPtr, 0);
                //}

                srcPtr++;
            }
            */

        //}

        return blobList;

    }

    public List<Blob> simplifyBlobs(List<Blob> blobs) {
        List<Blob> newBlobs = new ArrayList<Blob>(1);
        int xMin = Integer.MAX_VALUE;
        int xMax = 0;

        int yMin = Integer.MAX_VALUE;
        int yMax = 0;

        if (blobs != null && blobs.size() > 0) {
            for (Blob blob : blobs) {
                xMin = Math.min(xMin, blob.xMin);
                xMax = Math.max(xMax, blob.xMax);

                yMin = Math.min(yMin, blob.yMin);
                yMax = Math.max(yMax, blob.yMax);
            }

            newBlobs.add(new Blob(xMin, xMax, yMin, yMax, -1));
        }

        return newBlobs;

    }

    public List<Blob> detectBlobs(float[] srcData, JIPImage dstData, int minBlobMass, int maxBlobMass, float matchVal, List<Blob> blobList) {
        // This is the neighbouring pixel pattern. For position X, A, B, C & D are checked
        // A B C
        // D X

        int srcPtr = 0;
        int aPtr = -width - 1;
        int bPtr = -width;
        int cPtr = -width + 1;
        int dPtr = -1;

        int label = 1;

        //float[] srcData = srcImage.getAllPixelFlo();

        // Iterate through pixels looking for connected regions. Assigning labels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                labelBuffer[srcPtr] = 0;

                // Check if on foreground pixel
                if (srcData[srcPtr] == matchVal) {
                    // Find label for neighbours (0 if out of range)
                    int aLabel = (x > 0 && y > 0) ? labelTable[labelBuffer[aPtr]] : 0;
                    int bLabel = (y > 0) ? labelTable[labelBuffer[bPtr]] : 0;
                    int cLabel = (x < width - 1 && y > 0) ? labelTable[labelBuffer[cPtr]] : 0;
                    int dLabel = (x > 0) ? labelTable[labelBuffer[dPtr]] : 0;

                    // Look for label with least value
                    int min = Integer.MAX_VALUE;
                    if (aLabel != 0 && aLabel < min) min = aLabel;
                    if (bLabel != 0 && bLabel < min) min = bLabel;
                    if (cLabel != 0 && cLabel < min) min = cLabel;
                    if (dLabel != 0 && dLabel < min) min = dLabel;

                    // If no neighbours in foreground
                    if (min == Integer.MAX_VALUE) {
                        labelBuffer[srcPtr] = label;
                        labelTable[label] = label;

                        // Initialise min/max x,y for label
                        yMinTable[label] = y;
                        yMaxTable[label] = y;
                        xMinTable[label] = x;
                        xMaxTable[label] = x;
                        massTable[label] = 1;

                        label++;
                    }

                    // Neighbour found
                    else {
                        // Label pixel with lowest label from neighbours
                        labelBuffer[srcPtr] = min;

                        // Update min/max x,y for label
                        yMaxTable[min] = y;
                        massTable[min]++;
                        if (x < xMinTable[min]) xMinTable[min] = x;
                        if (x > xMaxTable[min]) xMaxTable[min] = x;

                        if (aLabel != 0) labelTable[aLabel] = min;
                        if (bLabel != 0) labelTable[bLabel] = min;
                        if (cLabel != 0) labelTable[cLabel] = min;
                        if (dLabel != 0) labelTable[dLabel] = min;
                    }
                }

                srcPtr++;
                aPtr++;
                bPtr++;
                cPtr++;
                dPtr++;
            }
        }

        // Iterate through labels pushing min/max x,y values towards minimum label
        if (blobList == null) blobList = new ArrayList<Blob>();

        for (int i = label - 1; i > 0; i--) {
            if (labelTable[i] != i) {
                if (xMaxTable[i] > xMaxTable[labelTable[i]]) xMaxTable[labelTable[i]] = xMaxTable[i];
                if (xMinTable[i] < xMinTable[labelTable[i]]) xMinTable[labelTable[i]] = xMinTable[i];
                if (yMaxTable[i] > yMaxTable[labelTable[i]]) yMaxTable[labelTable[i]] = yMaxTable[i];
                if (yMinTable[i] < yMinTable[labelTable[i]]) yMinTable[labelTable[i]] = yMinTable[i];
                massTable[labelTable[i]] += massTable[i];

                int l = i;
                while (l != labelTable[l]) {
                    l = labelTable[l];
                }
                labelTable[i] = l;
            } else {
                // Ignore blobs that butt against corners
                if (i == labelBuffer[0]) continue;                                    // Top Left
                if (i == labelBuffer[width]) continue;                                // Top Right
                int i1 = width * height;
                if (i == labelBuffer[i1 - width + 1]) continue;    // Bottom Left
                if (i == labelBuffer[i1 - 1]) continue;            // Bottom Right

                if (massTable[i] >= minBlobMass && (massTable[i] <= maxBlobMass || maxBlobMass == -1)) {
                    Blob blob = new Blob(xMinTable[i], xMaxTable[i], yMinTable[i], yMaxTable[i], massTable[i]);
                    blobList.add(blob);
                }
            }
        }

        // If dst buffer provided, fill with coloured blobs
        if (dstData != null) {
            for (int i = label - 1; i > 0; i--) {
                if (labelTable[i] != i) {
                    int l = i;
                    while (l != labelTable[l]) {
                        l = labelTable[l];
                    }
                    labelTable[i] = l;
                }
            }

            // Renumber labels into sequential numbers, starting with 0
            int newLabel = 0;
            for (int i = 1; i < label; i++) {
                if (labelTable[i] == i) {
                    labelTable[i] = newLabel++;
                } else {
                    labelTable[i] = labelTable[labelTable[i]];
                }
            }

            /*
            srcPtr = 0;          
            while (srcPtr < srcData.length) {
                if (srcData[srcPtr] == matchVal) {

                    //dstData.setIndexPixel(0, srcPtr, 0);
                    //dstData.setIndexPixel(1, srcPtr, 255);
                    //dstData.setIndexPixel(2, srcPtr, 0);

                    int c = labelTable[labelBuffer[srcPtr]] % COLOUR_ARRAY.length;
                    dstData.setIndexPixel(0, srcPtr, COLOUR_ARRAY[c][0]);
                    dstData.setIndexPixel(1, srcPtr, COLOUR_ARRAY[c][1]);
                    dstData.setIndexPixel(2, srcPtr, COLOUR_ARRAY[c][2]);

                }// else {
                //    dstData.setIndexPixel(0, srcPtr, 0);
                //    dstData.setIndexPixel(1, srcPtr, 0);
                //    dstData.setIndexPixel(2, srcPtr, 0);
                //}

                srcPtr++;
            }
            */

        }

        return blobList;
    }
}
