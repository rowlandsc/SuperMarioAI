package ch.idsia.scenarios.test;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.AgentsPool;
import ch.idsia.ai.agents.ai.NeuralNetworkAI.NeuralNetwork;
import ch.idsia.ai.agents.ai.NeuralNetworkAI.NeuralNetworkAIAgent;
import ch.idsia.ai.agents.ai.TimingAgent;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.Evaluator;
import ch.idsia.utils.StatisticalSummary;
import com.sun.org.apache.xpath.internal.SourceTree;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

//import ch.idsia.ai.agents.icegic.robin.AStarAgent;
//import ch.idsia.ai.agents.icegic.peterlawford.SlowAgent;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstName_at_idsia_dot_ch
 * Date: May 7, 2009
 * Time: 4:35:08 PM
 * Package: ch.idsia
 */

public class GeneticAlgorithmRun
{
    final static int numberOfTrials = 1;
    final static int generationSize = 20 ;


    static Agent[] population;
    static double totalFitness;
    static int populationThrowawayPercentage = 2;

    final static boolean scoring = true;
    private static int killsSum = 0;
    private static int marioStatusSum = 0;
    private static int timeLeftSum = 0;
    private static int marioModeSum = 0;

    public static void main(String[] args) {
        CmdLineOptions cmdLineOptions = new CmdLineOptions(args);
        EvaluationOptions evaluationOptions = cmdLineOptions;  // if none options mentioned, all defalults are used.
        totalFitness =0;
        createAgentsPool();


        if (scoring)
            scoreAllAgents(cmdLineOptions);
        else
        {
            Evaluator evaluator = new Evaluator(evaluationOptions);
            List<EvaluationInfo> evaluationSummary = evaluator.evaluate();
//        LOGGER.save("log.txt");
        }

        if (cmdLineOptions.isExitProgramWhenFinished())
            System.exit(0);
    }

    private static boolean calledBefore = false;
    public static void createAgentsPool()
    {
        population = new Agent[generationSize];
        for (int i=0; i<generationSize; i++) {
            NeuralNetworkAIAgent jawn = new NeuralNetworkAIAgent("Bryan" + i);
            population[i] = jawn;
            AgentsPool.addAgent(jawn);
        }
    }

    public static void scoreAllAgents(CmdLineOptions cmdLineOptions)
    {
        double parent1 = 0;
        double parent2 = 0;
        double worstChild = 1000000;

        NeuralNetworkAIAgent p1 = null;
        NeuralNetworkAIAgent p2 = null;
        NeuralNetworkAIAgent wChild = null;
        int i = 1;
        for (Agent agent : population)
        {

            System.out.println("I am here.");

            score((NeuralNetworkAIAgent) agent, 3143, cmdLineOptions);
            if (parent1 < agent.getCompScore())
            {
                p2 = p1;
                p1 = (NeuralNetworkAIAgent) agent;
                parent2 = parent1;
                parent1 = agent.getCompScore();
            }
            else if(parent2 < agent.getCompScore())
            {
                p2 = (NeuralNetworkAIAgent) agent;
                parent2 = agent.getCompScore();
            }
            if (worstChild > agent.getCompScore())
            {
                worstChild = agent.getCompScore();
                wChild = (NeuralNetworkAIAgent) agent;
            }/*
            if (i%5 ==0)
            {
                //crossing over best parents
                wChild.setNeuralNetwork(p1.getNeuralNetwork().Crossover(p2.getNeuralNetwork()));

                System.out.println("Crossing over p1 and p2 to replace " + wChild.getName());
                //adding new better child back to population
                AgentsPool.addAgent(wChild);
            } */

            System.out.println(agent.getCompScore() + " Score");
            System.out.println(parent1 + " Parent 1");
            System.out.println(parent2 + " Parent 2");
            System.out.println(worstChild + " Worst Child");
            i++;
        }

        System.out.println(p1.getName() + " Parent 1");
        System.out.println(p2.getName() + " Parent 2");
        System.out.println(wChild.getName() + " Worst Child/n");

        SortByFitness();
        setTotalFitness();
        setAccumulatedFitness();
        addNewGeneration();

        AgentsPool.getAgentsCollection().clear();

        for(int j=0; j < generationSize; ++j) {
            AgentsPool.addAgent(population[j]);
        }

        DisplayPopulation();
        scoreAllAgents(cmdLineOptions);


//        //crossing over best parents
//        wChild.setNeuralNetwork(p1.getNeuralNetwork().Crossover(p2.getNeuralNetwork()));
//
//       //adding new better child back to population
//        AgentsPool.addAgent(wChild);

    }

    static void addNewGeneration(){
        for(int i = 0; i < generationSize/populationThrowawayPercentage; ++i){
            population[generationSize - i - 1] = SelectAndCrossover();
        }
    }

    public static void SortByFitness(){
        Agent[] sortedPopulation = new Agent[generationSize];
        Agent c = null;
        for(int i=0; i < generationSize; ++i){
            double max = -1;
            int index = 0;
            for(int j=0; j<generationSize; ++j){
                if(population[j] != null){
                    double score = population[j].getCompScore();
                    if(max < score){
                        max = score;
                        c = population[j];
                        index = j;
                    }
                }
            }
            sortedPopulation[i] = (NeuralNetworkAIAgent)c;
            population[index] = null;
        }
        population = sortedPopulation;
    }

    static void setTotalFitness(){
        totalFitness = 0;
        for(int i=0; i<generationSize; ++i){
            totalFitness += population[i].getCompScore();
        }
    }

    static void setAccumulatedFitness(){
        double accFitnessValue = 0;
        for(int i = 0; i < generationSize; ++i){
            accFitnessValue += population[i].getCompScore() / totalFitness;
            ((NeuralNetworkAIAgent)population[i]).accumulatedFitness = accFitnessValue;
        }
    }

    static NeuralNetworkAIAgent SelectAndCrossover(){
        NeuralNetworkAIAgent[] selectedParents = new NeuralNetworkAIAgent[2];

        // For the random numbers used here
        Calendar seed = Calendar.getInstance();
        Random random = new Random(seed.getTimeInMillis());

        for(int i=0; i < 2; ++i){
            float chance = random.nextFloat();
            for(int j=0; j<generationSize; ++j){
                NeuralNetworkAIAgent n = (NeuralNetworkAIAgent)population[j];
                if(selectedParents[0] != null) {
                    if (!selectedParents[0].equals(n)) {
                        if (n.accumulatedFitness > chance) {
                            selectedParents[i] = n;
                            break;
                        }
                    }
                }
                else if(n.accumulatedFitness > chance){
                        selectedParents[i] = n;
                        break;
                }
                if(j == generationSize - 1){
                    selectedParents[i] = n;
                }
            }
        }

        if(selectedParents[0] == null || selectedParents[1] == null){
            System.out.println("NO PARENT SELECTED");
        }

        NeuralNetworkAIAgent child = new NeuralNetworkAIAgent("Child Of " + selectedParents[0].getName() + " + " + selectedParents[1].getName());
        child.setNeuralNetwork(((NeuralNetworkAIAgent) selectedParents[0]).getNeuralNetwork().Crossover(((NeuralNetworkAIAgent)selectedParents[1]).getNeuralNetwork()));
        return child;
    }

    public static void DisplayPopulation(){
        System.out.println();
        for(int i = 0; i < generationSize; ++i){
            System.out.println(population[i].getName());
        }
    }

    public static void score(NeuralNetworkAIAgent agent, int startingSeed, CmdLineOptions cmdLineOptions) {
        TimingAgent controller = new TimingAgent (agent);
//        RegisterableAgent.registerAgent (controller);
//        EvaluationOptions options = new CmdLineOptions(new String[0]);
        EvaluationOptions options = cmdLineOptions;

        options.setNumberOfTrials(numberOfTrials); // numberof trials here or 1?
//        options.setVisualization(false);
//        options.setMaxFPS(true);
        System.out.println("\nScoring controller " + agent.getName() + " with starting seed " + startingSeed);

        double competitionScore = 0;
        killsSum = 0;
        marioStatusSum = 0;
        timeLeftSum = 0;
        marioModeSum = 0;

        competitionScore += testConfig (controller, options, startingSeed, 0, false);
        //competitionScore += testConfig (controller, options, startingSeed, 3, false);
        //competitionScore += testConfig (controller, options, startingSeed, 5, false);
        //competitionScore += testConfig (controller, options, startingSeed, 10, false);

        //Set comp score on agent
        agent.setCompScore(competitionScore);

        System.out.println("Competition score: " + competitionScore);
        System.out.println("Total kills Sum = " + killsSum);
        System.out.println("marioStatus Sum  = " + marioStatusSum);
        System.out.println("timeLeft Sum = " + timeLeftSum);
        System.out.println("marioMode Sum = " + marioModeSum);
        System.out.println("TOTAL SUM for " + agent.getName() + " = " + (competitionScore + killsSum + marioStatusSum + marioModeSum + timeLeftSum));
    }

    public static double testConfig (TimingAgent controller, EvaluationOptions options, int seed, int levelDifficulty, boolean paused) {
        options.setLevelDifficulty(levelDifficulty);
        options.setPauseWorld(paused);
        StatisticalSummary ss = test (controller, options, seed);
        double averageTimeTaken = controller.averageTimeTaken();
        System.out.printf("Difficulty %d score %.4f (avg time %.4f)\n",
                levelDifficulty, ss.mean(), averageTimeTaken);
        if (averageTimeTaken > 40) {
            System.out.println("Maximum allowed average time is 40 ms per time step.\n" +
                    "Controller disqualified");
            System.exit (0);
        }
        return ss.mean();
    }

    public static StatisticalSummary test (Agent controller, EvaluationOptions options, int seed) {
        StatisticalSummary ss = new StatisticalSummary ();
        int kills = 0;
        int timeLeft = 0;
        int marioMode = 0;
        int marioStatus = 0;

        options.setNumberOfTrials(numberOfTrials);
        options.resetCurrentTrial();
        for (int i = 0; i < numberOfTrials; i++) {
            options.setLevelRandSeed(seed + i);
            options.setLevelLength (200 + (i * 128) + (seed % (i + 1)));
            options.setLevelType(i % 3);
            controller.reset();
            options.setAgent(controller);
            Evaluator evaluator = new Evaluator (options);
            EvaluationInfo result = evaluator.evaluate().get(0);
            kills += result.computeKillsTotal();
            timeLeft += result.timeLeft;
            marioMode += result.marioMode;
            marioStatus += result.marioStatus;
//            System.out.println("\ntrial # " + i);
//            System.out.println("result.timeLeft = " + result.timeLeft);
//            System.out.println("result.marioMode = " + result.marioMode);
//            System.out.println("result.marioStatus = " + result.marioStatus);
//            System.out.println("result.computeKillsTotal() = " + result.computeKillsTotal());
            ss.add (result.computeDistancePassed());
        }

        System.out.println("\n===================\nStatistics over 10 runs for " + controller.getName());
        System.out.println("Total kills = " + kills);
        System.out.println("marioStatus = " + marioStatus);
        System.out.println("timeLeft = " + timeLeft);
        System.out.println("marioMode = " + marioMode);
        System.out.println("===================\n");

        killsSum += kills;
        marioStatusSum += marioStatus;
        timeLeftSum += timeLeft;
        marioModeSum += marioMode;

        return ss;
    }
}
