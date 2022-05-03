package com.jug.exploration;

import com.jug.MoMA;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import static com.jug.util.JavaUtils.concatenateWithCollection;

/**
 * This class provides adapter methods to enable testing MoMA from within test-code. This includes an adapter to start
 * MoMA with command line arguments from test-code.
 */
public class ExplorationTestHelpers {
    /**
     * Method to start MoMA with command-line arguments from test-code.
     * @param headless start MoMA in headless mode, if true; corresponds to command line argument -headless
     * @param inputPath input path; corresponds to command line argument -i
     * @param outputPath output path; corresponds to command line argument -o
     * @param tmin starting frame of the time range to analyze; corresponds to command line argument -tmin
     * @param tmax stop frame of the time range to analyze; corresponds to command line argument -tmax
     * @param deleteProbabilityMaps controls whether the probability map (which are generated by MoMA) should be deleted before starting MoMA; this is useful to force MoMA to generate the probability map on start; this is not a command line option, but for convenience during testing
     */
    public static void startMoma(boolean headless, String inputPath, String outputPath, Integer tmin, Integer tmax, boolean deleteProbabilityMaps) {
        startMoma(headless, inputPath, outputPath, tmin, tmax, deleteProbabilityMaps, null);
    }

    /**
     * Method to start MoMA with command-line arguments from test-code.
     * @param headless start MoMA in headless mode, if true; corresponds to command line argument -headless
     * @param inputPath input path; corresponds to command line argument -i
     * @param outputPath output path; corresponds to command line argument -o
     * @param tmin starting frame of the time range to analyze; corresponds to command line argument -tmin
     * @param tmax stop frame of the time range to analyze; corresponds to command line argument -tmax
     * @param deleteProbabilityMaps controls whether the probability map (which are generated by MoMA) should be deleted before starting MoMA; this is useful to force MoMA to generate the probability map on start; this is not a command line option, but for convenience during testing
     * @param additionalArgs additional command line arguments
     */
    public static void startMoma(boolean headless, String inputPath, String outputPath, Integer tmin, Integer tmax, boolean deleteProbabilityMaps, String[] additionalArgs) {
        if (deleteProbabilityMaps) {
            remove_probability_maps(inputPath);
        }

        String[] args;

        if (tmin != null && tmax != null) {
            args = new String[]{"-i", inputPath, "-o", outputPath, "-tmin", tmin.toString(), "-tmax", tmax.toString()};
        } else if (tmin != null && tmax == null) {
            args = new String[]{"-i", inputPath, "-o", outputPath, "-tmin", tmin.toString()};
        } else if (tmin == null && tmax != null) {
            args = new String[]{"-i", inputPath, "-o", outputPath, "-tmax", tmax.toString()};
        } else if (outputPath != null) { // both tmin and tmax are null
            args = new String[]{"-i", inputPath, "-o", outputPath};
        } else {
            args = new String[]{"-i", inputPath};
        }
        if (additionalArgs != null) {
            args = concatenateWithCollection(args, additionalArgs);
        }

        if (outputPath != null){
            create_output_folder(outputPath);
        }

//        MoMA.HEADLESS = headless;
        if(headless){
            args = concatenateWithCollection(args, new String[]{"-headless"});
        }
        MoMA.main(args);
    }

    private static void create_output_folder(String outputPath) {
        File file = new File(outputPath);
        file.mkdir();
    }

    /**
     * Delete preexisting probability maps. During testing, we often want to test the generation
     * of the probability maps, which are cached to disk and loaded, if they exist for a given model.
     * This function removes those cached files to always run the U-Net preprocessing.
     *
     * @param path
     */
    private static void remove_probability_maps(String path) {
        PathMatcher matcher =
                FileSystems.getDefault().getPathMatcher("glob:*__model_*.tif*");
        File f = new File(path);
        File parentFolder = new File(f.getParent());

        String[] pathnames = parentFolder.list();
        for (String name : pathnames) {
            String filePath = parentFolder + "/" + name;
            if (matcher.matches(Paths.get(name))) {
                System.out.print(filePath);
                File f2 = new File(filePath);
                if (f2.delete())                      //returns Boolean value
                {
                    System.out.println("Deleted: " + f.getName());   //getting and printing the file name
                } else {
                    System.out.println("Failed to delete: " + f.getName());
                }
            }
        }
    }
}
