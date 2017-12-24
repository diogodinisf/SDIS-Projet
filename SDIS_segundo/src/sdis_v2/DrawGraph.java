
package sdis_v2;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import edu.princeton.cs.algs4.Edge;

public class DrawGraph {

    private static void createAndShowGui() {
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

    public static void main() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGui();
            }
        });
    }

    public static class MyEdge extends DefaultWeightedEdge {
        @Override
        public String toString() {
            return String.valueOf(getWeight());
        }
    }

    public static ListenableGraph<String, MyEdge> buildGraph() {
        ListenableUndirectedWeightedGraph<String, MyEdge> g = new ListenableUndirectedWeightedGraph<String, MyEdge>(MyEdge.class);
        String edges[][] = new String[Controller.G.E()][2] ;
        System.out.println("Edges: "+Controller.G.E());
        int edgeCount = 0;
        for (int i =0 ; i< Controller.G.V() ; i++){
            g.addVertex(Integer.toString(i));
        }
        
        for(int i =0 ; i<Controller.G.V();i++){
            for(Edge e: Controller.G.adj(i)){
                boolean exist = false;
                boolean pass = false;
                String v1 = Integer.toString(e.either());
                String v2 = Integer.toString(e.other(e.either()));
                if(v1.equals(v2)){
                    continue;
                }
                System.out.println(e);
                String str = v1 + " " + v2;
                String str2 = v2+ " " + v1;
                for(int j =0; j < edgeCount;j++){
                    System.out.println("stored "+edges[j][0]);
                    if(edges[j][0].equals(str)|| edges[j][0].equals(str2) ){
                        System.out.println("equal "+edges[j][1]);
                        if(Double.parseDouble(edges[j][1]) > e.weight()){
                            exist = true;
                        }
                        else{
                            pass=true;
                        }
                    }
                }
                if(pass){
                    continue;
                }
                if(exist == true){
                    g.removeEdge(v1,v2);
                    MyEdge edg = g.addEdge(v1,v2);
                    g.setEdgeWeight(edg,e.weight()); 
                    System.out.println("YO");
                }
                else{
                    MyEdge edg = g.addEdge(v1,v2);
                    g.setEdgeWeight(edg,e.weight()); 
                    edges[edgeCount][0] = str;
                    System.out.println("saved "+edges[edgeCount][0]);
                    edges[edgeCount][1] = Double.toString(e.weight());
                    edgeCount++;
                }
                
         
                

            }
        }


        return g;
    }
}