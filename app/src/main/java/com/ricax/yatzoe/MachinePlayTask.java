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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
                    currentGame.throwDices();
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    showDroidThrowsUI(currentGame.throwNb);
                    syncFiveDicesResultsWithUI();//Shows dice results
                    String target = machineChoseFromDices();
                    if (target.matches(".*(1|2|3|4|5|6|Appel|Carre|Full|Yam|Suite|Sec|Small).*")) {
                        selectDiceFromTarget(target);
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
    private void listAddNextTurnBoxPairIDPointsPerFigure(ArrayList<BoxPair> boxIdPointList, String figureList, String aFigure, int v1, int h1, int v2, int h2) {
        if (figureList.contains(aFigure)) {
            if (currentGame.checkerBox[v1][h1].color.equals("white")) {
                BoxPair boxIdPoint1 = new BoxPair(currentGame.checkerBox[v1][h1].id, getPotentialNextTurnPointsPerBox("red", currentGame.checkerBox[v1][h1]), getPotentialNextTurnPointsPerBox("blue", currentGame.checkerBox[v1][h1]));
                boxIdPointList.add(boxIdPoint1);
            }
            if (currentGame.checkerBox[v2][h2].color.equals("white")) {
                BoxPair boxIdPoint2 = new BoxPair(currentGame.checkerBox[v2][h2].id, getPotentialNextTurnPointsPerBox("red", currentGame.checkerBox[v2][h2]), getPotentialNextTurnPointsPerBox("blue", currentGame.checkerBox[v2][h2]));
                boxIdPointList.add(boxIdPoint2);

            }
        }
    }

    //returns potential next turn poinst per box
    private int getPotentialNextTurnPointsPerBox(String aColor, Box aBox) {
        int v = aBox.v;
        int h = aBox.h;
        int potentialPoints = 0;
        Jeu tempGame = new Jeu(currentGame);
        potentialPoints += getPotentialPointsNextTurnPerCoord(aColor, v, 0, h, 1, tempGame);//Horizontally
        potentialPoints += getPotentialPointsNextTurnPerCoord(aColor, v, 1, h, 0, tempGame);//Vertically
        potentialPoints += getPotentialPointsNextTurnPerCoord(aColor, v, 1, h, 1, tempGame);//to bottom right
        potentialPoints += getPotentialPointsNextTurnPerCoord(aColor, v, 1, h, -1, tempGame);//to bottom left
        // System.out.println("getPotentialNextTurnPointsPerBox: "+aColor+" points:"+potentialPoints);

        //TODO: voir si il faut faire la meme chose que ce qui suit pour le calcul de oponentPoints
        if ((tempGame.fullLine(aColor, aBox.id)) || (currentGame.redMarkers - 1 == 0)) {
            if (currentGame.redPoints + potentialPoints < currentGame.bluePoints)
                return -1;//pour passer le tour
        }
        return potentialPoints;
    }

    //Checks if the coord are within bounds of the board
    private boolean checkBoxColorWhithinBound(int v, int h, String aColor, Jeu aGame) {
        if (((v >= 0) && (v <= 4)) && ((h >= 0) && (h <= 4)))
            //   return currentGame.checkerBox[v][h].color.equals(aColor);
            return aGame.checkerBox[v][h].color.equals(aColor);
        return false;
    }

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

    private boolean colorInSpanPerLineExists(String aColor, int v, int incrV, int h, int incrH, int span, Jeu aGame) {
        if ((checkBoxColorWhithinBound(v + span * incrV, h + span * incrH, aColor, aGame))
                || (checkBoxColorWhithinBound(v - span * incrV, h - span * incrH, aColor, aGame)))
            return true;
        return false;
    }

    //returns potential points (next turn) from coordinates
    //Ex: XRW or XWR or RXW or WXR leads to future potentials points
    private int getPotentialPointsNextTurnPerCoord(String aColor, int v, int incrV, int h, int incrH, Jeu aGame) {
        int potentialPoints = 0;
        // Jeu nextGame = new Jeu(currentGame);
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
    private boolean boxListContains(List<BoxPair> aBoxPairList, String figType) {
        for (int i = 0; i < aBoxPairList.size() - 1; i++)
            if (currentGame.findBoxById(aBoxPairList.get(i).getPairId()).figType.equals(figType))
                return true;
        return false;
    }

    //returns optimal next turn box
    private BoxPair getOptimaNextTurnNextThrowBoxPairFromFigureList(List<BoxPair> boxIdPointList) {
        //If no points scored, try to figure out the best figure to get for next turn
        if (!boxIdPointList.isEmpty())
            if ((boxIdPointList.get(boxIdPointList.size() - 1)).getPairPoints() == 0) {
                for (int i = 0; i < boxIdPointList.size(); i++) {
                    //Add potential points for each box of the list
                    boxIdPointList.get(i).setPairPoints(getPotentialNextTurnPointsPerBox("red", currentGame.findBoxById(boxIdPointList.get(i).getPairId())));
                }
            }
        //Sort in ascending order
        Collections.sort(boxIdPointList);
     /*   System.out.println("getOptimaNextTurnNextThrowBoxPairFromFigureList");
        for (int i = 0; i< boxIdPointList.size(); i++)
        {
            System.out.println("****debut box***");
            System.out.println(boxIdPointList.get(i));
            currentGame.findBoxById(boxIdPointList.get(i).getPairId()).afficherBox();
            System.out.println("****fin box***");
        }*/
        if (!boxIdPointList.isEmpty())
            return boxIdPointList.get(boxIdPointList.size() - 1);
        else return new BoxPair(0, 0, 0);
    }

    //returns optimal current turn next throw box
    private BoxPair getOptimalCurrentTurnNextThrowBoxPairFromFigureList(List<BoxPair> boxIdPointList) {
        //Add points and oponentPoints if marker placed on boxes
        for (int i = 0; i < boxIdPointList.size(); i++) {
            boxIdPointList.get(i).setPairPoints(getPointsIfMarkerPlacedOnBox("red", boxIdPointList.get(i).getPairId()));
            boxIdPointList.get(i).setOponentPoints(getPointsIfMarkerPlacedOnBox("blue", boxIdPointList.get(i).getPairId()));
        }
        //Sort in ascending order
        Collections.sort(boxIdPointList);
        //Return the last of the list (the best)
   /*     System.out.println("getOptimalCurrentTurnNextThrowBoxPairFromFigureList");
        for (int i = 0; i< boxIdPointList.size(); i++)
        {
            System.out.println("****debut box***");
            System.out.println(boxIdPointList.get(i));
            currentGame.findBoxById(boxIdPointList.get(i).getPairId()).afficherBox();
            System.out.println("****fin box***");
        }*/
        if (!boxIdPointList.isEmpty())
            return boxIdPointList.get(boxIdPointList.size() - 1);
        else return new BoxPair(0, 0, 0);
    }

    //Adds points obtained if current given figtype is placed to given ArrayList of BoxPairs
    private void listAddCurrentThrowBoxPairIdPointsPerFigure(ArrayList<BoxPair> boxIdPointList, String figureList, String aFigure, int v1, int h1, int v2, int h2) {
        if (figureList.contains(aFigure)) {
            if (currentGame.checkerBox[v1][h1].color.equals("white")) {
                int points = getPointsIfMarkerPlacedOnBox("red", currentGame.checkerBox[v1][h1].id);
                int oponentPoints = getPointsIfMarkerPlacedOnBox("blue", currentGame.checkerBox[v1][h1].id);
                if (points >= 0) {
                    BoxPair boxIdPoint = new BoxPair(currentGame.checkerBox[v1][h1].id, points, oponentPoints);
                    boxIdPointList.add(boxIdPoint);
                }
            }
            if (currentGame.checkerBox[v2][h2].color.equals("white")) {
                int points = getPointsIfMarkerPlacedOnBox("red", currentGame.checkerBox[v2][h2].id);
                int oponentPoints = getPointsIfMarkerPlacedOnBox("blue", currentGame.checkerBox[v2][h2].id);
                if (points >= 0) {
                    BoxPair boxIdPoint2 = new BoxPair(currentGame.checkerBox[v2][h2].id, points, oponentPoints);
                    boxIdPointList.add(boxIdPoint2);
                }
            }
        }
    }

    //Returns the best  BoxPair (id+immediate points) for a given figureList
    private BoxPair getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList(String figureList) {
        ArrayList<BoxPair> boxIdPointList = new ArrayList<>();
        //check points per figure
        //Mettre appel en premier pour ne pas se faire shunter par appel
        listAddCurrentThrowBoxPairIdPointsPerFigure(boxIdPointList, figureList, "Appel", 0, 2, 2, 3);
        listAddCurrentThrowBoxPairIdPointsPerFigure(boxIdPointList, figureList, "1", 0, 0, 3, 4);
        listAddCurrentThrowBoxPairIdPointsPerFigure(boxIdPointList, figureList, "3", 0, 1, 4, 0);
        listAddCurrentThrowBoxPairIdPointsPerFigure(boxIdPointList, figureList, "4", 0, 3, 4, 4);
        listAddCurrentThrowBoxPairIdPointsPerFigure(boxIdPointList, figureList, "5", 1, 4, 4, 3);
        listAddCurrentThrowBoxPairIdPointsPerFigure(boxIdPointList, figureList, "6", 0, 4, 3, 0);
        listAddCurrentThrowBoxPairIdPointsPerFigure(boxIdPointList, figureList, "2", 1, 0, 4, 1);
        listAddCurrentThrowBoxPairIdPointsPerFigure(boxIdPointList, figureList, "Full", 1, 3, 2, 1);
        listAddCurrentThrowBoxPairIdPointsPerFigure(boxIdPointList, figureList, "Small", 2, 0, 3, 3);
        listAddCurrentThrowBoxPairIdPointsPerFigure(boxIdPointList, figureList, "Carre", 1, 1, 4, 2);
        listAddCurrentThrowBoxPairIdPointsPerFigure(boxIdPointList, figureList, "Suite", 2, 4, 3, 2);
        listAddCurrentThrowBoxPairIdPointsPerFigure(boxIdPointList, figureList, "Sec", 1, 2, 3, 1);
        if (figureList.contains("Yam")) {
            if (currentGame.checkerBox[2][2].color.equals("white")) {
                int points = getPointsIfMarkerPlacedOnBox("red", currentGame.checkerBox[2][2].id);
                int oponentPoints = getPointsIfMarkerPlacedOnBox("red", currentGame.checkerBox[2][2].id);
                if (points >= 0) {
                    BoxPair boxIdPoint = new BoxPair(currentGame.checkerBox[2][2].id, points, oponentPoints);
                    boxIdPointList.add(boxIdPoint);
                }
            }
        }
        //Les boxPairs sont rangées selon 1:les points, 2: les points de l'adversaire en cas d'égalité
        Collections.sort(boxIdPointList);
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
        if (!boxIdPointList.isEmpty())
            return boxIdPointList.get(boxIdPointList.size() - 1);
        //Just in case
        return new BoxPair(0, 0, 0);
    }

    //Returns points if marker placed on box
    private int getPointsIfMarkerPlacedOnBox(String aColor, int boxId) {
        int tmpPoints = 0;
        //copie de currentGame
        Jeu tempGame = new Jeu(currentGame);
        // tempGame.findBoxById(boxId).afficherBox();
        if (currentGame.findBoxById(boxId).color.equals("white")) {
            tempGame.findBoxById(boxId).color = aColor;
            tmpPoints = tempGame.countLine(3, aColor, boxId);
            if ((tempGame.fullLine(aColor, boxId)) || (currentGame.redMarkers - 1 == 0)) {
                if (((currentGame.redPoints + tmpPoints < currentGame.bluePoints)&& aColor.equals("red"))
                        ||
                        ((currentGame.bluePoints + tmpPoints < currentGame.redPoints)&& aColor.equals("blue")))
                {
                    System.out.println("getPointsIfMarkerPlacedOnBox Marquage perdant:"+tmpPoints+"mais -1");
                    tempGame.findBoxById(boxId).afficherBox();
                    System.out.println("fin getPointsIfMarkerPlacedOnBox Marquage perdant");
                    //Si on peut esperer gagner la partie avec les pions qui nous restent on retourne -1
                    return -1;  //Pour le mettre en queue des choix dans getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList
                    // pour passer le tour
                } else if (((currentGame.redPoints + tmpPoints > currentGame.bluePoints)&& aColor.equals("red"))||
                        ((currentGame.bluePoints + tmpPoints > currentGame.redPoints)&& aColor.equals("blue"))) {
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
    private BoxPair getOptimalNextTurnCurrentThrowBoxPairPerFigureList(String figureList) {
        ArrayList<BoxPair> boxIdPointList = new ArrayList<>();
        //check points per figure
        //Mis les brelans en premiers pour privilégier les figures centrales dans la fin de liste
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "1", 0, 0, 3, 4);
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "2", 1, 0, 4, 1);
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "3", 0, 1, 4, 0);
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "4", 0, 3, 4, 4);
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "5", 1, 4, 4, 3);
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "6", 0, 4, 3, 0);
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "Full", 1, 3, 2, 1);
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "Small", 2, 0, 3, 3);
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "Carre", 1, 1, 4, 2);
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "Suite", 2, 4, 3, 2);
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "Sec", 1, 2, 3, 1);
        if ((figureList.contains("Yam") && (currentGame.checkerBox[2][2].color.equals("white")))) {
            BoxPair boxIdPoint = new BoxPair(currentGame.checkerBox[2][2].id, getPotentialNextTurnPointsPerBox("red", currentGame.checkerBox[2][2]), getPotentialNextTurnPointsPerBox("blue", currentGame.checkerBox[2][2]));
            boxIdPointList.add(boxIdPoint);
        }
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "Appel", 0, 2, 2, 3);

        //Sort boxIdPointList
        Collections.sort(boxIdPointList);
        /*System.out.println("getOptimalNextTurnCurrentThrowBoxPairPerFigureList");
        for (int i = 0; i< boxIdPointList.size(); i++)
        {
            System.out.println("****debut box***");
            System.out.println(boxIdPointList.get(i));
            currentGame.findBoxById(boxIdPointList.get(i).getPairId()).afficherBox();
            System.out.println("****fin box***");
        }*/
        if (!boxIdPointList.isEmpty())
            return boxIdPointList.get(boxIdPointList.size() - 1);
        else {
            return new BoxPair(0, 0, 0);
        }
    }

    private void printOptimalBoxList(List<BoxPair> aBoxPairList) {
        if (!aBoxPairList.isEmpty())
            for (int i = 0; i < aBoxPairList.size(); i++) {
                currentGame.afficherBoxPair(aBoxPairList.get(i));
            }
        System.out.println(" ");
    }

    //returns a list of optimal next throw boxes from a given current figure list
    private List<BoxPair> getListOptimalNextThrowBoxpairFromFigureList(String aFigureList) {
        //TODO implémenter recherche de case qui ferme le jeu avec victoire (fullLine ou pion=0)
        //meme avec 1 seul dé (genre pour tenter brelan qui tue)
        List<BoxPair> boxIdPointList = new ArrayList<>();
        if (aFigureList.matches(".*([123456]).*")) {
            if (currentGame.throwNb == 1) {
                if (!boxListContains(boxIdPointList, "Appel"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
            }
            if (!aFigureList.contains("Carre")) {
                if (!boxListContains(boxIdPointList, "Carre"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Carre", "white"));
            }
            if (!aFigureList.contains("Full")) {
                if (!boxListContains(boxIdPointList, "Full"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Full", "white"));
            }
            if (!aFigureList.contains("Yam")) {
                if (!boxListContains(boxIdPointList, "Yam"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Yam", "white"));
            }
        }
        if (aFigureList.contains("Carre")) {
            if (currentGame.throwNb == 1)
                if (!boxListContains(boxIdPointList, "Appel")) {
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
                }
            if (!aFigureList.contains("Yam")) {
                if (!boxListContains(boxIdPointList, "Yam"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Yam", "white"));
            }
        }
        if (aFigureList.contains("1")) {
            if (currentGame.throwNb == 1) {
                if (!boxListContains(boxIdPointList, "Appel"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
            }
            if (!aFigureList.contains("Small")) {
                if (!boxListContains(boxIdPointList, "Small"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Small", "white"));
            }
        }
        if (aFigureList.contains("Full")) {
            if (currentGame.throwNb == 1) {
                if (!boxListContains(boxIdPointList, "Appel"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
            }
            if (currentGame.getListBoxPairColorPerFigure("Full", "white").isEmpty()) {
                if (!boxListContains(boxIdPointList, "Carre")) {
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Carre", "white"));
                }
                if (!boxListContains(boxIdPointList, "Yam")) {
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Yam", "white"));
                }
            }
        }

        if ((figureContainsDoublePair()) && (!aFigureList.contains("Full"))) {
            if (currentGame.throwNb == 1) {
                if (!boxListContains(boxIdPointList, "Appel"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
            }
            //Pour les brelans
            int valIdx1 = currentGame.fiveDices.tempDiceSetIndValues[1][1];
            int valIdx3 = currentGame.fiveDices.tempDiceSetIndValues[3][1];
            if (!boxListContains(boxIdPointList, Integer.toString(valIdx1))) {
                boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure(Integer.toString(valIdx1), "white"));
            }
            if (!boxListContains(boxIdPointList, Integer.toString(valIdx3))) {
                boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure(Integer.toString(valIdx3), "white"));
            }
            //Full
            if (!boxListContains(boxIdPointList, "Full")) {
                boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Full", "white"));
            }
            if (aFigureList.contains("Carre")) {
                //si 2  paires identiques
                if (!boxListContains(boxIdPointList, "Yam"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Yam", "white"));
            }
        }

        if (!aFigureList.contains("Suite"))//Pour tenter des suites
            if (figureContains4InARow() != 0) {
                //si figureContains4InARow()==1 alors voir si on ne peut pas tenter un brelan plutôt
                // car si figureContains4InARow==1 alors seule autre possibilite=singlepaire ou rien
                // si figureContains4InARow==2 alors tenter suite
                if (currentGame.throwNb == 1) {
                    if (!boxListContains(boxIdPointList, "Appel"))
                        boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
                }
                if (figureContains4InARow() == 2) {
                    if (!boxListContains(boxIdPointList, "Suite"))
                        boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Suite", "white"));
                }
                if (figureContains4InARow() == 1) {
                    //See if we'd better try a brelan instead if we've got a pair
                    if (!boxListContains(boxIdPointList, "Suite"))
                        boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Suite", "white"));
                }
            }
        //Pour tenter des brelans carré ou yam
        if (figureContainsPair()) {
            int val = getSinglePairValue();
            if (!currentGame.fiveDices.figureList.contains(Integer.toString(val))) {
                if (!boxListContains(boxIdPointList, Integer.toString(val)))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure(Integer.toString(val), "white"));
            }
            if (!aFigureList.contains("Carre")) {
                if (!boxListContains(boxIdPointList, "Carre"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Carre", "white"));
            }
            if (!aFigureList.contains("Yam")) {
                if (!boxListContains(boxIdPointList, "Yam"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Yam", "white"));
            }
        }

        //Pour tenter le small
        if (!aFigureList.contains("Small"))
            if (figureContainsAlmostSmall()) {
                if (currentGame.throwNb == 1) {
                    if (!boxListContains(boxIdPointList, "Appel"))
                        boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
                }
                if (!boxListContains(boxIdPointList, "Small"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Small", "white"));
            }
        return boxIdPointList;
    }

    /*xxxxxx Methods that aim to optimize strategy xxxxxx*/
    //Verifier si on ne gâche pas un pion
    private boolean checkMarkerSpoilByBoxId(int aBoxId) {
        /*
         * si nbPointsRouges <= nbPOintsBleus alors
         * Calculer le nb de points encore possibles par couleur
         * calculer la diff de points
         * si red - blue < 0 alors return true
         *
         *
         * */
        /*
         * Si pointsBleus>=pointRouges et si nbPionsBleus>=nbPionsrouges   alors ne pas placer de pion qui ne peut pas marquer
         * Si  pointsBleus >= pointRouges et si nbPionsBleus<nbPionsrouges   alors placer les pions qui peuvent marquer
         * et ceux qui peuvent empêcher les bleus de marquer
         *  etc ....
         *
         *
         * */

        /*
         * Implémenter recherche de cases qui apportent plus de points que la case courante
         * sans tenir compte de la figure courante
         * */
//Si le droid est en train de perdre et qu'il n'a pas plus de pion que l'humain ou qu'il n'en a plus beaucoup
        if (((currentGame.redMarkers <= currentGame.blueMarkers) || (currentGame.redMarkers < 5)) && (currentGame.redPoints < currentGame.bluePoints)) {
            if (((getPotentialNextTurnPointsPerBox("red", currentGame.findBoxById(aBoxId))) == 0)//no future red points
                    && (!colorInSpanPerBoxExists("white", currentGame.findBoxById(aBoxId), 2, currentGame)))//box is not rounded by white ones
                return true;
        }
        //Si la case ne sert à rien c'est à dire
        //Si, maintenant ou plus tard,  le pion ne marque pas pour les rouges ou n'empêche pas les bleus de marquer
        if (((getPotentialNextTurnPointsPerBox("red", currentGame.findBoxById(aBoxId))) == 0)//no future red points
                && (getPotentialNextTurnPointsPerBox("blue", currentGame.findBoxById(aBoxId)) == 0)//no future blue points
                && (getPointsIfMarkerPlacedOnBox("blue", aBoxId) == 0)//Blue cannot mark (consequently also close the game) later with such box
                && (!colorInSpanPerBoxExists("white", currentGame.findBoxById(aBoxId), 2, currentGame)))//box is not rounded by white ones
            return true;
        else
            return false;
    }

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

        Combinations aCombination = new Combinations(aGame.redMarkers, aBoxPairIdList);
        aCombination.Combinate();
        List<int[]> combSubsets = aCombination.getSubsets();
        List<JeuCombinaison> gameCombinationList = new ArrayList<>();
        for (int i = 0; i < combSubsets.size(); i++) {
            //utiliser le constructeur de copie pour Jeu
            JeuCombinaison aGameCombination = new JeuCombinaison(new Jeu(aGame), aColor, combSubsets.get(i));
            //aGameCombination.computeCombinationPoints();
            gameCombinationList.add(aGameCombination);
        }
        Collections.sort(gameCombinationList);
       /*
        System.out.println("Nombre de combinaisons possibles: "+gameCombinationList.size());
        for (int i = 0; i < gameCombinationList.size(); i++) {
              System.out.println("Combinaison["+i+"]: "+gameCombinationList.get(i).getPoints()+" points");
              gameCombinationList.get(i).printIdCombinationArrayList();
        }
        */
        // List<JeuCombinaison> bestGameCombinationsList = new ArrayList<>();
        // bestGameCombinationsList.add(gameCombinationList.get(gameCombinationList.size() - 1));

        //Bug dans ce bloc?, ne prend pas toutes les combinaisons qui répondent au critère
     /*
      for (int i = 0; i < gameCombinationList.size() - 1; i++) {
            if (gameCombinationList.get(i) == bestGameCombinationsList.get(0))
                bestGameCombinationsList.add(gameCombinationList.get(i));
        }

        return bestGameCombinationsList;
        */

        //le tri sera fait dans machineChoseFromDice, permet de shunter le bug précédent et de vérifier que toutes les combinaisons gagnantes sont prises en compte
        return gameCombinationList;
    }

    private List<Integer> getFreeBoxIdList(Jeu aGame) {
        List<Integer> aBoxPairList = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++) {
                if (aGame.checkerBox[i][j].color.equals("white")) {
                    aBoxPairList.add(aGame.checkerBox[i][j].id);
                }
            }
        return aBoxPairList;
    }

    private List<BoxPair> getFreeBoxList(Jeu aGame) {
        List<BoxPair> aBoxPairList = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++) {
                if (aGame.checkerBox[i][j].color.equals("white")) {
                    BoxPair aBoxPair = new BoxPair(aGame.checkerBox[i][j].id, 0, 0);
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
        final Pattern p = Pattern.compile("([123456])|(carre)|(yam)|(full)|(suite)|(small)|(sec)|(appel)");
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
    private ArrayList<String> getPossibleTargetsFromDiceSet(Jeu aGame){
        System.out.println("Debut getPossibleTargetsFromDiceSet");
        ArrayList<String> possibleFigureListFromDiceSet = new ArrayList<>();
        if (figureContains4InARow()!=0)
            if (!possibleFigureListFromDiceSet.contains("suite"))
                possibleFigureListFromDiceSet.add("suite");
        if (figureContainsAlmostSmall())
            if (!possibleFigureListFromDiceSet.contains("small"))
                possibleFigureListFromDiceSet.add("small");
        if (figureContainsDoublePair()){
            if (!possibleFigureListFromDiceSet.contains("full"))
                possibleFigureListFromDiceSet.add("full");
            //Ajouter les 2 brelans possibles
            if (!possibleFigureListFromDiceSet.contains(Integer.toString(aGame.fiveDices.tempDiceSetIndValues[1][1])))
                possibleFigureListFromDiceSet.add(Integer.toString(aGame.fiveDices.tempDiceSetIndValues[1][1]));
            if (!possibleFigureListFromDiceSet.contains(Integer.toString(aGame.fiveDices.tempDiceSetIndValues[3][1])))
                possibleFigureListFromDiceSet.add(Integer.toString(aGame.fiveDices.tempDiceSetIndValues[3][1]));
        }
        //Si on n'a qu'une seule paire
        if (getSinglePairValue()!=0)
            if (!possibleFigureListFromDiceSet.contains(Integer.toString(getSinglePairValue()))){
                possibleFigureListFromDiceSet.add(Integer.toString(getSinglePairValue()));
                possibleFigureListFromDiceSet.add("carre");
                possibleFigureListFromDiceSet.add("yam");
            }

        if (aGame.fiveDices.figureList.contains("carre"))
            if (! possibleFigureListFromDiceSet.contains("yam"))
                possibleFigureListFromDiceSet.add("yam");
        //et dans tous les cas
        for (int i=1; i<7; i++)
            if (figureContainsSingleValue(i))
                if (!possibleFigureListFromDiceSet.contains(Integer.toString(i))){
                    System.out.printf("Ajout de single value: %s%n", i);
                    possibleFigureListFromDiceSet.add(Integer.toString(i));
                }

        possibleFigureListFromDiceSet.add("sec");

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
            if (!TargetFigurefigTypeArrayList.contains(currentGame.findBoxById(noDuplicatesArrayListBoxPair.get(i).getPairId()).figType))
                TargetFigurefigTypeArrayList.add(currentGame.findBoxById(noDuplicatesArrayListBoxPair.get(i).getPairId()).figType);
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
        ArrayList<String> possibleTargetsFromDiceSet = getPossibleTargetsFromDiceSet(currentGame);
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

        //purger noDuplicatesBoxIdArrayList des box qui fullLinent à perte
        Iterator it = noDuplicatesBoxIdArrayList.iterator();
        while (it.hasNext()){
            BoxPair nextBp = (BoxPair) it.next();
            if (nextBp.isFullLine()&& (nextBp.getPairPoints()+currentGame.redPoints<currentGame.bluePoints)){
                System.out.print("On retire la box:"+currentGame.findBoxById(nextBp.getPairId()).toString()+" ");
                System.out.println(nextBp.toString());
                it.remove();
            }
        }

        //maintenant rechercher les correspondances entre les figures obtenues et la meilleure combinaison. Retourner la meilleure (fin de list)
        if (commonElementsList.size()>0){
            for (int i=0; i< noDuplicatesBoxIdArrayList.size(); i++){
                if (currentGame.findBoxById(noDuplicatesBoxIdArrayList.get(i).getPairId()).figType.equals(commonElementsList.get(commonElementsList.size()-1))){
                    System.out.println("Fin getAvailableBoxPairFromCombinationAndDice");
                    return noDuplicatesBoxIdArrayList.get(i);
                }
            }
        }
        //Sinon retourner un null
        BoxPair nullBoxPair = new BoxPair(0,0,0);
        System.out.println("getAvailableBoxPairFromCombinationAndDice :BoxPair NULL");
        return nullBoxPair;
    }

    /*Method where droid choses target from current throw*/
    //Returns the target figtype upon wich we will select the dices next turn if any else returns null string and place the marker if possible
    private String machineChoseFromDices() {


        //TODO revoir la procédure de decision, c'est pas clair!
        //Recensement des figures posables et/ou améliorables en fonction des dés courants
        List<BoxPair> boxIdPointList = getListOptimalNextThrowBoxpairFromFigureList(currentGame.fiveDices.figureList);

        //??Ajout de la figure améliorable d'abord pour privilégier les 2 autres solutions avant en cas d'égalité de points

        //Points éventuels au tour courant en fonction des dés actuels si on sélectionne + relance
        BoxPair boxPairTargetCurrentTurnNextThrow = getOptimalCurrentTurnNextThrowBoxPairFromFigureList(new ArrayList<>(boxIdPointList));
        //Points éventuels à un tour suivant en fonction des dés actuels si on sélectionne + relance
        BoxPair boxPairTargetNextTurnNextThrow = getOptimaNextTurnNextThrowBoxPairFromFigureList(new ArrayList<>(boxIdPointList));
        //Point immédiats au tour courant si on place maintenant
        BoxPair boxPairCurrentTurnCurrentThrow = getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList(currentGame.fiveDices.figureList);
        //Points éventuels à un  tour suivant si on place maintenant
        BoxPair boxPairNextTurnCurrentThrow = getOptimalNextTurnCurrentThrowBoxPairPerFigureList(currentGame.fiveDices.figureList);

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
                currentGame.afficherDes();
                List<JeuCombinaison> jclist = bestCombinationsAvailable("red", currentGame);
                // System.out.println("Liste des box optimales par jeu de combinaison (jclist): ");
                // for (int i=0; i<jclist.size(); i++)
                //    jclist.get(i).printIdCombinationArrayList();
                //Ne récupérer que les combinaisons qui permettent de dépasser ou d'égaler l'adversaire
                List<JeuCombinaison> bestjclist = new ArrayList<>();
                for (int i = 0; i < jclist.size(); i++)
                    if (jclist.get(i).getPoints() + currentGame.redPoints > currentGame.bluePoints)
                        bestjclist.add(jclist.get(i));

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
                ArrayList<BoxPair> noDuplicatesBoxPairArrayList = new ArrayList<>();
                ArrayList<Integer> noDuplicatesBoxPairIdArrayList = new ArrayList<>();
                for (int i = 0; i < bestjclist.size(); i++)
                    for (int j=0; j<bestjclist.get(i).getBoxPairCombinationArrayList().size(); j++)
                    {
                        if (!noDuplicatesBoxPairIdArrayList.contains(bestjclist.get(i).getBoxPairCombinationArrayList().get(j).getPairId())){
                            noDuplicatesBoxPairIdArrayList.add(bestjclist.get(i).getBoxPairCombinationArrayList().get(j).getPairId());
                            noDuplicatesBoxPairArrayList.add(bestjclist.get(i).getBoxPairCombinationArrayList().get(j));
                        }
                    }

                Collections.sort(noDuplicatesBoxPairArrayList);
                System.out.println("noDuplicatesBoxPairArrayList:");
                for (int i =0; i<noDuplicatesBoxPairArrayList.size(); i++)
                    currentGame.findBoxById(noDuplicatesBoxPairArrayList.get(i).getPairId()).afficherBox();
                System.out.println("*********");
                //TODO: verifier gérer le cas où la machine fait un appel, prendre la bonne case.
                //TODO: ne pas fermer le jeu (fullline) si on perd (normalement c'est géré dans les combinaisons
                //TODO: mais vu qu'elles ont fusionné, voir si il faut le re gérer)

                //On a fait appel, l'appel n'est pas encore réussi, on tente une dernière fois si l'appel est dans la combinaison
                if (currentGame.throwNb == 2 && currentGame.appelClicked && !currentGame.fiveDices.figureList.equals("Appel")) {
                    System.out.println("Gestion appel1");
                    //récupérer les boxPair correspondant aux appels
                    List<BoxPair> appelBoxPairList = currentGame.getListBoxPairColorPerFigure("appel", "white");
                    //trouver ceux qui sont dans la liste de la combinaison et faire appel si on en trouve
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
                    checkForMissedAppel();
                    return "blue";
                }

                //sinon si il n'y a pas d'appel, on continue avec les autres box de la combinaison
                if
                (
                        (currentGame.redPoints == currentGame.bluePoints && currentGame.redMarkers == currentGame.blueMarkers) ||
                                currentGame.redPoints <= currentGame.bluePoints
                ) {

                    //1: placer ou tenter une des figures/cases selectionnées si elle est aussi dans la combinaison
                    if (noDuplicatesBoxPairArrayList.contains(boxPairCurrentTurnCurrentThrow.getPairId())) {
                        System.out.println("Placer: noDuplicatesBoxIdArrayList.contains(boxPairCurrentTurnCurrentThrow.getPairId())");
                        currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).afficherBox();
                        System.out.println("----------------------------------------------------");
                        if (boxPairCurrentTurnCurrentThrow.getPairPoints()!=-1)
                            return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());
                    }
                    if (noDuplicatesBoxPairArrayList.contains(boxPairNextTurnCurrentThrow.getPairId())) {
                        System.out.println("Placer: noDuplicatesBoxIdArrayList.contains(boxPairNextTurnCurrentThrow.getPairId())");
                        currentGame.findBoxById(boxPairNextTurnCurrentThrow.getPairId()).afficherBox();
                        System.out.println("----------------------------------------------------");
                        if (boxPairNextTurnCurrentThrow.getPairPoints()!=-1)
                            return machinePlaceMarkerById(boxPairNextTurnCurrentThrow.getPairId());
                    }
                    if (currentGame.throwNb < currentGame.maxThrowNb)
                        if (noDuplicatesBoxPairArrayList.contains(boxPairTargetCurrentTurnNextThrow.getPairId())) {
                            System.out.println("Target: noDuplicatesBoxIdArrayList.contains(boxPairTargetCurrentTurnNextThrow.getPairId())");
                            currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).afficherBox();
                            System.out.println("----------------------------------------------------");
                            return currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).figType;
                        }
                    if (currentGame.throwNb < currentGame.maxThrowNb)
                        if (noDuplicatesBoxPairArrayList.contains(boxPairTargetNextTurnNextThrow.getPairId())) {
                            System.out.println("Target:noDuplicatesBoxIdArrayList.contains(boxPairTargetNextTurnNextThrow.getPairId())");
                            currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).afficherBox();
                            System.out.println("----------------------------------------------------");
                            return currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).figType;
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
                if (currentGame.appelClicked)
                    return "Appel";

        //Si on a une boxPair à placer maintenant
        if (boxPairCurrentTurnCurrentThrow.getPairId()>0)
        {
            //Si appel réussi, placer le pion
            if (currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).figType.equals("Appel"))
                if (currentGame.fiveDices.figureList.equals("Appel"))
                    return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());

            //Si on n'a pas fait appel, voir si on a avantage à retenter ou bien si on place maintenant
            if (currentGame.throwNb<currentGame.maxThrowNb)
            {
                if (boxPairTargetCurrentTurnNextThrow.getPairId()>0)
                {
                    //Ne faire nextthrow que si currenthrow pas posable
                    // ou si on peut LA garder (ex: BRELAN->full ok  mais pas FULL->carre ni Yam ni Sec)
                    if (boxPairTargetCurrentTurnNextThrow.getPairPoints()>=boxPairCurrentTurnCurrentThrow.getPairPoints())
                    {
                        if (!((currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).figType.equals("Full"))||
                                (currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).figType.equals("Yam"))||
                                (currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).figType.equals("Sec"))))
                        {
                            if (currentGame.throwNb < currentGame.maxThrowNb)
                                return currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).figType;
                        }
                        else {
                            return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());
                        }
                    }
                }
            }

            //1: Placer pour marquer au tour courant
            if (boxPairCurrentTurnCurrentThrow.getPairPoints()>0)
                return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());

            //2:placer maintenant pour pouvoir peut être marquer à un tour suivant.
            if (boxPairNextTurnCurrentThrow.getPairId()>0)
                if (boxPairNextTurnCurrentThrow.getPairPoints()>0)// verifier cela
                    return machinePlaceMarkerById(boxPairNextTurnCurrentThrow.getPairId());

            //3: Améliorer figure pour placer au jet suivant et pouvoir peut être marquer à un tour suivant
            if (currentGame.throwNb<currentGame.maxThrowNb)
                if (boxPairTargetNextTurnNextThrow.getPairId()>0)
                    if (boxPairTargetNextTurnNextThrow.getPairPoints()>0)//TODO semble que cela exclue les cases qui pourraient apporter des points avec 3 pions à placer (3 cases blanches)
                        return currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).figType;


            //TODO: vérifier avant que cela nous permet de marquer plus tard ou que cela ne nous gâche pas de pions en cas de fin de jeu serrée
            //TODO decommenter la ligne qui suit quand tout sera implémenté

            //    if (!checkMarkerSpoilByBoxId(boxPairCurrentTurnCurrentThrow.getPairId())){
            //         System.out.println("Pas de gâchis, on pose!");
            //TODO pour l'instant on laisse poser il faut toutefois vérifier que le TODO précédent soit fait
            return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());
            //     }
            //     else System.out.println("Quel gâchis, on ne pose pas!");

        }

        //Pas de figure posable au jet courant on tente d'améliorer la figure:
        else {
            if (currentGame.throwNb<currentGame.maxThrowNb){
                if (boxPairTargetCurrentTurnNextThrow.getPairId() > 0) {
                    if (boxPairTargetNextTurnNextThrow.getPairId()>0){
                        if ((boxPairTargetCurrentTurnNextThrow.getPairPoints()>=boxPairTargetNextTurnNextThrow.getPairPoints())){//On tente d'améliorer la figure pour placer au jet suivant
                            return currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).figType;
                        }
                        else{
                            return currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).figType;
                        }
                    }
                }
                else
                if (currentGame.throwNb < currentGame.maxThrowNb)
                    if (boxPairTargetNextTurnNextThrow.getPairId() > 0) {
                        if (boxPairTargetNextTurnNextThrow.getPairPoints() >= 0) {
                            return currentGame.findBoxById(boxPairNextTurnCurrentThrow.getPairId()).figType;
                        }
                    }
                    else
                    if (currentGame.throwNb < currentGame.maxThrowNb) {
                        return "sec";
                    }
            }
            else{
                checkForMissedAppel();
                return "blue";
            }
        }
        return "blue";
    }


    /*xxxxxx Methods to deal with dice selection according to target xxxxxx*/
    //Select dice from target as defined by choseFromeDice
    private void selectDiceFromTarget(String target) {
        switch (target) {
            case "Carre":
                selectForCarre();
                break;
            case "Full":
                selectForFull();
                break;
            case "Yam":
                selectForYam();
                break;
            case "Small":
                selectForSmall();
                break;
            case "Suite":
                selectForSuite();
                break;
            case "Sec":
                selectForSec();
                break;
            case "Appel":
                selectForAppel();
                break;
            case "1":
                selectForBrelan(1);
                break;
            case "2":
                selectForBrelan(2);
                break;
            case "3":
                selectForBrelan(3);
                break;
            case "4":
                selectForBrelan(4);
                break;
            case "5":
                selectForBrelan(5);
                break;
            case "6":
                selectForBrelan(6);
                break;
            case "allDice":
                selectAllDices();
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

    private void selectAllDices(){
        for (int i =0; i<5;i++)
            currentGame.fiveDices.diceSet[i].isSelected=true;
    }

    private void selectForCarre() {
        if (currentGame.fiveDices.figureList.matches( ".*([123456]).*")){
            for (int i = 0; i < 5; i++)
                this.currentGame.fiveDices.diceSet[i].isSelected = true;
            if (this.currentGame.fiveDices.tempDiceSetIndValues[0][1] == this.currentGame.fiveDices.tempDiceSetIndValues[2][1]) {
                for (int j = 0; j < 3; j++)
                    this.currentGame.fiveDices.diceSet[
                            currentGame.fiveDices.tempDiceSetIndValues[j][0]
                            ].isSelected = false;
            } else if (this.currentGame.fiveDices.tempDiceSetIndValues[1][1] == this.currentGame.fiveDices.tempDiceSetIndValues[3][1]) {
                for (int j = 1; j < 4; j++)
                    this.currentGame.fiveDices.diceSet[
                            currentGame.fiveDices.tempDiceSetIndValues[j][0]
                            ].isSelected = false;
            } else if (this.currentGame.fiveDices.tempDiceSetIndValues[2][1] == this.currentGame.fiveDices.tempDiceSetIndValues[4][1]) {
                for (int j = 2; j < 5; j++)
                    this.currentGame.fiveDices.diceSet[
                            currentGame.fiveDices.tempDiceSetIndValues[j][0]
                            ].isSelected = false;
            }
        }
        //Traiter les paires
        else if (figureContainsSinglePair())
            selectFromSinglePair();
        //pas besoin de traiter le full car si full alors brelan
    }

    private void selectForFull() {
        if (figureContainsDoublePair()) {
            selectfromDoublePair();
        }
        //Traiter brelan
        else if (currentGame.fiveDices.figureList.matches( ".*([123456]).*")){
            for (int i = 0; i < 5; i++)
                this.currentGame.fiveDices.diceSet[i].isSelected = true;
            if (this.currentGame.fiveDices.tempDiceSetIndValues[0][1] == this.currentGame.fiveDices.tempDiceSetIndValues[2][1]) {
                for (int j = 0; j < 3; j++)
                    this.currentGame.fiveDices.diceSet[
                            currentGame.fiveDices.tempDiceSetIndValues[j][0]
                            ].isSelected = false;
            } else if (this.currentGame.fiveDices.tempDiceSetIndValues[1][1] == this.currentGame.fiveDices.tempDiceSetIndValues[3][1]) {
                for (int j = 1; j < 4; j++)
                    this.currentGame.fiveDices.diceSet[
                            currentGame.fiveDices.tempDiceSetIndValues[j][0]
                            ].isSelected = false;
            } else if (this.currentGame.fiveDices.tempDiceSetIndValues[2][1] == this.currentGame.fiveDices.tempDiceSetIndValues[4][1]) {
                for (int j = 2; j < 5; j++)
                    this.currentGame.fiveDices.diceSet[
                            currentGame.fiveDices.tempDiceSetIndValues[j][0]
                            ].isSelected = false;
            }
        }
        //traiter paire
        else if (figureContainsPair()) {
            selectFromSinglePair();
        }
    }

    private void selectForYam() {
        if (currentGame.fiveDices.figureList.contains("Carre")) {
            for (int i = 0; i < 5; i++)
                currentGame.fiveDices.diceSet[currentGame.fiveDices.tempDiceSetIndValues[i][0]].isSelected = false;
            if (this.currentGame.fiveDices.tempDiceSetIndValues[0][1] == this.currentGame.fiveDices.tempDiceSetIndValues[3][1])
                currentGame.fiveDices.diceSet[currentGame.fiveDices.tempDiceSetIndValues[4][0]].isSelected = true;
            else
                currentGame.fiveDices.diceSet[currentGame.fiveDices.tempDiceSetIndValues[0][0]].isSelected = true;
        }
        else if (currentGame.fiveDices.figureList.matches( ".*([123456]).*")){
            selectForCarre();
        }
    }

    private void selectForSmall() {
        //Select dice so that (sum of 1s & 2s) < 5
        int sum=0;
        for (int i=0; i<5; i++)
            this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[i][0]].isSelected = true;
        for (int i = 0; i<5; i++){
            if (this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[i][0]].value==1){
                this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[i][0]].isSelected = false;
                sum+= this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[i][0]].value;
            }
        }
        for (int i = 0; i<5; i++){
            if (this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[i][0]].value==2){
                this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[i][0]].isSelected = false;
                if (sum+this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[i][0]].value<5)
                    sum+= this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[i][0]].value;
            }
        }
    }

    private void selectForSuite(){
        int idx= getIdxFrom4inARow();
        for (int i =0; i<5; i++)
            this.currentGame.fiveDices.diceSet[i].isSelected=false;
        this.currentGame.fiveDices.diceSet[idx].isSelected=true;
    }

    private void selectForSec(){
        for (int i =0; i<5; i++)
            this.currentGame.fiveDices.diceSet[i].isSelected=true;
    }

    private void selectForBrelan(int value){
        for (int i =0; i<5; i++)
            currentGame.fiveDices.diceSet[i].isSelected= currentGame.fiveDices.diceSet[i].value != value;
    }

    private void selectForAppel(){
        if (currentGame.throwNb==2){
            switch (currentGame.appelRegistered){
                case "Full":
                    selectForFull();
                    break;
                case "Suite":
                    selectForSuite();
                    break;
                case "Carre":
                    selectForCarre();
                    break;
                case "Small":
                    selectForSmall();
                case "Yam":
                    selectForYam();
                case "Sec":
                    selectForSec();
            }
        }
        else {
            //Placé en 1er donc appel au full passe d'abord
            if (figureContainsDoublePair()){
                selectForFull();
                appel("Full");
            }
            else if (figureContains4InARow()!=0){
                selectForSuite();
                appel("Suite");
            }
//             else  if (stringMatcher(currentGame.fiveDices.figureList, ".*1|2|3|4|5|6|Carre.*")){
            else if (currentGame.fiveDices.figureList.matches( ".*(1|2|3|4|5|6|Carre).*")){
                selectForCarre();//On peut aussi partir d'un carre pour appeler un carre
                appel("Carre");
            }
            else if (figureContainsAlmostSmall()){
                selectForSmall();
                appel("Small");
            }
            else if (figureContainsSinglePair()){
                // selectForCarre();
                // appel("Carre");
                selectForBrelan(getSinglePairValue());
                appel("full");
            }
            //pour gérer le cas où la machine décide de viser la case appel parce que c'est la seule option pour qu'elle gagne
            else {
                selectForSec();
                appel("sec");
            }
        }
    }
    //Pour Brelan  carre Full Small Yam  + Appel
    private boolean figureContainsSingleValue(int value){
        for (int i =0; i<3; i++){
            if (this.currentGame.fiveDices.tempDiceSetIndValues[i][1]==value && this.currentGame.fiveDices.tempDiceSetIndValues[i+1][1]!=value)
                return true;
            if (this.currentGame.fiveDices.tempDiceSetIndValues[4][1]==value)
                return true;
        }
        return false;
    }


    private boolean figureContainsPair(){
        for(int i = 0; i <4; i++)
            if (this.currentGame.fiveDices.tempDiceSetIndValues[i][1]==this.currentGame.fiveDices.tempDiceSetIndValues[i+1][1])
                return true;
        return false;
    }

    private boolean figureContainsSinglePair(){
        return figureContainsPair() && !figureContainsDoublePair();
    }

    private int getSinglePairValue() {
        if (figureContainsSinglePair()){
            for (int i = 0; i < 4; i++)
                if (this.currentGame.fiveDices.tempDiceSetIndValues[i][1] == this.currentGame.fiveDices.tempDiceSetIndValues[i + 1][1])
                    return this.currentGame.fiveDices.tempDiceSetIndValues[i][1];
        }
        return 0;
    }

    private void selectFromSinglePair(){
        for (int i =0; i<5; i++)
            this.currentGame.fiveDices.diceSet[i].isSelected=true;
        for(int i = 0; i <4; i++)
            if (this.currentGame.fiveDices.tempDiceSetIndValues[i][1]==this.currentGame.fiveDices.tempDiceSetIndValues[i+1][1]){
                for (int j=i+2; j<4; j++)
                    if (this.currentGame.fiveDices.tempDiceSetIndValues[j][1]==this.currentGame.fiveDices.tempDiceSetIndValues[j+1][1])
                        return;//if we find another pair
                this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[i][0]].isSelected=false;
                this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[i+1][0]].isSelected=false;
                return;
            }
    }

    private boolean figureContainsDoublePair(){
        if ((this.currentGame.fiveDices.tempDiceSetIndValues[0][1]==this.currentGame.fiveDices.tempDiceSetIndValues[1][1]) &&
                (this.currentGame.fiveDices.tempDiceSetIndValues[2][1]==this.currentGame.fiveDices.tempDiceSetIndValues[3][1])){
            return true;
        }
        else if ((this.currentGame.fiveDices.tempDiceSetIndValues[1][1]==this.currentGame.fiveDices.tempDiceSetIndValues[2][1]) &&
                (this.currentGame.fiveDices.tempDiceSetIndValues[3][1]==this.currentGame.fiveDices.tempDiceSetIndValues[4][1])){
            return true;
        }
        else if ((this.currentGame.fiveDices.tempDiceSetIndValues[0][1]==this.currentGame.fiveDices.tempDiceSetIndValues[1][1]) &&
                (this.currentGame.fiveDices.tempDiceSetIndValues[3][1]==this.currentGame.fiveDices.tempDiceSetIndValues[4][1])){
            return true;
        }
        return false;
    }

    private void selectfromDoublePair(){
        for (int i =0; i<5; i++)
            this.currentGame.fiveDices.diceSet[i].isSelected=false;
        if ((this.currentGame.fiveDices.tempDiceSetIndValues[0][1]==this.currentGame.fiveDices.tempDiceSetIndValues[1][1]) &&
                (this.currentGame.fiveDices.tempDiceSetIndValues[2][1]==this.currentGame.fiveDices.tempDiceSetIndValues[3][1])){
            this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[4][0]].isSelected=true;
        }
        else if ((this.currentGame.fiveDices.tempDiceSetIndValues[1][1]==this.currentGame.fiveDices.tempDiceSetIndValues[2][1]) &&
                (this.currentGame.fiveDices.tempDiceSetIndValues[3][1]==this.currentGame.fiveDices.tempDiceSetIndValues[4][1])){
            this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[0][0]].isSelected=true;
        }
        else if ((this.currentGame.fiveDices.tempDiceSetIndValues[0][1]==this.currentGame.fiveDices.tempDiceSetIndValues[1][1]) &&
                (this.currentGame.fiveDices.tempDiceSetIndValues[3][1]==this.currentGame.fiveDices.tempDiceSetIndValues[4][1])){
            this.currentGame.fiveDices.diceSet[this.currentGame.fiveDices.tempDiceSetIndValues[2][0]].isSelected=true;
        }
    }
    //4 à la suite
    private int getIdxFrom4inARow(){
        int idx = getMissingIdxForSuite(0);
        if (idx==-1)
            idx=getMissingIdxForSuite(1);
        return idx;
    }

    private int getMissingIdxForSuite(int inc){
        //inc=0 -> petite suite or 1 -> grande suite
        List <Integer> listDiceIdx= new ArrayList<>();
        for  (int value=1+inc; value<=5+inc; value++)  {
            for (int i=0; i<5; i++){
                if ((currentGame.fiveDices.tempDiceSetIndValues[i][1]==value)){
                    listDiceIdx.add(currentGame.fiveDices.tempDiceSetIndValues[i][0]);
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

    private int figureContains4InARow(){
        int idx1 = getMissingIdxForSuite(0);
        int idx2 = getMissingIdxForSuite(1);
        if ((idx1!=-1) && (idx2!=-1)){
            return 2;
        }
        if ((idx1!=-1) || (idx2!=-1)){
            return 1;
        }
        return 0;
    }

    //somme 3 dés  < 6
    private boolean figureContainsAlmostSmall(){
        int sumOfus = 0;
        for (int i = 0; i<3; i++){
            sumOfus += currentGame.fiveDices.tempDiceSetIndValues[i][1];
        }
        return sumOfus < 4;
    }

    /*Deal with appel*/
    //Checks if the appel is missed and resets flags
    private void checkForMissedAppel(){
        if (currentGame.appelClicked){
            final int appelId=currentGame.appelBoxId;
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
            final int figAppelId = currentGame.appelFigTypeBoxId;
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.ungrayAppelBoxOrFigAppelBoxToPreviousState(figAppelId);
                }
            });
            currentGame.appelClicked=false;
            currentGame.appelRegistered="";
            currentGame.appelBoxId=0;
            currentGame.appelFigTypeBoxId=0;
        }
    }

    private void appel(String figureAppel){
        // si jet = 1 et appel possible alors appel figure
        // montrer sur UI: click sur appel, click sur figure

        if (currentGame.throwNb==1){
            //Arbitraire: voir si on peut récupérer la case appel souhaitée par la machine
            final int appelBoxId = currentGame.checkerBox[0][2].id;
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
                    if (currentGame.checkerBox[i][j].figType.equals(figureAppel)){
                        tempAppelBoxFigTypeId=currentGame.checkerBox[i][j].id;
                        break;
                    }
            final int figureAppelId=tempAppelBoxFigTypeId;
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
