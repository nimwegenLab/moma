package com.jug.lp;

import com.jug.util.ComponentTreeUtils;
import com.jug.util.componenttree.AdvancedComponent;
import com.jug.util.componenttree.ComponentInterface;
import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

/**
 * This class is used to wrap away whatever object that represents one of the
 * segmentation hypothesis. See {@link AbstractAssignment} for a place where
 * this is
 * used.
 *
 * @author jug
 */
@SuppressWarnings("restriction")
public class Hypothesis<C extends AdvancedComponent<FloatType>> {

    private final C wrappedComponent;
    private final float cost;
    private GrowthlaneTrackingILP ilp;
    private final HypLoc location;
    public List<String> labels = new ArrayList<>();

    public boolean isForced() {
        GRBConstr grbConstr = getSegmentInSolutionConstraint();
        if (isNull(grbConstr)) {
            return false;  /* no variable was found so this assignment is not forced */
        }
        return true;
    }

    public void setIsForced(boolean targetStateIsTrue) {
        if (targetStateIsTrue == isForced()) {
            return;
        }

        if (isForceIgnored()) {
            setIsForceIgnored(false);
        }

        if (!targetStateIsTrue) {
            removeSegmentInSolutionConstraint();
            return;
        }

        if (targetStateIsTrue) {
            addSegmentInSolutionConstraint();
            return;
        }
        throw new RuntimeException("We should not reach here. Something went wrong.");
    }

    private void addSegmentInSolutionConstraint() {
        addSegmentConstraint(1.0, getSegmentInSolutionConstraintName());
    }

    private void removeSegmentInSolutionConstraint() {
        GRBConstr segmentInSolutionConstraint = getSegmentInSolutionConstraint();
        removeSegmentConstraint(segmentInSolutionConstraint);
    }

    private void removeSegmentConstraint(GRBConstr segmentInSolutionConstraint) {
        if (isNull(segmentInSolutionConstraint)) {
            return;
        }
        try {
            ilp.model.remove(segmentInSolutionConstraint);
            ilp.model.update();
        } catch (GRBException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeSegmentNotInSolutionConstraint() {
        GRBConstr segmentNotInSolutionConstraint = getSegmentNotInSolutionConstraint();
        removeSegmentConstraint(segmentNotInSolutionConstraint);
    }

    public void setIsForceIgnored(boolean targetStateIsTrue) {
        if (targetStateIsTrue == isForceIgnored()) {
            return;
        }
        if (isForced()) {
            setIsForced(false);
        }
        if (!targetStateIsTrue) {
            removeSegmentNotInSolutionConstraint();
            return;
        }
        if (targetStateIsTrue) {
            addSegmentNotInSolutionConstraint();
            return;
        }
        throw new RuntimeException("We should not reach here. Something went wrong.");
    }

    private void addSegmentNotInSolutionConstraint() {
        addSegmentConstraint(0.0, getSegmentNotInSolutionConstraintName());
    }

    private void addSegmentConstraint(double rhs, String segmentConstraintName) {
        Hypothesis<AdvancedComponent<FloatType>> hyp = (Hypothesis<AdvancedComponent<FloatType>>) this;
        final Set<AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>>> rightNeighbors = ilp.edgeSets.getRightNeighborhood(hyp);
        try {
            final GRBLinExpr expr = new GRBLinExpr();
            for (final AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assmnt : rightNeighbors) {
                expr.addTerm(1.0, assmnt.getGRBVar());
            }
            ilp.model.addConstr(expr, GRB.EQUAL, rhs, segmentConstraintName);
            ilp.model.update();
        } catch (final GRBException e) {
            throw new RuntimeException("Failed to add constraint: " + segmentConstraintName);
        }
    }

    @NotNull
    private String getSegmentNotInSolutionConstraintName() {
        return "HypIgnoreConstr_" + getStringId();
    }

    @Nullable
    private GRBConstr getSegmentInSolutionConstraint() {
        GRBConstr grbConstr;
        try {
            grbConstr = ilp.model.getConstrByName(getSegmentInSolutionConstraintName());
        } catch (GRBException e) {
            return null;
        }
        return grbConstr;
    }

    @NotNull
    private String getSegmentInSolutionConstraintName() {
        return "HypEnforceConstr_" + getStringId();
    }

    public boolean isForceIgnored() {
        GRBConstr grbConstr = getSegmentNotInSolutionConstraint();
        if (isNull(grbConstr)) {
            return false;  /* no variable was found so this assignment is not forced */
        }
        return true;
    }

    @Nullable
    private GRBConstr getSegmentNotInSolutionConstraint() {
        GRBConstr grbConstr;
        try {
            grbConstr = ilp.model.getConstrByName(getSegmentNotInSolutionConstraintName());
        } catch (GRBException e) {
            System.out.println(e);
            return null;
        }
        return grbConstr;
    }


    /**
     * Used to store track-branch pruning sources. This is a way to easily
     * exclude branches from data export etc.
     */
    private boolean isPruneRoot = false;
    private boolean isPruned = false;

    public Hypothesis(final int t, final C wrappedComponent, final float cost, GrowthlaneTrackingILP ilp) {
        this.wrappedComponent = wrappedComponent;
        this.cost = cost;
        this.ilp = ilp;
        location = new HypLoc(t, wrappedComponent);
    }

    public int getId() {
        return location.limits.getA() * 1000 + location.limits.getB();  // TODO-MM20210721: Why the factor 1000?! This would better be a hash-value based on (multiple) hypothesis properties (e.g. max. segment probability, segment limits, etc.).
    }

    public String getStringId() {
        return wrappedComponent.getStringId();
    }

    /**
     * @return the wrapped segmentHypothesis
     */
    public C getWrappedComponent() {
        return wrappedComponent;
    }

    /**
     * @return the costs
     */
    public float getCost() {
        return cost;
    }

    /**
     * @return the stored gurobi constraint that either forces this hypothesis
     * to be part of any solution to the ILP or forces this hypothesis
     * to be NOT included. Note: this function returns 'null' if such a
     * constraint was never created.
     */
    public GRBConstr getSegmentSpecificConstraint() {
        GRBConstr segmentNotInSolutionConstraint = getSegmentNotInSolutionConstraint();
        GRBConstr segmentInSolutionConstraint = getSegmentInSolutionConstraint();
        if (!isNull(segmentInSolutionConstraint) && !isNull(segmentNotInSolutionConstraint)) {
            throw new RuntimeException("conflicting segment constraints have occurred; this should not have happened");
        }
        if (!isNull(segmentNotInSolutionConstraint)) {
            return segmentNotInSolutionConstraint;
        }
        if (!isNull(segmentInSolutionConstraint)) {
            return segmentInSolutionConstraint;
        }
        return null;
    }

    /**
     * Used to store a 'segment in solution constraint' or a 'segment not in
     * solution constraint' after it was added to the ILP.
     */
    public ValuePair<Integer, Integer> getLocation() {
        return location.limits;
    }

    public int getTime() {
        return location.t;
    }

    public List<Hypothesis<AdvancedComponent<FloatType>>> getChildren() {
        try {
            AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assmnt = ilp.getOptimalRightAssignment((Hypothesis<AdvancedComponent<FloatType>>) this);
            return assmnt.getTargetHypotheses();
        } catch (GRBException e) {
            throw new RuntimeException("Unable to get the optimal right assignment for hypothesis: " + this.getStringId());
        }
    }

    public Hypothesis<AdvancedComponent<FloatType>> getParent() {
        try {
            AbstractAssignment<Hypothesis<AdvancedComponent<FloatType>>> assmnt = ilp.getOptimalLeftAssignment((Hypothesis<AdvancedComponent<FloatType>>) this);
            return assmnt.getSourceHypothesis();
        } catch (GRBException e) {
            throw new RuntimeException("Unable to get the optimal right assignment for hypothesis: " + this.getStringId());
        }
    }

    public void setPruneRoot(final boolean value, final GrowthlaneTrackingILP ilp) {
        this.isPruneRoot = value;

        if(getParent().isPruned()){
            throw new PruningException("Cannot prune this segment", "This segment cannot be pruned, because previous segments in this lineage are pruned. Please remove the pruning from the first pruned segment in this lineage.");
        }
        this.setPruned(value);
        this.setPruneStateRecursively(this.getChildren(), value);
    }

    private static void setPruneStateRecursively(List<Hypothesis<AdvancedComponent<FloatType>>> childNodes, boolean value){
        for (Hypothesis<?> child : childNodes) {
            child.setPruned(value);
            setPruneStateRecursively(child.getChildren(), value);
        }
    }

    /**
     * @return
     */
    public boolean isPruneRoot() {
        return isPruneRoot;
    }

    /**
     * @return
     */
    public boolean isPruned() {
        return this.isPruned;
    }

    /**
     * @param value
     */
    void setPruned(final boolean value) {
        this.isPruned = value;
    }

    public void toggleIsPrunedRoot() {
        this.setPruneRoot(!this.isPruneRoot(), ilp);
    }

    public class HypLoc {

        final int t;
        final ValuePair<Integer, Integer> limits;

        HypLoc(final int t, final C wrappedComponent) {
            this.t = t;
            this.limits = ComponentTreeUtils.getTreeNodeInterval(wrappedComponent);
        }
    }
}
