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

package com.ricax.yatzoe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

class MachinePlayTask implements Runnable {
    private final Jeu currentGame;
    private final MainActivity mainActivity;

    MachinePlayTask(Jeu game, MainActivity mainActivity) {
        currentGame = game;
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        //To give time for  the player to change turn ....
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean lock = true;
        while (lock) {
            if (currentGame.couleur.equals("red")) {
                if (currentGame.throwNb == 0) {
                    for (int j = 0; j < 5; j++)
                        currentGame.fiveDices.diceSet[j].isSelected = true;
                }

                syncFiveDicesSelectionWithUI(); //show machine selecting dices
                if (currentGame.throwNb < currentGame.maxThrowNb) {
                    if ((!mainActivity.diceCheat)||currentGame.throwNb>0) {//cette condition est pour le cheat, à enlever + tard
//                        mainActivity.throwDiceCheat=false;
                        currentGame.throwDices();
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        showDroidThrowsUI(currentGame.throwNb);
                        syncFiveDicesResultsWithUI();//Shows dice results
                    }
                    else{//ce bloc est pour le cheat
                        System.out.println("dice cheat1!");
                       /* while (mainActivity.throwDiceCheat){
                            //just wait till dice are set and flag is up
                        }*/
                        currentGame.throwNb++;
                        currentGame.fiveDices.figureList="";
                        currentGame.fiveDices.setListOfFiguresFromDiceSet();
                    }
                    String target = machineChoseFromDices();
                    System.out.println("target1: "+target);
                    if (target.matches(".*(1|2|3|4|5|6|Appel|Carre|Full|Yam|Suite|Sec|Small).*")) {
                        System.out.println("target2: "+target);
                        selectDiceFromTarget(currentGame, target);
                    } else if (target.equals("blue")) {
                        //Pour être sûr d'avoir la bonne couleur courante car le changement de couleur est fait dans le UI thread
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Test de fin de jeu
                        if (!currentGame.couleur.equals("white")) {
                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    currentGame.changeTurnColor("blue");
                                }
                            });
                        }
                        lock = false;//exit the while loop
                    }
                }
            }
        }
    }

    /*
    public boolean stringMatcher (String aFigureList, String aRegex){
        Pattern p = Pattern.compile(aRegex);
        Matcher m = p.matcher(aFigureList);
        if (m.find()){
            return true;
        }
        else 
            return false;
    }
    */

    /*****Methods that help droid sort all possibilities*******/
    //Adds potential future points per given figtype to given ArrayList of BoxPairs
    //TODO verifier setBoxWeight ici
    private void listAddNextTurnBoxPairIDPointsPerFigure(Jeu aGame, ArrayList<BoxPair> boxIdPointList, String figureList, String aFigure, int v1, int h1, int v2, int h2) {
        if (figureList.contains(aFigure)) {
            if (aGame.checkerBox[v1][h1].getColor().equals("white")) {
                //BoxPair boxIdPoint1 = new BoxPair(aGame.checkerBox[v1][h1], getPotentialNextTurnPointsPerBox(aGame, "red", aGame.checkerBox[v1][h1]), getPotentialNextTurnPointsPerBox(aGame, "blue", aGame.checkerBox[v1][h1]));
                BoxPair boxIdPoint1 = new BoxPair(aGame.checkerBox[v1][h1], 0, 0);
                setBoxWeight(aGame, boxIdPoint1, "red");
                boxIdPointList.add(boxIdPoint1);
            }
            if (aGame.checkerBox[v2][h2].getColor().equals("white")) {
                // BoxPair boxIdPoint2 = new BoxPair(aGame.checkerBox[v2][h2], getPotentialNextTurnPointsPerBox(aGame,"red", aGame.checkerBox[v2][h2]), getPotentialNextTurnPointsPerBox(aGame,"blue", aGame.checkerBox[v2][h2]));
                BoxPair boxIdPoint2 = new BoxPair(aGame.checkerBox[v2][h2], 0, 0);
                setBoxWeight(aGame, boxIdPoint2, "red");
                boxIdPointList.add(boxIdPoint2);
            }
        }
    }

    //returns potential next turn poinst per box
    private int getPotentialNextTurnPointsPerBox(Jeu aGame, String aColor, Box aBox) {
        int v = aBox.v;
        int h = aBox.h;
        int potentialPoints = 0;
        Jeu tempGame = new Jeu(aGame);
        potentialPoints += getPotentialPointsNextTurnPerCoord(tempGame, aColor, v, 0, h, 1 );//Horizontally
        potentialPoints += getPotentialPointsNextTurnPerCoord(tempGame, aColor, v, 1, h, 0);//Vertically
        potentialPoints += getPotentialPointsNextTurnPerCoord(tempGame, aColor, v, 1, h, 1);//to bottom right
        potentialPoints += getPotentialPointsNextTurnPerCoord(tempGame, aColor, v, 1, h, -1);//to bottom left
        // System.out.println("getPotentialNextTurnPointsPerBox: "+aColor+" points:"+potentialPoints);

        //TODO: voir si il faut faire la meme chose que ce qui suit pour le calcul de oponentPoints
        if ((tempGame.fullLine(aColor, aBox.getId())) || (aGame.redMarkers - 1 == 0)) {
            if (aGame.redPoints + potentialPoints < aGame.bluePoints)
                return -1;//pour passer le tour
        }
        return potentialPoints;
    }


    // donner un poids à chaque box
    private void setBoxWeight(Jeu aGame, BoxPair aBoxPair, String aColor){
        String oponentColor="";
        if (aColor.equals("red")) oponentColor="blue";
        else if (aColor.equals("blue")) oponentColor="red";
        Box aBox=aBoxPair.getBox();
        aBoxPair.setPairPoints(getPointsIfMarkerPlacedOnBox(aGame, aColor, aBox));
        aBoxPair.setOponentPoints(getPointsIfMarkerPlacedOnBox(aGame, oponentColor, aBox));
        aBoxPair.setAllPossiblePoints(setAllPossiblePointsAroundBox(aGame, aColor, aBox ));
        aBoxPair.setNextTurnPossiblePoints(getPotentialNextTurnPointsPerBox(aGame, aColor,aBox));
        //cibler en  priorité la case qui pourrait faire gagner l'adversaire en cas de fullLine
        if (aBoxPair.getOponentPoints()==10)
        {
            System.out.println("cible l'anti fullLine");
            aBoxPair.setPairPoints(10);
        }
    }

    public int setAllPossiblePointsAroundBox(Jeu aGame, String aColor, Box aBox){
        Jeu tempGame = new Jeu (aGame);
        int v=aBox.v;
        int h=aBox.h;
        String oponentColor="";
        if (aColor.equals("red")) oponentColor="blue";
        else if (aColor.equals("blue")) oponentColor="red";
        else return 0;
        ArrayList<BoxPair> allPossibleBoxpairs = new ArrayList<>();
        //horizontal
        for (int span=-2; span<3; span++){
            if (checkBoxColorWhithinBound(v, h+span, oponentColor, tempGame))
                continue;
            if (checkBoxColorWhithinBound(v, h+span, "white", tempGame)){
                if (h+span>=0&&h+span<5){
                    tempGame.checkerBox[v][h+span].setColor(aColor);
                    allPossibleBoxpairs.add(new BoxPair(tempGame.checkerBox[v][h+span], 0, 0));
                }
            }
        }
        //vertical
        for (int span=-2; span<3; span++){
            if (checkBoxColorWhithinBound(v+span, h, oponentColor, tempGame))
                continue;
            if (checkBoxColorWhithinBound(v+span, h, "white", tempGame)){
                if ((v+span>=0 && v+span<5)){
                    tempGame.checkerBox[v+span][h].setColor(aColor);
                    allPossibleBoxpairs.add(new BoxPair(tempGame.checkerBox[v+span][h], 0, 0));
                }
            }
        }
        //to bottom right
        for (int span=-2; span<3; span++){
            if (checkBoxColorWhithinBound(v+span, h+span, oponentColor, tempGame))
                continue;
            if (checkBoxColorWhithinBound(v+span, h+span, "white", tempGame)){
                if (((v+span>=0 && v+span<5))&&  ((h+span>=0 && h+span<5))){
                    tempGame.checkerBox[v+span][h+span].setColor(aColor);
                    allPossibleBoxpairs.add(new BoxPair(tempGame.checkerBox[v+span][h+span], 0, 0));
                }
            }
        }
        //to bottom left
        for (int span=-2; span<3; span++){
            if (checkBoxColorWhithinBound(v-span, h+span, oponentColor, tempGame))
                continue;
            if (checkBoxColorWhithinBound(v-span, h+span, "white", tempGame))
                if (((v-span>=0 && v-span<5))&&  ((h+span>=0 && h+span<5))){
                    tempGame.checkerBox[v-span][h+span].setColor(aColor);
                    allPossibleBoxpairs.add(new BoxPair(tempGame.checkerBox[v-span][h+span], 0, 0));
                }
        }
        /*
        System.out.println("----------------------------------");
        System.out.println("setAllPossiblePointsAroundBox: aBox: "+aBox);
        System.out.println("allPossibleBoxpairs="+allPossibleBoxpairs);
        System.out.println("----------------------------------");
        */

        return tempGame.countLine(3, aColor, aBox.getId());
    }


    //Checks if the coord are within bounds of the board
    private boolean checkBoxColorWhithinBound(int v, int h, String aColor, Jeu aGame) {
        if (((v >= 0) && (v <= 4)) && ((h >= 0) && (h <= 4)))
            return aGame.checkerBox[v][h].getColor().equals(aColor);
        return false;
    }
/*
    private boolean colorInSpanPerBoxExists(String aColor, Box aBox, int span, Jeu aGame) {
        int v = aBox.v;
        int h = aBox.h;
        if (colorInSpanPerLineExists(aColor, v, 0, h, 1, span, aGame)
                || colorInSpanPerLineExists(aColor, v, 1, h, 0, span, aGame)
                || colorInSpanPerLineExists(aColor, v, 1, h, 1, span, aGame)
                || colorInSpanPerLineExists(aColor, v, 1, h, -1, span, aGame)
        )
            return true;
        else
            return false;
    }

 */
/*
    private boolean colorInSpanPerLineExists(String aColor, int v, int incrV, int h, int incrH, int span, Jeu aGame) {
        if ((checkBoxColorWhithinBound(v + span * incrV, h + span * incrH, aColor, aGame))
                || (checkBoxColorWhithinBound(v - span * incrV, h - span * incrH, aColor, aGame)))
            return true;
        return false;
    }

 */

    //returns potential points (next turn) from coordinates
    //Ex: XRW or XWR or RXW or WXR leads to future potentials points
    private int getPotentialPointsNextTurnPerCoord(Jeu aGame, String aColor, int v, int incrV, int h, int incrH) {
        int potentialPoints = 0;
        if (checkBoxColorWhithinBound(v + incrV, h + incrH, aColor, aGame)) {
            if (checkBoxColorWhithinBound(v + 2 * incrV, h + 2 * incrH, "white", aGame))
                potentialPoints++;
        } else if (checkBoxColorWhithinBound(v + incrV, h + incrH, "white", aGame)) {
            if (checkBoxColorWhithinBound(v + 2 * incrV, h + 2 * incrH, aColor, aGame))
                potentialPoints++;
        }

        if (checkBoxColorWhithinBound(v - incrV, h - incrH, aColor, aGame)) {
            if (checkBoxColorWhithinBound(v - 2 * incrV, h - 2 * incrH, "white", aGame))
                potentialPoints++;
        } else if (checkBoxColorWhithinBound(v - incrV, h - incrH, "white", aGame)) {
            if (checkBoxColorWhithinBound(v - 2 * incrV, h - 2 * incrH, aColor, aGame))
                potentialPoints++;
        }

        if (checkBoxColorWhithinBound(v + incrV, h + incrH, aColor, aGame)) {
            if (checkBoxColorWhithinBound(v - incrV, h - incrH, "white", aGame))
                potentialPoints++;
        } else if (checkBoxColorWhithinBound(v - incrV, h - incrH, aColor, aGame)) {
            if (checkBoxColorWhithinBound(v + incrV, h + incrH, "white", aGame))
                potentialPoints++;
        }
        return potentialPoints;
    }

    //Checks if a List of boxes contains a box with such figType
  /*  private boolean boxListContains(List<BoxPair> aBoxPairList, String figType) {
        for (int i = 0; i < aBoxPairList.size() - 1; i++)
            if (currentGame.findBoxById(aBoxPairList.get(i).getPairId()).getFigType().equals(figType))
                return true;
        return false;
    }*/

    //returns optimal next turn box
    private BoxPair getOptimalNextTurnNextThrowBoxPairFromFigureList(Jeu aGame, List<BoxPair> optimalBoxPairList) {
        //If no points scored, try to figure out the best figure to get for next turn
        ArrayList<BoxPair> tmpOptimalBoxPairList = new ArrayList<>(optimalBoxPairList);
        if (!tmpOptimalBoxPairList.isEmpty())
            if ((tmpOptimalBoxPairList.get(tmpOptimalBoxPairList.size() - 1)).getPairPoints() == 0) {
                for (int i = 0; i < tmpOptimalBoxPairList.size(); i++) {
                    //Add potential points for each box of the list
                    tmpOptimalBoxPairList.get(i).setPairPoints(getPotentialNextTurnPointsPerBox(aGame,"red", aGame.findBoxById(tmpOptimalBoxPairList.get(i).getPairId())));
                }
            }
        //Sort in ascending order
        Collections.sort(tmpOptimalBoxPairList);
        System.out.println("Debut NextTurnNextThrow tmpOptimalBoxPairList:");
        System.out.println(tmpOptimalBoxPairList);
        System.out.println("fin affichage NextTurnNextThrow tmpOptimalBoxPairList");
     /*   System.out.println("getOptimaNextTurnNextThrowBoxPairFromFigureList");
        for (int i = 0; i< boxIdPointList.size(); i++)
        {
            System.out.println("****debut box***");
            System.out.println(boxIdPointList.get(i));
            currentGame.findBoxById(boxIdPointList.get(i).getPairId()).afficherBox();
            System.out.println("****fin box***");
        }*/
        if (!tmpOptimalBoxPairList.isEmpty())
            return tmpOptimalBoxPairList.get(tmpOptimalBoxPairList.size() - 1);
        else return new BoxPair(new Box("", "white", 0, 0, 0), 0, 0);
    }

    //TODO continuer de revoir l'attibution des poins aux boxpairs avec setBoxWeight, voir si VRAIMENT c'est jouable ou bien si cela ne fout pas le bazar ...
    //TODO en particulier lister les utilisations de getPOintsIfMarkerPlacedOnBox
    //returns optimal current turn next throw box
    private BoxPair getOptimalCurrentTurnNextThrowBoxPairFromFigureList(Jeu aGame, ArrayList<BoxPair> optimalBoxPairList) {
        ArrayList <BoxPair> tmpOptimalBoxPairList =  new ArrayList<>(optimalBoxPairList);
        //Add points and oponentPoints if marker placed on boxes
        for (int i = 0; i < tmpOptimalBoxPairList.size(); i++) {
            // tmpOptimalBoxPairList.get(i).setPairPoints(getPointsIfMarkerPlacedOnBox(aGame, "red", tmpOptimalBoxPairList.get(i).getBox()));
            // tmpOptimalBoxPairList.get(i).setOponentPoints(getPointsIfMarkerPlacedOnBox(aGame, "blue", tmpOptimalBoxPairList.get(i).getBox()));
            setBoxWeight(aGame, tmpOptimalBoxPairList.get(i),"red" );
        }
        //Sort in ascending order
        Collections.sort(tmpOptimalBoxPairList);
        System.out.println("Debut CurrentTurnNextThrow tmpOptimalBoxPairList:");
        System.out.println(tmpOptimalBoxPairList);
        System.out.println("fin affichage CurrentTurnNextThrow tmpOptimalBoxPairList");
        //Return the last of the list (the best)
   /*     System.out.println("getOptimalCurrentTurnNextThrowBoxPairFromFigureList");
        for (int i = 0; i< boxIdPointList.size(); i++)
        {
            System.out.println("****debut box***");
            System.out.println(boxIdPointList.get(i));
            currentGame.findBoxById(boxIdPointList.get(i).getPairId()).afficherBox();
            System.out.println("****fin box***");
        }*/
        if (!tmpOptimalBoxPairList.isEmpty())
            return tmpOptimalBoxPairList.get(tmpOptimalBoxPairList.size() - 1);
        else return new BoxPair(new Box(), 0, 0);
    }

    //Adds points obtained if current given figtype is placed to given ArrayList of BoxPairs
    private void listAddCurrentThrowBoxPairIdPointsPerFigure(Jeu aGame, ArrayList<BoxPair> boxIdPointList, String figureList, String aFigure, int v1, int h1, int v2, int h2) {

        if (figureList.contains(aFigure)) {
            if (aGame.checkerBox[v1][h1].getColor().equals("white")) {
                //            int points = getPointsIfMarkerPlacedOnBox(aGame,"red", aGame.checkerBox[v1][h1]);
                //          int oponentPoints = getPointsIfMarkerPlacedOnBox(aGame,"blue", aGame.checkerBox[v1][h1]);
                //          if (points >= 0) {
                // BoxPair boxIdPoint = new BoxPair(currentGame.checkerBox[v1][h1], points, oponentPoints);
                BoxPair boxIdPoint = new BoxPair(aGame.checkerBox[v1][h1], 0, 0);
                setBoxWeight(aGame, boxIdPoint, "red");
                //si points<0 ne pas le mettre ds la liste
                if (boxIdPoint.getPairPoints()>=0)
                    boxIdPointList.add(boxIdPoint);
                //        }
            }
            if (aGame.checkerBox[v2][h2].getColor().equals("white")) {
                //      int points = getPointsIfMarkerPlacedOnBox(aGame,"red", aGame.checkerBox[v2][h2]);
                //      int oponentPoints = getPointsIfMarkerPlacedOnBox(aGame,"blue", aGame.checkerBox[v2][h2]);
                //      if (points >= 0) {
                //          BoxPair boxIdPoint2 = new BoxPair(aGame.checkerBox[v2][h2], points, oponentPoints);
                BoxPair boxIdPoint2 = new BoxPair(aGame.checkerBox[v2][h2], 0, 0);
                setBoxWeight(aGame, boxIdPoint2, "red");
                if (boxIdPoint2.getPairPoints()>=0)
                    boxIdPointList.add(boxIdPoint2);
                //      }
            }
        }
    }

    //Returns the best  BoxPair (id+immediate points) for a given figureList
    private BoxPair getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList(Jeu aGame) {
        String figureList= aGame.fiveDices.figureList;
        System.out.println("getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList->aGame.fiveDices.figureList:"+aGame.fiveDices.figureList);
        ArrayList<BoxPair> boxPairPointList = new ArrayList<>();
        //check points per figure
        //Mettre appel en premier pour ne pas se faire shunter par appel
        listAddCurrentThrowBoxPairIdPointsPerFigure(aGame, boxPairPointList, figureList, "Appel", 0, 2, 2, 3);
        listAddCurrentThrowBoxPairIdPointsPerFigure(aGame, boxPairPointList, figureList, "1", 0, 0, 3, 4);
        listAddCurrentThrowBoxPairIdPointsPerFigure(aGame, boxPairPointList, figureList, "3", 0, 1, 4, 0);
        listAddCurrentThrowBoxPairIdPointsPerFigure(aGame, boxPairPointList, figureList, "4", 0, 3, 4, 4);
        listAddCurrentThrowBoxPairIdPointsPerFigure(aGame, boxPairPointList, figureList, "5", 1, 4, 4, 3);
        listAddCurrentThrowBoxPairIdPointsPerFigure(aGame, boxPairPointList, figureList, "6", 0, 4, 3, 0);
        listAddCurrentThrowBoxPairIdPointsPerFigure(aGame, boxPairPointList, figureList, "2", 1, 0, 4, 1);
        listAddCurrentThrowBoxPairIdPointsPerFigure(aGame, boxPairPointList, figureList, "Full", 1, 3, 2, 1);
        listAddCurrentThrowBoxPairIdPointsPerFigure(aGame, boxPairPointList, figureList, "Small", 2, 0, 3, 3);
        listAddCurrentThrowBoxPairIdPointsPerFigure(aGame, boxPairPointList, figureList, "Carre", 1, 1, 4, 2);
        listAddCurrentThrowBoxPairIdPointsPerFigure(aGame, boxPairPointList, figureList, "Suite", 2, 4, 3, 2);
        listAddCurrentThrowBoxPairIdPointsPerFigure(aGame, boxPairPointList, figureList, "Sec", 1, 2, 3, 1);
        if (figureList.contains("Yam")) {
            if (aGame.checkerBox[2][2].getColor().equals("white")) {
                /*int points = getPointsIfMarkerPlacedOnBox(aGame, "red", aGame.checkerBox[2][2]);
                int oponentPoints = getPointsIfMarkerPlacedOnBox(aGame, "red", aGame.checkerBox[2][2]);
                if (points >= 0) {
                    BoxPair boxIdPoint = new BoxPair(aGame.checkerBox[2][2], points, oponentPoints);
                    boxPairPointList.add(boxIdPoint);
                }*/
                BoxPair boxIdPoint = new BoxPair(aGame.checkerBox[2][2], 0, 0);
                setBoxWeight(aGame, boxIdPoint, "red");
                if (boxIdPoint.getPairPoints()>=0)
                    boxPairPointList.add(boxIdPoint);

            }
        }
        //Les boxPairs sont rangées selon 1:les points, 2: les points de l'adversaire en cas d'égalité
        Collections.sort(boxPairPointList);
        //       System.out.println("boxIdPointList:");
        //       System.out.println(boxIdPointList);
        //       System.out.println("Dernier de la liste 1:");
//        System.out.println(boxIdPointList.get(boxIdPointList.size() - 1));
//        System.out.println("Dernier de la liste 2:");
//        currentGame.findBoxById(boxIdPointList.get(boxIdPointList.size() - 1).getPairId()).afficherBox();

     /*   System.out.println("getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList");
        for (int i = 0; i< boxIdPointList.size(); i++)
        {
            System.out.println("****debut box***");
            System.out.println(boxIdPointList.get(i));
            currentGame.findBoxById(boxIdPointList.get(i).getPairId()).afficherBox();
            System.out.println("****fin box***");
        }*/
        if (!boxPairPointList.isEmpty()){
            System.out.println("Fin 1 getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList: boxPairId="+boxPairPointList.get(boxPairPointList.size() - 1).getPairId());
            return boxPairPointList.get(boxPairPointList.size() - 1);
        }
        System.out.println(" Fin2 getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList");
        //Just in case
        return new BoxPair(new Box(), 0, 0);
    }

    //Returns points if marker placed on box
    private int getPointsIfMarkerPlacedOnBox(Jeu aGame, String aColor, Box aBox) {
        int boxId=aBox.getId();
        int tmpPoints = 0;
        //copie de currentGame
        Jeu tempGame = new Jeu(aGame);
        // tempGame.findBoxById(boxId).afficherBox();
        if (tempGame.findBoxById(boxId).getColor().equals("white")) {
            tempGame.findBoxById(boxId).setColor(aColor);
            tmpPoints = tempGame.countLine(3, aColor, boxId);
            if ((tempGame.fullLine(aColor, boxId)) || (tempGame.redMarkers - 1 == 0)) {
                if (((tempGame.redPoints + tmpPoints < tempGame.bluePoints)&& aColor.equals("red"))
                        ||
                        ((tempGame.bluePoints + tmpPoints < tempGame.redPoints)&& aColor.equals("blue")))
                {
                    System.out.println("getPointsIfMarkerPlacedOnBox Marquage perdant:"+tmpPoints+"mais -1");
                    tempGame.findBoxById(boxId).afficherBox();
                    System.out.println("fin getPointsIfMarkerPlacedOnBox Marquage perdant");
                    //Si on peut esperer gagner la partie avec les pions qui nous restent on retourne -1
                    return -1;  //Pour le mettre en queue des choix dans getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList
                    // pour passer le tour
                } else if (((tempGame.redPoints + tmpPoints > tempGame.bluePoints)&& aColor.equals("red"))||
                        ((tempGame.bluePoints + tmpPoints > tempGame.redPoints)&& aColor.equals("blue"))) {
                    System.out.println("getPointsIfMarkerPlacedOnBox Marquage gagnant:"+tmpPoints+" mais 10");
                    tempGame.findBoxById(boxId).afficherBox();
                    System.out.println("fin getPointsIfMarkerPlacedOnBox Marquage gagnant");
                    return 10; //Pour le mettre en tête des choix dans getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList
                }
                /*
                 * Sinon il nous reste au moins 1 pion, on peut tenter le coup :
                 * soit:  on aura une meilleure option dans getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList
                 * soit: on passera le tour à la fin de choseFromDice
                 * on sort du if et retourne tmpPoints (en bas de la fonction)
                 */
            }
            //tempGame.findBoxById(boxId).color = "white";
        }
        // System.out.println("getPointsIfMarkerPlacedOnBox: "+aColor+" points:"+tmpPoints);
        return tmpPoints;
    }

    private boolean checkFullLinePoints(Jeu aGame, String aColor, int boxId) {
        //Si on peut fermer le jeu en gagnant
        if ((aGame.fullLine(aColor, boxId)) && (aGame.redPoints > aGame.bluePoints)) {
            return true;
        } else
            return false;
    }

    //Returns the best  BoxPair (id+potential future points in next throw) for a given figureList
    private BoxPair getOptimalNextTurnCurrentThrowBoxPairPerFigureList(Jeu aGame) {
        String figureList=aGame.fiveDices.figureList;
        ArrayList<BoxPair> boxPairPointList = new ArrayList<>();
        //check points per figure
        //Mis les brelans en premiers pour privilégier les figures centrales dans la fin de liste
        listAddNextTurnBoxPairIDPointsPerFigure(aGame, boxPairPointList, figureList, "1", 0, 0, 3, 4);
        listAddNextTurnBoxPairIDPointsPerFigure(aGame, boxPairPointList, figureList, "2", 1, 0, 4, 1);
        listAddNextTurnBoxPairIDPointsPerFigure(aGame, boxPairPointList, figureList, "3", 0, 1, 4, 0);
        listAddNextTurnBoxPairIDPointsPerFigure(aGame, boxPairPointList, figureList, "4", 0, 3, 4, 4);
        listAddNextTurnBoxPairIDPointsPerFigure(aGame, boxPairPointList, figureList, "5", 1, 4, 4, 3);
        listAddNextTurnBoxPairIDPointsPerFigure(aGame, boxPairPointList, figureList, "6", 0, 4, 3, 0);
        listAddNextTurnBoxPairIDPointsPerFigure(aGame, boxPairPointList, figureList, "Full", 1, 3, 2, 1);
        listAddNextTurnBoxPairIDPointsPerFigure(aGame, boxPairPointList, figureList, "Small", 2, 0, 3, 3);
        listAddNextTurnBoxPairIDPointsPerFigure(aGame, boxPairPointList, figureList, "Carre", 1, 1, 4, 2);
        listAddNextTurnBoxPairIDPointsPerFigure(aGame, boxPairPointList, figureList, "Suite", 2, 4, 3, 2);
        listAddNextTurnBoxPairIDPointsPerFigure(aGame, boxPairPointList, figureList, "Sec", 1, 2, 3, 1);
        if ((figureList.contains("Yam") && (aGame.checkerBox[2][2].getColor().equals("white")))) {
            BoxPair boxIdPoint = new BoxPair(aGame.checkerBox[2][2], getPotentialNextTurnPointsPerBox(aGame,"red", aGame.checkerBox[2][2]), getPotentialNextTurnPointsPerBox(aGame,"blue", currentGame.checkerBox[2][2]));
            boxPairPointList.add(boxIdPoint);
        }
        listAddNextTurnBoxPairIDPointsPerFigure(aGame, boxPairPointList, figureList, "Appel", 0, 2, 2, 3);

        //Sort boxIdPointList
        Collections.sort(boxPairPointList);
        /*System.out.println("getOptimalNextTurnCurrentThrowBoxPairPerFigureList");
        for (int i = 0; i< boxIdPointList.size(); i++)
        {
            System.out.println("****debut box***");
            System.out.println(boxIdPointList.get(i));
            currentGame.findBoxById(boxIdPointList.get(i).getPairId()).afficherBox();
            System.out.println("****fin box***");
        }*/
        if (!boxPairPointList.isEmpty())
            return boxPairPointList.get(boxPairPointList.size() - 1);
        else {
            return new BoxPair(new Box(), 0, 0);
        }
    }

    private void printOptimalBoxList(List<BoxPair> aBoxPairList) {
        if (!aBoxPairList.isEmpty())
            for (int i = 0; i < aBoxPairList.size(); i++) {
                currentGame.afficherBoxPair(aBoxPairList.get(i));
            }
        System.out.println(" ");
    }

    private ArrayList<BoxPair> getPossibleTargetBoxpairsFromFigureListFromDiceSet(Jeu aGame){
        ArrayList<BoxPair> possibleBoxpairsFromFigureListFromDiceSet = new ArrayList<>();
        ArrayList<String> possibleTargetFiguresFromDiceSet = getPossibleTargetFiguresFromDiceSet(aGame);
        for (int i=0; i<possibleTargetFiguresFromDiceSet.size(); i++)
            possibleBoxpairsFromFigureListFromDiceSet.addAll(aGame.getListBoxPairColorPerFigure(possibleTargetFiguresFromDiceSet.get(i), "white"));
        return possibleBoxpairsFromFigureListFromDiceSet;
    }

    //returns a list of optimal next throw target boxes from a given current figure list
    private ArrayList<BoxPair> getListOptimalNextThrowBoxpairFromFigureList(Jeu aGame) {
        //TODO implémenter recherche de case qui ferme le jeu avec victoire (fullLine ou pion=0)
        String aFigureList=aGame.fiveDices.figureList;
        //meme avec 1 seul dé (genre pour tenter brelan qui tue)
        ArrayList<BoxPair> boxPairPointList = new ArrayList<>();
        //Brelan->carre, full, yam
        if (aFigureList.matches(".*([123456]).*")) {
            if (aGame.throwNb == 1) {
                if (!boxPairListContains(boxPairPointList, "Appel"))
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Appel", "white"));
            }
            if (!aFigureList.contains("Carre")) {
                if (!boxPairListContains(boxPairPointList, "Carre"))
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Carre", "white"));
            }
            if (!aFigureList.contains("Full")) {
                if (!boxPairListContains(boxPairPointList, "Full"))
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Full", "white"));
            }
            if (!aFigureList.contains("Yam")) {
                if (!boxPairListContains(boxPairPointList, "Yam"))
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Yam", "white"));
            }
        }
        //Carre->yam
        if (aFigureList.contains("Carre")) {
            if (aGame.throwNb == 1)
                if (!boxPairListContains(boxPairPointList, "Appel")) {
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Appel", "white"));
                }
            if (!aFigureList.contains("Yam")) {
                if (!boxPairListContains(boxPairPointList, "Yam"))
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Yam", "white"));
            }
        }

        //Déjà fait plus bas
        //1->small
       /* if (aFigureList.contains("1")) {
            if (currentGame.throwNb == 1) {
                if (!boxPairListContains(boxPairPointList, "Appel"))
                    boxPairPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
            }
            if (!aFigureList.contains("Small")) {
                if (!boxPairListContains(boxPairPointList, "Small"))
                    boxPairPointList.addAll(currentGame.getListBoxPairColorPerFigure("Small", "white"));
            }
        }*/

        //Full->carre, yam
        if (aFigureList.contains("Full")) {
            if (aGame.throwNb == 1) {
                if (!boxPairListContains(boxPairPointList, "Appel"))
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Appel", "white"));
            }
            if (aGame.getListBoxPairColorPerFigure("Full", "white").isEmpty()) {
                if (!boxPairListContains(boxPairPointList, "Carre")) {
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Carre", "white"));
                }
                if (!boxPairListContains(boxPairPointList, "Yam")) {
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Yam", "white"));
                }
            }
        }
        //2 paires -> full
        if (figureContainsDoublePair(aGame)) {
            if (aGame.throwNb == 1) {
                if (!boxPairListContains(boxPairPointList, "Appel")){
                    System.out.println("add appel figureContainsDoublePair");
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Appel", "white"));
                }
            }
            //Pour les brelans
            int valIdx1 = aGame.fiveDices.tempDiceSetIndValues[1][1];
            int valIdx3 = aGame.fiveDices.tempDiceSetIndValues[3][1];
            System.out.println("double paire:"+valIdx1+"-"+valIdx3);
            if (!boxPairListContains(boxPairPointList, Integer.toString(valIdx1))) {
                boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure(Integer.toString(valIdx1), "white"));
            }
            if (!boxPairListContains(boxPairPointList, Integer.toString(valIdx3))) {
                boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure(Integer.toString(valIdx3), "white"));
            }
            //Full
            if (!aFigureList.contains("Full"))
                if (!boxPairListContains(boxPairPointList, "Full")) {
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Full", "white"));
                }
            //Le carré->yam est déjà traité plux haut
          /*  if (aFigureList.contains("Carre")) {
                //si 2  paires identiques
                if (!boxPairListContains(boxPairPointList, "Yam"))
                    boxPairPointList.addAll(currentGame.getListBoxPairColorPerFigure("Yam", "white"));
            }*/
        }
//presque suite->suite
        if (!aFigureList.contains("Suite"))//Pour tenter des suites
            if (figureContains4InARow(aGame) != 0) {
                //si figureContains4InARow()==1 alors voir si on ne peut pas tenter un brelan plutôt
                // car si figureContains4InARow==1 alors seule autre possibilite=singlepaire ou rien
                // si figureContains4InARow==2 alors tenter suite
                if (aGame.throwNb == 1) {
                    if (!boxPairListContains(boxPairPointList, "Appel"))
                        boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Appel", "white"));
                }
                if (figureContains4InARow(aGame) == 2) {
                    if (!boxPairListContains(boxPairPointList, "Suite"))
                        boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Suite", "white"));
                }
                if (figureContains4InARow(aGame) == 1) {
                    //See if we'd better try a brelan instead if we've got a pair
                    if (!boxPairListContains(boxPairPointList, "Suite"))
                        boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Suite", "white"));
                }
            }
        //TODO conflit à surveiller  avec le full ici, si double paire on prépare le yam/carre AUSSI. Si les points y sont supérieurs ou égaux(?)
        // , le full n'est pas tenté ALORS QU'il est PLUS FACILE A OBTENIR....exemple : 1 5 6 6 1 premier jet, tout est relancé et boxPairTargetCurrentTurnNextThrow: (2, 2) Yam (dernier de la liste)
        //paire(s)-> brelans carré ou yam
        if (figureContainsSinglePair(aGame)) {
            int val = getSinglePairValue(aGame);//retourne 0 si double paire -> du coup pas de sélection!
            // int val = getFirstAvailablePairValue(aGame);//retourne la valeur de la première paire (sur 1 ou 2 paires)
            if (!aGame.fiveDices.figureList.contains(Integer.toString(val))) {
                if (!boxPairListContains(boxPairPointList, Integer.toString(val)))
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure(Integer.toString(val), "white"));
            }
            if (aGame.throwNb == 1) {
                if (!boxPairListContains(boxPairPointList, "Appel"))
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Appel", "white"));
            }
            //Ne tenter le yam ou le carre que si on ne tente pas le full
            if (!boxPairListContains(boxPairPointList,"Full")){
                System.out.println("carre yam?");
                if (!aFigureList.contains("Carre")) {
                    if (!boxPairListContains(boxPairPointList, "Carre"))
                        boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Carre", "white"));
                }
                if (!aFigureList.contains("Yam")) {
                    if (!boxPairListContains(boxPairPointList, "Yam"))
                        boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Yam", "white"));
                }
            }
        }

        //Pour tenter le small
        if (!aFigureList.contains("Small"))
            if (figureContainsAlmostSmall(aGame)) {
                if (aGame.throwNb == 1) {
                    if (!boxPairListContains(boxPairPointList, "Appel"))
                        boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Appel", "white"));
                }
                if (!boxPairListContains(boxPairPointList, "Small"))
                    boxPairPointList.addAll(aGame.getListBoxPairColorPerFigure("Small", "white"));
            }

        ArrayList<String> boxIdPointFigTypeList = new ArrayList<>();
        for (int i = 0; i<boxPairPointList.size(); i++)
            boxIdPointFigTypeList.add(aGame.findBoxById(boxPairPointList.get(i).getPairId()).getFigType());
        System.out.println("boxIdNextTurn PointFigTypeList: "+boxIdPointFigTypeList);
        return boxPairPointList;
    }



    private boolean boxPairListContains(ArrayList<BoxPair> aBoxPairArrayList, String figType){
        for (int i =0; i< aBoxPairArrayList.size(); i++)
            if (aBoxPairArrayList.get(i).getFigType().equals(figType))
                return true;
        return false;
    }

    /*xxxxxx Methods that aim to optimize strategy xxxxxx*/

    //Verifier si on ne gâche pas un pion
    private boolean checkMarkerSpoilByBox(Jeu aGame, Box aBox) { return false;}

    private List<JeuCombinaison> bestCombinationsAvailable(String aColor, Jeu aGame) {
        /*
         * parcourir les possibilités restantes et additionner les points pour chaque possibilité
         * Retourner une liste des meilleures combinaisons
         */
        List<Integer> aBoxPairIdList = getFreeBoxIdList(aGame);
      /* System.out.println("debut getFreeBoxIdList");
        for (int i = 0; i<aBoxPairIdList.size(); i++)
            aGame.findBoxById(aBoxPairIdList.get(i)).afficherBox();
        System.out.println("fin getFreeBoxIdList");
        */
        Combinations aCombination;
        List<JeuCombinaison> gameCombinationList = new ArrayList<>();
        if (aColor.equals("red"))
            aCombination = new Combinations(aGame.redMarkers, aBoxPairIdList);
        else if (aColor.equals("blue"))
            aCombination=new Combinations(aGame.blueMarkers, aBoxPairIdList);
        else
            return gameCombinationList;
        aCombination.Combinate();
        List<int[]> combSubsets = aCombination.getSubsets();
        // List<JeuCombinaison> gameCombinationList = new ArrayList<>();
        for (int i = 0; i < combSubsets.size(); i++) {
            //utiliser le constructeur de copie pour Jeu
            JeuCombinaison aGameCombination = new JeuCombinaison(new Jeu(aGame), aColor, combSubsets.get(i));
            //aGameCombination.computeCombinationPoints();
            gameCombinationList.add(aGameCombination);
        }
        Collections.sort(gameCombinationList);

        //le tri sera fait dans machineChoseFromDice, permet de shunter le bug précédent et de vérifier que toutes les combinaisons gagnantes sont prises en compte
        return gameCombinationList;
    }

    private List<Integer> getFreeBoxIdList(Jeu aGame) {
        List<Integer> aBoxPairIDList = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++) {
                if (aGame.checkerBox[i][j].getColor().equals("white")) {
                    aBoxPairIDList.add(aGame.checkerBox[i][j].getId());
                }
            }
        return aBoxPairIDList;
    }

    private List<BoxPair> getFreeBoxList(Jeu aGame) {
        List<BoxPair> aBoxPairList = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++) {
                if (aGame.checkerBox[i][j].getColor().equals("white")) {
                    BoxPair aBoxPair = new BoxPair(aGame.checkerBox[i][j], 0, 0);
                    aBoxPairList.add(aBoxPair);
                }
            }
        return aBoxPairList;
    }

    private boolean figureContainsAlmostYam(Jeu aGame){
        if (aGame.fiveDices.figureList.contains("carre")|| currentGame.fiveDices.figureList.matches( ".*([123456]).*"))
            return true;
        return false;
    }

    //Lister les figures obtenues avec les dés
    private ArrayList<String> getAvailableFiguresFromdiceSet(Jeu aGame){
        System.out.println("getAvailableFiguresFromdiceSet");
        System.out.println("currentGame.fiveDices.figureList: "+currentGame.fiveDices.figureList);
        System.out.println("aGame.fiveDices.figureList: "+aGame.fiveDices.figureList);
        ArrayList<String> availableFigureListFromDiceSet = new ArrayList<>();
        //trouver les figures éventuelles des dés courants
        final Pattern p = Pattern.compile("([123456])|(Carre)|(Yam)|(Full)|(Suite)|(Small)|(Sec)|(Appel)");
        Matcher m = p.matcher(aGame.fiveDices.figureList);
        if (m.find()){
            availableFigureListFromDiceSet.add(m.group());
            while (m.find())
                availableFigureListFromDiceSet.add(m.group());
        }
        for (int i = 0; i< availableFigureListFromDiceSet.size(); i++){
            System.out.println(availableFigureListFromDiceSet.get(i));
        }
        System.out.println("availableFigureListFromDiceSet: "+ availableFigureListFromDiceSet);
        System.out.println("fin getAvailableFiguresFromdiceSet");
        return availableFigureListFromDiceSet;
    }

    //lister les figures cibles possibles en fonction des dés
    private ArrayList<String> getPossibleTargetFiguresFromDiceSet(Jeu aGame){
        System.out.println("Debut getPossibleTargetsFromDiceSet");
        ArrayList<String> possibleFigureListFromDiceSet = new ArrayList<>();
        if (figureContains4InARow(aGame)!=0)
            if (!possibleFigureListFromDiceSet.contains("Suite"))
                possibleFigureListFromDiceSet.add("Suite");
        if (figureContainsAlmostSmall(aGame))
            if (!possibleFigureListFromDiceSet.contains("Small"))
                possibleFigureListFromDiceSet.add("Small");
        if (figureContainsDoublePair(aGame)){
            if (!possibleFigureListFromDiceSet.contains("Full"))
                possibleFigureListFromDiceSet.add("Full");
            //Ajouter les 2 brelans possibles
            if (!possibleFigureListFromDiceSet.contains(Integer.toString(aGame.fiveDices.tempDiceSetIndValues[1][1])))
                possibleFigureListFromDiceSet.add(Integer.toString(aGame.fiveDices.tempDiceSetIndValues[1][1]));
            if (!possibleFigureListFromDiceSet.contains(Integer.toString(aGame.fiveDices.tempDiceSetIndValues[3][1])))
                possibleFigureListFromDiceSet.add(Integer.toString(aGame.fiveDices.tempDiceSetIndValues[3][1]));
        }
        //Si on n'a qu'une seule paire
        if (getSinglePairValue(aGame)!=0)
            if (!possibleFigureListFromDiceSet.contains(Integer.toString(getSinglePairValue(aGame)))){
                possibleFigureListFromDiceSet.add(Integer.toString(getSinglePairValue(aGame)));
                possibleFigureListFromDiceSet.add("Carre");
                possibleFigureListFromDiceSet.add("Yam");
                possibleFigureListFromDiceSet.add(Integer.toString(getSinglePairValue(aGame)));
            }

        if (aGame.fiveDices.figureList.contains("Carre"))
            if (! possibleFigureListFromDiceSet.contains("Yam"))
                possibleFigureListFromDiceSet.add("Yam");
        //et dans tous les cas
        for (int i=1; i<7; i++)
            if (figureContainsSingleValue(aGame, i))
                if (!possibleFigureListFromDiceSet.contains(Integer.toString(i))){
                    System.out.printf("Ajout de single value: %s%n", i);
                    possibleFigureListFromDiceSet.add(Integer.toString(i));
                }

        possibleFigureListFromDiceSet.add("Sec");

        //  for (int i  = 0; i < possibleFigureListFromDiceSet.size(); i++)
        //     System.out.println(possibleFigureListFromDiceSet.get(i));
        System.out.println(possibleFigureListFromDiceSet);
        System.out.println("fin getPossibleTargetsFromDiceSet");
        return possibleFigureListFromDiceSet;
    }

    //Recupérer les figures de la meilleure combinaison
  /*  private ArrayList<String> getTargetFigureFromBestCombination(ArrayList<Integer> noDuplicatesArrayListBoxId){
        //les figures de la combinaison
        //String aFigType = null;
        ArrayList<String> figTypeArrayList = new ArrayList<>();
        for (int i = 0; i<noDuplicatesArrayListBoxId.size(); i++)
            figTypeArrayList.add(currentGame.findBoxById(noDuplicatesArrayListBoxId.get(i)).figType);
        System.out.println("getTargetFigureFromBestCombination");
        for (int i = 0; i<figTypeArrayList.size(); i++)
            System.out.println(figTypeArrayList.get(i));
        System.out.println("fin getTargetFigureFromBestCombination");
        return figTypeArrayList;
    }*/

    private ArrayList<String> getTargetFigureFromBestCombination(ArrayList<BoxPair> noDuplicatesArrayListBoxPair){
        System.out.println("getTargetFigureFromBestCombination");
        //les figures de la combinaison
        ArrayList<String> TargetFigurefigTypeArrayList = new ArrayList<>();
        for (int i = 0; i<noDuplicatesArrayListBoxPair.size(); i++)
            if (!TargetFigurefigTypeArrayList.contains(currentGame.findBoxById(noDuplicatesArrayListBoxPair.get(i).getPairId()).getFigType()))
                TargetFigurefigTypeArrayList.add(currentGame.findBoxById(noDuplicatesArrayListBoxPair.get(i).getPairId()).getFigType());
        // for (int i = 0; i<figTypeArrayList.size(); i++)
        //     System.out.println(figTypeArrayList.get(i));
        System.out.println("TargetFigurefigTypeArrayList: "+ TargetFigurefigTypeArrayList);
        System.out.println("fin getTargetFigureFromBestCombination");
        return TargetFigurefigTypeArrayList;
    }

    private ArrayList<String> getCommonListElements(ArrayList<String> list1, ArrayList<String> list2){
        ArrayList<String> commonElementList = new ArrayList<>(list2);
        commonElementList.retainAll(list1);
        return commonElementList;
    }

    // retourne la 1ere figure cible commune aux 2 listes, celle des figures possibles et celle de la meilleure combinaison possible
    private String getTargetFromCombinationAndDice(ArrayList<BoxPair> noDuplicatesBoxPairArrayList){
        System.out.println("Debut getTargetFromCombinationAndDice");
        ArrayList<String> possibleTargetsFromDiceSet = getPossibleTargetFiguresFromDiceSet(currentGame);
        ArrayList<String> bestCombinationTargetList = getTargetFigureFromBestCombination(noDuplicatesBoxPairArrayList);
        //1st:  Compare and get the figure availableFiguresFromDiceSet and  bestCombinationTargetList have in common if any,
        ArrayList<String> commonElementsList = getCommonListElements(possibleTargetsFromDiceSet, bestCombinationTargetList);
        if (commonElementsList.size()>0)
            return commonElementsList.get(commonElementsList.size()-1);//return last element (best)
            // else throw all dice (target = sec)
        else System.out.println("getTargetFromCombinationAndDice: pas d'élément commun, on retourne sec");
        System.out.println("Fin getTargetFromCombinationAndDice");
        return "sec";
    }

    // retourne la 1ere case commune aux 2 listes, celle des figures obtenues et celle de la meilleure combinaison possible
  /*  private int getBoxIdFromCombinationAndDice(ArrayList<BoxPair> noDuplicatesBoxPairIdArrayList){
        ArrayList<String> availableFiguresFromDiceSet = getAvailableFiguresFromdiceSet(currentGame);
        ArrayList<String> bestCombinationTargetList = getTargetFigureFromBestCombination(noDuplicatesBoxPairIdArrayList);
        ArrayList<String> commonElementsList = getCommonListElements(availableFiguresFromDiceSet, bestCombinationTargetList);
        if (commonElementsList.size()>0){
            for (int i=0; i< noDuplicatesBoxPairIdArrayList.size(); i++){
                if (currentGame.findBoxById(noDuplicatesBoxPairIdArrayList.get(i)).figType.equals(commonElementsList.get(0)))
                    return noDuplicatesBoxPairIdArrayList.get(i);
            }
        }
        return 0;
    }*/

    private BoxPair getAvailableBoxPairFromCombinationAndDice(ArrayList<BoxPair> noDuplicatesBoxIdArrayList){
        System.out.println("Debut getAvailableBoxPairFromCombinationAndDice");
        ArrayList<String> availableFiguresFromDiceSet = getAvailableFiguresFromdiceSet(currentGame);
        // System.out.println("availableFiguresFromDiceSet: "+availableFiguresFromDiceSet);
        ArrayList<String> bestCombinationTargetList = getTargetFigureFromBestCombination(noDuplicatesBoxIdArrayList);
        //System.out.println("bestCombinationTargetList "+ bestCombinationTargetList);
        ArrayList<String> commonElementsList = getCommonListElements(availableFiguresFromDiceSet, bestCombinationTargetList);

      /*  //purger noDuplicatesBoxIdArrayList des box qui fullLinent à perte
        Iterator it = noDuplicatesBoxIdArrayList.iterator();
        while (it.hasNext()){
            BoxPair nextBp = (BoxPair) it.next();
            if (nextBp.isFullLine()&& (nextBp.getPairPoints()+currentGame.redPoints<currentGame.bluePoints)){
                System.out.print("On retire la box:"+currentGame.findBoxById(nextBp.getPairId()).toString()+" ");
                System.out.println(nextBp.toString());
                it.remove();
            }
            //Elimination des box qui ne rapportent pas de point alors que l'ennemi en a en réserve
            else if(maxBluePointsPossible>maxRedPointsPossible)
                if (nextBp.getPairPoints()==0)
                    it.remove();
        }*/

        //maintenant rechercher les correspondances entre les figures obtenues et la meilleure combinaison. Retourner la meilleure (fin de list)
        if (commonElementsList.size()>0){
            for (int i=0; i< noDuplicatesBoxIdArrayList.size(); i++){
                if (currentGame.findBoxById(noDuplicatesBoxIdArrayList.get(i).getPairId()).getFigType().equals(commonElementsList.get(commonElementsList.size()-1))){
                    System.out.println("Fin getAvailableBoxPairFromCombinationAndDice");
                    return noDuplicatesBoxIdArrayList.get(i);
                }
            }
        }
        //Sinon retourner un null
        BoxPair nullBoxPair = new BoxPair(new Box(),0,0);
        System.out.println("getAvailableBoxPairFromCombinationAndDice :BoxPair NULL");
        return nullBoxPair;
    }

    private void purgeFullLineBoxPairArrayList (ArrayList<BoxPair> noDuplicatesBoxIdArrayList, int maxRedPointsPossible, int maxBluePointsPossible){
        System.out.println("purgeBoxPairArrayList");
        //purger noDuplicatesBoxIdArrayList des box qui fullLinent à perte
        Iterator it = noDuplicatesBoxIdArrayList.iterator();
        while (it.hasNext()){
            BoxPair nextBp = (BoxPair) it.next();
            if (nextBp.isFullLine()&& (nextBp.getPairPoints()+currentGame.redPoints<currentGame.bluePoints)){
                System.out.print("On retire la box (FullLine):"+currentGame.findBoxById(nextBp.getPairId()).toString()+" ");
                System.out.println(nextBp.toString());
                it.remove();
            }
            //Elimination des box qui ne rapportent pas de point alors que l'ennemi en a en réserve
            else if(maxBluePointsPossible>maxRedPointsPossible)
                if (nextBp.getPairPoints()==0){
                    System.out.print("On retire la box (points=0):"+currentGame.findBoxById(nextBp.getPairId()).toString()+" ");
                    it.remove();
                }
        }
        System.out.println("Fin de purgeBoxPairArrayList");
    }
    /*Method where droid choses target from current throw*/
    //Returns the target figtype upon wich we will select the dices next turn if any else returns null string and place the marker if possible
    private String machineChoseFromDices() {
        System.out.println("#########################################################################################");
        currentGame.printSelectedDice();
        
        //TODO revoir la procédure de decision, c'est pas clair!
        //Recensement des figures posables et/ou améliorables en fonction des dés courants
        ArrayList<BoxPair> optimalNextThrowBoxPairList = getListOptimalNextThrowBoxpairFromFigureList(currentGame);

        //??Ajout de la figure améliorable d'abord pour privilégier les 2 autres solutions avant en cas d'égalité de points
        //TODO ABSOLUMENT implémenter la selection de cases (target ou à poser) qui empêchent les bleus de gagner si elles sont possibles après le 1er jet
        //TODO genre il faut le 2 et on a un 2 ou deux 2 au jet courant -> chercher le brelan de 2 en priorité
        //Points éventuels au tour courant en fonction des dés actuels si on sélectionne + relance avec copie de la liste
        BoxPair boxPairTargetCurrentTurnNextThrow = getOptimalCurrentTurnNextThrowBoxPairFromFigureList(currentGame, new ArrayList<>(optimalNextThrowBoxPairList));
        //Points éventuels à un tour suivant en fonction des dés actuels si on sélectionne + relance avec copie de la liste
        BoxPair boxPairTargetNextTurnNextThrow = getOptimalNextTurnNextThrowBoxPairFromFigureList(currentGame, new ArrayList<>(optimalNextThrowBoxPairList));
        //Point immédiats au tour courant si on place maintenant
        BoxPair boxPairCurrentTurnCurrentThrow = getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList(currentGame);
        //Points éventuels à un  tour suivant si on place maintenant
        BoxPair boxPairNextTurnCurrentThrow = getOptimalNextTurnCurrentThrowBoxPairPerFigureList(currentGame);

        System.out.println("boxPairCurrentTurnCurrentThrow: ");
        if (boxPairCurrentTurnCurrentThrow.getPairId()>0)
            currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).afficherBox();

        System.out.println("boxPairNextTurnCurrentThrow: ");
        if (boxPairNextTurnCurrentThrow.getPairId()>0)
            currentGame.findBoxById(boxPairNextTurnCurrentThrow.getPairId()).afficherBox();

        System.out.println("boxPairTargetCurrentTurnNextThrow: ");
        if (boxPairTargetCurrentTurnNextThrow.getPairId()>0)
            currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).afficherBox();

        System.out.println("boxPairTargetNextTurnNextThrow: ");
        if (boxPairTargetNextTurnNextThrow.getPairId()>0)
            currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).afficherBox();
        //Test de la nouvelle fonction ....ça pourra fonctionner...
        if (currentGame.redMarkers < 5) {
            System.out.println("redmarkers<5");
            if (currentGame.redPoints<=currentGame.bluePoints){
                System.out.println("redpPoints<=bluePoints!");
                // currentGame.afficherDes();
                //currentGame.printSelectedDice();
                List<JeuCombinaison> jcRedList = bestCombinationsAvailable("red", currentGame);
                int maxRedPointsPossible = 0;
                if (!jcRedList.isEmpty())
                    maxRedPointsPossible=jcRedList.get(jcRedList.size()-1).getPoints();

                List<JeuCombinaison> jcBlueList = bestCombinationsAvailable("blue", currentGame);
                int maxBluePointsPossible = 0;
                if (!jcBlueList.isEmpty())
                    maxBluePointsPossible=jcBlueList.get(jcBlueList.size()-1).getPoints();

                // System.out.println("Liste des box optimales par jeu de combinaison (jclist): ");
                // for (int i=0; i<jclist.size(); i++)
                //    jclist.get(i).printIdCombinationArrayList();
                //Ne récupérer que les combinaisons qui permettent de dépasser ou d'égaler l'adversaire
                List<JeuCombinaison> bestjclist = new ArrayList<>();
                for (int i = 0; i < jcRedList.size(); i++)
                    if (jcRedList.get(i).getPoints() + currentGame.redPoints > currentGame.bluePoints)
                        bestjclist.add(jcRedList.get(i));

               /* System.out.println("Liste des box optimales en tout (bestjclist): ");
                for (int i = 0; i < bestjclist.size(); i++) {
                    System.out.println("Max points additionnels: " + bestjclist.get(i).getPoints());
                    bestjclist.get(i).printBoxPairCombinationArrayList();
                    System.out.println("*********");
                }*/

                //les fusionner sans doublons dans un set puis en faire une liste
               /* Set<BoxPair> setBoxId = new LinkedHashSet<>();
                for (int i = 0; i < bestjclist.size(); i++)
                    setBoxId.addAll(bestjclist.get(i).getBoxPairCombinationArrayList());
                ArrayList<BoxPair> noDuplicatesBoxPairArrayList = new ArrayList<>(setBoxId);

                */
                //Bidouille pour retirer les doublons
                ArrayList<BoxPair> noDuplicatesBoxPairArrayList = new ArrayList<>();
                ArrayList<Integer> tempNoDuplicatesBoxPairIdArrayList = new ArrayList<>();
                for (int i = 0; i < bestjclist.size(); i++)
                    for (int j=0; j<bestjclist.get(i).getBoxPairCombinationArrayList().size(); j++)
                    {
                        if (!tempNoDuplicatesBoxPairIdArrayList.contains(bestjclist.get(i).getBoxPairCombinationArrayList().get(j).getPairId())){
                            tempNoDuplicatesBoxPairIdArrayList.add(bestjclist.get(i).getBoxPairCombinationArrayList().get(j).getPairId());
                            noDuplicatesBoxPairArrayList.add(bestjclist.get(i).getBoxPairCombinationArrayList().get(j));
                        }
                    }

                Collections.sort(noDuplicatesBoxPairArrayList);
                purgeFullLineBoxPairArrayList(noDuplicatesBoxPairArrayList, maxRedPointsPossible, maxBluePointsPossible);
                System.out.println("noDuplicatesBoxPairArrayList:");
                for (int i =0; i<noDuplicatesBoxPairArrayList.size(); i++)
                    currentGame.findBoxById(noDuplicatesBoxPairArrayList.get(i).getPairId()).afficherBox();
                System.out.println("*********");
                //TODO: verifier gérer le cas où la machine fait un appel, prendre la bonne case.

                //On a fait appel, l'appel n'est pas encore réussi, on tente une dernière fois si l'appel est dans la combinaison
                if (currentGame.throwNb == 2 && currentGame.appelClicked && !currentGame.fiveDices.figureList.equals("Appel")) {
                    System.out.println("Gestion appel1");
                    //récupérer les boxPair correspondant aux appels
                    List<BoxPair> appelBoxPairList = currentGame.getListBoxPairColorPerFigure("appel", "white");
                    //trouver ceux qui sont dans la liste de la combinaison et faire appel si on en trouve
                    if (appelBoxPairList.size()>0)
                        for (int i = 0; i < appelBoxPairList.size(); i++)
                            if (noDuplicatesBoxPairArrayList.contains(appelBoxPairList.get(i).getPairId())) {
                                System.out.println("Gestion appel2");
                                return "appel";
                            }
                }
                //sinon si l'appel est réussi et est dans la combinaison
                else if (currentGame.appelClicked && currentGame.fiveDices.figureList.equals("Appel")) {
                    List<BoxPair> appelBoxPairList = currentGame.getListBoxPairColorPerFigure("appel", "white");
                    //trouver ceux qui sont dans la liste de la combinaison
                    for (int i = 0; i < appelBoxPairList.size(); i++)
                        if (noDuplicatesBoxPairArrayList.contains(appelBoxPairList.get(i).getPairId())) {
                            System.out.println("Gestion appel 3");
                            return machinePlaceMarkerById(appelBoxPairList.get(i).getPairId());
                        }
                } else if (currentGame.appelClicked && currentGame.throwNb == 3 && !currentGame.fiveDices.figureList.equals("Appel")) {
                    System.out.println("Gestion appel 4");
                    checkForMissedAppel(currentGame);
                    return "blue";
                }

                //sinon si il n'y a pas d'appel, on continue avec les autres box de la combinaison
                if
                (
                        (currentGame.redPoints == currentGame.bluePoints && currentGame.redMarkers == currentGame.blueMarkers) ||
                                currentGame.redPoints <= currentGame.bluePoints
                ) {

                    //1: placer ou tenter une des figures/cases selectionnées si elle est aussi dans la combinaison
                    if (!noDuplicatesBoxPairArrayList.isEmpty())
                        if (noDuplicatesBoxPairArrayList.contains(boxPairCurrentTurnCurrentThrow.getPairId())) {
                            System.out.println("Placer: noDuplicatesBoxIdArrayList.contains(boxPairCurrentTurnCurrentThrow.getPairId())");
                            currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).afficherBox();
                            System.out.println("----------------------------------------------------");
                            if (boxPairCurrentTurnCurrentThrow.getPairPoints()!=-1){
                                return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());
                            }
                        }
                    if (!noDuplicatesBoxPairArrayList.isEmpty())
                        if (noDuplicatesBoxPairArrayList.contains(boxPairNextTurnCurrentThrow.getPairId())) {
                            System.out.println("Placer: noDuplicatesBoxIdArrayList.contains(boxPairNextTurnCurrentThrow.getPairId())");
                            currentGame.findBoxById(boxPairNextTurnCurrentThrow.getPairId()).afficherBox();
                            System.out.println("----------------------------------------------------");
                            if (boxPairNextTurnCurrentThrow.getPairPoints()!=-1){
                                return machinePlaceMarkerById(boxPairNextTurnCurrentThrow.getPairId());
                            }
                        }
                    if (currentGame.throwNb < currentGame.maxThrowNb)
                        if (!noDuplicatesBoxPairArrayList.isEmpty())
                            if (noDuplicatesBoxPairArrayList.contains(boxPairTargetCurrentTurnNextThrow.getPairId())) {
                                System.out.println("Target: noDuplicatesBoxIdArrayList.contains(boxPairTargetCurrentTurnNextThrow.getPairId())");
                                currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).afficherBox();
                                System.out.println("----------------------------------------------------");
                                showTargetUI(boxPairTargetCurrentTurnNextThrow.getPairId());
                                return currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).getFigType();
                            }
                    if (currentGame.throwNb < currentGame.maxThrowNb)
                        if (!noDuplicatesBoxPairArrayList.isEmpty())
                            if (noDuplicatesBoxPairArrayList.contains(boxPairTargetNextTurnNextThrow.getPairId())) {
                                System.out.println("Target:noDuplicatesBoxIdArrayList.contains(boxPairTargetNextTurnNextThrow.getPairId())");
                                currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).afficherBox();
                                System.out.println("----------------------------------------------------");
                                showTargetUI(boxPairTargetNextTurnNextThrow.getPairId());
                                return currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).getFigType();
                            }

                    //2: sinon placer une des éventuelles autres figures obtenues si elle est dans la combinaison
                    BoxPair AvailableBoxPairFromCombinationAndDiceBoxPair = getAvailableBoxPairFromCombinationAndDice(noDuplicatesBoxPairArrayList);
                    if (AvailableBoxPairFromCombinationAndDiceBoxPair.getPairId() > 0) {
                        System.out.print("AvailableBoxPairFromCombinationAndDiceBoxPair: ");
                        currentGame.findBoxById(AvailableBoxPairFromCombinationAndDiceBoxPair.getPairId()).afficherBox();
                        return machinePlaceMarkerById(AvailableBoxPairFromCombinationAndDiceBoxPair.getPairId());
                    }
                    //3: sinon il faut tenter une figure appartenant à la/les combinaisons gagnantes
                    /*
                     * récupérer les figures correspondant aux boxId des combinaisons gagnantes
                     * retourner celle qui convient le mieux en fonction des dés actuels
                     *
                     * */
                    //Tenter de selectionner les dés pour avoir une figure de la combinaison
                    if (currentGame.throwNb < currentGame.maxThrowNb) {
                        String targetFromCombinationAndDice = getTargetFromCombinationAndDice(noDuplicatesBoxPairArrayList);
                        System.out.println("targetFromCombinationAndDice: "+ targetFromCombinationAndDice);
                        for (int i= noDuplicatesBoxPairArrayList.size()-1; i>=0; i--){
                            if (noDuplicatesBoxPairArrayList.get(i).getFigType().equals(targetFromCombinationAndDice))
                                showTargetUI(noDuplicatesBoxPairArrayList.get(i).getPairId());
                        }
                        return targetFromCombinationAndDice;
                    }
                    //on relance tout
                    if (currentGame.throwNb < currentGame.maxThrowNb){
                        System.out.println("retourne sec");
                        return "sec";
                    }
                    //sinon on passe le tour
                    System.out.println("blue");
                    return "blue";
                }
            }
        }

        //redMarkers>5 ou/et  redpoints>bluepoints
        //Si on a  appelé une figure et qu'on n'a pas réussi l'appel au 2ème jet, tenter une fois encore
        if (currentGame.throwNb < currentGame.maxThrowNb)
            if ((currentGame.throwNb==2) && (!currentGame.fiveDices.figureList.equals("Appel")))
                if (currentGame.appelClicked){
                    System.out.println("-3: return appel");
                    return "Appel";
                }

        //Si on a une boxPair à placer maintenant
        if (boxPairCurrentTurnCurrentThrow.getPairId()>0)
        {
            System.out.println("boxPairCurrentTurnCurrentThrow.getPairId()>0");
            //Si appel réussi, placer le pion
            if (currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).getFigType().equals("Appel")){
                System.out.println("-2: boxPairCurrentTurnCurrentThrow.getPairId()).figType.equals(Appel)");
                if (currentGame.fiveDices.figureList.equals("Appel")){
                    System.out.println("-2: currentGame.fiveDices.figureList.equals(Appel)->placer");
                    return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());
                }
            }

            //Si on n'a pas fait appel, voir si on a avantage à retenter ou bien si on place maintenant
            if (currentGame.throwNb<currentGame.maxThrowNb)
            {
                if (boxPairTargetCurrentTurnNextThrow.getPairId()>0)
                {
                    System.out.println("-1: boxPairTargetCurrentTurnNextThrow.getPairId()>0");

                    //Ne faire nextthrow que si currenthrow pas posable
                    // ou si on peut LA garder (ex: BRELAN->full ok  mais pas FULL->carre ni Yam ni Sec)
                    if (boxPairTargetCurrentTurnNextThrow.getPairPoints()>=boxPairCurrentTurnCurrentThrow.getPairPoints())
                    {
                        System.out.println("boxPairTargetCurrentTurnNextThrow.getPairPoints()>=boxPairCurrentTurnCurrentThrow.getPairPoints()");
                        if (!((currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).getFigType().equals("Full"))||
                                (currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).getFigType().equals("Yam"))||
                                (currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).getFigType().equals("Sec"))))
                        {
                            System.out.println("-1: currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).figType="+currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).getFigType());
                            showTargetUI(boxPairTargetCurrentTurnNextThrow.getPairId());
                            return currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).getFigType();
                        }
                        else {
                            System.out.println("-1: return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId())");
                            return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());
                        }
                    }
                }
            }

            //1: Placer pour marquer au tour courant
            if (boxPairCurrentTurnCurrentThrow.getPairPoints()>0){
                System.out.println("1: boxPairCurrentTurnCurrentThrow.getPairPoints()>0");
                return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());
            }

            //2:placer maintenant pour pouvoir peut être marquer à un tour suivant.
            if (boxPairNextTurnCurrentThrow.getPairId()>0){
                System.out.println("2: boxPairNextTurnCurrentThrow.getPairId()>0)");
                if (boxPairNextTurnCurrentThrow.getPairPoints()>0){// verifier cela
                    System.out.println("2: boxPairNextTurnCurrentThrow.getPairPoints()>0)");
                    return machinePlaceMarkerById(boxPairNextTurnCurrentThrow.getPairId());
                }
            }

            //3: Améliorer figure pour placer au jet suivant et pouvoir peut être marquer à un tour suivant
            if (currentGame.throwNb<currentGame.maxThrowNb)
                if (boxPairTargetNextTurnNextThrow.getPairId()>0){
                    System.out.println("3: boxPairTargetNextTurnNextThrow.getPairId()>0");
                    if (boxPairTargetNextTurnNextThrow.getPairPoints()>0){
                        System.out.println("3: return currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).figType: "+currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).getFigType());
                        showTargetUI(boxPairTargetNextTurnNextThrow.getPairId());
                        return currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).getFigType();
                    }
                }


            //4: placer ce que l'on a
            System.out.println("4: return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId())");
            return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());
            //     }
            //     else System.out.println("Quel gâchis, on ne pose pas!");

        }

        //Pas de figure posable au jet courant on tente d'améliorer la figure:
        else {
            if (currentGame.throwNb<currentGame.maxThrowNb){
                if (boxPairTargetCurrentTurnNextThrow.getPairId() > 0) {
                    System.out.println("5: boxPairTargetCurrentTurnNextThrow.getPairId() > 0)");
                    if (boxPairTargetNextTurnNextThrow.getPairId()>0){
                        System.out.println("5: boxPairTargetNextTurnNextThrow.getPairId()>0)");
                        if ((boxPairTargetCurrentTurnNextThrow.getPairPoints()>=boxPairTargetNextTurnNextThrow.getPairPoints())){//On tente d'améliorer la figure pour placer au jet suivant
                            System.out.println("5: return currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).figType: "+currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).getFigType());
                            showTargetUI(boxPairTargetCurrentTurnNextThrow.getPairId());
                            return currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).getFigType();
                        }
                        else{
                            System.out.println("5: return currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).figType: "+currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).getFigType());
                            showTargetUI(boxPairTargetNextTurnNextThrow.getPairId());
                            return currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).getFigType();
                        }
                    }
                }
                else
                if (currentGame.throwNb < currentGame.maxThrowNb)
                    if (boxPairTargetNextTurnNextThrow.getPairId() > 0) {
                        System.out.println("6 boxPairTargetNextTurnNextThrow.getPairId() > 0");
                        if (boxPairTargetNextTurnNextThrow.getPairPoints() >= 0) {
                            System.out.println("6 return currentGame.findBoxById(boxPairNextTurnCurrentThrow.getPairId()).figType: "+currentGame.findBoxById(boxPairNextTurnCurrentThrow.getPairId()).getFigType());
                            showTargetUI(boxPairNextTurnCurrentThrow.getPairId());
                            return currentGame.findBoxById(boxPairNextTurnCurrentThrow.getPairId()).getFigType();
                        }
                    }
                    else
                    if (currentGame.throwNb < currentGame.maxThrowNb) {
                        System.out.println("7: return sec");
                        return "sec";
                    }
            }
            else{
                checkForMissedAppel(currentGame);
                System.out.println("8: return blue");
                return "blue";
            }
        }
        System.out.println("9: return blue");
        return "blue";
    }


    /*xxxxxx Methods to deal with dice selection according to target xxxxxx*/
    //Select dice from target as defined by choseFromeDice
    private void selectDiceFromTarget(Jeu aGame, String target) {
        switch (target) {
            case "Carre":
                selectForCarre(aGame);
                break;
            case "Full":
                selectForFull(aGame);
                break;
            case "Yam":
                selectForYam(aGame);
                break;
            case "Small":
                selectForSmall(aGame);
                break;
            case "Suite":
                selectForSuite(aGame);
                break;
            case "Sec":
                selectForSec(aGame);
                break;
            case "Appel":
                selectForAppel(aGame);
                break;
            case "1":
                selectForBrelan(aGame,  1);
                break;
            case "2":
                selectForBrelan(aGame, 2);
                break;
            case "3":
                selectForBrelan(aGame, 3);
                break;
            case "4":
                selectForBrelan(aGame, 4);
                break;
            case "5":
                selectForBrelan(aGame, 5);
                break;
            case "6":
                selectForBrelan(aGame, 6);
                break;
            case "allDice":
                selectAllDices(aGame);
                break;
            case "blue":
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentGame.changeTurnColor("blue");
                    }
                });
                break;
        }
    }

    private void selectAllDices(Jeu aGame){
        for (int i =0; i<5;i++)
            aGame.fiveDices.diceSet[i].isSelected=true;
    }

    private void selectForCarre(Jeu aGame) {
        if (aGame.fiveDices.figureList.matches( ".*([123456]).*")){
            for (int i = 0; i < 5; i++)
                aGame.fiveDices.diceSet[i].isSelected = true;
            if (aGame.fiveDices.tempDiceSetIndValues[0][1] == aGame.fiveDices.tempDiceSetIndValues[2][1]) {
                for (int j = 0; j < 3; j++)
                    aGame.fiveDices.diceSet[
                            aGame.fiveDices.tempDiceSetIndValues[j][0]
                            ].isSelected = false;
            } else if (aGame.fiveDices.tempDiceSetIndValues[1][1] == aGame.fiveDices.tempDiceSetIndValues[3][1]) {
                for (int j = 1; j < 4; j++)
                    aGame.fiveDices.diceSet[
                            aGame.fiveDices.tempDiceSetIndValues[j][0]
                            ].isSelected = false;
            } else if (aGame.fiveDices.tempDiceSetIndValues[2][1] == aGame.fiveDices.tempDiceSetIndValues[4][1]) {
                for (int j = 2; j < 5; j++)
                    aGame.fiveDices.diceSet[
                            aGame.fiveDices.tempDiceSetIndValues[j][0]
                            ].isSelected = false;
            }
        }
        //Traiter les paires
        else if (figureContainsSinglePair(aGame))
            selectFromSinglePair(aGame);
        //pas besoin de traiter le full car si full alors brelan
    }

    private void selectForFull(Jeu aGame) {
        if (figureContainsDoublePair(aGame)) {
            selectfromDoublePair(aGame);
        }
        //Traiter brelan
        else if (currentGame.fiveDices.figureList.matches( ".*([123456]).*")){
            for (int i = 0; i < 5; i++)
                aGame.fiveDices.diceSet[i].isSelected = true;
            if (aGame.fiveDices.tempDiceSetIndValues[0][1] == aGame.fiveDices.tempDiceSetIndValues[2][1]) {
                for (int j = 0; j < 3; j++)
                    aGame.fiveDices.diceSet[
                            currentGame.fiveDices.tempDiceSetIndValues[j][0]
                            ].isSelected = false;
            } else if (aGame.fiveDices.tempDiceSetIndValues[1][1] == aGame.fiveDices.tempDiceSetIndValues[3][1]) {
                for (int j = 1; j < 4; j++)
                    aGame.fiveDices.diceSet[
                            currentGame.fiveDices.tempDiceSetIndValues[j][0]
                            ].isSelected = false;
            } else if (aGame.fiveDices.tempDiceSetIndValues[2][1] == aGame.fiveDices.tempDiceSetIndValues[4][1]) {
                for (int j = 2; j < 5; j++)
                    aGame.fiveDices.diceSet[
                            currentGame.fiveDices.tempDiceSetIndValues[j][0]
                            ].isSelected = false;
            }
        }
        //traiter paire
        else if (figureContainsPair(aGame)) {
            selectFromSinglePair(aGame);
        }
    }

    private void selectForYam(Jeu aGame) {
        System.out.println("Select for yam1");
        if (aGame.fiveDices.figureList.contains("Carre")) {
            for (int i = 0; i < 5; i++)
                aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[i][0]].isSelected = false;
            if (aGame.fiveDices.tempDiceSetIndValues[0][1] == aGame.fiveDices.tempDiceSetIndValues[3][1])
                aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[4][0]].isSelected = true;
            else
                aGame.fiveDices.diceSet[currentGame.fiveDices.tempDiceSetIndValues[0][0]].isSelected = true;
        }
        else if (aGame.fiveDices.figureList.matches( ".*([123456]).*")){
            selectForCarre(aGame);
        }
        //       else if (figureContainsSinglePair()){
        else  if (figureContainsPair(aGame)){
            //selectForBrelan(getSinglePairValue());
            if (figureContainsDoublePair(aGame)){
                int valIdx1 = aGame.fiveDices.tempDiceSetIndValues[1][1];
                int valIdx3 = aGame.fiveDices.tempDiceSetIndValues[3][1];
                ArrayList<BoxPair>  pairs = new ArrayList<>();
                pairs.addAll(aGame.getListBoxPairColorPerFigure(Integer.toString(valIdx1), "white"));
                pairs.addAll(aGame.getListBoxPairColorPerFigure(Integer.toString(valIdx3), "white"));
                //choisir le meilleur brelan fallback
                for (int i =0; i<pairs.size(); i++){
                    pairs.get(i).setPairPoints(getPointsIfMarkerPlacedOnBox(aGame, "red", pairs.get(i).getBox()));
                }
                Collections.sort(pairs);
                if (pairs.size()>0){
                    selectForBrelan(aGame, Integer.valueOf(pairs.get(pairs.size()-1).getFigType()));
                    System.out.println("select for yam1: "+Integer.valueOf(pairs.get(pairs.size()-1).getFigType()));
                }
            }
            else {
                System.out.println("select for yam2: "+getFirstAvailablePairValue(aGame));
                selectForBrelan(aGame, getFirstAvailablePairValue(aGame));
            }
        }
        //TODO Ici verifier : optimiser le choix de la paire, prendre celle qui pourra aussi en cas d'échec faire un brelan posable (non pris déjà)
    }

    private void selectForSmall(Jeu aGame) {
        //Select dice so that (sum of 1s & 2s) < 5
        int sum=0;
        for (int i=0; i<5; i++)
            aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[i][0]].isSelected = true;
        for (int i = 0; i<5; i++){
            if (aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[i][0]].value==1){
                aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[i][0]].isSelected = false;
                sum+= aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[i][0]].value;
            }
        }
        for (int i = 0; i<5; i++){
            if (aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[i][0]].value==2){
                aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[i][0]].isSelected = false;
                if (sum+aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[i][0]].value<5)
                    sum+= aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[i][0]].value;
            }
        }
    }

    private void selectForSuite(Jeu aGame){
        int idx= getIdxFrom4inARow(aGame);
        for (int i =0; i<5; i++)
            aGame.fiveDices.diceSet[i].isSelected=false;
        aGame.fiveDices.diceSet[idx].isSelected=true;
    }

    private void selectForSec(Jeu aGame){
        for (int i =0; i<5; i++)
            aGame.fiveDices.diceSet[i].isSelected=true;
    }

    private void selectForBrelan(Jeu aGame, int value){
        for (int i =0; i<5; i++)
            aGame.fiveDices.diceSet[i].isSelected= aGame.fiveDices.diceSet[i].value != value;
    }

    private void selectForAppel(Jeu aGame){
        if (aGame.throwNb==2){
            System.out.println("Select for appel au "+aGame.appelRegistered);
            switch (aGame.appelRegistered){
                case "Full":
                    selectForFull(aGame);
                    break;
                case "Suite":
                    selectForSuite(aGame);
                    break;
                case "Carre":
                    selectForCarre(aGame);
                    break;
                case "Small":
                    selectForSmall(aGame);
                    break;
                case "Yam":
                    selectForYam(aGame);
                    break;
                case "Sec":
                    selectForSec(aGame);
                    break;
            }
        }
        else {
            //Placé en 1er donc appel au full passe d'abord
            if (figureContainsDoublePair(aGame)){
                selectForFull(aGame);
                appel(aGame, "Full");
            }
            else if (figureContains4InARow(aGame)!=0){
                selectForSuite(aGame);
                appel(aGame, "Suite");
            }
//             else  if (stringMatcher(aGame.fiveDices.figureList, ".*1|2|3|4|5|6|Carre.*")){
            else if (aGame.fiveDices.figureList.matches( ".*(1|2|3|4|5|6|Carre).*")){
                selectForCarre(aGame);//On peut aussi partir d'un carre pour appeler un carre
                appel(aGame,"Carre");
            }
            else if (figureContainsAlmostSmall(aGame)){
                selectForSmall(aGame);
                appel(aGame, "Small");
            }
            else if (figureContainsSinglePair(aGame)){
                // selectForCarre();
                // appel("Carre");
                selectForBrelan(aGame, getSinglePairValue(aGame));
                appel(aGame, "Full");
            }
            //pour gérer le cas où la machine décide de viser la case appel parce que c'est la seule option pour qu'elle gagne
            else {
                selectForSec(aGame);
                appel(aGame, "Sec");
            }
        }
    }
    //Pour Brelan  carre Full Small Yam  + Appel
    private boolean figureContainsSingleValue(Jeu aGame, int value){
        for (int i =0; i<3; i++){
            if (aGame.fiveDices.tempDiceSetIndValues[i][1]==value && aGame.fiveDices.tempDiceSetIndValues[i+1][1]!=value)
                return true;
            if (aGame.fiveDices.tempDiceSetIndValues[4][1]==value)
                return true;
        }
        return false;
    }


    private boolean figureContainsPair(Jeu aGame){
        for(int i = 0; i <4; i++)
            if (aGame.fiveDices.tempDiceSetIndValues[i][1]==aGame.fiveDices.tempDiceSetIndValues[i+1][1])
                return true;
        return false;
    }

    private int getFirstAvailablePairValue(Jeu aGame){
        for(int i = 0; i <4; i++)
            if (aGame.fiveDices.tempDiceSetIndValues[i][1]==aGame.fiveDices.tempDiceSetIndValues[i+1][1])
                return aGame.fiveDices.tempDiceSetIndValues[i][1];
        return 0;
    }

    private boolean figureContainsSinglePair(Jeu aGame){
        return figureContainsPair(aGame) && !figureContainsDoublePair(aGame);
    }

    private int getSinglePairValue(Jeu aGame) {
        if (figureContainsSinglePair(aGame)){
            for (int i = 0; i < 4; i++)
                if (aGame.fiveDices.tempDiceSetIndValues[i][1] == aGame.fiveDices.tempDiceSetIndValues[i + 1][1])
                    return aGame.fiveDices.tempDiceSetIndValues[i][1];
        }
        return 0;
    }

    private void selectFromSinglePair(Jeu aGame){
        for (int i =0; i<5; i++)
            aGame.fiveDices.diceSet[i].isSelected=true;
        for(int i = 0; i <4; i++)
            if (aGame.fiveDices.tempDiceSetIndValues[i][1]==aGame.fiveDices.tempDiceSetIndValues[i+1][1]){
                for (int j=i+2; j<4; j++)
                    if (aGame.fiveDices.tempDiceSetIndValues[j][1]==aGame.fiveDices.tempDiceSetIndValues[j+1][1])
                        return;//if we find another pair
                aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[i][0]].isSelected=false;
                aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[i+1][0]].isSelected=false;
                return;
            }
    }

    private boolean figureContainsDoublePair(Jeu aGame){
        if ((aGame.fiveDices.tempDiceSetIndValues[0][1]==aGame.fiveDices.tempDiceSetIndValues[1][1]) &&
                (aGame.fiveDices.tempDiceSetIndValues[2][1]==aGame.fiveDices.tempDiceSetIndValues[3][1])){
            return true;
        }
        else if ((aGame.fiveDices.tempDiceSetIndValues[1][1]==aGame.fiveDices.tempDiceSetIndValues[2][1]) &&
                (aGame.fiveDices.tempDiceSetIndValues[3][1]==aGame.fiveDices.tempDiceSetIndValues[4][1])){
            return true;
        }
        else if ((aGame.fiveDices.tempDiceSetIndValues[0][1]==aGame.fiveDices.tempDiceSetIndValues[1][1]) &&
                (aGame.fiveDices.tempDiceSetIndValues[3][1]==aGame.fiveDices.tempDiceSetIndValues[4][1])){
            return true;
        }
        return false;
    }

    private void selectfromDoublePair(Jeu aGame){
        for (int i =0; i<5; i++)
            aGame.fiveDices.diceSet[i].isSelected=false;
        if ((aGame.fiveDices.tempDiceSetIndValues[0][1]==aGame.fiveDices.tempDiceSetIndValues[1][1]) &&
                (aGame.fiveDices.tempDiceSetIndValues[2][1]==aGame.fiveDices.tempDiceSetIndValues[3][1])){
            aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[4][0]].isSelected=true;
        }
        else if ((aGame.fiveDices.tempDiceSetIndValues[1][1]==aGame.fiveDices.tempDiceSetIndValues[2][1]) &&
                (aGame.fiveDices.tempDiceSetIndValues[3][1]==aGame.fiveDices.tempDiceSetIndValues[4][1])){
            aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[0][0]].isSelected=true;
        }
        else if ((aGame.fiveDices.tempDiceSetIndValues[0][1]==aGame.fiveDices.tempDiceSetIndValues[1][1]) &&
                (aGame.fiveDices.tempDiceSetIndValues[3][1]==aGame.fiveDices.tempDiceSetIndValues[4][1])){
            aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[2][0]].isSelected=true;
        }
    }
    //4 à la suite
    private int getIdxFrom4inARow(Jeu aGame){
        int idx = getMissingIdxForSuite(aGame,0);
        if (idx==-1)
            idx=getMissingIdxForSuite(aGame,1);
        return idx;
    }

    private int getMissingIdxForSuite(Jeu aGame, int inc){
        //inc=0 -> petite suite or 1 -> grande suite
        List <Integer> listDiceIdx= new ArrayList<>();
        for  (int value=1+inc; value<=5+inc; value++)  {
            for (int i=0; i<5; i++){
                if ((aGame.fiveDices.tempDiceSetIndValues[i][1]==value)){
                    listDiceIdx.add(aGame.fiveDices.tempDiceSetIndValues[i][0]);
                    break;
                }
            }
        }
        if ((listDiceIdx.size()==4))
            for (int i=0; i<5;i++){
                if (!listDiceIdx.contains(i)){
                    return i;
                }
            }
        return -1;
    }

    private int figureContains4InARow(Jeu aGame){
        int idx1 = getMissingIdxForSuite(aGame,0);
        int idx2 = getMissingIdxForSuite(aGame,1);
        if ((idx1!=-1) && (idx2!=-1)){
            return 2;
        }
        if ((idx1!=-1) || (idx2!=-1)){
            return 1;
        }
        return 0;
    }

    //somme 3 dés  < 5
    private boolean figureContainsAlmostSmall(Jeu aGame){
        int sumOfus = 0;
        for (int i = 0; i<3; i++){
            sumOfus += aGame.fiveDices.tempDiceSetIndValues[i][1];
        }
        return sumOfus < 5;
    }

    /*Deal with appel*/
    //Checks if the appel is missed and resets flags
    private void checkForMissedAppel(Jeu aGame){
        if (aGame.appelClicked){
            final int appelId=aGame.appelBoxId;
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.ungrayAppelBoxOrFigAppelBoxToPreviousState(appelId);
                }
            });
            try{
                sleep(500);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            final int figAppelId = aGame.appelFigTypeBoxId;
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.ungrayAppelBoxOrFigAppelBoxToPreviousState(figAppelId);
                }
            });
            aGame.appelClicked=false;
            aGame.appelRegistered="";
            aGame.appelBoxId=0;
            aGame.appelFigTypeBoxId=0;
        }
    }

    private void showTargetUI(int targetId){

        final int finalTargetId = targetId;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.showMachineTarget(finalTargetId);
            }
        });
        try{
            sleep(1000);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void appel(Jeu aGame, String figureAppel){
        // si jet = 1 et appel possible alors appel figure
        // montrer sur UI: click sur appel, click sur figure
        System.out.println("appel au "+figureAppel);
        if (aGame.throwNb==1){
            //Arbitraire: voir si on peut récupérer la case appel souhaitée par la machine
            final int appelBoxId = aGame.checkerBox[0][2].getId();
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // mainActivity.onBoxClicked(mainActivity.findViewById(appelBoxId));
                    mainActivity.onBoxAppelClicked(mainActivity.findViewById(appelBoxId));
                }
            });
            //Pour mettre un délai entre l'affichage de l'appel et celui de la figure appelée
            try{
                sleep(1000);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            int tempAppelBoxFigTypeId=0;
            for (int i =0; i<5; i++)
                for (int j = 0; j<5; j++)
                    if (aGame.checkerBox[i][j].getFigType().equals(figureAppel)){
                        tempAppelBoxFigTypeId=aGame.checkerBox[i][j].getId();
                        break;
                    }
            final int figureAppelId=tempAppelBoxFigTypeId;
            System.out.println("figureAppelId="+figureAppelId);
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.onBoxFigureAppelClicked(mainActivity.findViewById(figureAppelId));
                }
            });
        }
    }

    /*xxxxxxxxxxxxxxMethods that deal with communication with UIxxxxxxxxxxxxxxxxxx*/
    private void syncFiveDicesResultsWithUI(){
        for (int i = 0; i < 5; i++) {
            if (currentGame.fiveDices.diceSet[i].isSelected) {
                final int diceId=currentGame.fiveDices.diceSet[i].id;
                final int diceValue=currentGame.fiveDices.diceSet[i].value;
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        mainActivity.updateOneDice(diceId, diceValue);
                    }
                });
            }
        }
    }

    private void syncFiveDicesSelectionWithUI(){
        for (int i = 0; i < 5; i++) {
            if ((currentGame.fiveDices.diceSet[i].isSelected) && (!currentGame.fiveDices.diceSet[i].color.equals("green"))) {
                try{
                    sleep(500);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                final int diceId=currentGame.fiveDices.diceSet[i].id;
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        mainActivity.UI_setDiceColor(diceId, "green");
                    }
                });
                currentGame.fiveDices.diceSet[i].color = "green";
            }
            else if (!(currentGame.fiveDices.diceSet[i].isSelected) && (!currentGame.fiveDices.diceSet[i].color.equals("white"))) {
                try{
                    sleep(500);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                final int diceId=currentGame.fiveDices.diceSet[i].id;
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        mainActivity.UI_setDiceColor(diceId,"white");
                    }
                });
                currentGame.fiveDices.diceSet[i].color = "white";
            }
        }

    }

    public void showDroidThrowsUI(int throwNb){
        final int tnb= throwNb;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run(){
                mainActivity.showDroidThrows(tnb);
            }
        });
    }
    //Calls main UI to place a marker
    private String machinePlaceMarkerById(int boxId){
        try{
            sleep(1000);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        //Place the marker
        final int optimalFinalBoxId = boxId;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.placeMarkerById(optimalFinalBoxId);
            }
        });
        return "blue";
    }

}
//TODO ajouter strategie pour empecher l'autre de marquer -->fait mais pas vérifié
//TODO ajouter strategie pour ne pas gâcher ses pions (skip) -->fait mais pas vérifié entièrement
//TODO ajouter prise en compte des singlepairs pour obtenir une figure optimale qd rien d'autre n'est possible-->fait (je crois)
