package com.jug.lp;

import com.jug.config.IConfiguration;
import com.jug.datahandling.IImageProvider;

public class AssignmentFilterFactory {
    private final IConfiguration configuration;
    private final ImageProperties imageProperties;

    public AssignmentFilterFactory(IConfiguration configuration, ImageProperties imageProperties) {
        this.configuration = configuration;
        this.imageProperties = imageProperties;
    }

    public IAssignmentFilter getAssignmentFilter(IImageProvider imageProvider) {
        if (!configuration.getFilterAssignmentsUsingFluorescenceFeatureFlag()) {
            return new DummyAssignmentFilter();
        }
        int channelNumber = configuration.getFluorescentAssignmentFilterChannel();
        double intensityMean = imageProperties.getBackgroundIntensityMean(imageProvider, channelNumber);
        double intensityStd = imageProperties.getBackgroundIntensityStd(imageProvider, channelNumber);
        double numberOfSigmas = configuration.getFluorescentAssignmentFilterNumberOfSigmas();
        double threshold = intensityMean + numberOfSigmas * intensityStd;
        AssignmentFluorescenceFilter filter = new AssignmentFluorescenceFilter();
        filter.setTargetChannelNumber(channelNumber);
        filter.setFluorescenceThreshold(threshold);
        return filter;
    }
}