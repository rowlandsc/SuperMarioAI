package ch.idsia.ai.agents.ai.NeuralNetworkAI;

import ch.idsia.mario.environments.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Alex on 3/1/2016.
 */
public class NeuralNetwork {

    ArrayList<Integer> LayerNumbers = new ArrayList<Integer>();

    ArrayList< ArrayList<Integer> > Weights = new ArrayList<ArrayList<Integer>>();

    float Threshold = 0.46f;
    float MutationRate = 0.1f;

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

    NeuralNetwork(NeuralNetwork parent, ArrayList<ArrayList<Integer>> newWeights){
        ArrayList<Integer> layerNumbers = new ArrayList<Integer>();
        layerNumbers = parent.LayerNumbers;
        this.LayerNumbers = layerNumbers;
        this.Weights = newWeights;
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
                //System.out.print("1");
            }
            else {
                buttons[i] = false;
                //System.out.print("0");
            }
        }
        //System.out.println();
        return buttons;
    }

    float GetNeuronValue(float value) {
        float val = (1.0f / (1.0f + (float) Math.exp(value)));
        return val;
    }

    public void OutputToFile(String filename)  {
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

    // Crossover 2 Neural Networks and return the child
    public NeuralNetwork Crossover(NeuralNetwork Parent2){

        // Just to make it more readable
        NeuralNetwork Parent1 = this;

        // What will be returned
        NeuralNetwork Child = null;

        // For the random numbers used here
        Calendar seed = Calendar.getInstance();
        Random random = new Random(seed.getTimeInMillis());

        // Get a crossover point
        int crossoverRow = random.nextInt(Parent1.Weights.size());
        int crossoverColumn = random.nextInt(Parent1.Weights.get(crossoverRow).size());

        // Will it mutate???
        float MutationChance = random.nextFloat();

        // Initialize the new weights array
        ArrayList<ArrayList<Integer>> childWeights = new ArrayList<ArrayList<Integer>>();

        for (int i=0; i<Parent1.Weights.size(); i++) {
            childWeights.add(new ArrayList<Integer>());
        }

        // - - - - - Crossover - - - - -

        // Go through all the rows
        for(int i = 0; i < Parent1.Weights.size(); ++i){

            /*// If the crossover row hasn't been reached, just copy parent 1
            if(i < crossoverRow){
                for(int j = 0; j < Parent1.Weights.get(i).size(); ++j){
                    childWeights.get(i).add(Parent1.Weights.get(i).get(j));
                }
            }

            // If we're at the crossover row, copy parent 1 until the specific
            // point is reached. Then start copying parent 2.
            if(i == crossoverRow){
                for(int j = 0; j < crossoverColumn; ++j){
                    childWeights.get(i).add(Parent1.Weights.get(i).get(j));
                }
                for(int j = crossoverColumn; j < Parent1.Weights.get(i).size(); ++j){
                    childWeights.get(i).add(Parent2.Weights.get(i).get(j));
                }
            }

            // Copy parent 2 now
            if(i > crossoverRow){
                for(int j = 0; j < Parent2.Weights.get(i).size(); ++j){
                    childWeights.get(i).add(Parent2.Weights.get(i).get(j));
                }
            }*/

            for (int j=0; j < Weights.get(i).size(); j++) {
                int r = random.nextInt(99);
                if (r < 33) {
                    childWeights.get(i).add(Parent1.Weights.get(i).get(j));
                }
                else if (r < 66) {
                    childWeights.get(i).add(Parent2.Weights.get(i).get(j));
                }
                else {
                    childWeights.get(i).add((Parent1.Weights.get(i).get(j) + Parent2.Weights.get(i).get(j)) / 2);
                }
            }
        }

        // - - - - - END CROSSOVER - - - - -

        // Mutate
        if(MutationChance <= this.MutationRate){

            float keepMutatingChance = random.nextFloat();

            while(keepMutatingChance < .99999f) {
                int swap1 = random.nextInt(Parent1.Weights.size());
                int swap2 = random.nextInt(Parent1.Weights.get(swap1).size());

                int swap3 = random.nextInt(Parent1.Weights.size());
                int swap4 = random.nextInt(Parent1.Weights.get(swap3).size());

                int temp = childWeights.get(swap1).get(swap2);
                childWeights.get(swap1).set(swap2, childWeights.get(swap3).get(swap4));
                childWeights.get(swap3).set(swap4, temp);
                keepMutatingChance = random.nextFloat();
            }
        }

        // Create Child
        Child = new NeuralNetwork(Parent1, childWeights);

        // Return Child
        return Child;
    }
}
