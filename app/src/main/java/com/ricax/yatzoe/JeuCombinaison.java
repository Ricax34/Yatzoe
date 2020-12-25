package com.ricax.yatzoe;

public class JeuCombinaison implements Comparable<JeuCombinaison>{
    Jeu aGame;
    private int combinationPoints = 0;
    private int [] idCombination;
    private String aColor;

    public JeuCombinaison(Jeu aGame, String aColor, int [] idCombination){
        this.aGame = aGame;
        this.aColor = aColor;
        this.idCombination = idCombination;
        this.combinationPoints=this.computeCombinationPoints();
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

    public Integer getPoints(){
        return combinationPoints;
    }
    public int getFirstBoxId(){ return idCombination[0]; }
    public int[] getIdCombination(){
        return idCombination;
    }

    @Override
    public int compareTo(JeuCombinaison o) {
        return this.getPoints().compareTo(o.getPoints());
    }
}
