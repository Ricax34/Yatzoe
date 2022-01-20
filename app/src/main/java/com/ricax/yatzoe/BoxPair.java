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

class BoxPair implements Comparable<BoxPair>{
    private final Box aBox;
    private int points;
    private int oponentPoints;
    private int allPossiblePoints;
    private int nextTurnPossiblePoints=0;
    private int  probability=0;
    private int bonus =0;
    private int endOfGameBonus=0;
    private int probaBonus=0;
    private Double boxWeight=0.0;
    private boolean isFullLine = false;
    private boolean isOponentFullLine = false;
    BoxPair(Box aBox, int points, int oponentPoints){
        this.aBox = aBox;
        this.points= points;
        this.oponentPoints=  oponentPoints;
    }

    public Integer getPairId(){
        return this.aBox.getId();
    }
    public Box getBox(){return aBox;}
    public String getFigType(){return aBox.getFigType(); }
    public void setPairPoints(int p){this.points =  p;}
    public double getPairPoints(){ return  this.points; }
    public void setOponentPoints(int p){this.oponentPoints=p;}
    public double getOponentPoints(){return  this.oponentPoints;}
    public boolean isFullLine(){return isFullLine;}
    public boolean isOponentFullLine(){return isOponentFullLine;}
    public  void setFullLine(boolean isFullLine){this.isFullLine=isFullLine;}
    public void setOponentFullLine(boolean isOponentFullLine){this.isOponentFullLine=isOponentFullLine;}
    public void setAllPossiblePoints(int allPossiblePoints){this.allPossiblePoints= allPossiblePoints;}
    public double getAllPossiblePoints(){return allPossiblePoints;}
    public void setNextTurnPossiblePoints(int p){this.nextTurnPossiblePoints=p;}
    public double getNextTurnPossiblePoints(){return  nextTurnPossiblePoints;}
    public int getProbability(){return  this.probability;}
    public void setProbability(int probability){ this.probability=probability;}
    public void setBonus(int bonus){this.bonus=bonus;}
    public void setEndOfGameBonus(int endOfGameBonus){this.endOfGameBonus=endOfGameBonus; }
    public double getEndOfGameBonus(){return endOfGameBonus;}
    public double getBonus(){return this.bonus;}
    public void setProbaBonus(int probaBonus){this.probaBonus=probaBonus;}
    public void setBoxWeight() {
        this.boxWeight=(double)this.points
                +(double)this.probability*1.5
                +(double)this.nextTurnPossiblePoints
                +(double)this.allPossiblePoints
                +(double)this.oponentPoints
                +(double)this.bonus
                +(double)this.endOfGameBonus
                +(double)this.probaBonus ;
    }
    public Double getBoxWeight() {
        return boxWeight;
    }

    @Override
    public String toString() {
        return aBox
                +", "+ boxWeight
                + " ("+ points
                + "+"+ probability
                + "+"+ nextTurnPossiblePoints
                + "+"+ allPossiblePoints
                + "+"+ oponentPoints
                + "+"+bonus
                +"+"+endOfGameBonus
                +"+"+probaBonus
                + ")\n";
    }

    @Override
    public int compareTo(BoxPair bp) { return this.getBoxWeight().compareTo(bp.getBoxWeight());
    }
}
