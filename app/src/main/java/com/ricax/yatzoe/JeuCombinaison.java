package com.ricax.yatzoe;

import java.util.ArrayList;

public class JeuCombinaison implements Comparable<JeuCombinaison>{
    Jeu aGame;
    private int combinationPoints = 0;
    private int [] idCombination;
    // private ArrayList<Integer> idCombinationArrayList;
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
        for (int i = 0; i<boxPairCombinationArrayList.size(); i++){
            boxPairCombinationArrayList.get(i).setPairPoints(computeBoxPairPoints(boxPairCombinationArrayList.get(i).getPairId(), aGame));
        }
    }

//tranforms the array into an ArrayList
   /* private ArrayList<Integer> setIdCombinationArrayList(int [] idCombination){
        ArrayList<Integer> anArrayListInteger = new ArrayList<>();
        for (int value : idCombination) anArrayListInteger.add(value);
        return anArrayListInteger;
    }*/

    private ArrayList<BoxPair> setBoxPairCombinationArrayList(int [] idCombination){
        ArrayList<BoxPair> aBoxPairCombinationList = new ArrayList<>();
        for (int i= 0; i<idCombination.length; i++){
            BoxPair tmpBoxPair = new BoxPair(idCombination[i], 0, 0);
            aBoxPairCombinationList.add(tmpBoxPair);
        }
        return aBoxPairCombinationList;
    }

/*    private int computeCombinationPoints(){
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
    }*/

  /*  private int computeCombinationPointsArrayList(){
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
    }*/

    //Pour chaque case de la liste: ajoute les points pour chaque couleur
    private int computeCombinationPointsBoxPairArrayList(){
        int points = 0;
        for (int i = 0; i< boxPairCombinationArrayList.size(); i++){
            if (aGame.fullLine(aColor, boxPairCombinationArrayList.get(i).getPairId()) && i != boxPairCombinationArrayList.size() - 1) {
                //swap with next in array
                BoxPair tmpBoxPair = boxPairCombinationArrayList.get(i);
                boxPairCombinationArrayList.set(i, boxPairCombinationArrayList.get(i+1));
                boxPairCombinationArrayList.set(i, tmpBoxPair);
            }
            //On teste la case avec la couleur adverse et ajoute les poinst obtenus à la boxPair de la liste (pas de combinaison donc OK)
            aGame.findBoxById(boxPairCombinationArrayList.get(i).getPairId()).color=oponentColor;
            int tmpOponentPoints = aGame.countLine(3, oponentColor, boxPairCombinationArrayList.get(i).getPairId());
            boxPairCombinationArrayList.get(i).setOponentPoints(tmpOponentPoints);
            //Idem avec la couleur du joueur (la machine=red)
            aGame.findBoxById(boxPairCombinationArrayList.get(i).getPairId()).color=aColor;
            //TODO verifier le calcul des points de chaque boxPair (ne correspond pas au nb de pts REEL obtenus par la boxPair avec TOUTE la combinaison)
            int tmpPoints= aGame.countLine(3, aColor, boxPairCombinationArrayList.get(i).getPairId());
           // boxPairCombinationArrayList.get(i).setPairPoints(tmpPoints);
            points+= tmpPoints;
        }
        return points;
    }

    public int computeBoxPairPoints(int id, Jeu tmpGame){
        int points = 0;
        for (int i = 0; i< boxPairCombinationArrayList.size(); i++){
            if (boxPairCombinationArrayList.get(i).getPairId()!=id){
                tmpGame.findBoxById(boxPairCombinationArrayList.get(i).getPairId()).color=aColor;
                points+= tmpGame.countLine(3, aColor, boxPairCombinationArrayList.get(i).getPairId());
            }
        }
        return combinationPoints-points;
    }

    public void printBoxPairCombinationArrayList(){
        for (int i =0; i<boxPairCombinationArrayList.size(); i++)
            aGame.findBoxById(boxPairCombinationArrayList.get(i).getPairId()).afficherBox();
    }

    public boolean isInCombination(int boxId){
        for (int i=0; i< idCombination.length; i++)
            if (idCombination[i]== boxId)
                return true;
        return false;
    }

  /*  public boolean isCombinationArrayList(int boxId) {
        for (int i=0; i< idCombinationArrayList.size(); i++)
            if (idCombinationArrayList.get(i)== boxId)
                return true;
        return false;
    }*/

    public Integer getPoints(){ return combinationPoints; }
    public int getFirstBoxId(){ return idCombination[0]; }
    public int[] getIdCombination(){ return idCombination; }
    // public ArrayList<Integer> getIdCombinationArrayList(){ return idCombinationArrayList; }
    public ArrayList<BoxPair> getBoxPairCombinationArrayList(){ return boxPairCombinationArrayList; }
    @Override
    public int compareTo(JeuCombinaison o) {
        return this.getPoints().compareTo(o.getPoints());
    }
}
