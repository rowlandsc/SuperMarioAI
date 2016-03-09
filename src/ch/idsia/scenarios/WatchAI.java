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

import java.io.File;
import java.util.*;

//import ch.idsia.ai.agents.icegic.robin.AStarAgent;
//import ch.idsia.ai.agents.icegic.peterlawford.SlowAgent;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstName_at_idsia_dot_ch
 * Date: May 7, 2009
 * Time: 4:35:08 PM
 * Package: ch.idsia
 */

public class WatchAI
{
    final static int numberOfTrials = 1;
    final static int generationSize = 20 ;

    static NeuralNetworkAIAgent agent = null;
    static double totalFitness;
    static int populationThrowawayPercentage = 2;

    final static boolean scoring = true;
    private static int killsSum = 0;
    private static int marioStatusSum = 0;
    private static int timeLeftSum = 0;
    private static int marioModeSum = 0;
    private static int currentGeneration = 1;
    private static int generatedAgents = 0;
    private static String name = "Gangsta";

    public static class SSBoolPair {
        public StatisticalSummary ss;
        public Boolean b;

        SSBoolPair(StatisticalSummary a, Boolean z) {
            ss = a;
            b = z;
        }
    }

    public static class DoubleBoolPair {
        public Double d;
        public Boolean b;

        DoubleBoolPair(Double a, Boolean z) {
            d = a;
            b = z;
        }
    }

    public static void main(String[] args) {
        CmdLineOptions cmdLineOptions = new CmdLineOptions(args);
        EvaluationOptions evaluationOptions = cmdLineOptions;  // if none options mentioned, all defalults are used.
        evaluationOptions.setLevelDifficulty(3);
        totalFitness =0;

        System.out.println("Enter AI file to load: ");
        Scanner s = new Scanner(System.in);
        name = s.nextLine();

        File newFile = new File(name);
        if (!newFile.exists()) {
            System.out.println("Could not find file: " + name);
            return;
        }

        agent = new NeuralNetworkAIAgent(newFile);

        if (scoring) {
            while (true) {
                scoreAllAgents(cmdLineOptions);
                currentGeneration++;
            }
        }
        else
        {
            Evaluator evaluator = new Evaluator(evaluationOptions);
            List<EvaluationInfo> evaluationSummary = evaluator.evaluate();
//        LOGGER.save("log.txt");
        }

        if (cmdLineOptions.isExitProgramWhenFinished())
            System.exit(0);
    }

    public static void scoreAllAgents(CmdLineOptions cmdLineOptions)
    {


        score(agent, 407, cmdLineOptions);

        System.out.println(agent.getCompScore() + " Score");
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

        boolean again = true;
        int i = 0;
        while (again && i < 100) {
            DoubleBoolPair res = testConfig (controller, options, startingSeed + i, i % 10, false);
            competitionScore += res.d;
            again = res.b;
            i++;
        }


        //Set comp score on agent
        agent.setCompScore(competitionScore);

        System.out.println("Competition score: " + competitionScore);
        System.out.println("Total kills Sum = " + killsSum);
        System.out.println("marioStatus Sum  = " + marioStatusSum);
        System.out.println("timeLeft Sum = " + timeLeftSum);
        System.out.println("marioMode Sum = " + marioModeSum);
        System.out.println("TOTAL SUM for " + agent.getName() + " = " + (competitionScore + killsSum + marioStatusSum + marioModeSum + timeLeftSum));
    }

    public static DoubleBoolPair testConfig (TimingAgent controller, EvaluationOptions options, int seed, int levelDifficulty, boolean paused) {
        options.setLevelDifficulty(levelDifficulty);
        options.setPauseWorld(paused);
        SSBoolPair res = test (controller, options, seed);
        StatisticalSummary ss = res.ss;
        double averageTimeTaken = controller.averageTimeTaken();
        System.out.printf("Difficulty %d score %.4f (avg time %.4f)\n",
                levelDifficulty, ss.mean(), averageTimeTaken);
        if (averageTimeTaken > 40) {
            System.out.println("Maximum allowed average time is 40 ms per time step.\n" +
                    "Controller disqualified");
            System.exit (0);
        }

        DoubleBoolPair res2 = new DoubleBoolPair(ss.mean(), res.b);

        return res2;
    }

    public static SSBoolPair test (Agent controller, EvaluationOptions options, int seed) {
        StatisticalSummary ss = new StatisticalSummary ();
        Boolean won = false;
        int kills = 0;
        int timeLeft = 0;
        int marioMode = 0;
        int marioStatus = 0;

        float totalDistance = 0;

        options.setNumberOfTrials(numberOfTrials);
        options.resetCurrentTrial();
        int rounds = 1;
        for (int i = 0; i < numberOfTrials; i++) {
            options.setLevelRandSeed(seed + i);
            options.setLevelLength (200 + (i * 128) + (seed % (i + 1)));
            options.setLevelType(i % 3);
            options.setLevelDifficulty(options.getLevelDifficulty());
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

            double x = result.computeDistancePassed();
            double y = result.computeBasicFitness();
            double z = result.livesLeft;

            if (result.livesLeft > 0 && result.timeLeft > 0) won = true;
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

        SSBoolPair res = new SSBoolPair(ss, won);

        return res;
    }
}
