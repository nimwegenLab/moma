package com.jug.development.featureflags;

public class FeatureFlags {
    /**
     * Feature flags.
     */
    public static final boolean featureFlagUseAssignmentPlausibilityFilter = false; /* Sets whether to filter assignments (currently only mapping-assignments) by their plausibility as determined from the amount of total "cell-area" above/below the source and target components. */
    public static final boolean featureFlagDisableMaxCellDrop = false; /* Sets assignments are filtered for components which jump backward in the GL by a too high value. */
    public static final ComponentCostCalculationMethod featureFlagComponentCost = ComponentCostCalculationMethod.Legacy; /* this controls how the component cost will be calculated*/
    public static final boolean featureFlagFilterAssignmentsByComponentSizeMatch = true; /* this feature compares the size of components being mapped; a too strong mismatch will be considered implausible */
}