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
import java.util.List;

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
        try{
            sleep(1000);
        }
        catch (InterruptedException e){
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
                    try{
                        sleep(1000);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    showDroidThrowsUI(currentGame.throwNb);
                    syncFiveDicesResultsWithUI();//Shows dice results
                    String target = machineChoseFromDices();
                    if (target.matches(".*(1|2|3|4|5|6|Appel|Carre|Full|Yam|Suite|Sec|Small).*")){
                        selectDiceFromTarget(target);
                    } else if (target.equals("blue")) {
                        //Pour être sûr d'avoir la bonne couleur courante car le changement de couleur est fait dans le UI thread
                        try{
                            sleep(1000);
                        }
                        catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        //Test de fin de jeu
                        if (!currentGame.couleur.equals("white")){
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
                BoxPair boxIdPoint1 = new BoxPair(currentGame.checkerBox[v1][h1].id, getPotentialNextTurnPointsPerBox("red", currentGame.checkerBox[v1][h1]));
                boxIdPointList.add(boxIdPoint1);
            }
            if (currentGame.checkerBox[v2][h2].color.equals("white")) {
                BoxPair boxIdPoint2 = new BoxPair(currentGame.checkerBox[v2][h2].id, getPotentialNextTurnPointsPerBox("red", currentGame.checkerBox[v2][h2]));
                boxIdPointList.add(boxIdPoint2);

            }
        }
    }

    //returns potential next turn poinst per box
    private int getPotentialNextTurnPointsPerBox(String aColor, Box aBox) {
        int v = aBox.v;
        int h = aBox.h;
        int potentialPoints = 0;
        potentialPoints+= getPotentialPointsNextTurnPerCoord(aColor, v,0, h, 1);//Horizontally
        potentialPoints+= getPotentialPointsNextTurnPerCoord(aColor, v, 1, h, 0);//Vertically
        potentialPoints+= getPotentialPointsNextTurnPerCoord(aColor, v,1, h, 1);//to bottom right
        potentialPoints+= getPotentialPointsNextTurnPerCoord(aColor, v,1, h, -1);//to bottom left
        System.out.println("getPotentialNextTurnPointsPerBox: "+aColor+" points:"+potentialPoints);
        return potentialPoints;
    }

    //Checks if the coord are within bounds of the board
    private boolean checkBoxColorWhithinBound(int v, int h, String aColor ){
        if (((v>=0)&& (v<=4)) && ((h>=0)&&(h<=4)))
            return currentGame.checkerBox[v][h].color.equals(aColor);
        return false;
    }

    private boolean colorInSpanPerBoxExists(String aColor, Box aBox, int span){
        int v = aBox.v;
        int h = aBox.h;
        if(colorInSpanPerLineExists(aColor, v, 0, h, 1, span)
                ||colorInSpanPerLineExists(aColor, v, 1, h, 0, span)
                ||colorInSpanPerLineExists(aColor, v, 1, h, 1, span)
                ||colorInSpanPerLineExists(aColor, v, 1, h, -1, span)
        )
            return true;
        else
            return false;
    }

    private boolean colorInSpanPerLineExists(String aColor, int v, int incrV, int h, int incrH, int span){
        if ((checkBoxColorWhithinBound(v+span*incrV, h+span*incrH, aColor))
                ||(checkBoxColorWhithinBound(v-span*incrV, h-span*incrH, aColor)))
            return true;
        return false;
    }

    //returns potential points (next turn) from coordinates
    //Ex: XRW or XWR or RXW or WXR leads to future potentials points
    private int getPotentialPointsNextTurnPerCoord(String aColor, int v, int incrV, int h, int incrH){
        int potentialPoints = 0;
        if (checkBoxColorWhithinBound(v+incrV, h+incrH, aColor)){
            if (checkBoxColorWhithinBound(v+2*incrV, h+2*incrH, "white"))
                potentialPoints++;
        }
        else if (checkBoxColorWhithinBound(v+incrV, h+incrH, "white")){
            if (checkBoxColorWhithinBound(v+2*incrV, h+2*incrH, aColor))
                potentialPoints++;
        }

        if (checkBoxColorWhithinBound(v-incrV, h-incrH, aColor)){
            if (checkBoxColorWhithinBound(v-2*incrV, h-2*incrH, "white"))
                potentialPoints++;
        }
        else if (checkBoxColorWhithinBound(v-incrV, h-incrH, "white")){
            if (checkBoxColorWhithinBound(v-2*incrV, h-2*incrH, aColor))
                potentialPoints++;
        }

        if (checkBoxColorWhithinBound(v+incrV, h+incrH, aColor)){
            if (checkBoxColorWhithinBound(v-incrV, h-incrH, "white"))
                potentialPoints++;
        }
        else if (checkBoxColorWhithinBound(v-incrV, h-incrH, aColor)){
            if (checkBoxColorWhithinBound(v+incrV, h+incrH, "white"))
                potentialPoints++;
        }
        return potentialPoints;
    }

    //Checks if a List of boxes contains a box with such figType
    private boolean boxListContains(List <BoxPair> aBoxPairList, String figType){
        for (int i =0; i< aBoxPairList.size()-1; i++)
            if (currentGame.findBoxById(aBoxPairList.get(i).getPairId()).figType.equals(figType))
                return true;
        return false;
    }

    //returns optimal next turn box
    private BoxPair getOptimaNextTurnNextThrowBoxPairFromFigureList(List<BoxPair> boxIdPointList){
        //If no points scored, try to figure out the best figure to get for next turn
        if (!boxIdPointList.isEmpty())
            if ( (boxIdPointList.get(boxIdPointList.size() - 1)).getPairPoints()==0){
                for (int i = 0; i < boxIdPointList.size(); i++) {
                    //Add potential points for each box of the list
                    boxIdPointList.get(i).setPairPoints(getPotentialNextTurnPointsPerBox("red", currentGame.findBoxById(boxIdPointList.get(i).getPairId())));
                }
            }
        //Sort in ascending order
        Collections.sort(boxIdPointList);
        System.out.println("getOptimaNextTurnNextThrowBoxPairFromFigureList");
        for (int i = 0; i< boxIdPointList.size(); i++)
        {
            System.out.println("****debut box***");
            System.out.println(boxIdPointList.get(i));
            currentGame.findBoxById(boxIdPointList.get(i).getPairId()).afficherBox();
            System.out.println("****fin box***");
        }
        if (!boxIdPointList.isEmpty())
            return boxIdPointList.get(boxIdPointList.size() - 1);
        else return new BoxPair(0, 0);
    }

    //returns optimal current turn next throw box
    private BoxPair getOptimalCurrentTurnNextThrowBoxPairFromFigureList(List<BoxPair> boxIdPointList) {
        //Add points if marker placed on boxes
        for (int i = 0; i < boxIdPointList.size(); i++) {
            boxIdPointList.get(i).setPairPoints(getPointsIfMarkerPlacedOnBox("red", boxIdPointList.get(i).getPairId()));
        }
        //Sort in ascending order
        Collections.sort(boxIdPointList);
        //Return the last of the list (the best)
        System.out.println("getOptimalCurrentTurnNextThrowBoxPairFromFigureList");
        for (int i = 0; i< boxIdPointList.size(); i++)
        {
            System.out.println("****debut box***");
            System.out.println(boxIdPointList.get(i));
            currentGame.findBoxById(boxIdPointList.get(i).getPairId()).afficherBox();
            System.out.println("****fin box***");
        }
        if (!boxIdPointList.isEmpty())
            return boxIdPointList.get(boxIdPointList.size() - 1);
        else return new BoxPair(0, 0);
    }

    //Adds points obtained if current given figtype is placed to given ArrayList of BoxPairs
    private void listAddCurrentThrowBoxPairIdPointsPerFigure(ArrayList<BoxPair> boxIdPointList, String figureList, String aFigure, int v1, int h1, int v2, int h2) {
        if (figureList.contains(aFigure)) {
            if (currentGame.checkerBox[v1][h1].color.equals("white")) {
                int points = getPointsIfMarkerPlacedOnBox("red", currentGame.checkerBox[v1][h1].id);
                if (points>=0){
                    BoxPair boxIdPoint = new BoxPair(currentGame.checkerBox[v1][h1].id, points);
                    boxIdPointList.add(boxIdPoint);
                }
            }
            if (currentGame.checkerBox[v2][h2].color.equals("white")) {
                int points = getPointsIfMarkerPlacedOnBox("red", currentGame.checkerBox[v2][h2].id);
                if (points>=0) {
                    BoxPair boxIdPoint2 = new BoxPair(currentGame.checkerBox[v2][h2].id, points);
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
                if (points>=0) {
                    BoxPair boxIdPoint = new BoxPair(currentGame.checkerBox[2][2].id, points);
                    boxIdPointList.add(boxIdPoint);
                }
            }
        }
        Collections.sort(boxIdPointList);
        //       System.out.println("boxIdPointList:");
        //       System.out.println(boxIdPointList);
        //       System.out.println("Dernier de la liste 1:");
//        System.out.println(boxIdPointList.get(boxIdPointList.size() - 1));
//        System.out.println("Dernier de la liste 2:");
//        currentGame.findBoxById(boxIdPointList.get(boxIdPointList.size() - 1).getPairId()).afficherBox();

        System.out.println("getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList");
        for (int i = 0; i< boxIdPointList.size(); i++)
        {
            System.out.println("****debut box***");
            System.out.println(boxIdPointList.get(i));
            currentGame.findBoxById(boxIdPointList.get(i).getPairId()).afficherBox();
            System.out.println("****fin box***");
        }
        if (!boxIdPointList.isEmpty())
            return boxIdPointList.get(boxIdPointList.size() - 1);
        //Just in case
        return new BoxPair(0, 0);
    }

    //Returns points if marker placed on box
    private int getPointsIfMarkerPlacedOnBox(String aColor, int boxId) {
        int tmpPoints = 0;
        //copie de currentGame
        Jeu tempGame = new Jeu(currentGame);
        tempGame.findBoxById(boxId).afficherBox();
        if (currentGame.findBoxById(boxId).color.equals("white")) {
            tempGame.findBoxById(boxId).color = aColor;
            tmpPoints = tempGame.countLine(3, aColor, boxId);
            if ((tempGame.fullLine(aColor, boxId))|| (currentGame.redMarkers-1==0)){
                if (currentGame.redPoints+tmpPoints<currentGame.bluePoints) {
                    System.out.println("getPointsIfMarkerPlacedOnBox Marquage perdant:"+tmpPoints+"mais -1");
                    tempGame.findBoxById(boxId).afficherBox();
                    //Si on peut esperer gagner la partie avec les pions qui nous restent on retourne -1
                    return -1;  //Pour le mettre en queue des choix dans getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList
                    //sinon on pose qd même et on perd
                    //TODO: implémenter recherche de case qui permet de gagner ou au moins de
                    // marquer le plus indépendement de la figure obtenue
                }
                else if (currentGame.redPoints+tmpPoints>=currentGame.bluePoints){
                    System.out.println("getPointsIfMarkerPlacedOnBox Marquage gagnant:"+tmpPoints+" mais 10");
                    tempGame.findBoxById(boxId).afficherBox();
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
        System.out.println("getPointsIfMarkerPlacedOnBox: "+aColor+" points:"+tmpPoints);
        return tmpPoints;
    }

    private boolean checkFullLinePoints (Jeu aGame, String aColor, int boxId){
        //Si on peut fermer le jeu en gagnant
        if ((aGame.fullLine(aColor, boxId))&&(aGame.redPoints>aGame.bluePoints)){
            return true;
        }
        else
            return false;
    }

    //Returns the best  BoxPair (id+potential future points) for a given figureList
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
            BoxPair boxIdPoint = new BoxPair(currentGame.checkerBox[2][2].id, getPotentialNextTurnPointsPerBox("red", currentGame.checkerBox[2][2]));
            boxIdPointList.add(boxIdPoint);
        }
        listAddNextTurnBoxPairIDPointsPerFigure(boxIdPointList, figureList, "Appel", 0, 2, 2, 3);

        //Sort boxIdPointList
        Collections.sort(boxIdPointList);
        System.out.println("getOptimalNextTurnCurrentThrowBoxPairPerFigureList");
        for (int i = 0; i< boxIdPointList.size(); i++)
        {
            System.out.println("****debut box***");
            System.out.println(boxIdPointList.get(i));
            currentGame.findBoxById(boxIdPointList.get(i).getPairId()).afficherBox();
            System.out.println("****fin box***");
        }
        if (!boxIdPointList.isEmpty())
            return boxIdPointList.get(boxIdPointList.size() - 1);
        else {
            return new BoxPair(0, 0);
        }
    }

    private void   printOptimalBoxList(List<BoxPair> aBoxPairList){
        if (!aBoxPairList.isEmpty())
            for (int i =0; i <aBoxPairList.size();i++){
                currentGame.afficherBoxPair(aBoxPairList.get(i));
            }
        System.out.println(" ");
    }

    //returns a list of optimal next throw boxes from a given current figure list
    private List <BoxPair> getListOptimalNextThrowBoxpairFromFigureList(String aFigureList){
        //TODO implémenter recherche de case qui ferme le jeu avec victoire (fullLine ou pion=0)
        //meme avec 1 seul dé (genre pour tenter brelan qui tue)
        List<BoxPair> boxIdPointList = new ArrayList<>();
        if (aFigureList.matches(".*([123456]).*")) {
            if (currentGame.throwNb==1){
                if (!boxListContains(boxIdPointList, "Appel"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
            }
            if (!aFigureList.contains("Carre")){
                if (!boxListContains(boxIdPointList, "Carre"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Carre", "white"));
            }
            if(!aFigureList.contains("Full")){
                if (!boxListContains(boxIdPointList, "Full"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Full", "white"));
            }
            if (!aFigureList.contains("Yam")){
                if (!boxListContains(boxIdPointList, "Yam"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Yam", "white"));
            }
        }
        if (aFigureList.contains("Carre")) {
            if (currentGame.throwNb==1)
                if (!boxListContains(boxIdPointList, "Appel")){
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
                }
            if (!aFigureList.contains("Yam")){
                if (!boxListContains(boxIdPointList, "Yam"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Yam", "white"));
            }
        }
        if (aFigureList.contains("1")) {
            if (currentGame.throwNb==1){
                if (!boxListContains(boxIdPointList, "Appel"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
            }
            if (!aFigureList.contains("Small")){
                if (!boxListContains(boxIdPointList, "Small"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Small", "white"));
            }
        }
        if (aFigureList.contains("Full")) {
            if (currentGame.throwNb==1){
                if (!boxListContains(boxIdPointList, "Appel"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
            }
            if (currentGame.getListBoxPairColorPerFigure("Full", "white").isEmpty()){
                if (!boxListContains(boxIdPointList, "Carre")){
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Carre", "white"));
                }
                if (!boxListContains(boxIdPointList, "Yam")){
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Yam", "white"));
                }
            }
        }

        if ((figureContainsDoublePair()) && (!aFigureList.contains("Full"))) {
            if (currentGame.throwNb==1){
                if (!boxListContains(boxIdPointList, "Appel"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
            }
            //Pour les brelans
            int valIdx1 = currentGame.fiveDices.tempDiceSetIndValues[1][1];
            int valIdx3 = currentGame.fiveDices.tempDiceSetIndValues[3][1];
            if (!boxListContains(boxIdPointList, Integer.toString(valIdx1))){
                boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure(Integer.toString(valIdx1), "white"));
            }
            if (!boxListContains(boxIdPointList, Integer.toString(valIdx3))){
                boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure(Integer.toString(valIdx3), "white"));
            }
            //Full
            if (!boxListContains(boxIdPointList, "Full")){
                boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Full", "white"));
            }
            if (aFigureList.contains("Carre")){
                //si 2  paires identiques
                if (!boxListContains(boxIdPointList, "Yam"))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Yam", "white"));
            }
        }

        if (!aFigureList.contains("Suite"))//Pour tenter des suites
            if (figureContains4InARow()!=0) {
                //si figureContains4InARow()==1 alors voir si on ne peut pas tenter un brelan plutôt
                // car si figureContains4InARow==1 alors seule autre possibilite=singlepaire ou rien
                // si figureContains4InARow==2 alors tenter suite
                if (currentGame.throwNb==1){
                    if (!boxListContains(boxIdPointList, "Appel"))
                        boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Appel", "white"));
                }
                if (figureContains4InARow()==2){
                    if (!boxListContains(boxIdPointList, "Suite"))
                        boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Suite", "white"));
                }
                if (figureContains4InARow()==1){
                    //See if we'd better try a brelan instead if we've got a pair
                    if (!boxListContains(boxIdPointList, "Suite"))
                        boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure("Suite", "white"));
                }
            }
        //Pour tenter des brelans
        if (figureContainsPair()) {
            int val = getSinglePairValue();
            if (!currentGame.fiveDices.figureList.contains(Integer.toString(val))){
                if (!boxListContains(boxIdPointList, Integer.toString(val)))
                    boxIdPointList.addAll(currentGame.getListBoxPairColorPerFigure(Integer.toString(val), "white"));
            }
        }

        //Pour tenter le small
        if (!aFigureList.contains("Small"))
            if (figureContainsAlmostSmall()) {
                if (currentGame.throwNb==1){
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
    private boolean checkMarkerSpoilByBoxId(int aBoxId){
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
        if (((currentGame.redMarkers<=currentGame.blueMarkers)||(currentGame.redMarkers<5))&&(currentGame.redPoints<currentGame.bluePoints)){
            if (((getPotentialNextTurnPointsPerBox("red", currentGame.findBoxById(aBoxId)))==0)//no future red points
                    &&(!colorInSpanPerBoxExists("white", currentGame.findBoxById(aBoxId), 2)))//box is not rounded by white ones
                return true;
        }
        //Si la case ne sert à rien c'est à dire
        //Si, maintenant ou plus tard,  le pion ne marque pas pour les rouges ou n'empêche pas les bleus de marquer
        if (((getPotentialNextTurnPointsPerBox("red", currentGame.findBoxById(aBoxId)))==0)//no future red points
                &&(getPotentialNextTurnPointsPerBox("blue", currentGame.findBoxById(aBoxId))==0)//no future blue points
                &&(getPointsIfMarkerPlacedOnBox("blue", aBoxId)==0)//Blue cannot mark (consequently also close the game) later with such box
                &&(!colorInSpanPerBoxExists("white", currentGame.findBoxById(aBoxId), 2)))//box is not rounded by white ones
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
        Combinations aCombination = new Combinations(aGame.redMarkers, aBoxPairIdList);
        aCombination.Combinate();
        List<int[]> combSubsets = aCombination.getSubsets();
        List<JeuCombinaison> gameCombinationList = new ArrayList<>();
        for (int i = 0; i< combSubsets.size(); i++){
            //utiliser le constructeur de copie pour Jeu
            JeuCombinaison aGameCombination = new JeuCombinaison(new Jeu (aGame), aColor, combSubsets.get(i));
            //aGameCombination.computeCombinationPoints();
            gameCombinationList.add(aGameCombination);
        }
        Collections.sort(gameCombinationList);
        System.out.println("Nombre de combinaisons possibles: "+gameCombinationList.size());
        for (int i =0; i<gameCombinationList.size(); i++)
        {
            System.out.println("Combinaison["+i+"]: "+gameCombinationList.get(i).getPoints()+" points");
        }
        List<JeuCombinaison> bestGameCombinationsList = new ArrayList<>();
        bestGameCombinationsList.add(gameCombinationList.get(gameCombinationList.size()-1));
        for (int i=0; i<gameCombinationList.size()-2; i++)
        {
            if (gameCombinationList.get(i)==bestGameCombinationsList.get(0))
                bestGameCombinationsList.add(gameCombinationList.get(i));
        }
        return bestGameCombinationsList;
    }

    private List<Integer> getFreeBoxIdList(Jeu aGame){
        List<Integer> aBoxPairList = new ArrayList<>();
        for (int i = 0; i<5; i++)
            for (int j = 0; j<5; j++)
            {
                if (aGame.checkerBox[i][j].color.equals("white"))
                {
                    aBoxPairList.add(aGame.checkerBox[i][j].id);
                }
            }
        return  aBoxPairList;
    }

    private List<BoxPair> getFreeBoxList(Jeu aGame){
        List<BoxPair> aBoxPairList = new ArrayList<>();
        for (int i = 0; i<5; i++)
            for (int j = 0; j<5; j++)
            {
                if (aGame.checkerBox[i][j].color.equals("white"))
                {
                    BoxPair aBoxPair = new BoxPair(aGame.checkerBox[i][j].id, 0);
                    aBoxPairList.add(aBoxPair);
                }
            }
        return  aBoxPairList;
    }


    /*Method where droid choses target from current throw*/
    //Returns the target figtype upon wich we will select the dices next turn if any else returns null string and place the marker if possible
    private String machineChoseFromDices() {

        List<BoxPair> boxIdPointList = getListOptimalNextThrowBoxpairFromFigureList(currentGame.fiveDices.figureList);

        //Ajout de la figure améliorable d'abord pour privilégier les 2 autres solutions avant en cas d'égalité de points
        BoxPair boxPairTargetCurrentTurnNextThrow=new BoxPair(0, 0);
        BoxPair boxPairTargetNextTurnNextThrow = new BoxPair(0,0);
        BoxPair boxPairCurrentTurnCurrentThrow = getOptimalCurrentTurnCurrentThrowBoxPairPerFigureList(currentGame.fiveDices.figureList);
        BoxPair boxPairNextTurnCurrentThrow = getOptimalNextTurnCurrentThrowBoxPairPerFigureList(currentGame.fiveDices.figureList);

        //Test de la nouvelle fonction ....ça semble fonctionner...
        if (currentGame.redMarkers<5)
        {
            System.out.println("Max points additionnels: "+bestCombinationsAvailable("red", currentGame).get(0).getPoints());
        }

        if (currentGame.throwNb < currentGame.maxThrowNb) {
            if ((currentGame.throwNb==2) && (!currentGame.fiveDices.figureList.equals("Appel"))){
                if (currentGame.appelClicked)
                    return "Appel";
            }
            boxPairTargetNextTurnNextThrow = getOptimaNextTurnNextThrowBoxPairFromFigureList(boxIdPointList);
            boxPairTargetCurrentTurnNextThrow = getOptimalCurrentTurnNextThrowBoxPairFromFigureList(boxIdPointList);
        }
        if (boxPairCurrentTurnCurrentThrow.getPairId()>0){
            if (currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).figType.equals("Appel"))
                if (currentGame.fiveDices.figureList.equals("Appel")){
                    return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());
                }

            if (currentGame.throwNb<currentGame.maxThrowNb){
                if (boxPairTargetCurrentTurnNextThrow.getPairId()>0){
                    //TODO bug ici
                    //Ne faire nextthrow que si currenthrow pas posable
                    // ou si on peut LA garder (ex: BRELAN->full ok  mais pas FULL->carre ni Yam ni Sec)
                    if (boxPairTargetCurrentTurnNextThrow.getPairPoints()>=boxPairCurrentTurnCurrentThrow.getPairPoints()){
                        if (!((currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).figType.equals("Full"))||
                                (currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).figType.equals("Yam"))||
                                (currentGame.findBoxById(boxPairCurrentTurnCurrentThrow.getPairId()).figType.equals("Sec")))){
                            return currentGame.findBoxById(boxPairTargetCurrentTurnNextThrow.getPairId()).figType;
                        }
                        else {
                            return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());
                        }
                    }
                }
            }
            //1: Marquer current throw
            if (boxPairCurrentTurnCurrentThrow.getPairPoints()>0){
                return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());
            }
            //2:Améliorer figure courante
            if (boxPairNextTurnCurrentThrow.getPairId()>0){
                if (boxPairNextTurnCurrentThrow.getPairPoints()>0){//verifier cela
                    return machinePlaceMarkerById(boxPairNextTurnCurrentThrow.getPairId());
                }
            }
            //3: Améliorer figure point potentiels next turn
            if (boxPairTargetNextTurnNextThrow.getPairId()>0){
                if (boxPairTargetNextTurnNextThrow.getPairPoints()>0){
                    return currentGame.findBoxById(boxPairTargetNextTurnNextThrow.getPairId()).figType;
                }
            }
            //TODO: vérifier avant que cela nous permet de marquer plus tard ou que cela ne nous gâche pas de pions en cas de fin de jeu serrée
            //TODO decommenter la ligne qui suit quand tout sera implémenté
            if (!checkMarkerSpoilByBoxId(boxPairCurrentTurnCurrentThrow.getPairId())){
                System.out.println("Pas de gâchis, on pose!");
                return machinePlaceMarkerById(boxPairCurrentTurnCurrentThrow.getPairId());
            }
            else System.out.println("Quel gâchis, on ne pose pas!");
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
                if (boxPairTargetNextTurnNextThrow.getPairId() > 0) {
                    if (boxPairTargetNextTurnNextThrow.getPairPoints() >= 0) {
                        return currentGame.findBoxById(boxPairNextTurnCurrentThrow.getPairId()).figType;
                    }
                }
                else
                if (currentGame.throwNb < currentGame.maxThrowNb) {
                    return "allDice";
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
            else if (figureContainsPair()){
                selectForCarre();
                appel("Carre");
            }
        }
    }
    //Pour Brelan  carre Full Small Yam  + Appel
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
//TODO rechercher nb de points maximal possible en fonction de l'état du jeu (combinaison optimale pour gagner) avec combinaison de K parmi N
