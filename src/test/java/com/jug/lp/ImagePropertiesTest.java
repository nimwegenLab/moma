package com.jug.lp;

import com.jug.datahandling.IImageProvider;
import com.jug.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Path;

public class ImagePropertiesTest {
    private final TestUtils testUtils;
    private final ImageProperties sut;
    private final IImageProvider imageProvider;

    public ImagePropertiesTest() throws IOException {
        testUtils = new TestUtils();
        sut = new ImageProperties(testUtils.getImglib2Utils());
        Path testDataFolder = testUtils.getAbsolutTestFilePath("src/test/resources/00_probability_maps/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12/frames_445-460__20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12");
        imageProvider = testUtils.getImageProviderFromDataFolder(testDataFolder);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MAX_VALUE})
    public void getBackgroundIntensityStd__when_called_with_invalid_channel_number__throw_InvalidArgument(int channelNumber) {
        Assert.assertThrows(IllegalArgumentException.class, () -> sut.getBackgroundIntensityStd(imageProvider, channelNumber));
    }

    @ParameterizedTest()
    @CsvSource({
            "0, 24.339816352770796",
            "1, 23.712322855821302",
            "2, 5.7702834107518495",
    })
    public void getBackgroundIntensityStd__when_called_with_valid_channel_number__returns_expected_value(int channelNumber, double expectedBackgroundIntensityStd) throws IOException {
        double backgroundIntensity = sut.getBackgroundIntensityStd(imageProvider, channelNumber);
        Assert.assertEquals(expectedBackgroundIntensityStd, backgroundIntensity, 1e-6);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MAX_VALUE})
    public void getBackgroundIntensityMean__when_called_with_invalid_channel_number__throw_InvalidArgument(int channelNumber) {
        Assert.assertThrows(IllegalArgumentException.class, () -> sut.getBackgroundIntensityMean(imageProvider, channelNumber));
    }

    @ParameterizedTest()
    @CsvSource({
            "0, 6.2809052255378655",
            "1, 11.770502803772166",
            "2, 105.6257109466303",
    })
    public void getBackgroundIntensityMean__when_called_with_valid_channel_number__returns_expected_value(int channelNumber, double expectedMeanBackgroundIntensity) throws IOException {
        double backgroundIntensity = sut.getBackgroundIntensityMean(imageProvider, channelNumber);
        Assert.assertEquals(expectedMeanBackgroundIntensity, backgroundIntensity, 1e-6);
    }
}
