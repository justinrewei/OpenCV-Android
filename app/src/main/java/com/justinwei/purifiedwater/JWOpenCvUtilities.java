package com.justinwei.purifiedwater;

import android.content.Context;
import android.media.FaceDetector;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.Core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.opencv.core.Core.NORM_MINMAX;
import static org.opencv.core.CvType.CV_32FC3;

/**
 * Created by justinwei on 12/29/2016.
 */

public class JWOpenCvUtilities {

    public static void faceDetection(Mat image, String faceDetectXML) {

        CascadeClassifier faceDetector = new CascadeClassifier(faceDetectXML);
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
        }

        /*
        String filename = "ouput.png";
        System.out.println(String.format("Writing %s", filename));
        Imgcodecs.imwrite(filename, image);
        */
    }

    public static String getFilePath(String filename, Context context){
        File f = new File(context.getCacheDir()+"/"+filename);
        if (!f.exists()) try {

            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();


            FileOutputStream fos = new FileOutputStream(f);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) { throw new RuntimeException(e); }

        return f.getPath();
    }



    public static Mat skinDetection(Mat src) {
        //color to fill in where skin is detected
        double[] cblack = {0, 0, 0};   //Vec3b::all(0);

        Mat dst = src.clone();

        Mat src_ycrcb = new Mat(src.rows(), src.cols(), src.type());
        Imgproc.cvtColor(src, src_ycrcb, Imgproc.COLOR_BGR2YCrCb);


        Mat srcHSV = new Mat(src.rows(), src.cols(), src.type());
        src.convertTo(srcHSV, CV_32FC3);
        Imgproc.cvtColor(src, srcHSV, Imgproc.COLOR_BGR2HSV);
        Core.normalize(srcHSV, srcHSV);

        for(int i = 0; i < src.rows(); i++) {
            for(int j = 0; j < src.cols(); j++) {

                double B = src.get(i, j)[0];
                double G = src.get(i, j)[1];
                double R = src.get(i, j)[2];

                // apply rgb rule
                boolean a = R1(R,G,B);

                double Y = src_ycrcb.get(i, j)[0];
                double Cr = src_ycrcb.get(i, j)[1];
                double Cb = src_ycrcb.get(i, j)[2];
                // apply ycrcb rule
                boolean b = R2(Y,Cr,Cb);

                double H = srcHSV.get(i, j)[0];
                double S =srcHSV.get(i, j)[1];
                double V = srcHSV.get(i, j)[2];
                // apply hsv rule
                boolean c = R3(H,S,V);

                if(!(a && b && c))
                    //this pixel is determined to be skin, so we fill it with black color
                    dst.put(i, j, cblack);
            }
        }

        return dst;
    }

    private static boolean R1(double R, double G, double B) {
        boolean e1 = (R>95) && (G>40) && (B>20) && ((Math.max(R, Math.max(G,B)) - Math.min(R, Math.min(G,B)))>15) && (Math.abs(R-G)>15) && (R>G) && (R>B);
        boolean e2 = (R>220) && (G>210) && (B>170) && (Math.abs(R-G)<=15) && (R>B) && (G>B);
        return (e1||e2);
    }

    private static boolean R2(double Y, double Cr, double Cb) {
        boolean e3 = Cr <= 1.5862*Cb+20;
        boolean e4 = Cr >= 0.3448*Cb+76.2069;
        boolean e5 = Cr >= -4.5652*Cb+234.5652;
        boolean e6 = Cr <= -1.15*Cb+301.75;
        boolean e7 = Cr <= -2.2857*Cb+432.85;
        return e3 && e4 && e5 && e6 && e7;
    }

    private static boolean R3(double H, double S, double V) {
        return (H<25) || (H > 230);
    }

}
