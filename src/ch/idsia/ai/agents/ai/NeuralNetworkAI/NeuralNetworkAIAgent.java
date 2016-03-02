package ch.idsia.ai.agents.ai.NeuralNetworkAI;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 12:30:41 AM
 * Package: ch.idsia.ai.agents.ai;
 */
public class NeuralNetworkAIAgent implements Agent
{
    protected boolean action[] = new boolean[Environment.numberOfButtons];
    protected String name = "NeuralNetworkAIAgent.Gir";
    protected NeuralNetwork neuralNetwork;

    public NeuralNetworkAIAgent(String s)
    {
        setName(s);

        ArrayList<Integer> hiddenLayers = new ArrayList<Integer>();
        hiddenLayers.add(121);
        hiddenLayers.add(49);
        neuralNetwork = new NeuralNetwork(968, hiddenLayers, 5);
        System.out.println("Made neural network");
    }

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];// Empty action
    }

    public boolean[] getAction(Environment observation)
    {
        byte[][] levelObservation = observation.getLevelSceneObservation();
        byte[][] enemyObservation = observation.getEnemiesObservation();
        ArrayList<Float> input = getNetInput(levelObservation, enemyObservation);
        return neuralNetwork.GetNetOutput(input); // Empty action
    }

    public ArrayList<Float> getNetInput(byte[][] level, byte[][] enemies) {
        ArrayList<Float> input = new ArrayList<Float>();
        for (byte[] bytelist : level) {
            for (byte b : bytelist) {
                input.add((float) b);
            }
        }
        for (byte[] bytelist : enemies) {
            for (byte b : bytelist) {
                input.add((float) b);
            }
        }
        return input;

    }

    public AGENT_TYPE getType()
    {
        return AGENT_TYPE.AI;
    }

    public String getName() {        return name;    }

    public void setName(String Name) { this.name = Name;    }
}
