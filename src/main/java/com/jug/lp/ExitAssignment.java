/**
 *
 */
package com.jug.lp;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.numeric.real.FloatType;

import com.jug.MotherMachine;

/**
 * @author jug
 */
public class ExitAssignment extends AbstractAssignment< Hypothesis< Component< FloatType, ? > > > {

	private final List< Hypothesis< Component< FloatType, ? >>> Hup;
	@SuppressWarnings( "unused" )
	private final AssignmentsAndHypotheses< AbstractAssignment< Hypothesis< Component< FloatType, ? > > >, Hypothesis< Component< FloatType, ? > > > nodes;
	private final HypothesisNeighborhoods< Hypothesis< Component< FloatType, ? > >, AbstractAssignment< Hypothesis< Component< FloatType, ? > > > > edges;
	private final Hypothesis< Component< FloatType, ? >> who;

	private static int dcId = 0;

	/**
	 * Creates an ExitAssignment.
	 * 
	 * @param nodes
	 * @param edges
	 * @param who
	 * @throws GRBException
	 */
	public ExitAssignment( final int t, final GRBVar ilpVariable, final GRBModel model, final AssignmentsAndHypotheses< AbstractAssignment< Hypothesis< Component< FloatType, ? > > >, Hypothesis< Component< FloatType, ? > > > nodes, final HypothesisNeighborhoods< Hypothesis< Component< FloatType, ? > >, AbstractAssignment< Hypothesis< Component< FloatType, ? > > > > edges, final List< Hypothesis< Component< FloatType, ? >>> Hup, final Hypothesis< Component< FloatType, ? >> who ) throws GRBException {
		super( GrowthLineTrackingILP.ASSIGNMENT_EXIT, ilpVariable, model );
		this.Hup = Hup;
		this.edges = edges;
		this.nodes = nodes;
		this.who = who;
	}

	/**
	 * @throws GRBException
	 * @see com.jug.lp.AbstractAssignment#addConstraintsToLP(gurobi.GRBModel,
	 *      com.jug.lp.AssignmentsAndHypotheses,
	 *      com.jug.lp.HypothesisNeighborhoods)
	 */
	@Override
	public void addConstraintsToLP() throws GRBException {
		final GRBLinExpr expr = new GRBLinExpr();

		expr.addTerm( Hup.size(), this.getGRBVar() );

		for ( final Hypothesis< Component< FloatType, ? >> upperHyp : Hup ) {
			if ( edges.getRightNeighborhood( upperHyp ) != null ) {
				for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? >>> a_j : edges.getRightNeighborhood( upperHyp ) ) {
					if ( a_j.getType() == GrowthLineTrackingILP.ASSIGNMENT_EXIT ) {
						continue;
					}
					// add term if assignment is NOT another exit-assignment
					expr.addTerm( 1.0, a_j.getGRBVar() );
				}
			}
		}

		if ( !MotherMachine.DISABLE_EXIT_CONSTRAINTS ) {
			model.addConstr( expr, GRB.LESS_EQUAL, Hup.size(), "dc_" + dcId );
		}
		dcId++;
	}

	/**
	 * Adds a list of constraints and factors as strings.
	 * 
	 * @see com.jug.lp.AbstractAssignment#getConstraint()
	 */
	@Override
	public void addFunctionsAndFactors( final FactorGraphFileBuilder fgFile, final List< Integer > regionIds ) {
		final List< Integer > varIds = new ArrayList< Integer >();
		final List< Integer > coeffs = new ArrayList< Integer >();

		// expr.addTerm( Hup.size(), this.getGRBVar() );
		coeffs.add( new Integer( Hup.size() ) );
		varIds.add( new Integer( this.getVarIdx() ) );

		for ( final Hypothesis< Component< FloatType, ? >> upperHyp : Hup ) {
			if ( edges.getRightNeighborhood( upperHyp ) != null ) {
				for ( final AbstractAssignment< Hypothesis< Component< FloatType, ? >>> a_j : edges.getRightNeighborhood( upperHyp ) ) {
					if ( a_j.getType() == GrowthLineTrackingILP.ASSIGNMENT_EXIT ) {
						continue;
					}
					// add term if assignment is NOT another exit-assignment
					// expr.addTerm( 1.0, a_j.getGRBVar() );
					coeffs.add( new Integer( 1 ) );
					varIds.add( new Integer( a_j.getVarIdx() ) );
				}
			}
		}

		// model.addConstr( expr, GRB.LESS_EQUAL, Hup.size(), "dc_" + dcId );
		final int fkt_id = fgFile.addConstraintFkt( coeffs, "<=", Hup.size() );
		fgFile.addFactor( fkt_id, varIds, regionIds );
	}

	/**
	 * Returns the segmentation hypothesis this exit-assignment is associated
	 * with.
	 * 
	 * @return the associated segmentation-hypothesis.
	 */
	public Hypothesis< Component< FloatType, ? >> getAssociatedHypothesis() {
		return who;
	}
}
