package com.jug.util.componenttree;

import com.jug.util.FloatTypeImgLoader;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class UnetProcessorTest {

    public static void main(String[] args) throws FileNotFoundException {
//        new UnetProcessorTest().process_runs_on_datasets_shapes_below_unet_input_layer();
        new UnetProcessorTest().process_test_data_with_short_growthlane();
    }

    @Test
    public void process_runs_on_datasets_shapes_below_unet_input_layer() throws FileNotFoundException {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        String basePath = "/home/micha/Documents/01_work/git/MoMA/src/test/resources/ImageFormatTest/";
        String path = basePath + "new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3_shape_106x531_original.tif"; // wide and high enough; UNet input-layer shape is: 32x512
//        String path = basePath + "new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3_shape_24x444.tif"; // too thin and too low; UNet input-layer shape is: 32x512
//        String path = basePath + "new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3_shape_24x531.tif"; // too thin, but high enough; UNet input-layer shape is: 32x512
//        String path = basePath + "new_10frames_20190424_hi2_hi3_med2_rplN_4_MMStack_Pos0_GL3_shape_106x444.tif"; // wide enough, but too low; UNet input-layer shape is: 32x512
        Img<FloatType> inputImage = readImageData(path);
        Img<FloatType> processedImage = new UnetProcessor().process(inputImage);
        ImageJFunctions.show(inputImage, "Input image");
        ImageJFunctions.show(processedImage, "Processed image");
    }


    @Test
    public void process_test_data_with_short_growthlane() throws FileNotFoundException {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
//        String path = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01/20190515_hi1_med1_med2_rpmB_glu_gly_7_MMStack_Pos25_preproc_GL01__frames_400-450.tif";
        String path = "/home/micha/Documents/01_work/git/MoMA/test_datasets/20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15/20191105_glc_spcm_1_MMStack_Pos7_preproc_GL15.tif";
        Img<FloatType> inputImage = readImageData(path);
        Img<FloatType> processedImage = new UnetProcessor().process(inputImage);
        ImageJFunctions.show(inputImage, "Input image");
        ImageJFunctions.show(processedImage, "Processed image");
    }


    /**
     * Read image data used in the tests.
     *
     * @param path
     * @return First channel of the image stack that was read.
     * @throws FileNotFoundException
     */
    private Img<FloatType> readImageData(String path) throws FileNotFoundException {
        ImagePlus imp = IJ.openImage(path);
        int minTime = 1;
        int maxTime = imp.getNFrames();
        int minChannelIdx = 1;
        int maxChannelIdx = imp.getNChannels();
        ArrayList<Img<FloatType>> rawChannelImgs = FloatTypeImgLoader.loadTiffsFromFileOrFolder(path, minTime, maxTime, minChannelIdx, maxChannelIdx);
        return rawChannelImgs.get( 0 );
    }
}