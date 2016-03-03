package ch.idsia.ai.agents.ai.NeuralNetworkAI;

import ch.idsia.mario.environments.Environment;
import jdk.internal.util.xml.impl.Input;
import sun.plugin.javascript.navig.Array;
import sun.plugin.javascript.navig4.Layer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.util.*;

/**
 * Created by Alex on 3/1/2016.
 */
public class NeuralNetwork {

    ArrayList<Integer> LayerNumbers = new ArrayList<Integer>();

    ArrayList< ArrayList<Integer> > Weights = new ArrayList<ArrayList<Integer>>();

    float Threshold = 0.46f;

    NeuralNetwork(Integer inputs, ArrayList<Integer> hiddenLayerNumbers, Integer outputs) {
        Calendar seed = Calendar.getInstance();
        Random random = new Random(seed.getTimeInMillis());

        LayerNumbers.add(inputs);
        LayerNumbers.addAll(hiddenLayerNumbers);
        LayerNumbers.add(outputs);
        for (int i=1; i<LayerNumbers.size(); i++) {
            Weights.add(new ArrayList<Integer>());
            for (int j=0; j < LayerNumbers.get(i - 1) * LayerNumbers.get(i); j++) {
                if (random.nextInt(100) > 0)
                    Weights.get(i - 1).add(random.nextInt(200) - 100);
                else
                    Weights.get(i - 1).add(0);
            }
        }
    }

    NeuralNetwork(File file) {
        try {
            Scanner input = new Scanner(file);

            String line = input.nextLine();
            Scanner lineInput = new Scanner(line);
            while (lineInput.hasNextInt()) {
                LayerNumbers.add(lineInput.nextInt());
            }

            while (input.hasNextLine()) {
                line = input.nextLine();
                lineInput = new Scanner(line);

                Weights.add(new ArrayList<Integer>());
                while (lineInput.hasNextInt()) {
                    Weights.get(Weights.size() - 1).add(lineInput.nextInt());
                }
            }
            input.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Could not find file " + file);
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
                values.get(i + 1).add(GetNeuronValue(value));
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

    float GetNeuronValue(float value) {
        float val = (1.0f / (1.0f + (float) Math.exp(value)));
        return val;
    }

    void OutputToFile(String filename)  {
        List<String> lines = new ArrayList<String>();
        lines.add("");
        for (int i=0; i<LayerNumbers.size(); i++) {
            lines.set(0, lines.get(0) + " " + LayerNumbers.get(i));
        }
        for (ArrayList<Integer> weights : Weights) {
            lines.add("");
            for (Integer weight : weights) {
                lines.set(lines.size() - 1, lines.get(lines.size() - 1) + weight.toString() + " ");
            }
        }
        Path file = Paths.get(filename);
        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        }
        catch (IOException e) {
            System.out.print("Could not write to file " + filename);
        }

    }
}
