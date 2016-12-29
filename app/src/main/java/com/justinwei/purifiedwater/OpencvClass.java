package com.justinwei.purifiedwater;

import org.opencv.core.Mat;

/**
 * Created by justinwei on 12/18/2016.
 */

public class OpencvClass {
    public native static void faceDetection(long addrRgba);
    public native static Mat skinDetection(long addrRgba);
}


