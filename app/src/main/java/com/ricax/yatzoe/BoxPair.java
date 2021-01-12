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
    private final int id;
    private int points;

    BoxPair(int id, int points){
        this.id=id;
        this.points=points;
    }
    Integer getPairId(){ return this.id; }
    void setPairPoints(int p){this.points = p;}
    Integer getPairPoints(){
        return this.points;
    }

    @Override
    public String toString() {
        return "BoxPair{" +
                "id=" + id +
                ", points=" + points +
                '}';
    }

    @Override
    public int compareTo(BoxPair bp) {
        return this.getPairPoints().compareTo(bp.getPairPoints());
    }

}
