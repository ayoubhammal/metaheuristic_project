package meta.projet.classesi.solver;

import meta.projet.classesi.solver.heuristic.Heuristic;

import java.util.Collection;
import java.util.HashSet;
import java.util.PriorityQueue;

/**
 * Implements AStar solver
 *
 */
public class AStar extends Solver {
    private int initialState;
    private int finalState;
    private Heuristic heuristic;
    private int maxLevel;
    private int numberOfDevelopedStates;

    private Node solution;
    private PriorityQueue<Node> opened;
    private HashSet<Node> closed;

    /**
     * AStar constructor.
     * @param initialState the initial state that the solver starts from
     * @param finalState the target state that the solver needs to reach 
     * @param heuristic the heuristic that the astar will use to evaluate states
     *                  valide values are objects of the classes : 
     *                  - MinMoves
     *                  - MissPlaced
     *                  - DepthFirst (is only for internal test)
     *                  - BreadthFirst (is only for intenal test)
     * @param maxLevel the maximum level astar is allowed to reach,
     *                 if set to -1 the maximum level is not limited
     */
    public AStar(int initialState, int finalState, Heuristic heuristic, int maxLevel) {
        this.initialState = initialState;
        this.finalState = finalState;
        this.heuristic = heuristic;
        this.maxLevel = maxLevel;
        this.numberOfDevelopedStates = 0;

        short[][] targetState = new short[3][3];
        intToMatrix(this.finalState, targetState);
        this.heuristic.setTargetState(targetState);
    }

    /**
     * This method uses AStar algorithm to find the sequence of actions to reach
     * the finalState from the initialState. The solution if found will be 
     * stored in solution member attribute, while opened and closed attributes
     * will contain the nodes to be developed and the developed nodes
     * respectively.
     * @preturn boolean true if the algorithm found a solution, false otherwise
     */
    public boolean solve() {
        Node currentNode;
        short[][] matrixState = new short[3][3];
        Node[] children;

        this.numberOfDevelopedStates = 0;
        this.opened = new PriorityQueue<Node>();  
        this.closed = new HashSet<Node>();  

        intToMatrix(this.initialState, matrixState);
        this.opened.add(
            new Node(
                this.initialState, 
                null, 
                this.heuristic.score(matrixState, 0),
                0
            )
        );

        // main loop
        while (!this.opened.isEmpty()) {
            currentNode = this.opened.poll(); 

            //System.out.println(String.format("Current Node : %09d with level %d and score %d", currentNode.getState(), currentNode.getLevel(), currentNode.getScore()));

            // test if the current node is a goal
            if (currentNode.getState() == this.finalState) {
                this.solution =  currentNode;
                return true;
            }

            // develop children of current node
            if (!this.closed.contains(currentNode)) {
                this.closed.add(currentNode);

                if (this.maxLevel == -1 || currentNode.getLevel() < maxLevel) {
                    this.numberOfDevelopedStates = this.numberOfDevelopedStates + 1;
                    // generate children
                    intToMatrix(currentNode.getState(), matrixState);
                    children = getChildren(matrixState, currentNode);
                    //System.out.println("Developed children : ");
                    for (Node child : children) {
                        if (child != null && !this.closed.contains(child)) {
                            //System.out.println(String.format("%09d (%d)", child.getState(), child.getScore()));
                            this.opened.add(child);
                        }
                    }
                }
            } else {
                //System.out.println("Already tested");
            }
        }

        return false;
    }

    public Node[] getChildren(short[][] state, Node parent) {
        Node node;
        Node[] children = new Node[4];
        int zi = 0, zj = 0;
        
        // find 0 position
        for (int i = 0; i < state.length; ++i) {
            for (int j = 0; j < state[0].length; ++j) {
                if (state[i][j] == 0) {
                    zi = i;
                    zj = j;
                }
            }
        }

        // up
        if (zi != 0) {
            state[zi][zj] = state[zi - 1][zj];
            state[zi - 1][zj] = 0;
            node = new Node(
                matrixToInt(state),
                parent,
                this.heuristic.score(state, parent.getLevel() + 1),
                parent.getLevel() + 1
            );
            children[0] = node;
            state[zi - 1][zj] = state[zi][zj];
            state[zi][zj] = 0;
        }

        // down
        if (zi != state.length - 1) {
            state[zi][zj] = state[zi + 1][zj];
            state[zi + 1][zj] = 0;
            node = new Node(
                matrixToInt(state),
                parent,
                this.heuristic.score(state, parent.getLevel() + 1),
                parent.getLevel() + 1
            );
            children[1] = node;
            state[zi + 1][zj] = state[zi][zj];
            state[zi][zj] = 0;
        }
        
        // left
        if (zj != 0) {
            state[zi][zj] = state[zi][zj - 1];
            state[zi][zj - 1] = 0;
            node = new Node(
                matrixToInt(state),
                parent,
                this.heuristic.score(state, parent.getLevel() + 1),
                parent.getLevel() + 1
            );
            children[2] = node;
            state[zi][zj - 1] = state[zi][zj];
            state[zi][zj] = 0;
        }
        
        // right
        if (zj != state.length - 1) {
            state[zi][zj] = state[zi][zj + 1];
            state[zi][zj + 1] = 0;
            node = new Node(
                matrixToInt(state),
                parent,
                this.heuristic.score(state, parent.getLevel() + 1),
                parent.getLevel() + 1
            );
            children[3] = node;
            state[zi][zj + 1] = state[zi][zj];
            state[zi][zj] = 0;
        }

        return children;
    }

    public static void intToMatrix(int state, short[][] matrix) {
        for (int i = matrix.length - 1; i >= 0; --i) {
            for (int j = matrix[0].length - 1; j >= 0 ; --j) {
                matrix[i][j] = (short)(state % 10);
                state = state / 10;
            }
        }
    }
    
    public static int matrixToInt(short[][] matrix) {
        int p = matrix.length * matrix[0].length - 1;
        int state = 0;

        for (int i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[0].length; ++j) {
                state = state + (int)Math.pow(10, p) * matrix[i][j];
                p = p - 1;
            }
        }
        return state;
    }

    public Node getSolution() {
        return this.solution;
    }
    public Collection<Node> getOpened() {
        return this.opened;
    }
    public Collection<Node> getClosed() {
        return this.closed;
    }
    public int getNumberOfDevelopedStates() {
        return this.numberOfDevelopedStates;
    }
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
    public void setHeuristic(Heuristic heuristic) {
        this.heuristic = heuristic;
        short[][] targetState = new short[3][3];
        intToMatrix(this.finalState, targetState);
        this.heuristic.setTargetState(targetState);
    }
    public void setInitialState(int initialState) {
        this.initialState = initialState;
    }
    public void setFinalState(int finalState) {
        this.finalState = finalState;
        short[][] targetState = new short[3][3];
        intToMatrix(this.finalState, targetState);
        this.heuristic.setTargetState(targetState);
    }
}
