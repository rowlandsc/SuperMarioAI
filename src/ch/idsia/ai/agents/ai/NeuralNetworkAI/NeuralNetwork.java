package ch.idsia.ai.agents.ai.NeuralNetworkAI;

import ch.idsia.mario.environments.Environment;
import jdk.internal.util.xml.impl.Input;
import sun.plugin.javascript.navig.Array;
import sun.plugin.javascript.navig4.Layer;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by Alex on 3/1/2016.
 */
public class NeuralNetwork {

    ArrayList<Integer> LayerNumbers = new ArrayList<Integer>();

    ArrayList< ArrayList<Integer> > Weights = new ArrayList<ArrayList<Integer>>();

    float Threshold = 0.5f;

    NeuralNetwork(Integer inputs, ArrayList<Integer> hiddenLayerNumbers, Integer outputs) {
        Calendar seed = Calendar.getInstance();
        Random random = new Random(seed.getTimeInMillis());

        LayerNumbers.add(inputs);
        LayerNumbers.addAll(hiddenLayerNumbers);
        LayerNumbers.add(outputs);
        for (int i=1; i<LayerNumbers.size(); i++) {
            Weights.add(new ArrayList<Integer>());
            for (int j=0; j < LayerNumbers.get(i - 1) * LayerNumbers.get(i); j++) {
                Weights.get(i - 1).add(random.nextInt(100));
            }
        }
    }

    void Randomize() {
        Calendar seed = Calendar.getInstance();
        Random random = new Random(seed.getTimeInMillis());

        for (ArrayList<Integer> layerWeights : Weights) {
            for (int i=0; i<layerWeights.size(); i++) {
                layerWeights.set(i, random.nextInt(100));
            }
        }
    }

    boolean[] GetNetOutput(ArrayList<Float> input) {
        ArrayList<ArrayList<Float>> values = new ArrayList<ArrayList<Float>>();
        values.add(input);
        for (int i=0; i<Weights.size(); i++) {
            values.add(new ArrayList<Float>());
            for (int j=0; j<LayerNumbers.get(i+1); j++) {
                Float value = 0f;
                Integer totalWeight = 0;
                for (int k=0; k<LayerNumbers.get(i); k++) {
                    totalWeight += Weights.get(i).get((j+1)*k);
                    value += new Float(Weights.get(i).get((j+1)*k)) * values.get(i).get(k);
                }
                values.get(i + 1).add(value / (1.0f * totalWeight));
            }
        }

        boolean[] buttons = new boolean[Environment.numberOfButtons];
        for (int i=0; i<values.get(values.size() - 1).size(); i++) {
            if (values.get(values.size() - 1).get(i) > Threshold) {
                buttons[i] = true;
                System.out.print("1");
            }
            else {
                buttons[i] = false;
                System.out.print("0");
            }
        }
        System.out.println();
        return buttons;
    }
}
