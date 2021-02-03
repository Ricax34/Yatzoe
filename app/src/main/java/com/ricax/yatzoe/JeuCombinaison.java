package com.ricax.yatzoe;

import java.util.ArrayList;

public class JeuCombinaison implements Comparable<JeuCombinaison>{
    Jeu aGame;
    private int combinationPoints;
    private int [] idCombination;
    private ArrayList<BoxPair> boxPairCombinationArrayList;
    private String aColor;
    private String oponentColor;
    public JeuCombinaison(Jeu aGame, String aColor, int [] idCombination){
        this.aGame = aGame;
        this.aColor = aColor;
        if (aColor.equals("red"))
            oponentColor="blue";
        else if (aColor.equals("blue"))
            oponentColor="red";
        this.idCombination = idCombination;
        this.boxPairCombinationArrayList=this.setBoxPairCombinationArrayList(idCombination);
        this.combinationPoints=this.computeCombinationPointsBoxPairArrayList();
        //calcul des points de chaque box dans la combinaison
        computeBoxPairPoints(aGame);
    }


    private ArrayList<BoxPair> setBoxPairCombinationArrayList(int [] idCombination){
        ArrayList<BoxPair> aBoxPairCombinationList = new ArrayList<>();
        for (int value : idCombination) {
            BoxPair tmpBoxPair = new BoxPair(value, 0, 0);
            aBoxPairCombinationList.add(tmpBoxPair);
        }
        return aBoxPairCombinationList;
    }


    //Additionner les points apportés par chaque case de la liste, fullLine en dernier(est-ce nécessaire?) )
    private int computeCombinationPointsBoxPairArrayList(){
        int points = 0;
        for (int i = 0; i< boxPairCombinationArrayList.size(); i++){
            if (aGame.fullLine(aColor, boxPairCombinationArrayList.get(i).getPairId()) && i != boxPairCombinationArrayList.size() - 1) {
                //swap with next in array
                BoxPair tmpBoxPair = boxPairCombinationArrayList.get(i);
                boxPairCombinationArrayList.set(i, boxPairCombinationArrayList.get(i+1));
                boxPairCombinationArrayList.set(i, tmpBoxPair);
            }

            aGame.findBoxById(boxPairCombinationArrayList.get(i).getPairId()).color=aColor;
            //TODO verifier le calcul des points de chaque boxPair (ne correspond pas au nb de pts REEL obtenus par la boxPair avec TOUTE la combinaison)
            int tmpPoints= aGame.countLine(3, aColor, boxPairCombinationArrayList.get(i).getPairId());
            points+= tmpPoints;
        }
        return points;
    }

    //Calcul des points obtenus par chaque case si on y mettait un pion MAINTENANT
    public void computeBoxPairPoints(Jeu tmpGame){
        //TODO verification gérer le fullLine
        for (int i = 0; i< boxPairCombinationArrayList.size(); i++){
            BoxPair tmpBp = boxPairCombinationArrayList.get(i);
            int tmpId = boxPairCombinationArrayList.get(i).getPairId();
           //Calcul des points et des fullLine pour chaque box et pour chaque couleur
            tmpGame.findBoxById(tmpId).color=aColor;
            if (tmpGame.countLine(5, aColor, tmpId)>0)
                tmpBp.setFullLine(true);
            tmpBp.setPairPoints(tmpGame.countLine(3, aColor, tmpId));

            tmpGame.findBoxById(tmpId).color=oponentColor;
            if (tmpGame.countLine(5, oponentColor, tmpId)>0)
                tmpBp.setOponentFullLine(true);
            tmpBp.setOponentPoints(tmpGame.countLine(3, oponentColor, tmpId));
            //remise à blanc avant de passer à la box suivante
            tmpGame.findBoxById(tmpId).color="white";
        }
    }


    public void printBoxPairCombinationArrayList(){
        for (int i =0; i<boxPairCombinationArrayList.size(); i++)
            aGame.findBoxById(boxPairCombinationArrayList.get(i).getPairId()).afficherBox();
    }



    public Integer getPoints(){ return combinationPoints; }
    public int[] getIdCombination(){ return idCombination; }
    public ArrayList<BoxPair> getBoxPairCombinationArrayList(){ return boxPairCombinationArrayList; }
    @Override
    public int compareTo(JeuCombinaison o) {
        return this.getPoints().compareTo(o.getPoints());
    }
}
