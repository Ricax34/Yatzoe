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

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.lang.Thread.sleep;

class MachinePlayTask implements Runnable {
    private final Jeu currentGame;
    private final MainActivity mainActivity;
    private String machineFigureAppel;
    private Box appelBox;
    //private boolean logFlag = false;
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
                if (currentGame.throwNb == 0)
                    if (!mainActivity.diceCheat){
                        for (int j = 0; j < 5; j++)
                            currentGame.fiveDices.diceSet[j].isSelected = true;
                    }

                syncFiveDicesSelectionWithUI(); //show machine selecting dices
                if (currentGame.throwNb < currentGame.maxThrowNb) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if ((!mainActivity.diceCheat)||currentGame.throwNb>0) {//cette condition est pour le cheat, à enlever + tard
                        currentGame.throwDices();
                        showDroidThrowsUI(currentGame.throwNb);
                        syncFiveDicesResultsWithUI();//Shows dice results
                    }
                    else{//ce bloc est pour le cheat
                        currentGame.throwNb++;//remplacé par la ligne suivante si la condition est vraie
                        if (mainActivity.thrownbCheat>0)
                            currentGame.throwNb=mainActivity.thrownbCheat;
                        currentGame.fiveDices.figureList="";
                        currentGame.fiveDices.setListOfFiguresFromDiceSet();
                    }
                    String target = machineChoseFromDices();
                    if (mainActivity.logFlag)
                    {
                        System.out.println("Target: "+target);
                        appendLog("Target: "+target);
                    }
                    if (target.matches(".*(1|2|3|4|5|6|Appel|Carre|Full|Yam|Suite|Sec|Small).*")) {
                        selectDiceFromTarget(currentGame, target);
                    }
                    else if (target.equals("blue")) {
                        //Pour être sûr d'avoir la bonne couleur courante car le changement de couleur est fait dans le UI thread
                        try {
                            sleep(1500);
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
                        if (mainActivity.thrownbCheat>0)
                            mainActivity.thrownbCheat=0;
                        lock = false;//exit the while loop
                    }
                }
            }
        }
    }


    /*****Methods that help droid sort all possibilities*******/
    //retourne la proba modulo 50 (sort of)
    public int getModulo50Prob(double proba){
        for (int i =0; i<20; i++)
            if (proba>=i*50 && proba<(i+1)*50){
                return i;
            }
        return 0;
    }

    //retourner le float calculer les probas (faits certains mais beaucoup pompés d'une page web  albert Franck etude systématique du yams)
    public int getProbByThrownNb(int throwNb, double prob1jet, double prob2jets){

        if (throwNb==1) {
            return getModulo50Prob(prob2jets*1000);
        }
        else {
            return getModulo50Prob(prob1jet*1000);
        }

        //Test
        // return getModulo50Prob(prob2jets*1000);
        // return getModulo50Prob(prob1jet*1000);
    }

    public int getBoxProbability(Jeu aGame, Box targetBox){
        String targetFigure;
        if (aGame.appelClicked)
            targetFigure=aGame.appelRegistered;
        else if (targetBox.getFigType().equals("Appel")&& aGame.throwNb==1){
            BoxPair aBoxPair= getBestAppelTargetFigureFromDiceSet(aGame);
            machineFigureAppel=aBoxPair.getFigType();
            appelBox=targetBox;
            return aBoxPair.getProbability();
        }
        else if (targetBox.getFigType().equals("Appel")&& aGame.throwNb>1)
            return 0;
        else targetFigure=targetBox.getFigType();
        return getFigProb(aGame, targetFigure);
    }

    public  BoxPair getBestAppelTargetFigureFromDiceSet(Jeu aGame){
        ArrayList<BoxPair> boxPairAppelFigures = new ArrayList<>();
        //Pas d'appel au sec ni au yam, cela n'a aucun sens du pt de vue probabilité
        String [] appelFigures ={"Carre", "Full", "Suite", "Small"};
        for (String appFig: appelFigures){
            //Populate boxPairAppelFigures with empty BoxPairs
            BoxPair appFigBP = new BoxPair(new Box(appFig, "White", 0, 0, 0), 0, 0);
            //Add the probability for each boxpair
            //Si on appelle une figure déjà obtenue il faut relancer au moins un dé
            //et tester avec un diceset nouveau
            if(aGame.fiveDices.figureList.contains(appFig)){
                Jeu tempAppelGame = new Jeu(aGame);
                //modifier le diceset du jeu en fonction de la figure à appeler pour obtenir la bonne proba
                tempAppelGame.fiveDices=setAppelDiceset(tempAppelGame, appFig);
                appFigBP.setProbability(getFigProb(tempAppelGame, appFig));
            }
            else
                appFigBP.setProbability(getFigProb(aGame, appFig));
            appFigBP.setBoxWeight();
            boxPairAppelFigures.add(appFigBP);
        }
        //Sort according to the only probability
        Collections.sort(boxPairAppelFigures);
        //System.out.println("getBestAppelTargetFigureFromDiceSet: "+boxPairAppelFigures.get(boxPairAppelFigures.size()-1).getFigType());
        return boxPairAppelFigures.get(boxPairAppelFigures.size()-1);
    }

    private Figure setAppelDiceset(Jeu aGame, String aFigType){
        Figure aFigure = new Figure(aGame.fiveDices);
        //Modifier le diceset
        switch (aFigType){
            case "Carre":
                //rien à faire on relancera celui qui n'est pas ds le carre ou bien le 5éme dé si yam
                //la proba reste donc le max: 20
                //géré ds figure.selectForCarre
                break;
            case "Full":
            {
                aFigure.diceSet[aFigure.tempDiceSetIndValues[2][0]].value=0;
                break;
            }
            case "Suite":
            {
                if (aFigure.tempDiceSetIndValues[0][1]==1)
                    aFigure.diceSet[aFigure.tempDiceSetIndValues[0][0]].value=0;
                else
                    aFigure.diceSet[aFigure.tempDiceSetIndValues[4][0]].value=0;
                break;
            }
            case "Small":
            {
                aFigure.diceSet[aFigure.tempDiceSetIndValues[4][0]].value=0;
                break;
            }
        }
        //TODO fixer ca
        aFigure.setListOfFiguresFromDiceSet();
        return aFigure;
    }

    public int setBrelanBoxBonus(Jeu aGame, BoxPair aboxPair){
        ArrayList<Box> freeBoxList= getFreeBoxList(aGame);
        //Si on a un brelan dont la box est libre
        String currFigs = aGame.fiveDices.figureList;
        // int bonus =0;
        if (currFigs.matches(".*([123456]).*")) {
            int bonus =0;
            String brelanValue = aGame.fiveDices.checkForBrelan();
            if (aboxPair.getFigType().equals("Yam")) {
               /* System.out.println("Yam1");
                System.out.println("currfigs:");
                System.out.println(currFigs);
                System.out.println("freeBoxList:");
                System.out.println(freeBoxList);
                System.out.println("A:"+(boxListContains(freeBoxList, brelanValue) && !(boxListContains(freeBoxList, "Full") && currFigs.contains("Full"))));
                System.out.println("B:"+!(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec")));
                System.out.println("C:"+!(boxListContains(freeBoxList, "Small") && currFigs.contains("Small")));
                */
                if (boxListContains(freeBoxList, brelanValue)) {
                    //System.out.println("bonus1 +20");
                    bonus += 20;//pour ne pas se faire squeezer par la proba de 20 du brelan
                }
                if ((boxListContains(freeBoxList, brelanValue) && !(boxListContains(freeBoxList, "Full") && currFigs.contains("Full")))
                        && !(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec"))
                        && !(boxListContains(freeBoxList, "Small") && currFigs.contains("Small"))
                ){
                    //System.out.println("bonus2 +20");
                    bonus +=20;
                }
                if ((boxListContains(freeBoxList, "Carre") && !(boxListContains(freeBoxList, "Full") && currFigs.contains("Full")))
                        && !(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec"))
                        && !(boxListContains(freeBoxList, "Small") && currFigs.contains("Small"))
                ){
                    //System.out.println("bonus3 +20");
                    bonus +=20;
                }
                if (currFigs.contains("Carre"))
                    if (boxListContains(freeBoxList, "Carre")
                            && !(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec"))
                            && !(boxListContains(freeBoxList, "Small") && currFigs.contains("Small"))
                    ){
                        //System.out.println("bonus4 +20");
                        bonus+=20;
                    }
                if ((boxListContains(freeBoxList, "Full")&& ! currFigs.contains("Full"))
                        && !(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec"))
                        && !(boxListContains(freeBoxList, "Small") && currFigs.contains("Small"))
                ){
                    //System.out.println("bonus5 +20");
                    bonus +=20;
                }
                if (brelanValue.equals(Integer.toString(1)))
                    if ((boxListContains(freeBoxList, "Small") && !currFigs.contains("Small"))
                            && !(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec"))
                    ){
                        //System.out.println("bonus6 +20");
                        bonus+=20;
                    }
                //System.out.println("bonus pour "+aboxPair.getFigType()+": "+bonus);
                return bonus;
            }
            else if (aboxPair.getFigType().equals("Carre")) {
                if ((boxListContains(freeBoxList, brelanValue) && !currFigs.contains("Carre"))
                        && !(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec"))
                        && !(boxListContains(freeBoxList, "Small") && currFigs.contains("Small"))
                ){
                    //System.out.println("bonus7 +20");
                    return 20;
                }
                if ((boxListContains(freeBoxList, "Full") && !currFigs.contains("Full"))
                        && !(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec"))
                        && !(boxListContains(freeBoxList, "Small") && currFigs.contains("Small"))
                ){
                    //System.out.println("bonus8 +20");
                    return 20;
                }
                if ((boxListContains(freeBoxList, "Yam") && ! currFigs.contains("Yam"))
                        && !(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec"))
                        && !(boxListContains(freeBoxList, "Small") && currFigs.contains("Small"))
                ){
                    //System.out.println("bonus9 +20");
                    return 20;
                }
            }
            else if (aboxPair.getFigType().equals("Small")) {
                // int bonus =0;
                if (brelanValue.equals(Integer.toString(1))){
                    if (boxListContains(freeBoxList, brelanValue)
                            && !(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec"))
                    )
                    {
                        //System.out.println("bonus10 +20");
                        bonus += 20;
                    }
                    if ((boxListContains(freeBoxList, "Carre") && !(currFigs.contains("Small")&& boxListContains(freeBoxList, "Small")))
                            && !(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec"))){
                        //System.out.println("bonus11 +20");
                        bonus += 20;
                    }
                }
                return bonus;
            }

            else if (aboxPair.getFigType().equals("Full")){
                //int bonus = 0;
                if (brelanValue.equals(Integer.toString(1))){
                    if (
                            (boxListContains(freeBoxList, "Small") && !currFigs.contains("Small"))
                                    && !(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec"))
                    ){
                        //System.out.println("bonus12 +20");
                        bonus += 20;

                    }
                }
                if ((boxListContains(freeBoxList, brelanValue) &&
                        !(boxListContains(freeBoxList,"Carre")&& currFigs.contains("Carre")))
                        && !(boxListContains(freeBoxList, "Sec") && currFigs.contains("Sec"))
                        && !(boxListContains(freeBoxList, "Small") && currFigs.contains("Small"))
                ){
                    //System.out.println("bonus13 +20");
                    bonus+=20;
                }
                return bonus;
            }
        }
        //TODO verifier de l'opportunité de ce qui suit
        /*else if(aboxPair.getFigType().equals("Full")){
            //si on part d'une paire de 1 et d'une paire de 2
            if (aGame.fiveDices.figureContainsDoublePair())
                if (boxListContains(freeBoxList, "Small"))
                    if (aGame.fiveDices.getPairValues(true, false)==1 && aGame.fiveDices.getPairValues(false, true)==2){
                        bonus += 5;
                        return bonus;
                    }
        }*/
        return 0;
    }

    public int getFigProb (Jeu aGame, String targetFigure){
        String currFig=aGame.fiveDices.figureList;
        int throwNb=aGame.throwNb;
        if (currFig.contains(targetFigure)|| (aGame.appelClicked && targetFigure.equals(machineFigureAppel))) return 20; //20*50=1000
        else if (throwNb<3) {
            if (targetFigure.equals("Yam")) {
                if (aGame.fiveDices.figureContainsPair()) {
                    if (currFig.matches(".*(1|2|3|4|5|6).*")) {
                        if (currFig.contains("Carre")) {
                            return getProbByThrownNb(throwNb, (double) 1 / 6, (double) 11 / 36);//0.1666 et 0.3055
                        }
                        else {
                            return getProbByThrownNb(throwNb, (double) 1 / 36, (double) 121 / 1296);//0.0277 et 0.0933
                        }
                    } else
                        return getProbByThrownNb(throwNb, (double) 6 / 100, (double) 11 / 100); //a calculer 6% et 11%
                } else
                    return getProbByThrownNb(throwNb, (double) 1 / 1296, (double) 2 / 1296); // 0.0007 et 0.00015 singleton ->yam a calculer
            }
            else if (targetFigure.equals("Carre")) {
                if (aGame.fiveDices.figureContainsPair()) {
                    if (currFig.matches(".*(1|2|3|4|5|6).*")) {
                        return getProbByThrownNb(throwNb, (double) 11 / 36, (double) 671 / 1296);//0.3055 et 0.5177
                    } else
                        return getProbByThrownNb(throwNb, (double) 2 / 27, (double) 22 / 100);//0.0740 et 22%
                } else
                    return getProbByThrownNb(throwNb, (double) 5 / 324, (double) 8 / 100);//0.154 et 8%
            }
            else if (targetFigure.equals("Full")) {
                if (currFig.matches(".*(1|2|3|4|5|6).*")) {
                    return getProbByThrownNb(throwNb, (double) 1 / 6, (double) 17 / 100);
                } else if (figureContainsDoublePair(aGame))
                    return getProbByThrownNb(throwNb, (double) 1 / 3, (double) 5 / 9);//0.3333 et 0.5555
                else if (figureContainsSinglePair(aGame))
                    return getProbByThrownNb(throwNb, (double) 21 / 216, (double) 89 / 324);//0.0972 et 0.2746
                    //TODO else  calcul singleton->full
                else
                    return getProbByThrownNb(throwNb, (double) 300 / 7776, (double) 600 / 7776); //0.0385 et 0.0771
                //Calculer prob full sec en 2 coups
            }
            else if (targetFigure.matches(".*(1|2|3|4|5|6).*")) {
                if (aGame.fiveDices.figureContainsPair()) {
                    if ((getPairValues(aGame, true, false) == Integer.parseInt(targetFigure))
                            || (getPairValues(aGame, false, true) == Integer.parseInt(targetFigure))) {
                        return getProbByThrownNb(throwNb, (double) 91 / 216, (double) 62 / 100); //0.4212 et 62%
                    }
                }
                if (figureContainsSingleValue(aGame, Integer.parseInt(targetFigure)))
                    return getProbByThrownNb(throwNb, (double) 25 / 216, (double) 28 / 100);//0.1157 et 28%
                else
                    return getProbByThrownNb(throwNb, (double) 200 / 7776, (double) 400 / 7776);//0.1543 et 0.3086 A calculer pour 2 jets
            }
            else if (targetFigure.equals("Suite")) {
                if (aGame.fiveDices.figureContains4InARow() == 2)
                    return getProbByThrownNb(throwNb, (double) 1 / 3, (double) 5 / 9);//0.3333 et 0.5555
                else if (aGame.fiveDices.figureContains4InARow() == 1)
                    return getProbByThrownNb(throwNb, (double) 1 / 6, (double) 11 / 36);//0.1666 et 0.3055
                else if (figureContainsSingleValue(aGame, 5)) {
                    if (figureContainsSingleValue(aGame, 4)) {
                        if (figureContainsSingleValue(aGame, 2))
                            return getProbByThrownNb(throwNb, (double) 1 / 9, (double) 5 / 18);//0.1111 et 0.2777
                        else
                            return getProbByThrownNb(throwNb, (double) 1 / 18, (double) 91 / 486);//0.0555 et 0.1872
                    } else if (figureContainsSingleValue(aGame, 6))
                        return getProbByThrownNb(throwNb, (double) 1 / 36, (double) 6 / 100);//0.1666 et 6%
                    else
                        return getProbByThrownNb(throwNb, (double) 1 / 27, (double) 1 / 27);//0.0370  et 0.0370 ??pas de 2éme valeur trouvée je laisse celle du 1er jet
                } else if (figureContainsSingleValue(aGame, 6))
                    return getProbByThrownNb(throwNb, (double) 1 / 54, (double) 6 / 100); //0.0185 et 6%
            }
            //Continuer pour les small
            else if (targetFigure.equals("Small")) {
                int sumOfDice = 0;
                //nb de 1, 2, 3 et 4
                int nbOf1 = 0;
                int nbOf2 = 0;
                int nbOf3 = 0;
                int nbOf4 = 0;
                for (int i = 0; i < 5; i++) {
                    if (aGame.fiveDices.tempDiceSetIndValues[i][1] == 1)
                        nbOf1++;
                    else if (aGame.fiveDices.tempDiceSetIndValues[i][1] == 2)
                        nbOf2++;
                    else if (aGame.fiveDices.tempDiceSetIndValues[i][1] == 3)
                        nbOf3++;
                    else if (aGame.fiveDices.tempDiceSetIndValues[i][1] == 4)
                        nbOf4++;
                    sumOfDice += aGame.fiveDices.tempDiceSetIndValues[i][1];
                }
                if (sumOfDice > 8) {
                    if (nbOf1 == 4)
                        return getProbByThrownNb(throwNb, (double) 4 / 6, (double) 30 / 36); //0.6666 et 0.8333 ???
                    else if (nbOf1 == 3 && nbOf2 == 1)
                        return getProbByThrownNb(throwNb, (double) 3 / 6, (double) 27 / 36);//0.5 et 0.75
                    else if ((nbOf1 == 3 && nbOf3 == 1) || (nbOf1 == 2 && nbOf2 == 2))
                        return getProbByThrownNb(throwNb, (double) 2 / 6, (double) 20 / 36);//0.3333 et 0.5555
                    else if ((nbOf1 == 3 && nbOf4 == 1) || (nbOf1 == 2 && nbOf2 == 1 && nbOf3 == 1))
                        return getProbByThrownNb(throwNb, (double) 1 / 6, (double) 11 / 36);//0.1666 et 0.3055
                    else if (nbOf1 == 3 && nbOf2 == 0)
                        return getProbByThrownNb(throwNb, (double) 10 / 36, (double) 620 / 1296);//0.2777 et 0.4782
                    else if (nbOf1 == 2 && nbOf2 == 1)
                        return getProbByThrownNb(throwNb, (double) 6 / 36, (double) 326 / 1296);//0.1666 et 0.2515
                    else if (nbOf1 == 2)
                        return getProbByThrownNb(throwNb, (double) 7 / 216, (double) 676 / 46656);//0.0324 et 0.0144
                    else
                        return getProbByThrownNb(throwNb, (double) 1 / 1000, (double) 2 / 1000);//proba minus pas la peine de calculer
                }
            } else if (targetFigure.equals("Sec"))
                return getProbByThrownNb(throwNb, (double) 703 / 7776, (double) 10438847 / 60466176);//0.0904 et 0.1726
        }
        return 0;
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

        //TODO: voir si il faut faire la meme chose que ce qui suit pour le calcul de oponentPoints
        if ((tempGame.fullLine(aColor, aBox.getId())) || (aGame.redMarkers - 1 == 0)) {
            if (aGame.redPoints + potentialPoints < aGame.bluePoints)
                return -10;//pour passer le tour
        }
        return potentialPoints;
    }


    // donner un poids à chaque box
    //TODO régler la question EXISTENCIELLE: la proba sur 2 jets ne vaut que si on tente VRAIMENT 2 jets
    //si la sélection se fait sur la proba sur 2 jets (ie: la proba EST le facteur qui fait pencher la balance pour 2 jets),
    // alors faire en sorte que cela ne change pas l'ordre des choix après le 2éme jet (sur 3)
    private void setBoxWeight(Jeu aGame, BoxPair aBoxPair, String aColor){
        String oponentColor="";
        if (aColor.equals("red")) oponentColor="blue";
        else if (aColor.equals("blue")) oponentColor="red";
        Box aBox=aBoxPair.getBox();
        aBoxPair.setPairPoints(getPointsIfMarkerPlacedOnBox(aGame, aColor, aBox));
        aBoxPair.setOponentPoints(getPointsIfMarkerPlacedOnBox(aGame, oponentColor, aBox));
        aBoxPair.setAllPossiblePoints(setAllPossiblePointsAroundBox(aGame, aColor, aBox ));
        aBoxPair.setNextTurnPossiblePoints(getPotentialNextTurnPointsPerBox(aGame, aColor,aBox));
        aBoxPair.setProbability(getBoxProbability(aGame, aBox));
        int endOfGameBonus = setEndOfGameBonus(aGame, "red", aBox) +setEndOfGameBonus(aGame, "blue", aBox);
        aBoxPair.setEndOfGameBonus(endOfGameBonus);
        int bonus=0;
        if (aGame.throwNb<aGame.maxThrowNb)
            bonus += setBrelanBoxBonus(aGame, aBoxPair);
        aBoxPair.setBonus(bonus);
        aBoxPair.setBoxWeight();
    }

    public Boolean manageEndOfGameBonus(Jeu currentGame, ArrayList<BoxPair> bpArrayList){
        //Ne tenter de faire/bloquer le end of game que si la prob est favorable
        // et qu'il n'y a pas d'autre case meilleure
        BoxPair firstBoxPair=bpArrayList.get(bpArrayList.size()-1);
        if (firstBoxPair.getEndOfGameBonus()>0){
            for (int i =0; i<bpArrayList.size(); i++){
                if(bpArrayList.get(i).getProbability()>firstBoxPair.getProbability())
                    if (bpArrayList.get(i).getPairPoints()>firstBoxPair.getPairPoints())
                        if (currentGame.redPoints+bpArrayList.get(i).getPairPoints()>currentGame.bluePoints)
                            firstBoxPair.setEndOfGameBonus(0);
            }
            if (firstBoxPair.getEndOfGameBonus()==0){
                //System.out.println("managed end of game!: bp="+firstBoxPair);
                firstBoxPair.setBoxWeight();
                return true;
            }
        }
        return false;
    }

    public int setAllPossiblePointsAroundBox(Jeu aGame, String aColor, Box aBox){
        Jeu tempGame = new Jeu (aGame);
        int v=aBox.v;
        int h=aBox.h;
        String oponentColor;
        if (aColor.equals("red")) oponentColor="blue";
        else if (aColor.equals("blue")) oponentColor="red";
        else return 0;
        //horizontal
        for (int span=-2; span<3; span++){
            if (checkBoxColorWhithinBound(v, h+span, oponentColor, tempGame))
                continue;
            if (checkBoxColorWhithinBound(v, h+span, "white", tempGame)){
                if (h+span>=0&&h+span<5){
                    tempGame.checkerBox[v][h+span].setColor(aColor);
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
                }
        }
        return tempGame.countLine(3, aColor, aBox.getId());
    }



    //Checks if the coord are within bounds of the board
    private boolean checkBoxColorWhithinBound(int v, int h, String aColor, Jeu aGame) {
        if (((v >= 0) && (v <= 4)) && ((h >= 0) && (h <= 4)))
            return aGame.checkerBox[v][h].getColor().equals(aColor);
        return false;
    }

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

    //Returns points if marker placed on box
    private int getPointsIfMarkerPlacedOnBox(Jeu aGame, String aColor, Box aBox) {
        int boxId=aBox.getId();
        int tmpPoints = 0;
        //copie de currentGame
        Jeu tempGame = new Jeu(aGame);
        if (tempGame.findBoxById(boxId).getColor().equals("white")) {
            tempGame.findBoxById(boxId).setColor(aColor);
            tmpPoints = tempGame.countLine(3, aColor, boxId);
        }
        return tmpPoints;
    }

    // attribuer les bonus négatifs ou positifs en fonction du fulline perdant ou gagnant
    private int setEndOfGameBonus(Jeu aGame, String aColor, Box aBox){
        //Color est toujours rouge mais au cas où ...
        Jeu tmpGame = new Jeu(aGame);
        int boxId=aBox.getId();
        tmpGame.findBoxById(boxId).setColor(aColor);
        int tmpPoints = tmpGame.countLine(3, aColor, boxId);
        int bonus = 0;
        if (aColor.equals("red")){
            if (tmpGame.fullLine("red", boxId)|| (tmpGame.redMarkers-1 ==0)) {
                if (tmpPoints + tmpGame.redPoints > tmpGame.bluePoints) {
                    System.out.println("setEndOfGameBonus1: 20 pour la box "+aBox);
                    appendLog("setEndOfGameBonus1: 20 pour la box "+aBox);
                    bonus= 20;
                } else if (tmpPoints + tmpGame.redPoints < tmpGame.bluePoints) {
                    System.out.println("setEndOfGameBonus1: -20 pour la box "+aBox);
                    appendLog("setEndOfGameBonus1: -20 pour la box "+aBox);
                    bonus= -20;
                }
            }
        }
        else if (aColor.equals("blue")){
            if (tmpGame.fullLine("blue", boxId)||(tmpGame.blueMarkers-1==0)){
                if (tmpPoints+tmpGame.bluePoints> tmpGame.redPoints){
                    System.out.println("setEndOfGameBonus2 : 20 pour la box "+aBox);
                    appendLog("setEndOfGameBonus2: 20 pour la box "+aBox);
                    bonus = 20;
                }
                else if (tmpPoints+tmpGame.bluePoints<tmpGame.redPoints) {
                    //on fait rien pour l'instant
                }
            }
        }
        return bonus;
    }

    //returns a list of optimal next throw free target boxes from a given current figure list
    private ArrayList<Box> getListFreeBox(Jeu aGame) {
        //meme avec 1 seul dé (genre pour tenter brelan qui tue)
        ArrayList<Box> freeBoxList = new ArrayList<>();
        //Stocker les box libres dans une liste
        String [] allFigTypes = {"1", "2", "3", "4", "5", "6", "Appel", "Small", "Full", "Carre", "Yam", "Sec", "Suite"};
        for (String figType: allFigTypes){
            // if (!boxListContains(boxPointList, figType))//Verification superflue
            freeBoxList.addAll(aGame.getListBoxColorPerFigure(figType, "white"));
        }
        return freeBoxList;
    }

    private boolean boxListContains(ArrayList<Box> aBoxArrayList, String figType){
        for (int i =0; i< aBoxArrayList.size(); i++)
            if (aBoxArrayList.get(i).getFigType().equals(figType))
                return true;
        return false;
    }

    /*xxxxxx Methods that aim to optimize strategy xxxxxx*/



    private List<JeuCombinaison> AllCombinationsAvailable(String aColor, Jeu aGame) {
        /*
         * parcourir les possibilités restantes et additionner les points pour chaque possibilité
         * Retourner une liste des meilleures combinaisons
         */
        List<Integer> aBoxPairIdList = getFreeBoxIdList(aGame);
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
        for (int i = 0; i < combSubsets.size(); i++) {
            //utiliser le constructeur de copie pour Jeu
            JeuCombinaison aGameCombination = new JeuCombinaison(new Jeu(aGame), aColor, combSubsets.get(i));
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

    private ArrayList<Box> getFreeBoxList(Jeu aGame) {
        // List<BoxPair> aBoxPairList = new ArrayList<>();
        ArrayList <Box> aBoxList = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++) {
                if (aGame.checkerBox[i][j].getColor().equals("white")) {
                    aBoxList.add(aGame.checkerBox[i][j]);
                }
            }
        return aBoxList;
    }

    private ArrayList<Box> getBestUltimateBox(Jeu aGame) {
        List<JeuCombinaison> jcRedList = AllCombinationsAvailable("red", aGame);

      /*
        int maxRedPointsPossible = 0;
        if (!jcRedList.isEmpty())
            maxRedPointsPossible = jcRedList.get(jcRedList.size() - 1).getPoints();
        List<JeuCombinaison> jcBlueList = AllCombinationsAvailable("blue", aGame);
        int maxBluePointsPossible = 0;
        if (!jcBlueList.isEmpty())
            maxBluePointsPossible = jcBlueList.get(jcBlueList.size() - 1).getPoints();
       */

        ArrayList<Box> noDuplicatesBoxArrayList = new ArrayList<>();
        List<JeuCombinaison> bestjclist = new ArrayList<>();
        //Chercher les listes de combinaisons gagnantes
        for (int i = 0; i < jcRedList.size(); i++) {
            if (jcRedList.get(i).getPoints() + aGame.redPoints > aGame.bluePoints)
                bestjclist.add(jcRedList.get(i));
        }
        //Si pas de listes gagnantes chercher celles qui égalent l'adversaire
        if (bestjclist.isEmpty()) {
            for (int i = 0; i < jcRedList.size(); i++)
                if (jcRedList.get(i).getPoints() + aGame.redPoints == aGame.bluePoints)
                    bestjclist.add(jcRedList.get(i));
        }
        //Sinon on ajoute les perdantes, on prendra celle qui perd le moins
        if (bestjclist.isEmpty()) {
            bestjclist.addAll(jcRedList);
        }

        //Bidouille pour retirer les doublons
        ArrayList<BoxPair> noDuplicatesBoxPairArrayList = new ArrayList<>();
        ArrayList<Integer> tempNoDuplicatesBoxPairIdArrayList = new ArrayList<>();
        for (int i = 0; i < bestjclist.size(); i++)
            for (int j = 0; j < bestjclist.get(i).getBoxPairCombinationArrayList().size(); j++) {
                if (!tempNoDuplicatesBoxPairIdArrayList.contains(bestjclist.get(i).getBoxPairCombinationArrayList().get(j).getPairId())) {
                    tempNoDuplicatesBoxPairIdArrayList.add(bestjclist.get(i).getBoxPairCombinationArrayList().get(j).getPairId());
                    noDuplicatesBoxPairArrayList.add(bestjclist.get(i).getBoxPairCombinationArrayList().get(j));
                }
            }
        Collections.sort(noDuplicatesBoxPairArrayList);
        for (int i =0; i< noDuplicatesBoxPairArrayList.size(); i++)
            noDuplicatesBoxArrayList.add(noDuplicatesBoxPairArrayList.get(i).getBox());

        return noDuplicatesBoxArrayList;
    }


    private String machineChoseFromDices(){
        if (mainActivity.logFlag){
            if (currentGame.throwNb==1){
                appendLog(gameStateToString());
                System.out.println(gameStateToString());
            }
            appendLog(currentGame.printSelectedDice());
            System.out.println(currentGame.printSelectedDice());
        }

        if (currentGame.throwNb < currentGame.maxThrowNb)
            if (currentGame.appelClicked)
                if ((currentGame.throwNb==2) && (!currentGame.fiveDices.figureList.equals("Appel"))){
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "Appel";
                }

        //TODO gérer le cas où il n'y a pas de combinaison gagnante pour les rouges
        //Recupérer les box libres
        ArrayList<Box> aBoxList;
        //TODO gérer le cas où il y a beaucoup de possibilités pour les red et que les bleus sont <5
        if ((currentGame.redMarkers < 5) || (currentGame.blueMarkers < 5))
            aBoxList = getBestUltimateBox(currentGame); //Normalement jamais vide
        else aBoxList = getListFreeBox(currentGame);//Normalement jamais vide
        //si thrownb==3 alors n'inclure dans la liste que les box ou on a une figure posable
        if (currentGame.throwNb==currentGame.maxThrowNb){
            String currentFigureList = currentGame.fiveDices.figureList;
            Iterator<Box> it = aBoxList.iterator();
            while (it.hasNext()){
                Box nextBox = it.next();
                if (!currentFigureList.contains(nextBox.getFigType()))
                    it.remove();
            }
        }
        //les stocker dans une liste de boxPairs
        ArrayList<BoxPair> nextBoxPairList = new ArrayList<>();
        for (int i = 0; i < aBoxList.size(); i++)
            nextBoxPairList.add(new BoxPair(aBoxList.get(i),0,  0));

        //Leur donner leur poids
        for (int i = 0; i<nextBoxPairList.size(); i++)
            setBoxWeight(currentGame, nextBoxPairList.get(i), "red");
        //Trier
        if (!nextBoxPairList.isEmpty()){
            Collections.sort(nextBoxPairList);
            //manageEndOfGameBonus
            if (manageEndOfGameBonus(currentGame, nextBoxPairList))
                Collections.sort(nextBoxPairList);
            //si endOfGameBonus=-20, on enlève les cases qui feraient perdre la machine
            for (int i =0; i<nextBoxPairList.size(); i++)
            if (nextBoxPairList.get(i).getEndOfGameBonus()<0){
                System.out.println("remove boxPair: "+nextBoxPairList.get(i));
                appendLog("remove boxPair: "+nextBoxPairList.get(i));
                nextBoxPairList.remove(i);
            }
        }
        if (mainActivity.logFlag){
            //Afficher
            System.out.println("nextBoxPairList");
            System.out.println("Box         BW (Pts pb NTPP APP OPts B EGB)");
            System.out.println(nextBoxPairList);
            appendLog("nextBoxPairList");
            appendLog("Box BW (Pts pb NTPP APP OPts B EGB)");
            appendLog(nextBoxPairList.toString());
        }

        Box optimalBox = new Box();
        if (!currentGame.appelClicked){
            if (!nextBoxPairList.isEmpty())
                optimalBox= nextBoxPairList.get(nextBoxPairList.size()-1).getBox();
        }
        else{
            optimalBox=appelBox;
        }
        if (currentGame.throwNb==currentGame.maxThrowNb)
            if (currentGame.appelClicked && !currentGame.fiveDices.figureList.equals("Appel"))
                checkForMissedAppel(currentGame);
        if (mainActivity.logFlag) {
            System.out.println("optimalBox:");
            System.out.println("Box BW Pts pb NTPP APP OPts B EGB");
            System.out.println(optimalBox);
            if (optimalBox.getFigType().equals("Appel"))
                System.out.println("Figure appelée: "+machineFigureAppel);

            appendLog("optimalBox");
            appendLog("Box BW Pts pb NTPP APP OPts B EGB");
            appendLog(optimalBox.toString());
            if (optimalBox.getFigType().equals("Appel"))
                appendLog("Figure appelée: "+machineFigureAppel);
        }
        if (optimalBox.getId()!=0){
            //Si la meilleure box correspond à la figure obtenue
            if (currentGame.fiveDices.figureList.contains(optimalBox.getFigType()))
                //Alors poser un pion dessus (y compris case appel)
                //if (optimalBox.)
                return machinePlaceMarkerById(optimalBox.getId());
                //sinon tenter cette box
            else if (currentGame.throwNb<currentGame.maxThrowNb){
                showTargetUI(optimalBox.getId());
                return optimalBox.getFigType();
            }
        }

        //Si pas de box et qu'on veut qd même relancer (en gros la machine a déjà perdu de toute façons, plus moyen de gagner)
        if (currentGame.throwNb<currentGame.maxThrowNb)
            return "Sec";
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

    private void selectForFull(Jeu aGame) {
        aGame.fiveDices.selectForFull();
    }

    private void selectForCarre(Jeu aGame) {
        int singleton;
        if (aGame.fiveDices.figureList.matches(".*([123456]).*"))
        {
            int valBrelan = aGame.fiveDices.getBrelanValue();
            if (valBrelan>0) {
                //TODO voir si on privilégie la tentative de carre à partir d'un brelan non posable
                //ou bien si on tente a partir d'une paire/singleton->brelan posable
                //ArrayList<BoxPair> pairs = new ArrayList<>();
                //pairs.addAll(aGame.getListBoxPairColorPerFigure(Integer.toString(valBrelan), "white"));
                //if (pairs.size() > 0)
                    selectForBrelan(aGame, valBrelan);
            }
        }
        else if (aGame.fiveDices.figureContainsPair()){
            if (aGame.fiveDices.figureContainsDoublePair()){
                int bestBrelanAvailable=getBestBrelanAvailableFromDoublePair(aGame);
                if (bestBrelanAvailable>0)
                    aGame.fiveDices.selectForBrelan(getBestBrelanAvailableFromDoublePair(aGame));
                else
                    aGame.fiveDices.selectForBrelan(getFirstAvailablePairValue(aGame));
            }
            else
                aGame.fiveDices.selectForBrelan(getFirstAvailablePairValue(aGame));
        }
        else if ((singleton = getBestBrelanAvailableFromSingleton(aGame))!=0)
            aGame.fiveDices.selectForBrelan(singleton);
        else
            aGame.fiveDices.selectForCarre();
    }

    private int getBestBrelanAvailableFromSingleton(Jeu aGame){
        //System.out.println("getBestBrelanAvailableFromSingleton!");
        ArrayList <Integer> singletonArrayList = new ArrayList<>();
        for (int i =0; i<5; i++){
            int diceValue=aGame.fiveDices.getDiceValue(i);
            if (!singletonArrayList.contains(diceValue))
                singletonArrayList.add(diceValue);
        }
        ArrayList<BoxPair>  pairs = new ArrayList<>();
        for (int i =0; i< singletonArrayList.size(); i++){
            pairs.addAll(aGame.getListBoxPairColorPerFigure(Integer.toString(singletonArrayList.get(i)), "white"));
        }
        //choisir le meilleur brelan fallback
        for (int i =0; i<pairs.size(); i++){
            pairs.get(i).setPairPoints(getPointsIfMarkerPlacedOnBox(aGame, "red", pairs.get(i).getBox()));
            pairs.get(i).setOponentPoints(getPointsIfMarkerPlacedOnBox(aGame, "blue", pairs.get(i).getBox()));
            pairs.get(i).setAllPossiblePoints(setAllPossiblePointsAroundBox(aGame, "red", pairs.get(i).getBox() ));
            pairs.get(i).setNextTurnPossiblePoints(getPotentialNextTurnPointsPerBox(aGame, "red",pairs.get(i).getBox()));
            int bonus = setEndOfGameBonus(aGame, "red", pairs.get(i).getBox()) +setEndOfGameBonus(aGame, "blue", pairs.get(i).getBox());
            if (aGame.throwNb<aGame.maxThrowNb)
                bonus += setBrelanBoxBonus(aGame, pairs.get(i));
            pairs.get(i).setBonus(bonus);
            pairs.get(i).setBoxWeight();
        }
        Collections.sort(pairs);
        if (pairs.size()>0){
            return Integer.valueOf(pairs.get(pairs.size()-1).getFigType());
        }
        else return 0;
    }

    private void selectForYam(Jeu aGame) {
        //System.out.println("selectForYam1");
        if (! aGame.fiveDices.figureList.contains("Yam")){
            //System.out.println("selectForYam2");
            if (aGame.fiveDices.figureList.contains("Carre")) {
                //System.out.println("selectForYam3");
                for (int i = 0; i < 5; i++)
                    aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[i][0]].isSelected = false;
                if (aGame.fiveDices.tempDiceSetIndValues[0][1] == aGame.fiveDices.tempDiceSetIndValues[3][1])
                    aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[4][0]].isSelected = true;
                else
                    aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[0][0]].isSelected = true;
            }
            else if (aGame.fiveDices.figureList.matches( ".*([123456]).*")){
                //System.out.println("selectForYam4");
                selectForCarre(aGame);
            }
            else  if (aGame.fiveDices.figureContainsPair()){
                //System.out.println("selectForYam5");
                if (figureContainsDoublePair(aGame)){
                    //System.out.println("selectForYam6");
                    selectForBrelan(aGame, getBestBrelanAvailableFromDoublePair(aGame));
                }
                else {
                    //System.out.println("selectForYam7");
                    selectForBrelan(aGame, getFirstAvailablePairValue(aGame));
                }
            }
        }
        //Sinon c'est qu'on tente l'appel au yam
        else {
            //System.out.println("selectForYam1");
            aGame.fiveDices.diceSet[aGame.fiveDices.tempDiceSetIndValues[4][0]].isSelected = true;
        }
    }

    private int getBestBrelanAvailableFromDoublePair(Jeu aGame){
        //System.out.println("getBestBrelanAvailableFromDoublePair");
        int valIdx1 = aGame.fiveDices.tempDiceSetIndValues[1][1];
        int valIdx3 = aGame.fiveDices.tempDiceSetIndValues[3][1];
        ArrayList<BoxPair>  pairs = new ArrayList<>();
        pairs.addAll(aGame.getListBoxPairColorPerFigure(Integer.toString(valIdx1), "white"));
        pairs.addAll(aGame.getListBoxPairColorPerFigure(Integer.toString(valIdx3), "white"));
        //choisir le meilleur brelan fallback
        for (int i =0; i<pairs.size(); i++){
            pairs.get(i).setPairPoints(getPointsIfMarkerPlacedOnBox(aGame, "red", pairs.get(i).getBox()));
            pairs.get(i).setBoxWeight();
            //System.out.println(pairs.get(i).getBox()+": "+getPointsIfMarkerPlacedOnBox(aGame, "red", pairs.get(i).getBox())+" points");
        }
        Collections.sort(pairs);
        if (pairs.size()>0)
            return Integer.valueOf(pairs.get(pairs.size()-1).getFigType());
        else return 0;
    }

    private void selectForSmall(Jeu aGame) {
        aGame.fiveDices.selectForSmall();
    }

    private void selectForSuite(Jeu aGame){
        aGame.fiveDices.selectForSuite();
    }

    private void selectForSec(Jeu aGame){
        aGame.fiveDices.selectForSec();
    }

    private void selectForBrelan(Jeu aGame, int value){
        aGame.fiveDices.selectForBrelan(value);
    }

    private void selectForAppel(Jeu aGame){
        if (aGame.throwNb==2){
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
            String appelFigure= machineFigureAppel;
            switch (appelFigure){
                case "Full":
                    selectForFull(aGame);
                    break;
                case "Carre":
                    selectForCarre(aGame);
                    break;
                case "Suite":
                    selectForSuite(aGame);
                    break;
                case "Small":
                    selectForSmall(aGame);
                    break;
            }
            appel(aGame, appelFigure);
        }
    }
    //Pour Brelan  carre Full Small Yam  + Appel
    private boolean figureContainsSingleValue(Jeu aGame, int value){
        return aGame.fiveDices.figureContainsSingleValue(value);
    }


    private boolean figureContainsPair(Jeu aGame){
        return aGame.fiveDices.figureContainsPair();
    }

    private int getFirstAvailablePairValue(Jeu aGame){
        return aGame.fiveDices.getFirstAvailablePairValue();
    }

    private boolean figureContainsSinglePair(Jeu aGame){
        return aGame.fiveDices.figureContainsSinglePair();
    }

    private int getSinglePairValue(Jeu aGame) {
        return aGame.fiveDices.getSinglePairValue();
    }

    //Choisir SOIT 1ere paire SOIT 2nde paire
    private int getPairValues(Jeu aGame, boolean firstPair, boolean secondPair){
        return aGame.fiveDices.getPairValues(firstPair, secondPair);
    }

    private void selectFromSinglePair(Jeu aGame){
        aGame.fiveDices.selectFromSinglePair();
    }

    private boolean figureContainsDoublePair(Jeu aGame){
        return aGame.fiveDices.figureContainsDoublePair();
    }

    private void selectfromDoublePair(Jeu aGame){
        aGame.fiveDices.selectfromDoublePair();
    }
    //4 à la suite
    private int getIdxFrom4inARow(Jeu aGame){
        int idx = aGame.fiveDices.getMissingIdxForSuite(0);
        if (idx==-1)
            idx=aGame.fiveDices.getMissingIdxForSuite(1);
        return idx;
    }


    /*Deal with appel*/
    //Checks if the appel is missed and resets flags
    private void checkForMissedAppel(Jeu aGame){
        if (mainActivity.logFlag){
            System.out.println("checkForMissedAppel");
            appendLog("checkForMissedAppel");
        }
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
        if (aGame.throwNb==1){
            //Arbitraire: voir si on peut récupérer la case appel souhaitée par la machine
            //final int appelBoxId = aGame.checkerBox[0][2].getId();
            final int appelBoxId = appelBox.getId();
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
                        // System.out.println("tempAppelBoxFigTypeId1: "+tempAppelBoxFigTypeId);
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
                        mainActivity.UI_setDiceScale(diceId, "small");
                        mainActivity.UI_bounceImageView(diceId);
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
                        mainActivity.UI_setDiceScale(diceId,"big");
                        mainActivity.UI_bounceImageView(diceId);
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


    public void appendLog(String text)
    {
        Context context = mainActivity.getApplicationContext();
        File path= context.getExternalFilesDir(null);
        File logFile = new File(path, "YatzoeLog"+currentGame.dateFormat+".txt");

        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (FileNotFoundException e) {
            System.out.println(e);
        }
        catch (IOException e) {
            Log.e("Exception", "File write raté: " + e.toString());
            e.printStackTrace();
        }
    }


    public String gameStateToString(){
        String gameState="";
        if (currentGame.throwNb==1){
            gameState+="\nbleus: "+currentGame.blueMarkers+" "+currentGame.bluePoints+"  rouges: "+currentGame.redMarkers+" "+currentGame.redPoints;
            for (int ligne=0; ligne<5; ligne++)
            {
                gameState+="\n|";
                for (int col=0; col<5; col++)
                {
                    if (currentGame.checkerBox[ligne][col].getColor().equals("blue"))
                        gameState+="b";
                    else if (currentGame.checkerBox[ligne][col].getColor().equals("red"))
                        gameState+="r";
                    else
                        gameState+=" ";
                }
                gameState+="|";
            }
        }
        if (currentGame.throwNb==2 && currentGame.appelClicked)
            gameState+="\nappel: "+currentGame.appelRegistered;
        return gameState;
    }
}
