/*
 Copyright (C) 2020 Eric Robert
    roberteric@laposte.net

	 This library is free software; you can redistribute it and/or
	 modify it under the terms of the GNU Library General Public
	 License as published by the Free Software Foundation; either
	 version 2 of the License, or (at your option) any later version.

		 This library is distributed in the hope that it will be useful,
	 but WITHOUT ANY WARRANTY; without even the implied warranty of
		 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
		 Library General Public License for more details.

		 You should have received a copy of the GNU Library General Public License
		 along with this library; see the file COPYING.LIB.  If not, write to
		 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
	 Boston, MA 02111-1307, USA.

		 */

//branche animations?

package com.ricax.yatzoe;

// class Jeu


import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Jeu {
    private final MainActivity mainActivity;
    Figure fiveDices;
    final Box[][] checkerBox;
    int throwNb = 0;
    int maxThrowNb = 3;
    ArrayList<Integer> diceArrayList = new ArrayList<>();
    //Human player's color got to modify this somehow
    String couleur = "blue";
    boolean appelClicked = false; //First: User clicked on appel imageView
    String appelRegistered = ""; //Second: the appel figure is defined by clicking on an "appel" figure
    int appelFigTypeBoxId; //to retrieve an appel figure box when needed
    int appelBoxId; //To remember the appel box we las activated

    int redMarkers = 12, blueMarkers = 12;
    int redPoints = 0, bluePoints = 0;

    //pour logger
    Date aujourdhui = new Date();
    SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.FRANCE);
    public String dateFormat = formater.format(aujourdhui);

    Jeu(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        checkerBox = new Box[5][5];
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++) {
                checkerBox[i][j] = new Box(i, j);
            }
    }

    //constructeur de copie
    Jeu(Jeu game) {
        fiveDices = new Figure(game.fiveDices);
        checkerBox = new Box[5][5];
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++) {
                checkerBox[i][j] = new Box(game.checkerBox[i][j]);
            }

        mainActivity = game.mainActivity;
        couleur = game.couleur;
        throwNb = game.throwNb;
        maxThrowNb = game.maxThrowNb;
        appelClicked = game.appelClicked;
        appelRegistered = game.appelRegistered;
        appelFigTypeBoxId = game.appelFigTypeBoxId;
        appelBoxId = game.appelBoxId;
        redMarkers = game.redMarkers;
        blueMarkers = game.blueMarkers;
        redPoints = game.redPoints;
        bluePoints = game.bluePoints;
        diceArrayList = game.diceArrayList;
        aujourdhui=game.aujourdhui;
        formater=game.formater;
        dateFormat=game.dateFormat;
    }


    public Box findBoxById(int aBoxId) {
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++)
                if (checkerBox[i][j].getId() == aBoxId)
                    return checkerBox[i][j];
        return null;
    }


    protected boolean fullLine(String color, int boxId) {
        return countLine(5, color, boxId) >= 1;
    }

    private int hCountline(int markerNb, String color, int v, int h) {
        int totalLines = 0;
        int totalBoxes = 0;
        //Horizontally
        for (int i = -(markerNb - 1); i <= (markerNb - 1); i++)
            if ((h + i >= 0) && (h + i <= 4)) {
                if (checkerBox[v][h + i].getColor().equals(color))
                    totalBoxes++;
                else
                    totalBoxes = 0;
                if (totalBoxes == markerNb) {
                    totalLines++;
                    totalBoxes--;
                }
            }
        return totalLines;
    }

    private int vCountline(int markerNb, String color, int v, int h) {
        int totalLines = 0;
        int totalBoxes = 0;
        for (int i = -(markerNb - 1); i <= (markerNb - 1); i++)
            if ((v + i >= 0) && (v + i <= 4)) {
                if (checkerBox[v + i][h].getColor().equals(color))
                    totalBoxes++;
                else
                    totalBoxes = 0;
                if (totalBoxes == markerNb) {
                    totalLines++;
                    totalBoxes--;
                }
            }
        return totalLines;
    }

    private int r2lCountline(int markerNb, String color, int v, int h) {
        int totalLines = 0;
        int totalBoxes = 0;
        for (int i = -(markerNb - 1); i <= (markerNb - 1); i++)
            if (((h + i >= 0) && (h + i <= 4)) && ((v - i >= 0) && (v - i <= 4))) {
                if (checkerBox[v - i][h + i].getColor().equals(color))
                    totalBoxes++;
                else
                    totalBoxes = 0;
                if (totalBoxes == markerNb) {
                    totalLines++;
                    totalBoxes--;
                }
            }
        return totalLines;
    }

    private int l2rCountline(int markerNb, String color, int v, int h) {
        int totalLines = 0;
        int totalBoxes = 0;
        for (int i = -(markerNb - 1); i <= (markerNb - 1); i++)
            if ((h + i >= 0) && (h + i <= 4) && (v + i >= 0) && (v + i <= 4)) {
                if (checkerBox[v + i][h + i].getColor().equals(color))
                    totalBoxes++;
                else
                    totalBoxes = 0;
                if (totalBoxes == markerNb) {
                    totalLines++;
                    totalBoxes--;
                }
            }
        return totalLines;
    }

    int countLine(int markerNb, String color, int boxID) {
        int h = findBoxById(boxID).h;
        int v = findBoxById(boxID).v;
        int totalLines = 0;
        //Horizontally
        totalLines += hCountline(markerNb, color, v, h);
        //vertically
        totalLines += vCountline(markerNb, color, v, h);
        //upleft to bottomright
        totalLines += l2rCountline(markerNb, color, v, h);
        //upright to bottomleft
        totalLines += r2lCountline(markerNb, color, v, h);
        return totalLines;
    }

    boolean endOfGame(int boxId) {
        return (this.blueMarkers == 0) || (this.redMarkers == 0) || (this.fullLine(this.couleur, boxId));
    }

    String showEndOfGame() {
        if (bluePoints > redPoints) {
            return "blueWin";
        } else if (redPoints > bluePoints) {
            return "redWin";
        }
        return "draw";
    }

    void changeTurnColor(String color) {
        System.out.println("changeTurnColor: "+color);
        this.couleur = color;
        this.throwNb = 0;
        //remettre les couleurs de box appel à blanc
        if (appelBoxId>0)
            mainActivity.ungrayAppelBoxOrFigAppelBoxToPreviousState(this.appelBoxId);
        if (appelFigTypeBoxId>0)
            mainActivity.ungrayAppelBoxOrFigAppelBoxToPreviousState(this.appelFigTypeBoxId);
        this.appelBoxId = 0;
        this.appelClicked = false;
        this.appelFigTypeBoxId = 0;
        this.appelRegistered = "";
        for (int i = 0; i < 5; i++) {
            //Don't change dice.value so it is coherent with the current diceImage
            this.fiveDices.diceSet[i].isSelected = false;
            this.fiveDices.diceSet[i].color = "white";
            mainActivity.UI_setDiceScale(this.fiveDices.diceSet[i].id, "big");
        }
        switch (couleur) {
            case "red":
                mainActivity.changeTurnColorTextviewMessage("red");
                machinePlayThread();
                break;
            case "blue":
                mainActivity.changeTurnColorTextviewMessage("blue");
                break;
            case "white":
                mainActivity.changeTurnColorTextviewMessage("white");
                break;
        }
    }


    private void machinePlayThread() {
        new Thread(new MachinePlayTask(this, mainActivity)).start();
    }

    public void setDiceArrayListRandomValue() {
        diceArrayList.add(1 + (int) (Math.random() * 6));
    }

    void toggleSelectDice(int i) {
        this.fiveDices.diceSet[i].toggleDiceSelected();
    }


    //  private  static Random random =  new Random() ;
    private void rollOneDice(int i) {
        //SecureRandom secureRandomGenerator = SecureRandom.getInstance("SHA1PRNG", "SUN");
        SecureRandom secureRandomGenerator = new SecureRandom();
        int randInRange = secureRandomGenerator.nextInt(6);
        fiveDices.diceSet[i].value = randInRange + 1;
        // fiveDices.diceSet[i].value=getRandomValue();
        //      fiveDices.diceSet[i].value = 1 + (int) (Math.random() * 6);
        //           fiveDices.diceSet[i].value=ThreadLocalRandom.current().nextInt(5)+1;
        //    fiveDices.diceSet[i].value=1+random.nextInt(6) ;
    }

    boolean throwDices() {
        this.fiveDices.figureList = "";
        if (this.throwNb >= this.maxThrowNb) {
            fiveDices.setListOfFiguresFromDiceSet(); //To be able to place the marker after
            return false;
        }
        if ((appelClicked) && (appelRegistered.isEmpty())) {
            return false;
        }
        if (this.throwNb == 0) { //first throw check if all dices are selected
            for (int i = 0; i < 5; i++)
                if (!this.fiveDices.diceSet[i].selected()) {
                    return false;//return if one dice at least is not selected
                }
            throwNb++;
            for (int j = 0; j < 5; j++)
                rollOneDice(j); //then roll all dices
            fiveDices.setListOfFiguresFromDiceSet();
        } else { //second & third throw
            throwNb++;
            for (int i = 0; i < 5; i++)
                if (fiveDices.diceSet[i].selected()) //roll all selected dices
                    rollOneDice(i);
            if ((throwNb == 2) || (throwNb == this.maxThrowNb)) {
                fiveDices.setListOfFiguresFromDiceSet();
                if ((appelClicked) && (fiveDices.figureList.contains(appelRegistered))) {//Appel OK, set figureList to appel
                    fiveDices.figureList = "Appel";
                } else if (throwNb == maxThrowNb)
                    if ((appelClicked) && (!fiveDices.figureList.contains(appelRegistered))) {
                        fiveDices.figureList = "";
                    }
            }
        }
        return true;
    }




    public String printSelectedDice() {
        System.out.println("**SelectedDice**");
        StringBuilder selectedDiceSet = new StringBuilder("\n");
        for (int i = 0; i < 5; i++) {
            selectedDiceSet.append(this.fiveDices.diceSet[i].value).append(" ");
        }
        selectedDiceSet.append("\n");
        for (int i = 0; i < 5; i++) {
            if (this.fiveDices.diceSet[i].isSelected)
                selectedDiceSet.append("x ");
            else
                selectedDiceSet.append("o ");
        }
        return selectedDiceSet.toString();
    }

    void terminate() {
        this.couleur = "white";
    }

    String afficherDes() {
        return fiveDices.printDiceSet();
    }

    ArrayList<Box> getListFreeBoxPerFigureList(String aFigureList) {
        ArrayList<Box> aBoxPairList = new ArrayList<>();
        List<String> aListOfFigures = new ArrayList<>();
        Pattern aPattern = Pattern.compile("1|2|3|4|5|6|Appel|Full|Carre|Small|Suite|Sec|Yam");
        Matcher aMatcher = aPattern.matcher(aFigureList);
        while (aMatcher.find()) {
            aListOfFigures.add(aMatcher.group());
        }
        for (int i = 0; i < aListOfFigures.size(); i++) {
            aBoxPairList.addAll(getListBoxColorPerFigure(aListOfFigures.get(i), "white"));
        }
        return aBoxPairList;
    }

   public  ArrayList<Box> getListBoxColorPerFigure(String aFigure, String aColor) {
        ArrayList<Box> aBoxList = new ArrayList<>();
        if (aFigure.equals("1")) {
            if ((this.checkerBox[0][0].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[0][0]);
            }
            if ((this.checkerBox[3][4].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[3][4]);
            }
        }
        if (aFigure.equals("2")) {
            if ((this.checkerBox[1][0].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[1][0]);
            }
            if ((this.checkerBox[4][1].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[4][1]);
            }
        }
        if (aFigure.equals("3")) {
            if ((this.checkerBox[0][1].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[0][1]);
            }
            if ((this.checkerBox[4][0].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[4][0]);
            }
        }
        if (aFigure.equals("4")) {
            if ((this.checkerBox[0][3].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[0][3]);
            }
            if ((this.checkerBox[4][4].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[4][4]);
            }
        }
        if (aFigure.equals("5")) {
            if ((this.checkerBox[1][4].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[1][4]);
            }
            if ((this.checkerBox[4][3].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[4][3]);
            }
        }
        if (aFigure.equals("6")) {
            if ((this.checkerBox[0][4].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[0][4]);
            }
            if ((this.checkerBox[3][0].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[3][0]);
            }
        }
        if (aFigure.equals("Appel")) {
            if ((this.checkerBox[0][2].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[0][2]);
            }
            if ((this.checkerBox[2][3].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[2][3]);
            }
        }
        if (aFigure.equals("Carre")) {
            if ((this.checkerBox[1][1].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[1][1]);
            }
            if ((this.checkerBox[4][2].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[4][2]);
            }
        }
        if (aFigure.equals("Sec")) {
            if ((this.checkerBox[1][2].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[1][2]);
            }
            if ((this.checkerBox[3][1].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[3][1]);
            }
        }
        if (aFigure.equals("Full")) {
            if ((this.checkerBox[1][3].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[1][3]);
            }
            if ((this.checkerBox[2][1].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[2][1]);
            }
        }
        if (aFigure.equals("Small")) {
            if ((this.checkerBox[2][0].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[2][0]);
            }
            if ((this.checkerBox[3][3].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[3][3]);
            }
        }
        if (aFigure.equals("Suite")) {
            if ((this.checkerBox[2][4].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[2][4]);
            }
            if ((this.checkerBox[3][2].getColor().equals(aColor))) {
                aBoxList.add(this.checkerBox[3][2]);
            }
        }
        if (aFigure.equals("Yam")) {
            if (this.checkerBox[2][2].getColor().equals(aColor)) {
                aBoxList.add(this.checkerBox[2][2]);
            }

        }
       return aBoxList;
    }



    //returns a List of BoxPair  with such color per figure (points =0)
   public  List<BoxPair> getListBoxPairColorPerFigure (String aFigure, String aColor){
            List<BoxPair> aBoxPairList = new ArrayList<>();
            if (aFigure.equals("1")) {
                if ((this.checkerBox[0][0].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[0][0], 0, 0));
                }
                if ((this.checkerBox[3][4].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[3][4], 0, 0));
                }
            }
            if (aFigure.equals("2")) {
                if ((this.checkerBox[1][0].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[1][0], 0, 0));
                }
                if ((this.checkerBox[4][1].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[4][1], 0, 0));
                }
            }
            if (aFigure.equals("3")) {
                if ((this.checkerBox[0][1].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[0][1], 0, 0));
                }
                if ((this.checkerBox[4][0].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[4][0], 0, 0));
                }
            }
            if (aFigure.equals("4")) {
                if ((this.checkerBox[0][3].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[0][3], 0, 0));
                }
                if ((this.checkerBox[4][4].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[4][4], 0, 0));
                }
            }
            if (aFigure.equals("5")) {
                if ((this.checkerBox[1][4].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[1][4], 0, 0));
                }
                if ((this.checkerBox[4][3].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[4][3], 0, 0));
                }
            }
            if (aFigure.equals("6")) {
                if ((this.checkerBox[0][4].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[0][4], 0, 0));
                }
                if ((this.checkerBox[3][0].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[3][0], 0, 0));
                }
            }
            if (aFigure.equals("Appel")) {
                if ((this.checkerBox[0][2].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[0][2], 0, 0));
                }
                if ((this.checkerBox[2][3].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[2][3], 0, 0));
                }
            }
            if (aFigure.equals("Carre")) {
                if ((this.checkerBox[1][1].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[1][1], 0, 0));
                }
                if ((this.checkerBox[4][2].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[4][2], 0, 0));
                }
            }
            if (aFigure.equals("Sec")) {
                if ((this.checkerBox[1][2].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[1][2], 0, 0));
                }
                if ((this.checkerBox[3][1].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[3][1], 0, 0));
                }
            }
            if (aFigure.equals("Full")) {
                if ((this.checkerBox[1][3].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[1][3], 0, 0));
                }
                if ((this.checkerBox[2][1].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[2][1], 0, 0));
                }
            }
            if (aFigure.equals("Small")) {
                if ((this.checkerBox[2][0].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[2][0], 0, 0));
                }
                if ((this.checkerBox[3][3].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[3][3], 0, 0));
                }
            }
            if (aFigure.equals("Suite")) {
                if ((this.checkerBox[2][4].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[2][4], 0, 0));
                }
                if ((this.checkerBox[3][2].getColor().equals(aColor))) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[3][2], 0, 0));
                }
            }
            if (aFigure.equals("Yam")) {
                if (this.checkerBox[2][2].getColor().equals(aColor)) {
                    aBoxPairList.add(new BoxPair(this.checkerBox[2][2], 0, 0));
                }
            }
            return aBoxPairList;
        }

    public  Figure getFiveDices(){
            return this.fiveDices;
        }
}
