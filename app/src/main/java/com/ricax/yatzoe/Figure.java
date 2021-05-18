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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Figure {
    private boolean hasAFigure;
    private final boolean appel; //If the human player pressed on any appel box or the machine
    public Dice[] diceSet;
    final int [][] tempDiceSetIndValues;
    private final int dicesetLength;

    //       figureList can contain: brelan (1, 2, 3, 4, 5 or 6), carre, suite, full, yam, small, appel, sec
    String figureList = "";

    //constructors
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
        for (int i=0; i<5; i++)
            diceSet[i]=new Dice(aFigure.diceSet[i]);
        tempDiceSetIndValues=new int[aFigure.diceSet.length][2];
        for (int i=0; i<aFigure.diceSet.length;i++)
            System.arraycopy(aFigure.tempDiceSetIndValues[i], 0, tempDiceSetIndValues[i], 0, 2);
    }

    public int getDiceValue(int i){
        return this.diceSet[i].value;
    }

    public void setDiceSet(Dice[] aDiceset){
        this.diceSet=aDiceset;
    }

    public void setDice(Dice aDice, int idx){
        this.diceSet[idx]=aDice;
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
        figureList="";
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

    public String printDiceSet(){
        String des = "\n";
        for (int i =0; i<dicesetLength; i++)
            des+= diceSet[i].value +" ";
        return des;
    }

    @Override
    public String toString(){
        String des = "\n"+this.figureList+"\n";
        for (int i =0; i<dicesetLength; i++)
            des+= diceSet[i].value +" ";
        des+="\n";
        for (int i =0; i<dicesetLength; i++){
           if (diceSet[i].isSelected)
            des+= "x ";
           else des+="o ";
        }
        return des;
    }

    public int getMissingIdxForSuite(int inc){
        //inc=0 -> petite suite or 1 -> grande suite
        List<Integer> listDiceIdx= new ArrayList<>();
        for  (int value=1+inc; value<=5+inc; value++)  {
            for (int i=0; i<5; i++){
                if ((this.tempDiceSetIndValues[i][1]==value)){
                    listDiceIdx.add(this.tempDiceSetIndValues[i][0]);
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

    public int figureContains4InARow(){
        int idx1 = getMissingIdxForSuite(0);
        int idx2 = getMissingIdxForSuite(1);
        if ((idx1!=-1) && (idx2!=-1)){
            return 2;//Manquent 1 OU 6 (on a 2345)
        }
        if ((idx1!=-1) || (idx2!=-1)){
            return 1;
        }
        return 0;
    }

    public int getIdxFrom4inARow(){
        int idx = getMissingIdxForSuite(0);
        if (idx==-1)
            idx=getMissingIdxForSuite(1);
        return idx;
    }

    public void selectForSuite(){
        if (!figureList.contains("Suite")){
            int idx= getIdxFrom4inARow();
            for (int i =0; i<5; i++)
               diceSet[i].isSelected=false;
            diceSet[idx].isSelected=true;
        }
        else {
            //Appel à la suite à partir d'une suite, on tente de partir d'une suite bilatérale
            for (int i =0; i<5; i++)
                diceSet[i].isSelected=false;
            if (tempDiceSetIndValues[0][1]==1)
                diceSet[tempDiceSetIndValues[0][0]].isSelected=true;
            else
                diceSet[tempDiceSetIndValues[4][0]].isSelected=true;
        }
    }

    public void selectForSec(){
        for (int i =0; i<5; i++)
            diceSet[i].isSelected=true;
    }

    public void selectForBrelan(int value){
        for (int i =0; i<5; i++)
            diceSet[i].isSelected= diceSet[i].value != value;
    }

    public void selectForSmall() {
        if (!figureList.contains("Small")){

            //Select dice so that (sum of 1s & 2s) < 5
            int sum=0;
            for (int i=0; i<5; i++)
                diceSet[tempDiceSetIndValues[i][0]].isSelected = true;
            for (int i = 0; i<5; i++){
                if (diceSet[tempDiceSetIndValues[i][0]].value==1){
                    diceSet[tempDiceSetIndValues[i][0]].isSelected = false;
                    sum+= diceSet[tempDiceSetIndValues[i][0]].value;
                }
            }
            for (int i = 0; i<5; i++){
                if (diceSet[tempDiceSetIndValues[i][0]].value==2){
                    diceSet[tempDiceSetIndValues[i][0]].isSelected = false;
                    if (sum+diceSet[tempDiceSetIndValues[i][0]].value<6)
                        sum+= diceSet[tempDiceSetIndValues[i][0]].value;
                }
            }


        }
        else {
            //On a déjà un small, on tente l'appel en relançant le dé le +grand
            diceSet[tempDiceSetIndValues[4][0]].isSelected=true;
        }
    }

    public void selectForFull() {
        if (!figureList.contains("Full")){
            if (figureContainsDoublePair()) {
                selectfromDoublePair();
            }
            //Traiter brelan
            else if (figureList.matches( ".*([123456]).*")){
                for (int i = 0; i < 5; i++)
                    diceSet[i].isSelected = true;
                if (tempDiceSetIndValues[0][1] == tempDiceSetIndValues[2][1]) {
                    for (int j = 0; j < 3; j++)
                        diceSet[
                                tempDiceSetIndValues[j][0]
                                ].isSelected = false;
                } else if (tempDiceSetIndValues[1][1] == tempDiceSetIndValues[3][1]) {
                    for (int j = 1; j < 4; j++)
                        diceSet[
                                tempDiceSetIndValues[j][0]
                                ].isSelected = false;
                } else if (tempDiceSetIndValues[2][1] == tempDiceSetIndValues[4][1]) {
                    for (int j = 2; j < 5; j++)
                        diceSet[
                                tempDiceSetIndValues[j][0]
                                ].isSelected = false;
                }
            }
            //traiter paire
            else if (figureContainsPair()) {
                selectFromSinglePair();
            }
        }
        else {
            //On a un full et on tente l'appel au full en relançant le premier (ou dernier) du brelan (cas où on trie les dés je me comprend)
            diceSet[tempDiceSetIndValues[2][0]].isSelected=true;
        }
    }

    public void selectForCarre() {
        if (!figureList.contains("Carre")){
            if (figureList.matches( ".*([123456]).*")){
                for (int i = 0; i < 5; i++)
                    diceSet[i].isSelected = true;
                if (tempDiceSetIndValues[0][1] == tempDiceSetIndValues[2][1]) {
                    for (int j = 0; j < 3; j++)
                        diceSet[
                                tempDiceSetIndValues[j][0]
                                ].isSelected = false;
                } else if (tempDiceSetIndValues[1][1] == tempDiceSetIndValues[3][1]) {
                    for (int j = 1; j < 4; j++)
                        diceSet[
                                tempDiceSetIndValues[j][0]
                                ].isSelected = false;
                } else if (tempDiceSetIndValues[2][1] == tempDiceSetIndValues[4][1]) {
                    for (int j = 2; j < 5; j++)
                        diceSet[
                                tempDiceSetIndValues[j][0]
                                ].isSelected = false;
                }
            }
            //Traiter les paires
            else if (figureContainsSinglePair())
                selectFromSinglePair();
            //pas besoin de traiter le full car si full alors brelan
        }
        else {
            //On a un carre et on tente l'appel au carre
            if (figureList.contains("Yam")){
                //relancer le 5e dé
                diceSet[4].isSelected=true;
            }
            else{
                //on a un carre et on tente l'appel au carre, relancer le dé qui n'est pas ds la carré ;-) cela inclus le cas du Yam
                if (tempDiceSetIndValues[0][1]== tempDiceSetIndValues[1][1])
                    diceSet[tempDiceSetIndValues[4][0]].isSelected=true;
                else
                    diceSet[tempDiceSetIndValues[0][0]].isSelected=true;
            }
        }
    }

    public void selectfromDoublePair(){
        for (int i =0; i<5; i++)
            diceSet[i].isSelected=false;
        if ((tempDiceSetIndValues[0][1]==tempDiceSetIndValues[1][1]) &&
                (tempDiceSetIndValues[2][1]==tempDiceSetIndValues[3][1])){
            diceSet[tempDiceSetIndValues[4][0]].isSelected=true;
        }
        else if ((tempDiceSetIndValues[1][1]==tempDiceSetIndValues[2][1]) &&
                (tempDiceSetIndValues[3][1]==tempDiceSetIndValues[4][1])){
            diceSet[tempDiceSetIndValues[0][0]].isSelected=true;
        }
        else if ((tempDiceSetIndValues[0][1]==tempDiceSetIndValues[1][1]) &&
                (tempDiceSetIndValues[3][1]==tempDiceSetIndValues[4][1])){
            diceSet[tempDiceSetIndValues[2][0]].isSelected=true;
        }
    }

    public boolean figureContainsDoublePair(){
        if ((tempDiceSetIndValues[0][1]==tempDiceSetIndValues[1][1]) &&
                (tempDiceSetIndValues[2][1]==tempDiceSetIndValues[3][1])){
            return true;
        }
        else if ((tempDiceSetIndValues[1][1]==tempDiceSetIndValues[2][1]) &&
                (tempDiceSetIndValues[3][1]==tempDiceSetIndValues[4][1])){
            return true;
        }
        else if ((tempDiceSetIndValues[0][1]==tempDiceSetIndValues[1][1]) &&
                (tempDiceSetIndValues[3][1]==tempDiceSetIndValues[4][1])){
            return true;
        }
        return false;
    }

    public void selectFromSinglePair(){
        for (int i =0; i<5; i++)
            diceSet[i].isSelected=true;
        for(int i = 0; i <4; i++)
            if (tempDiceSetIndValues[i][1]==tempDiceSetIndValues[i+1][1]){
                for (int j=i+2; j<4; j++)
                    if (tempDiceSetIndValues[j][1]==tempDiceSetIndValues[j+1][1])
                        return;//if we find another pair
                diceSet[tempDiceSetIndValues[i][0]].isSelected=false;
                diceSet[tempDiceSetIndValues[i+1][0]].isSelected=false;
                return;
            }
    }

    public boolean figureContainsPair(){
        for(int i = 0; i <4; i++)
            if (tempDiceSetIndValues[i][1]==tempDiceSetIndValues[i+1][1])
                return true;
        return false;
    }

    public boolean figureContainsSinglePair(){
        return figureContainsPair() && !figureContainsDoublePair();
    }

    public int getSinglePairValue() {
        if (figureContainsSinglePair()){
            for (int i = 0; i < 4; i++)
                if (tempDiceSetIndValues[i][1] == tempDiceSetIndValues[i + 1][1])
                    return tempDiceSetIndValues[i][1];
        }
        return 0;
    }
    //Choisir SOIT 1ere paire SOIT 2nde paire
    public int getPairValues(boolean firstPair, boolean secondPair){
        if (firstPair){
            if (!figureContainsDoublePair())
                return getSinglePairValue();
            else
                return tempDiceSetIndValues[1][1];
        }
        else if (secondPair){
            if (figureContainsDoublePair())
                return tempDiceSetIndValues[3][1];
        }
        return 0;
    }

    public int getFirstAvailablePairValue(){
        for(int i = 0; i <4; i++)
            if (tempDiceSetIndValues[i][1]==tempDiceSetIndValues[i+1][1])
                return tempDiceSetIndValues[i][1];
        return 0;
    }
    //Pour Brelan  carre Full Small Yam  + Appel
    public boolean figureContainsSingleValue(int value){
        int count = 0;
        for (int i =0; i<5; i++){
            if (tempDiceSetIndValues[i][1]==value )
                count++;
        }
        if (count == 1)
            return true;
        else
            return false;
    }

}
