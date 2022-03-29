package com.jug.config;

public interface IComponentTreeGeneratorConfiguration {
    /**
     * Returns the minimal size of leaf components in [px].
     * Leaf components with size lower than this are not considered.
     *
     * @return minimal size of leaf components in [px]
     */
    int getSizeMinimumOfLeafComponent();

    /**
     * Returns the minimal allowed size of a parent component in [px].
     * Root components with size lower than this are not considered.
     *
     * @return minimal size of root components in [px]
     */
    int getSizeMinimumOfParentComponent();
}