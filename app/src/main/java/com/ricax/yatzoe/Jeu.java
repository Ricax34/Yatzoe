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


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import static java.lang.Thread.sleep;

class Jeu {
    private final MainActivity mainActivity;
    Figure fiveDices;
    final Box[][] checkerBox;
    int throwNb = 0;
    int maxThrowNb = 3;

    //Human player's color got to modify this somehow
    String couleur = "blue";
    boolean appelClicked = false; //First: User clicked on appel imageView
    String appelRegistered = ""; //Second: the appel figure is defined by clicking on an "appel" figure
    int appelFigTypeBoxId; //to retrieve an appel figure box when needed
    int appelBoxId; //To remember the appel box we las activated

    int redMarkers = 12, blueMarkers = 12;
    int redPoints = 0, bluePoints = 0;


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
    }


    Box findBoxById(int aBoxId) {
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++)
                if (checkerBox[i][j].id == aBoxId)
                    return checkerBox[i][j];
        return null;
    }

    void toggleSelectDice(int i) {
        this.fiveDices.diceSet[i].toggleDiceSelected();
    }


    protected boolean fullLine(String color, int boxId) {
        return countLine(5, color, boxId) == 1;
    }

    private int hCountline(int markerNb, String color, int v, int h) {
        int totalLines = 0;
        int totalBoxes = 0;
        //Horizontally
        for (int i = -(markerNb - 1); i <= (markerNb - 1); i++)
            if ((h + i >= 0) && (h + i <= 4)) {
                if (checkerBox[v][h + i].color.equals(color))
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
                if (checkerBox[v + i][h].color.equals(color))
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
                if (checkerBox[v - i][h + i].color.equals(color))
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
                if (checkerBox[v + i][h + i].color.equals(color))
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

        this.couleur = color;
        this.throwNb = 0;
        this.appelBoxId = 0;
        this.appelClicked = false;
        this.appelFigTypeBoxId = 0;
        this.appelRegistered = "";
        for (int i = 0; i < 5; i++) {
            //Don't change dice.value so it is coherent with the current diceImage
            this.fiveDices.diceSet[i].isSelected = false;
            this.fiveDices.diceSet[i].color = "white";
            mainActivity.UI_setDiceColor(this.fiveDices.diceSet[i].id, "white");
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

    private void rollOneDice(int i) {
        fiveDices.diceSet[i].value = 1 + (int) (Math.random() * ((6 - 1) + 1));
    }

    boolean throwDices() {
        this.fiveDices.figureList = "";
        if (this.throwNb >= this.maxThrowNb) {
            fiveDices.setListOfFiguresFromDiceSet(); //To be able to place the marker after
            return false;
        }
        if ((appelClicked)&&(appelRegistered.isEmpty())){
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
            if ((throwNb == 2)||(throwNb==this.maxThrowNb)) {
                fiveDices.setListOfFiguresFromDiceSet();
                if ((appelClicked) && (fiveDices.figureList.contains(appelRegistered))) {//Appel OK, set figureList to appel
                    fiveDices.figureList = "Appel";
                }
                else if (throwNb==maxThrowNb)
                    if ((appelClicked) && (!fiveDices.figureList.contains(appelRegistered))){
                        fiveDices.figureList="";
                    }
            }
        }
        //DEBUG
 //       if (couleur.equals("blue"))
 //           fiveDices.figureList="SmallSuiteSecFullCarreAppelYam123456";
        //DEBUG
        return true;
    }

    void printSortedDices(){

        System.out.println("**SortedDices**");
        for (int i=0; i<5; i++)
        {
            System.out.print(this.fiveDices.tempDiceSetIndValues[i][0]+" ");
        }
        System.out.println();
        for (int i=0; i<5; i++)
        {
            System.out.print(this.fiveDices.tempDiceSetIndValues[i][1]+" ");
        }
        System.out.println();
    }

    public void printSelectedSortedDice(){

        System.out.println("**SelectedSortedDice**");
        for (int i=0; i<5; i++)
        {
            System.out.print(this.fiveDices.tempDiceSetIndValues[i][0]+" ");
        }
        System.out.println();
        for (int i=0; i<5; i++)
        {
            System.out.print(this.fiveDices.tempDiceSetIndValues[i][1]+" ");
        }
        System.out.println();
        for (int i=0; i<5; i++)
        {
            if (this.fiveDices.diceSet[this.fiveDices.tempDiceSetIndValues[i][0]].isSelected)
                System.out.print("x ");
            else
                System.out.print("o ");
        }
        System.out.println();
    }

    void terminate() { this.couleur = "white"; }

    void afficherBoxPair(BoxPair aBoxPair){
        if (aBoxPair.getPairId()>0) {
            System.out.println("boxId: "+aBoxPair.getPairId()+" Box[" + this.findBoxById(aBoxPair.getPairId()).v + "]["
                    + this.findBoxById(aBoxPair.getPairId()).h + "]: "
                    + this.findBoxById(aBoxPair.getPairId()).figType
                    +" "
                    + this.findBoxById(aBoxPair.getPairId()).color
                    + "->"
                    + aBoxPair.getPairPoints() + " points"
                    );
        }
        else System.out.println("null box");
    }

    void afficherDes(){
        fiveDices.printDiceSet();
    }

    //returns a list of boxpair available for given figureList
    List <BoxPair> getListFreeBoxesPerFigureList(String aFigureList){
        List <BoxPair> aBoxPairList = new ArrayList<>();
        List<String> aListOfFigures = new ArrayList<>();
        Pattern aPattern = Pattern.compile("1|2|3|4|5|6|Appel|Full|Carre|Small|Suite|Sec|Yam");
        Matcher aMatcher = aPattern.matcher(aFigureList);
        while (aMatcher.find()){
            aListOfFigures.add(aMatcher.group());
        }
        for (int i=0; i<aListOfFigures.size(); i++){
            aBoxPairList.addAll(getListBoxPairColorPerFigure(aListOfFigures.get(i), "white"));
        }
        return  aBoxPairList;
    }

    //returns a List of BoxPair  with such color per figure (points =0)
    List<BoxPair> getListBoxPairColorPerFigure(String aFigure, String aColor) {
        List<BoxPair> aBoxPairList = new ArrayList<>();
        if (aFigure.equals("1")) {
            if ((this.checkerBox[0][0].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[0][0].id, 0, 0));
            }
            if ((this.checkerBox[3][4].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[3][4].id, 0, 0));
            }
        }
        if (aFigure.equals("2")) {
            if ((this.checkerBox[1][0].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[1][0].id, 0, 0));
            }
            if ((this.checkerBox[4][1].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[4][1].id, 0, 0));
            }
        }
        if (aFigure.equals("3")) {
            if ((this.checkerBox[0][1].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[0][1].id, 0, 0));
            }
            if ((this.checkerBox[4][0].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[4][0].id, 0, 0));
            }
        }
        if (aFigure.equals("4")) {
            if ((this.checkerBox[0][3].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[0][3].id, 0, 0));
            }
            if ((this.checkerBox[4][4].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[4][4].id, 0, 0));
            }
        }
        if (aFigure.equals("5")) {
            if ((this.checkerBox[1][4].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[1][4].id, 0, 0));
            }
            if ((this.checkerBox[4][3].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[4][3].id, 0, 0));
            }
        }
        if (aFigure.equals("6")) {
            if ((this.checkerBox[0][4].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[0][4].id, 0, 0));
            }
            if ((this.checkerBox[3][0].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[3][0].id, 0, 0));
            }
        }
        if (aFigure.equals("Appel")) {
            if ((this.checkerBox[0][2].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[0][2].id, 0, 0));
            }
            if ((this.checkerBox[2][3].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[2][3].id, 0, 0));
            }
        }
        if (aFigure.equals("Carre")) {
            if ((this.checkerBox[1][1].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[1][1].id, 0, 0));
            }
            if ((this.checkerBox[4][2].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[4][2].id, 0, 0));
            }
        }
        if (aFigure.equals("Sec")) {
            if ((this.checkerBox[1][2].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[1][2].id, 0, 0));
            }
            if ((this.checkerBox[3][1].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[3][1].id, 0, 0));
            }
        }
        if (aFigure.equals("Full")) {
            if ((this.checkerBox[1][3].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[1][3].id, 0, 0));
            }
            if ((this.checkerBox[2][1].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[2][1].id, 0, 0));
            }
        }
        if (aFigure.equals("Small")) {
            if ((this.checkerBox[2][0].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[2][0].id, 0, 0));
            }
            if ((this.checkerBox[3][3].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[3][3].id, 0, 0));
            }
        }
        if (aFigure.equals("Suite")) {
            if ((this.checkerBox[2][4].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[2][4].id, 0, 0));
            }
            if ((this.checkerBox[3][2].color.equals(aColor))) {
                aBoxPairList.add(new BoxPair(this.checkerBox[3][2].id, 0, 0));
            }
        }
        if (aFigure.equals("Yam")) {
            if (this.checkerBox[2][2].color.equals(aColor)) {
                aBoxPairList.add(new BoxPair(this.checkerBox[2][2].id, 0, 0));
            }
        }
        return aBoxPairList;
    }
}
