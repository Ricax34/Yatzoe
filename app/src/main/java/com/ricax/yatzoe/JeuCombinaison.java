package com.ricax.yatzoe;

import java.util.ArrayList;

public class JeuCombinaison implements Comparable<JeuCombinaison>{
    Jeu aGame;
    private final int combinationPoints;
    private final ArrayList<BoxPair> boxPairCombinationArrayList;
    private final String aColor;
    private String oponentColor;
    public JeuCombinaison(Jeu aGame, String aColor, int [] idCombination){
        this.aGame = aGame;
        this.aColor = aColor;
        if (aColor.equals("red"))
            oponentColor="blue";
        else if (aColor.equals("blue"))
            oponentColor="red";
        this.boxPairCombinationArrayList=this.setBoxPairCombinationArrayList(idCombination);
        this.combinationPoints=this.computeCombinationPointsBoxPairArrayList();
        //calcul des points de chaque box dans la combinaison
        computeBoxPairPoints(aGame);
    }


    private ArrayList<BoxPair> setBoxPairCombinationArrayList(int [] idCombination){
        ArrayList<BoxPair> aBoxPairCombinationList = new ArrayList<>();
        for (int value : idCombination) {
            BoxPair tmpBoxPair = new BoxPair(new Box (aGame.findBoxById(value)), 0, 0);
            aBoxPairCombinationList.add(tmpBoxPair);
        }
        return aBoxPairCombinationList;
    }


    //Additionner les points apportés par chaque case de la liste, fullLine en dernier(est-ce nécessaire?) )
    private int computeCombinationPointsBoxPairArrayList(){
        int points = 0;
        for (int i = 0; i< boxPairCombinationArrayList.size(); i++){
            aGame.findBoxById(boxPairCombinationArrayList.get(i).getPairId()).setColor(aColor);
            int tmpPoints= aGame.countLine(3, aColor, boxPairCombinationArrayList.get(i).getPairId());
            points+= tmpPoints;
        }
        return points;
    }

    //Calcul des points obtenus par chaque case si on y mettait un pion MAINTENANT
    public void computeBoxPairPoints(Jeu tmpGame){
        for (int i = 0; i< boxPairCombinationArrayList.size(); i++){
            BoxPair tmpBp = boxPairCombinationArrayList.get(i);
            int tmpId = boxPairCombinationArrayList.get(i).getPairId();
            //Calcul des points et des fullLine pour chaque box et pour chaque couleur
            tmpGame.findBoxById(tmpId).setColor(aColor);
            if (tmpGame.countLine(5, aColor, tmpId)>0)
                tmpBp.setFullLine(true);
            tmpBp.setPairPoints(tmpGame.countLine(3, aColor, tmpId));

            tmpGame.findBoxById(tmpId).setColor(oponentColor);
            if (tmpGame.countLine(5, oponentColor, tmpId)>0)
                tmpBp.setOponentFullLine(true);
            // tmpBp.setOponentPoints(tmpGame.countLine(3, oponentColor, tmpId));
            //remise à blanc avant de passer à la box suivante
            tmpGame.findBoxById(tmpId).setColor("white");
        }
    }



    public Integer getPoints(){ return combinationPoints; }
    public ArrayList<BoxPair> getBoxPairCombinationArrayList(){ return boxPairCombinationArrayList; }
    @Override
    public int compareTo(JeuCombinaison o) {
        return this.getPoints().compareTo(o.getPoints());
    }
    @Override
    public String toString(){
        return "cp: "+combinationPoints
                + " boxpairs: "
                + this.getBoxPairCombinationArrayList();
    }
}
