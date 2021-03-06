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

class Box {
    private String figType = "";
    private String color="white";
    private boolean checked = false;
     int h = 0;
     int v = 0;
    private int id = 0;

    public Box(){}
    //Constructeur de copie
    public Box (Box aBox){
        figType=aBox.figType;
        color=aBox.color;
        checked=aBox.checked;
        h=aBox.h;
        v=aBox.v;
        id=aBox.id;
    }

    public Box(String fig, String col, int vert, int hor, int androidId) {
        this.figType = fig;
        this.color = col;
        this.h = hor;
        this.v = vert;
        this.id = androidId;
    }

    public Box(int hor, int vert) {
        this.h = hor;
        this.v = vert;
    }

    public int getId(){
        return id;
    }

    public String getColor(){return color;}
    public void setColor(String aColor){ color=aColor; }
    public String getFigType(){ return figType; }

    @Override
    public String toString(){
        return "("+v+", "+h+")"+" "+figType;
    }

    public void afficherBox(){
        System.out.println("("+v+", "+h+")"+" "+figType);
    }
}
