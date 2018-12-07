package be.kul.gantry.domain;

import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Main {
    static ArrayList<Item>[][] storage;
    static HashMap<Item, Coordinaat> hashMap;
    static Integer layoutX;
    static Integer layoutY;
    static Problem prob;
    /* static ArrayList<Integer> order = new ArrayList<>(
             Arrays.asList(0, -1, 1, -2, 2, -3, 3, -4, 4, -5, 5, -6, 6, -7, 7, -8, 8, -9, 9));*/
    static ArrayList<Coordinaat> offsetEdge = new ArrayList<>();
    static ArrayList<Coordinaat> offsetCenter = new ArrayList<>();
    static ArrayList<Item> output = new ArrayList<>();
    static ArrayList<String> outputLog = new ArrayList<>();
    public static double timer = 0;
    public static double timer2 = 0;
    public static double lastTimer1 = 0;
    public static double lastTimerRelease1 = 0;
    public static double lastTimerRelease2 = 0;
    public static double lastTimer2 = 0;
    public static double checkTimer2 = 0;
    public static ArrayList<Coordinaat> obstructedStacks = new ArrayList<>();

    public static void main(String[] args) {
        File file = new File(args[0]);
        String outputFileName = args[1];

        hashMap = new HashMap<>();
        try {
            prob = Problem.fromJson(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(prob.isDoubleGantry());
        long curt = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j + i <= 50; j++) {
                if (isValidYValue(Math.abs(j + 1))) {
                    offsetEdge.add(new Coordinaat(i + 1, j + 1));
                }
                if (isValidYValue(Math.abs(j - 5))) {
                    offsetCenter.add(new Coordinaat(i - 5, j - 5));
                }
            }
        }
        offsetEdge.sort(null);
        offsetCenter.sort(null);
        layoutX = prob.isGeschrankt() ? (((prob.getMaxX() + prob.getMinX()) / 10) * 2) : ((prob.getMaxX() + prob.getMinX()) / 10);

        layoutY = prob.getMaxY() / 10;
        storage = new ArrayList[layoutX][layoutY];
        for (int i = 0; i < storage.length; i++) {
            for (int j = 0; j < storage[0].length; j++) {
                storage[i][j] = new ArrayList<>();
            }
        }
        for (Slot s : prob.getSlots()) {
            if (s.getItem() != null && s.getType() == Slot.SlotType.STORAGE) {

                storage[(s.getCenterX() - 5) / (prob.isGeschrankt() ? 5 : 10)][(s.getCenterY() - 5) / 10].add(/*s.getZ(),*/ s.getItem());
                hashMap.put(s.getItem(), new Coordinaat(((s.getCenterX() - 5) / (prob.isGeschrankt() ? 5 : 10)), (s.getCenterY() - 5) / 10));
            } else if (s.getType() == Slot.SlotType.INPUT) {
                prob.setInputSlot(s);
            } else if (s.getType() == Slot.SlotType.OUTPUT) {
                prob.setOutputSlot(s);
            }
        }

        // outputLog.add(prob.getGantries().get(0).toLog());
        for (Job job : prob.getOutputJobSequence()) {
            if (hashMap.containsKey(job.getItem())) {
                getFromStacked(getStack(hashMap.get(job.getItem())), job.getItem(), true);
                moveItemToOutput(job.getItem());
            } else {
                while (!job.isFinished()) {
                    if (prob.getInputJobSequence().get(0).getItem().getId() == job.getItem().getId()) {

                        hashMap.put(prob.getInputJobSequence().get(0).getItem(), prob.getInputSlotCoordinaat());
                        if (prob.isDoubleGantry()) {
                            int middenX=prob.isGeschrankt() ? ((prob.getMaxX() + prob.getMinX())/20) : ((prob.getMaxX() + prob.getMinX()) / 10);
                            int middenY=prob.getMaxY()/20;
                            findNearestStack(new Coordinaat(middenX, middenY), job.getItem());
                            moveItemToOutput(job.getItem());

                        } else {
                            moveItemToOutput(job.getItem());
                        }
                        output.add(job.getItem());
                        prob.getInputJobSequence().remove(0);

                        job.setFinished(true);


                    } else if (prob.getOutputJobSequenceItemId().contains(prob.getInputJobSequence().get(0).getItem().getId())) {
                        hashMap.put(prob.getInputJobSequence().get(0).getItem(), prob.getInputSlotCoordinaat());
                        findNearestStack(new Coordinaat(50, 5), prob.getInputJobSequence().get(0).getItem());

                        prob.getInputJobSequence().remove(0);


                    } else {
                        hashMap.put(prob.getInputJobSequence().get(0).getItem(), prob.getInputSlotCoordinaat());
                        moveItemCloseToEntrance(prob.getInputJobSequence().get(0).getItem());
                        prob.getInputJobSequence().remove(0);

                    }

                }
            }
        }
        while (!prob.getInputJobSequence().isEmpty()) {
            hashMap.put(prob.getInputJobSequence().get(0).getItem(), prob.getInputSlotCoordinaat());
            moveItemCloseToEntrance(prob.getInputJobSequence().get(0).getItem());
            prob.getInputJobSequence().remove(0);


        }
        outputLog.add(prob.getGantries().get(0).toLog());
        //  outputLog.add(prob.getGantries().get(1).toLog());

        System.out.println(System.currentTimeMillis() - curt);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
            for (String s : outputLog) {
                bw.write(s + "\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(System.currentTimeMillis() - curt);

        System.out.println(output);
    }


    private static void moveItemToOutput(Item item) {
        moveItem(item, prob.getOutputSlotCoordinaat());
        output.add(item);

    }

    private static void moveItemCloseToEntrance(Item item) {
        int highestValue = Integer.MIN_VALUE;
        Coordinaat highestValueCoord = new Coordinaat(-1, -1);
        for (int i = 0; i < offsetEdge.size(); i++) {
            int x = offsetEdge.get(i).getX();
            int y = offsetEdge.get(i).getY();
            if (isValidYValue(y)) {
                ArrayList<Item> stack = storage[x][y];
                boolean oddEven = (x & 1) == 0; //Even=true odd=False
                boolean validStack = false;
                if (prob.isGeschrankt()) {
                    if (isValidXValue(x - 1) && isValidXValue(x + 1)) {
                        if (oddEven) {
                            ArrayList<Item> stackLeft = storage[x - 1][y];
                            ArrayList<Item> stackRight = storage[x + 1][y];
                            validStack = stackLeft.size() == stack.size() && stackRight.size() == stack.size();


                        } else {
                            ArrayList<Item> stackLeft = storage[x - 1][y];
                            ArrayList<Item> stackRight = storage[x + 1][y];
                            validStack = stackLeft.size() + 1 == stack.size() && stackRight.size() + 1 == stack.size();


                        }
                    }
                } else {
                    validStack = true;
                }
                if (validStack &&
                        stack.size() < (prob.isGeschrankt() ? (oddEven ? ((int) (prob.getMaxLevels() / 2) + prob.getMaxLevels() % 2) :
                                (int) (prob.getMaxLevels() / 2)) : prob.getMaxLevels())) {
                    boolean containsOutputItems = false;
                    int highestValueInStack = Integer.MIN_VALUE;

                    for (int j = stack.size() - 1; j >= 0; j--) {
                        if (prob.getOutputJobSequenceItemId().contains(stack.get(j).getId())) {
                            containsOutputItems = true;
                            if (highestValueInStack <= prob.getOutputJobSequenceItemId().indexOf(stack.get(j).getId())) {
                                highestValueInStack = prob.getOutputJobSequenceItemId().indexOf(stack.get(j).getId());

                            }
                        }
                    }
                    if (containsOutputItems) {
                        if (highestValue < highestValueInStack) {
                            highestValue = highestValueInStack;
                            highestValueCoord.setX(x);
                            highestValueCoord.setY(y);
                        }
                    } else {
                        Coordinaat coord = new Coordinaat(x, y);

                        moveItem(item, coord);
                        return;
                    }
                }
            }
        }
        moveItem(item, highestValueCoord);
    }


    private static void moveItemCloseToExit(Item item) {
        int highestValue = Integer.MIN_VALUE;
        Coordinaat highestValueCoord = new Coordinaat(-1, -1);
        for (int i = 0; i < offsetEdge.size(); i++) {
            int y = offsetEdge.get(i).getY();
            int x = layoutX - offsetEdge.get(i).getX();
            if (isValidYValue(y)) {
                ArrayList<Item> stack = storage[x][y];
                boolean oddEven = (x & 1) == 0; //Even=true odd=False
                boolean validStack = false;
                if (prob.isGeschrankt()) {
                    if (isValidXValue(x - 1) && isValidXValue(x + 1)) {
                        if (oddEven) {
                            ArrayList<Item> stackLeft = storage[x - 1][y];
                            ArrayList<Item> stackRight = storage[x + 1][y];
                            validStack = stackLeft.size() == stack.size() && stackRight.size() == stack.size();


                        } else {
                            ArrayList<Item> stackLeft = storage[x - 1][y];
                            ArrayList<Item> stackRight = storage[x + 1][y];
                            validStack = stackLeft.size() - 1 == stack.size() && stackRight.size() - 1 == stack.size();


                        }
                    }
                } else {
                    validStack = true;
                }
                if (validStack && stack.size() < (prob.isGeschrankt() ? oddEven ? ((int) (prob.getMaxLevels() / 2) + prob.getMaxLevels() % 2) :
                        (int) (prob.getMaxLevels() / 2) : 4)) {
                    boolean containsOutputItems = false;
                    int highestValueInStack = Integer.MIN_VALUE;

                    for (int j = stack.size() - 1; j >= 0; j--) {
                        if (prob.getOutputJobSequenceItemId().contains(stack.get(j).getId())) {
                            containsOutputItems = true;
                            if (highestValueInStack <= prob.getOutputJobSequenceItemId().indexOf(stack.get(j).getId())) {
                                highestValueInStack = prob.getOutputJobSequenceItemId().indexOf(stack.get(j).getId());

                            }
                        }
                    }
                    if (containsOutputItems) {
                        if (highestValue < highestValueInStack) {
                            highestValue = highestValueInStack;
                            highestValueCoord.setX(x);
                            highestValueCoord.setY(y);
                        }
                    } else {
                        Coordinaat coord = new Coordinaat(x, y);

                        moveItem(item, coord);
                        return;
                    }
                }
            }
        }
        moveItem(item, highestValueCoord);

    }

    private static void moveItemSingleGantry(Item item, Coordinaat coord, Gantry gantry) {
        Coordinaat c = null;
        if (hashMap.containsKey(item)) {
            c = hashMap.get(item);
            if (isValidXValue(c.getX()) && isValidYValue(c.getY())) {
                storage[c.getX()][c.getY()].remove(item);
            }

        }
        outputLog.add(gantry.toLog());
        timer = timer + gantry.moveGantry(c);
        outputLog.add(gantry.toLog());
        timer = timer + prob.getPickupPlaceDuration();
        gantry.setItem(item);
        outputLog.add(gantry.toLog());

        if (isValidXValue(coord.getX()) && isValidYValue(coord.getY())) {
            ArrayList<Item> stack = storage[coord.getX()][coord.getY()];
            stack.add(item);
        }
        timer = timer + gantry.moveGantry(coord);
        outputLog.add(gantry.toLog());
        timer = timer + prob.getPickupPlaceDuration();

        gantry.setItem(null);
        outputLog.add(gantry.toLog());
        hashMap.put(item, coord);

    }


    //TODO: Dubbele kranen
    private static void moveItem(Item item, Coordinaat coord) {
        if (prob.getGantries().size() == 1) {
            moveItemSingleGantry(item, coord, prob.getGantries().get(0));
        } else {
            if (hashMap.containsKey(item)) {
                if (hashMap.get(item).equals(prob.getInputSlotCoordinaat())) {
                    if (item.getId() == 2627) {
                        System.out.println();
                    }
                    if (lastTimer2 == timer && lastTimerRelease2 > lastTimerRelease1) {
                        timer = lastTimerRelease2;
                        lastTimer2 = Integer.MIN_VALUE;
                    }


                    moveItemSingleGantry(item, coord, prob.getGantries().get(0));
                    outputLog.add(prob.getGantries().get(0).toLog());


                    lastTimerRelease1 = timer;
                    timer = timer + prob.getGantries().get(0).moveGantry(prob.getInputSlotCoordinaat());
                    outputLog.add(prob.getGantries().get(0).toLog());
                    lastTimer1 = timer;

                    timer = lastTimer2 > timer ? lastTimer2 : timer;


                } else {
                    if (item.getId() == 2600) {
                        System.out.println();
                    }
                    if (timer == lastTimer1 && lastTimerRelease1 > checkTimer2) {
                        timer = lastTimerRelease1;
                    }
                    lastTimerRelease2 = timer;
                    moveItemSingleGantry(item, coord, prob.getGantries().get(1));
                    checkTimer2 = timer;
                    lastTimer2 = timer;

                    timer = lastTimer1 > timer ? lastTimer1 : timer;


                }
            }
        }
    }

    public static ArrayList<Item> getStack(Coordinaat coordinaat) {
        int x;
        int y;
        x = coordinaat.getX();
        y = coordinaat.getY();
        return storage[x][y];


    }

    public static void getFromStacked(ArrayList<Item> stacked, Item item, boolean root) {
        boolean found = false;
        if (prob.isGeschrankt()) {
            Coordinaat c = hashMap.get(item);
            boolean oddEven = (c.getX() & 1) == 0; //Even=true odd=False

            if (oddEven) {
                if (isValidXValue(c.getX() - 1)) {
                    ArrayList<Item> stack = storage[c.getX() - 1][c.getY()];
                    if (stack.size() > stacked.indexOf(item)) {
                        getFromStacked(storage[c.getX() - 1][c.getY()], storage[c.getX() - 1][c.getY()].get(stacked.indexOf(item)), false);
                    }
                }
                if (isValidXValue(c.getX() + 1)) {
                    ArrayList<Item> stack = storage[c.getX() + 1][c.getY()];
                    if (stack.size() > stacked.indexOf(item)) {
                        getFromStacked(storage[c.getX() + 1][c.getY()], storage[c.getX() + 1][c.getY()].get(stacked.indexOf(item)), false);
                    }
                }
                verplaats(item);
            } else {
                if (isValidXValue(c.getX() - 1)) {
                    ArrayList<Item> stack = storage[c.getX() - 1][c.getY()];
                    if (stack.size() > stacked.indexOf(item) + 1) {
                        getFromStacked(storage[c.getX() - 1][c.getY()], storage[c.getX() - 1][c.getY()].get(stacked.indexOf(item) + 1), false);
                    }
                }
                if (isValidXValue(c.getX() + 1)) {
                    ArrayList<Item> stack = storage[c.getX() + 1][c.getY()];
                    if (stack.size() > stacked.indexOf(item) + 1) {
                        getFromStacked(storage[c.getX() + 1][c.getY()], storage[c.getX() + 1][c.getY()].get(stacked.indexOf(item) + 1), false);
                    }
                }
                if (!root) {
                    verplaats(item);
                }
            }
        } else {
            while (!found) {
                if (stacked.get(stacked.size() - 1).getId() == item.getId()) {
                    found = true;
                } else {
                    //TODO: Verplaats
                    verplaats(stacked.get(stacked.size() - 1));
                    // stacked.remove(stacked.size() - 1);
                }
            }
        }
        //TODO: Verplaats naar output
        // System.out.println("verplaats naar output");
        output.add(item);

    }

    public static void verplaats(Item item) {
        Coordinaat coord = hashMap.get(item);
        ArrayList<Item> stack = findNearestStack(coord, item);


    }

    private static ArrayList<Item> findNearestStack(Coordinaat coord, Item item) {
        boolean found = false;
        int highestValue = Integer.MIN_VALUE;
        Coordinaat highestValueCoord = new Coordinaat(-1, -1);

        for (int i = 1; i < offsetCenter.size(); i++) {
            if (offsetCenter.get(i).getY() != 0) {
                int x = coord.getX() + offsetCenter.get(i).getX();
                //TODO CHECK IF OFFSETCENTER X IS 0
                int y = coord.getY() + offsetCenter.get(i).getY();
                if (isValidXValue(x) && isValidYValue(y)) {
                    ArrayList<Item> stack = storage[x][y];
                    boolean oddEven = (x & 1) == 0; //Even=true odd=False
                    boolean validStack = false;

                    if (prob.isGeschrankt()) {
                        if (isValidXValue(x - 1) && isValidXValue(x + 1)) {
                            if (oddEven) {
                                ArrayList<Item> stackLeft = storage[x - 1][y];
                                ArrayList<Item> stackRight = storage[x + 1][y];
                                validStack = stackLeft.size() == stack.size() && stackRight.size() == stack.size();


                            } else {
                                ArrayList<Item> stackLeft = storage[x - 1][y];
                                ArrayList<Item> stackRight = storage[x + 1][y];
                                validStack = stackLeft.size() - 1 == stack.size() && stackRight.size() - 1 == stack.size();


                            }
                        }
                    } else {
                        validStack = true;
                    }
                    if (validStack && stack.size() < (prob.isGeschrankt() ? oddEven ? ((int) (prob.getMaxLevels() / 2) + prob.getMaxLevels() % 2) :
                            (int) (prob.getMaxLevels() / 2) : 4)) {
                        boolean containsOutputItems = false;
                        int highestValueInStack = Integer.MIN_VALUE;

                        for (int j = stack.size() - 1; j >= 0; j--) {
                            if (prob.getOutputJobSequenceItemId().contains(stack.get(j).getId())) {
                                containsOutputItems = true;
                                if (highestValueInStack <= prob.getOutputJobSequenceItemId().indexOf(stack.get(j).getId())) {
                                    highestValueInStack = prob.getOutputJobSequenceItemId().indexOf(stack.get(j).getId());

                                }
                            }
                        }
                        if (containsOutputItems) {
                            if (highestValue < highestValueInStack) {
                                highestValue = highestValueInStack;
                                highestValueCoord.setX(x);
                                highestValueCoord.setY(y);
                            }
                        } else {
                            Coordinaat newcoord = new Coordinaat(x, y);

                            moveItem(item, newcoord);
                            return stack;
                        }
                    }

                }

            }
        }
        moveItem(item, highestValueCoord);

        return storage[highestValueCoord.getX()][highestValueCoord.getY()];

    }


    private static boolean isValidYValue(int i) {
        return i >= 0 && i < 10;
    }

    private static boolean isValidXValue(int i) {
        return i >= 0 && i < (prob.isGeschrankt() ? 200 : 100);
    }
}