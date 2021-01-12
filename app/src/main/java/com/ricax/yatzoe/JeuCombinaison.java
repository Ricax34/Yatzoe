package com.ricax.yatzoe;

import java.util.ArrayList;

public class JeuCombinaison implements Comparable<JeuCombinaison>{
    Jeu aGame;
    private int combinationPoints = 0;
    private int [] idCombination;
    private ArrayList<Integer> idCombinationArrayList;
    private String aColor;

    public JeuCombinaison(Jeu aGame, String aColor, int [] idCombination){
        this.aGame = aGame;
        this.aColor = aColor;
       this.idCombination = idCombination;
        //this.combinationPoints=this.computeCombinationPoints();
        this.idCombinationArrayList=this.setIdCombinationArrayList(idCombination);
        this.combinationPoints=this.computeCombinationPointsArrayList();
    }

//tranforms the array into an ArrayList
    private ArrayList<Integer> setIdCombinationArrayList(int [] idCombination){
        ArrayList<Integer> anArrayListInteger = new ArrayList<>();
        for (int value : idCombination) anArrayListInteger.add(value);
        return anArrayListInteger;
    }

    private int computeCombinationPoints(){
        int points=0;
        for (int i = 0; i< idCombination.length; i++){
            if (aGame.fullLine(aColor, idCombination[i]) && i != idCombination.length - 1) {
                //swap with next in array
                int tempId = idCombination[i];
                idCombination[i] = idCombination[i + 1];
                idCombination[i + 1] = tempId;
            }
            aGame.findBoxById(idCombination[i]).color=aColor;
            points+=aGame.countLine(3, aColor, idCombination[i]);
        }
        return points;
    }
    private int computeCombinationPointsArrayList(){
        int points=0;
        for (int i = 0; i< idCombinationArrayList.size(); i++){
            if (aGame.fullLine(aColor, idCombinationArrayList.get(i)) && i != idCombinationArrayList.size() - 1) {
                //swap with next in array
                int tempId = idCombinationArrayList.get(i);
                idCombinationArrayList.set(i, idCombinationArrayList.get(i+1));
                idCombinationArrayList.set(i, tempId);
            }
            aGame.findBoxById(idCombinationArrayList.get(i)).color=aColor;
            points+=aGame.countLine(3, aColor, idCombinationArrayList.get(i));
        }
        return points;
    }

    public void printIdCombinations(){
        for (int i =0; i<idCombination.length; i++)
        {
            aGame.findBoxById(idCombination[i]).afficherBox();
            System.out.println("");
        }
    }

    public void printIdCombinationArrayList(){
        for (int i =0; i<idCombinationArrayList.size(); i++)
        {
            aGame.findBoxById(idCombinationArrayList.get(i)).afficherBox();
            System.out.println("");
        }
    }

    public boolean isInCombination(int boxId){
        for (int i=0; i< idCombination.length; i++)
            if (idCombination[i]== boxId)
                return true;
        return false;
    }

    public boolean isCombinationArrayList(int boxId) {
        for (int i=0; i< idCombinationArrayList.size(); i++)
            if (idCombinationArrayList.get(i)== boxId)
                return true;
        return false;
    }

    public Integer getPoints(){
        return combinationPoints;
    }
    public int getFirstBoxId(){ return idCombination[0]; }
    public int[] getIdCombination(){
        return idCombination;
    }
    public ArrayList<Integer> getIdCombinationArrayList(){ return idCombinationArrayList; }
    @Override
    public int compareTo(JeuCombinaison o) {
        return this.getPoints().compareTo(o.getPoints());
    }
}
