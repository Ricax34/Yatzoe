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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Figure {
    private boolean hasAFigure;
    private final boolean appel; //If the human player pressed on any appel box or the machine
    final Dice[] diceSet;
    final int [][] tempDiceSetIndValues;
    private int dicesetLength;

    //       figureList can contain: brelan (1, 2, 3, 4, 5 or 6), carre, suite, full, yam, small, appel, sec
    String figureList = "";
    //constructors
    Figure(int idDice0, int idDice1, int idDice2, int idDice3, int idDice4){
        hasAFigure = false;
        appel = false;
        diceSet = new Dice[5];
        tempDiceSetIndValues = new int[5][2];
        for (int i = 0; i < 5; i++) {
            diceSet[i] = new Dice();
            diceSet[i].setDice(false, 0);
            diceSet[i].index = i;
        }
        diceSet[0].id=idDice0;
        diceSet[1].id=idDice1;
        diceSet[2].id=idDice2;
        diceSet[3].id=idDice3;
        diceSet[4].id=idDice4;
    }

    Figure (int [] aDiceset){
        hasAFigure = false;
        appel = false;
        dicesetLength=aDiceset.length;
        diceSet = new Dice[dicesetLength];
        tempDiceSetIndValues = new int[aDiceset.length][2];
        for (int i = 0; i < aDiceset.length; i++) {
            diceSet[i] = new Dice();
            diceSet[i].setDice(false, 0);
            diceSet[i].index = i;
            diceSet[i].id=aDiceset[i];
        }
    }
    //Constructeur de copie
    Figure(Figure aFigure){
        figureList=aFigure.figureList;
        hasAFigure=aFigure.hasAFigure;
        appel=aFigure.appel;
        diceSet=new Dice[aFigure.diceSet.length];
        dicesetLength=aFigure.dicesetLength;
        //diceSet=new Dice[5];
        for (int i=0; i<5; i++)
            diceSet[i]=new Dice(aFigure.diceSet[i]);
        tempDiceSetIndValues=new int[aFigure.diceSet.length][2];
        for (int i=0; i<aFigure.diceSet.length;i++)
            System.arraycopy(aFigure.tempDiceSetIndValues[i], 0, tempDiceSetIndValues[i], 0, 2);
    }

    private void sortDoubleArray(int[][] aDoubleArray)
    {
        int n = aDoubleArray.length;
        // One by one move boundary of unsorted subarray
        for (int i = 0; i < n-1; i++)
        {
            // Find the minimum element in unsorted array
            int min_idx = i;
            for (int j = i+1; j < n; j++)
                if (aDoubleArray[j][1] < aDoubleArray[min_idx][1])
                    min_idx = j;

            // Swap the found minimum element with the first element
            int[] temp;
            temp = new int[2];

            temp[0]=aDoubleArray[min_idx][0];
            temp [1]= aDoubleArray[min_idx][1];
            aDoubleArray[min_idx][0] = aDoubleArray[i][0];
            aDoubleArray[min_idx][1] = aDoubleArray[i][1];
            aDoubleArray[i][0] = temp[0];
            aDoubleArray[i][1] = temp[1];
        }
    }
    //set list of figures obtained from the diceSet
    void setListOfFiguresFromDiceSet(){
        //populate tempDiceSetIndValues with the result of the throw
        for (int i=0; i<dicesetLength; i++) {
            this.tempDiceSetIndValues[i][0] = diceSet[i].index;
            this.tempDiceSetIndValues[i][1] = diceSet[i].value;
        }
        //sort tempDiceSetIndValues
        sortDoubleArray(this.tempDiceSetIndValues);

        //check for figures and add the results to figureList
        figureList += checkForYam();
        figureList += checkForFull();
        figureList += checkForCarre();
        figureList += checkForSmall();
        figureList += checkForSuite();
        figureList += checkForSec();
        figureList += checkForBrelan();
        hasAFigure= !figureList.isEmpty();
    }

    private String checkForFull(){
        if (this.dicesetLength==5)
            if(
                    ((this.tempDiceSetIndValues[0][1] == this.tempDiceSetIndValues [2][1])
                            && (this.tempDiceSetIndValues[3][1] == this.tempDiceSetIndValues [4][1])) ||
                            ((this.tempDiceSetIndValues[0][1] == this.tempDiceSetIndValues [1][1])
                                    && (this.tempDiceSetIndValues[2][1] == this.tempDiceSetIndValues [4][1]))
            )
                return "Full";
        return "";
    }

    private String checkForSmall() {
        int sumOfus = 0;
        for (int i = 0; i<dicesetLength; i++)
            sumOfus += this.tempDiceSetIndValues[i][1];
        if (sumOfus < 9)
            return "Small";
        return "";
    }

    private String checkForCarre(){
        if((this.tempDiceSetIndValues[0][1] == this.tempDiceSetIndValues [3][1])||(this.tempDiceSetIndValues[1][1] == this.tempDiceSetIndValues [4][1]))
            return  "Carre";
        else
            return "";
    }

    private String checkForSuite(){
        if (this.dicesetLength==5){
            for (int i = 0; i <this.dicesetLength-1; i++) {
                if (this.tempDiceSetIndValues[i][1]+1 != this.tempDiceSetIndValues[i+1][1])
                    return "";
            }
            return "Suite";
        }
        return "";
    }

    private String checkForYam(){
        if (this.dicesetLength==5)
            if(this.tempDiceSetIndValues[0][1] == this.tempDiceSetIndValues [dicesetLength-1][1])
                return  "Yam";
            else
                return "";
        else return "";
    }

    private String checkForSec(){
        //Execution AFTER all the checkFor... above
        if (this.dicesetLength==5){
            for (int i = 0; i<5; i++)
                if (!this.diceSet[i].selected()){
                    return "";
                }
            if (figureList.matches(".*(Small|Carre|Suite|Yam|Full).*"))
                return "Sec";
            else return "";
        }
        else return "";
    }

    public String checkForBrelan(){
        if (dicesetLength==5
                &&(     (this.tempDiceSetIndValues[0][1] == this.tempDiceSetIndValues [2][1])||
                        (this.tempDiceSetIndValues[1][1] == this.tempDiceSetIndValues [3][1])||
                        (this.tempDiceSetIndValues[2][1] == this.tempDiceSetIndValues [4][1])
                )
        )
            return String.valueOf(this.tempDiceSetIndValues[2][1]);
        return "";
    }
//TODO finir ça pour selectionner la meillure sélection de dé pour le small
    public int getSortedDiceSetSum(int nbOfDice){
        int sum=0;
        if (nbOfDice<dicesetLength){
            int [][] tempSortedDiceset = new int [dicesetLength][2];
            for (int i=0; i<dicesetLength; i++){
                tempSortedDiceset[i][0] = diceSet[i].index;
                tempSortedDiceset[i][1] = diceSet[i].value;
            }
            sortDoubleArray(tempSortedDiceset);
            for (int i =0; i<nbOfDice; i++)
                sum+=tempSortedDiceset[i][1];
        }
        return sum;
    }
    public String printDiceSet(){
        String des = "";
        for (int i =0; i<dicesetLength; i++)
            des+= diceSet[i].value +" ";
        return des;
    }
    public Dice getDice(int idx){
        return diceSet[idx];
    }
}
