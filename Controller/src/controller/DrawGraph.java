package controller;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import edu.princeton.cs.algs4.Edge;
import nodedatagramsocket.utils.Display;

/**
 *
 * @author eduardo
 */
public class DrawGraph {
    Controller controller;
    
    public DrawGraph(Controller controller) {
        this.controller = controller;
    }
    
    private void createAndShowGui() {
        JFrame frame = new JFrame("DemoGraph");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ListenableGraph<String, MyEdge> g = buildGraph();
        JGraphXAdapter<String, MyEdge> graphAdapter = new JGraphXAdapter<String, MyEdge>(g);
        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());
        
        frame.add(new mxGraphComponent(graphAdapter));
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    public void run() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGui();
            }
        });
    }

    public static class MyEdge extends DefaultWeightedEdge {
        @Override
        public String toString() {
            //return String.valueOf(getWeight());
            return String.valueOf("");
        }
    }

    public ListenableGraph<String, MyEdge> buildGraph() {
        ListenableUndirectedWeightedGraph<String, MyEdge> g = new ListenableUndirectedWeightedGraph<>(MyEdge.class);
        String edges[][] = new String[controller.getEdgeWeightGraph().E()][2] ;
        System.out.println("Edges: " + controller.getEdgeWeightGraph().E());
        int edgeCount = 0;
        
        for (int i = 0 ; i < controller.getEdgeWeightGraph().V(); i++) {
            g.addVertex(Integer.toString(i));
        }
        
        for (int i =0 ; i < controller.getEdgeWeightGraph().V(); i++) {
            for (Edge e: controller.getEdgeWeightGraph().adj(i)) {
                boolean exist = false;
                boolean pass = false;
                String v1 = Integer.toString(e.either());
                String v2 = Integer.toString(e.other(e.either()));
                
                if (v1.equals(v2)) {
                    continue;
                }
                
                Display.info(e.toString());
                
                String str = v1 + " " + v2;
                String str2 = v2 + " " + v1;
                for (int j = 0; j < edgeCount; j++) {
                    //System.out.println("stored "+edges[j][0]);
                    if (edges[j][0].equals(str) || edges[j][0].equals(str2)) {
                        //System.out.println("equal "+edges[j][1]);
                        if (Double.parseDouble(edges[j][1]) > e.weight()) {
                            exist = true;
                        } else {
                            pass = true;
                        }
                    }
                }
                
                if (pass) {
                    continue;
                }
                
                if (exist) {
                    g.removeEdge(v1, v2);
                    MyEdge edg = g.addEdge(v1, v2);
                    g.setEdgeWeight(edg,e.weight()); 
                    //System.out.println("YO");
                } else {
                    MyEdge edg = g.addEdge(v1, v2);
                    g.setEdgeWeight(edg,e.weight()); 
                    edges[edgeCount][0] = str;
                    //System.out.println("saved "+edges[edgeCount][0]);
                    edges[edgeCount][1] = Double.toString(e.weight());
                    edgeCount++;
                }
            }
        }
        return g;
    }
}
