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


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity {

    boolean boxFlagCheat = false;
    boolean blueFlagCheat = false;
    boolean redFlagCheat = false;
    boolean diceCheat = false;
    int diceCheatIdx=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startGame();
        Button startGameButton = findViewById(R.id.buttonStartNewGame);
        startGameButton.setEnabled(false);
    }

    public void showHelp(View v){
        Intent intent = new Intent(this, ShowHelpActivity.class);
        startActivity(intent);
    }

    //new game
    private Jeu game;

    //    array of ImageView containing the dices
    private ImageView[] imViewDices;

    //private TextView textview_Message;
    public void startNewGame(View v){
        startGame();
        setContentView(R.layout.activity_main);
        Button startGameButton = findViewById(R.id.buttonStartNewGame);
        startGameButton.setEnabled(false);
    }


    public void showDroidThrows(int tnb){
        TextView textview_Message = findViewById(R.id.TextViewMessage);
        switch (tnb){
            case 1:
                textview_Message.setText(getResources().getIdentifier("FirstThrow", "string", getPackageName()));
                break;
            case 2:
                textview_Message.setText(getResources().getIdentifier("SecondThrow", "string", getPackageName()));
                break;
            case 3:
                textview_Message.setText(getResources().getIdentifier("ThirdThrow", "string", getPackageName()));
                break;
        }
    }

    public void enableNewGameButton(){
        Button startGameButton = findViewById(R.id.buttonStartNewGame);
        startGameButton.setEnabled(true);
    }

    public void startGame() {
        game = new Jeu(MainActivity.this);
        //initialisation of dices
        imViewDices = new ImageView[5];
        imViewDices[0] = findViewById(R.id.imageViewDiceNb0);
        imViewDices[1] = findViewById(R.id.imageViewDiceNb1);
        imViewDices[2] = findViewById(R.id.imageViewDiceNb2);
        imViewDices[3] = findViewById(R.id.imageViewDiceNb3);
        imViewDices[4] = findViewById(R.id.imageViewDiceNb4);
        //initialisation of fiveDices
        int [] imViewDicesId = new int[5];
        for (int i =0; i<5; i++)
            imViewDicesId[i]=imViewDices[i].getId();
        game.fiveDices=new Figure(imViewDicesId);

        //initialisation of board
        game.checkerBox[0][0] = new Box("1", "white", 0, 0, findViewById(R.id.imageView1Un).getId());
        game.checkerBox[0][1] = new Box("3", "white", 0, 1, findViewById(R.id.imageView2Trois).getId());
        game.checkerBox[0][2] = new Box("Appel", "white", 0, 2, findViewById(R.id.imageView3Appel).getId());
        game.checkerBox[0][3] = new Box("4", "white", 0, 3, findViewById(R.id.imageView4Quatre).getId());
        game.checkerBox[0][4] = new Box("6", "white", 0, 4, findViewById(R.id.imageView5Six).getId());
        game.checkerBox[1][0] = new Box("2", "white", 1, 0, findViewById(R.id.imageView6Deux).getId());
        game.checkerBox[1][1] = new Box("Carre", "white", 1, 1, findViewById(R.id.imageView7Carre).getId());
        game.checkerBox[1][2] = new Box("Sec", "white", 1, 2, findViewById(R.id.imageView8Sec).getId());
        game.checkerBox[1][3] = new Box("Full", "white", 1, 3, findViewById(R.id.imageView9Full).getId());
        game.checkerBox[1][4] = new Box("5", "white", 1, 4, findViewById(R.id.imageView10Cinq).getId());
        game.checkerBox[2][0] = new Box("Small", "white", 2, 0, findViewById(R.id.imageView11Small).getId());
        game.checkerBox[2][1] = new Box("Full", "white", 2, 1, findViewById(R.id.imageView12Full).getId());
        game.checkerBox[2][2] = new Box("Yam", "white", 2, 2, findViewById(R.id.imageView13Yam).getId());
        game.checkerBox[2][3] = new Box("Appel", "white", 2, 3, findViewById(R.id.imageView14Appel).getId());
        game.checkerBox[2][4] = new Box("Suite", "white", 2, 4, findViewById(R.id.imageView15Suite).getId());
        game.checkerBox[3][0] = new Box("6", "white", 3, 0, findViewById(R.id.imageView16Six).getId());
        game.checkerBox[3][1] = new Box("Sec", "white", 3, 1, findViewById(R.id.imageView17Sec).getId());
        game.checkerBox[3][2] = new Box("Suite", "white", 3, 2, findViewById(R.id.imageView18Suite).getId());
        game.checkerBox[3][3] = new Box("Small", "white", 3, 3, findViewById(R.id.imageView19Small).getId());
        game.checkerBox[3][4] = new Box("1", "white", 3, 4, findViewById(R.id.imageView20Un).getId());
        game.checkerBox[4][0] = new Box("3", "white", 4, 0, findViewById(R.id.imageView21Trois).getId());
        game.checkerBox[4][1] = new Box("2", "white", 4, 1, findViewById(R.id.imageView22Deux).getId());
        game.checkerBox[4][2] = new Box("Carre", "white", 4, 2, findViewById(R.id.imageView23Carre).getId());
        game.checkerBox[4][3] = new Box("5", "white", 4, 3, findViewById(R.id.imageView24Cinq).getId());
        game.checkerBox[4][4] = new Box("4", "white", 4, 4, findViewById(R.id.imageView25Quatre).getId());
        //initialisation of textview_Message
        TextView textview_Message = findViewById(R.id.TextViewMessage);
        textview_Message.setText(getResources().getIdentifier("selectAllDices", "string", getPackageName()));
    }

    public void setOrangeBackgroundBlueOrRed(ImageView v){
        switch (game.findBoxById(v.getId()).getColor()) {
            case "blue": {
                v.setBackgroundResource(R.drawable.ic_semitransparentbackgroundpionbleucontour_appel);
                break;
            }
            case "red": {
                v.setBackgroundResource(R.drawable.ic_semitransparentbackgroundpionrougecontour_appel);
                break;
            }
            case "white": {
                v.setBackgroundResource(R.drawable.ic_semitransparentbackground_appel);
            }
        }
    }

    public void ungrayAppelBoxOrFigAppelBoxToPreviousState(int appelOrFigBoxId){
        //Ungray appelFigBoxView
        System.out.println("appelOrFigBoxId: "+appelOrFigBoxId);
        ImageView appelOrFigBoxView = findViewById(appelOrFigBoxId);
        appelOrFigBoxView.setBackgroundResource(0);
        switch (game.findBoxById(appelOrFigBoxId).getColor()) {
            case "blue": {
                Resources res = getApplicationContext().getResources();
                Drawable pionBleu = ResourcesCompat.getDrawable(res, R.drawable.ic_pion_bleu_contour_noir, null);
                appelOrFigBoxView.setBackground(pionBleu);
                break;
            }
            case "red": {
                Resources res = getApplicationContext().getResources();
                Drawable pionRouge = ResourcesCompat.getDrawable(res, R.drawable.ic_pion_rouge_contour_noir, null);
                appelOrFigBoxView.setBackground(pionRouge);
                break;
            }
        }
    }

    int oldTargetID=0;
    public void showMachineTarget(int targetID){
        if (oldTargetID>0) {
            ImageView oldTargetView = findViewById(oldTargetID);
            oldTargetView.clearColorFilter();
        }
        oldTargetID=targetID;
        ImageView targetView = findViewById(targetID);
        targetView.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_purple));
    }

    public void onBoxAppelClicked(View v){
        //It is an appel toggled ON or OFF-using-the-same-Box
        //game.appelClicked Devrait être 0 depuis machienPlayTask
        if ((!game.appelClicked) || (game.appelBoxId == v.getId())) {//if game.appelBoxId==v.getId() then game.appelClicked is true
            if (!game.appelClicked) {
                System.out.println("game.appelClicked est mis à true");
                game.appelClicked=true;
                game.appelBoxId=v.getId();
                //Gray the Box
                ImageView appelView = findViewById(game.appelBoxId);
                setOrangeBackgroundBlueOrRed(appelView);
            }
            else {
                //Back to previous state for appelBox
                ungrayAppelBoxOrFigAppelBoxToPreviousState(game.appelBoxId);
                game.appelBoxId=0;
                game.appelClicked=false;
                System.out.println("game.appelClicked est remis à false");
                // un-gray the figtype Box that was called and reset variables about appel
                //reset to previous state
                if (game.appelFigTypeBoxId != 0) {
                    ungrayAppelBoxOrFigAppelBoxToPreviousState(game.appelFigTypeBoxId);
                    game.appelFigTypeBoxId = 0;
                    game.appelRegistered = "";
                }
            }
        }
    }
    public void onBoxFigureAppelClicked(View v) {
        if ((game.findBoxById(v.getId()).getFigType().matches(".*(Full|Suite|Carre|Sec|Yam|Small).*"))) {
            if (game.appelRegistered.isEmpty()) {
                game.appelRegistered = game.findBoxById(v.getId()).getFigType();
                game.appelFigTypeBoxId = v.getId();
                ImageView figAppelTypeView = findViewById(game.appelFigTypeBoxId);

                //  figAppelTypeView.setBackgroundResource(R.drawable.ic_semitransparentbackground_appel);
                setOrangeBackgroundBlueOrRed(figAppelTypeView);
                TextView textview_Message = findViewById(R.id.TextViewMessage);
                textview_Message.setText(getString(R.string.AppelFigure, game.appelRegistered));
            }
            else {
                //means that we want to un-select this Box OR select another Box+figtype
                if (game.appelFigTypeBoxId == v.getId()) {
                    //means we reclicked on the previous Box to uncheck it
                    game.appelRegistered = "";
                    ungrayAppelBoxOrFigAppelBoxToPreviousState(v.getId());
                    game.appelFigTypeBoxId = 0;
                    TextView textview_Message = findViewById(R.id.TextViewMessage);
                    textview_Message.setText("");
                }
                else {
                    //Means we clicked on another to select it instead
                    //Fetch the previously clicked Box and un-gray it
                    ungrayAppelBoxOrFigAppelBoxToPreviousState(game.appelFigTypeBoxId);
                    //Gray and register the new one
                    game.appelRegistered = game.findBoxById(v.getId()).getFigType();
                    //remember the Box we clicked on and gray it
                    game.appelFigTypeBoxId = v.getId();
                    ImageView figAppelTypeView = findViewById(game.appelFigTypeBoxId);
                    // figAppelTypeView.setBackgroundResource(R.drawable.ic_semitransparentbackground_appel);
                    setOrangeBackgroundBlueOrRed(figAppelTypeView);
                    TextView textview_Message = findViewById(R.id.TextViewMessage);
                    textview_Message.setText(getString(R.string.AppelFigure, game.appelRegistered));
                }
            }
        }
    }

    //TODO revoir le thrownb cheat pour vérifier YatzoeLog20210509-234336 jetN°2 du dernier gamestate
    // (pointeur nul sur dé, surement pas de procédure prévue pour sélectionner
    // des dés pour une suite à partir de ce diceset)
    public void onBoxClicked(View v) {
        if (diceCheat&&!boxFlagCheat){
           System.out.println("diceCheat="+diceCheat+"boxFlagcheat="+boxFlagCheat);
                if (diceCheatIdx<5 && game.throwNb<3){
                String tagToString = v.getTag().toString();
                switch (tagToString) {
                    case "imageView1Un": {
                        game.fiveDices.diceSet[diceCheatIdx].value=1;
                        break;
                    }
                    case "imageView6Deux": {
                        game.fiveDices.diceSet[diceCheatIdx].value=2;
                        break;
                    }
                    case "imageView2Trois": {
                        game.fiveDices.diceSet[diceCheatIdx].value=3;
                        break;
                    }
                    case "imageView4Quatre": {
                        game.fiveDices.diceSet[diceCheatIdx].value=4;
                        break;
                    }
                    case "imageView10Cinq": {
                        game.fiveDices.diceSet[diceCheatIdx].value=5;
                        break;
                    }
                    case "imageView5Six": {
                        game.fiveDices.diceSet[diceCheatIdx].value=6;
                        break;
                    }
                }
                updateOneDice(game.fiveDices.diceSet[diceCheatIdx].id, game.fiveDices.diceSet[diceCheatIdx].value);
                diceCheatIdx++;
            }
            if (diceCheatIdx==5){
                diceCheatIdx=0;
                game.changeTurnColor("red");
            }
        }
        else if (boxFlagCheat&&!diceCheat){
            Resources res = getApplicationContext().getResources();
            Box clickedBox = game.findBoxById(v.getId());
            ImageView uneCase = findViewById(v.getId());

            if (blueFlagCheat){
                Drawable pionBleu = ResourcesCompat.getDrawable(res, R.drawable.ic_pion_bleu_contour_noir, null);
                uneCase.setBackground(pionBleu);
                clickedBox.setColor("blue");
                game.blueMarkers--;
                TextView blueMarkerTV = findViewById(R.id.blueMarkerTextView);
                blueMarkerTV.setText(String.format("%s", game.blueMarkers));
                game.bluePoints+=game.countLine(3, "blue", v.getId());
                TextView bluePointsTV = findViewById(R.id.bluePoints);
                bluePointsTV.setText(String.format("%s", game.bluePoints));
            }
            else if (redFlagCheat){
                Drawable pionRouge = ResourcesCompat.getDrawable(res, R.drawable.ic_pion_rouge_contour_noir, null);
                uneCase.setBackground(pionRouge);
                clickedBox.setColor("red");
                game.redMarkers--;
                TextView redMarkerTV = findViewById(R.id.redMarkerTextView);
                redMarkerTV.setText(String.format("%s", game.redMarkers));
                game.redPoints+=game.countLine(3, "red", v.getId());
                TextView redPointsTV = findViewById(R.id.redPoints);
                redPointsTV.setText(String.format("%s", game.redPoints));
            }
        }
        else if (boxFlagCheat&&diceCheat){
            Box clickedBox = game.findBoxById(v.getId());
            if (clickedBox.getFigType().equals(Integer.toString(1)))
                game.throwNb=1;
            else if (clickedBox.getFigType().equals(Integer.toString(2)))
                game.throwNb=2;
            else if (clickedBox.getFigType().equals(Integer.toString(3)))
                game.throwNb=3;
            System.out.println("Game thronb cheat: "+game.throwNb);
        }
        else if (game.couleur.equals("blue")) {
            //Chaque fois que l'on clique sur une box on prépare une valeur aléatoire
            game.setDiceArrayListRandomValue();
            Box clickedBox = game.findBoxById(v.getId());
            //Check whether it is an appel called or un-called + gray or un-gray Boxes
            if (game.throwNb == 1) {
                if (game.findBoxById(v.getId()).getFigType().equals("Appel")) {
                    if (!game.getListBoxPairColorPerFigure("Appel", "white").isEmpty())
                        onBoxAppelClicked(v);
                    else {
                        TextView textview_Message = findViewById(R.id.TextViewMessage);
                        textview_Message.setText(getResources().getIdentifier("appelNotPossible", "string", getPackageName()));
                    }
                }
                //Then if it is an appel, check which Figure is called or un-called (Box figtype appel (un)selected)
                else if (game.appelClicked) {
                    onBoxFigureAppelClicked(v);
                } else if ((game.fiveDices.figureList.contains(clickedBox.getFigType())) && (clickedBox.getColor().equals("white"))) {//If it is not an appel
                    placeMarker(v);
                }
                else {
                    TextView textview_Message = findViewById(R.id.TextViewMessage);
                    textview_Message.setText(getResources().getIdentifier("wrongBox", "string", getPackageName()));
                }
            }
            if (game.throwNb > 1) {
                if ((game.appelClicked) && (game.findBoxById(v.getId()).getFigType().equals("Appel"))) {//We've asked an appel  + we clicked on an appel Box
                    if (game.fiveDices.figureList.equals("Appel")) {//Appel is OK
                        //the current Box is an appel one, ungray the one that was grayed on turn 1 and the appel figType Box as well
                        //and reset the clicked Boxes to their previous state
                        if (clickedBox.getColor().equals("white")) {
                            //place the marker on the current clicked appel Box
                            placeMarker(v);

                        } else {
                            TextView textview_Message = findViewById(R.id.TextViewMessage);
                            textview_Message.setText(getResources().getIdentifier("selectWhiteBox", "string", getPackageName()));
                        }
                    } else {
                        TextView textview_Message = findViewById(R.id.TextViewMessage);
                        textview_Message.setText(getResources().getIdentifier("missedAppel", "string", getPackageName()));
                    }
                } else if ((game.appelClicked) && (!game.findBoxById(v.getId()).getFigType().equals("Appel"))) {
                    TextView textview_Message = findViewById(R.id.TextViewMessage);
                    textview_Message.setText(getResources().getIdentifier("wrongBox", "string", getPackageName()));
                } else {
                    if ((!game.appelClicked) && (game.fiveDices.figureList.contains(clickedBox.getFigType())) && (clickedBox.getColor().equals("white"))) {
                        placeMarker(v);
                    } else {
                        TextView textview_Message = findViewById(R.id.TextViewMessage);
                        textview_Message.setText(getResources().getIdentifier("wrongBox", "string", getPackageName()));
                    }
                }
            }
        }
    }


    //pour poser des markers
    public void onButtonCheatClicked(View v){

        System.out.println("onButtonCheatClicked1 boxFlagCheat:"+boxFlagCheat);
        if (!boxFlagCheat) {
            boxFlagCheat=true;
            ImageView boxFlagCheatView = findViewById(v.getId());
            boxFlagCheatView.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_light));
            TextView blueMarkersTextview = findViewById(R.id.blueMarkerTextView);
            blueMarkersTextview.setClickable(true);
            TextView redMarkersTextview = findViewById(R.id.redMarkerTextView);
            redMarkersTextview.setClickable(true);
        }
        else{
            boxFlagCheat=false;
            ImageView boxFlagCheatView = findViewById(v.getId());
            boxFlagCheatView.clearColorFilter();
            TextView blueMarkersTextview = findViewById(R.id.blueMarkerTextView);
            blueMarkersTextview.setClickable(false);
            blueFlagCheat=false;
            TextView redMarkersTextview = findViewById(R.id.redMarkerTextView);
            redMarkersTextview.setClickable(false);
            redFlagCheat=false;
        }
        System.out.println("onButtonCheatClicked2 boxFlagCheat:"+boxFlagCheat);
    }

    public void onColorMarkersNbTVClicked(View v){
        if (boxFlagCheat){
            //System.out.println("flagCheat: "+boxFlagCheat);
            if (v.getTag().toString().equals("blueMarkerTextView")){
                blueFlagCheat=true;
                redFlagCheat=false;
            }
            else if (v.getTag().toString().equals("redMarkerTextView")){
                redFlagCheat=true;
                blueFlagCheat=false;
            }
        }
        else
            System.out.println("No cheats!");
    }

    public void onButtonDiceCheatClicked (View v){
        diceCheat= !diceCheat;
        System.out.println("DiceCheat: "+diceCheat);
        ImageView diceCheatView = findViewById(v.getId());
        if (diceCheat)
            diceCheatView.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_light));
        else
            diceCheatView.clearColorFilter();
    }

    public void placeMarkerById(int id){
        //Chaque fois que l'on clique sur une box on prépare une valeur aléatoire
        game.setDiceArrayListRandomValue();
        placeMarker(findViewById(id));
    }

    private void placeMarker(View v) {
        Resources res = getApplicationContext().getResources();
        Box clickedBox = game.findBoxById(v.getId());
        //Gerer le cas ou c'est un appel reussi
        if (game.findBoxById(v.getId()).getFigType().equals("Appel")){
            ungrayAppelBoxOrFigAppelBoxToPreviousState(game.appelBoxId);
            try{
                sleep(300);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            ungrayAppelBoxOrFigAppelBoxToPreviousState(game.appelFigTypeBoxId);
            game.appelRegistered="";
            game.appelClicked=false;
            game.appelBoxId=0;
            game.appelFigTypeBoxId=0;
        }

        if (game.couleur.equals("blue")) { //Check if it is this player's turn to play
            //Check if it's ok to place the marker in this particular Box
            Drawable pionBleu = ResourcesCompat.getDrawable(res, R.drawable.ic_pion_bleu_contour_noir, null);
            ImageView uneCase = findViewById(v.getId());
            uneCase.setBackground(pionBleu);
            clickedBox.setColor("blue");
            TextView textview_Message = findViewById(R.id.TextViewMessage);
            textview_Message.setText(getResources().getIdentifier("red_turn", "string", getPackageName()));
            for (int i = 0; i < 5; i++) {
                imViewDices[i].clearColorFilter();
            }
            game.blueMarkers--;
            TextView blueMarkerTV = findViewById(R.id.blueMarkerTextView);
            blueMarkerTV.setText(String.format("%s", game.blueMarkers));
            game.bluePoints+=game.countLine(3, game.couleur, v.getId());
            TextView bluePointsTV = findViewById(R.id.bluePoints);
            bluePointsTV.setText(String.format("%s", game.bluePoints));
            if (game.endOfGame(v.getId())){
                game.terminate();
                textview_Message.setText(getResources().getIdentifier(game.showEndOfGame(), "string", getPackageName()));
                enableNewGameButton();
            }
            else game.changeTurnColor("red");
        }
        else if (game.couleur.equals("red")) { //Check if it is this player's turn to play
            //Check if it's ok to place the marker in this particular Box
            Drawable pionRouge = ResourcesCompat.getDrawable(res, R.drawable.ic_pion_rouge_contour_noir, null);
            ImageView uneCase = findViewById(v.getId());
            uneCase.setBackground(pionRouge);
            clickedBox.setColor("red");
            TextView textview_Message = findViewById(R.id.TextViewMessage);
            textview_Message.setText(getResources().getIdentifier("blue_turn", "string", getPackageName()));
            for (int i = 0; i < 5; i++) {
                imViewDices[i].clearColorFilter();
            }
            game.redMarkers--;
            TextView redMarkerTV = findViewById(R.id.redMarkerTextView);
            redMarkerTV.setText(String.format("%s", game.redMarkers));
            game.redPoints+=game.countLine(3,game.couleur,v.getId());
            TextView redPointsTV = findViewById(R.id.redPoints);
            redPointsTV.setText(String.format("%s",game.redPoints ));
            if (game.endOfGame(v.getId())){
                textview_Message.setText(getResources().getIdentifier(game.showEndOfGame(), "string", getPackageName()));
                game.terminate();
                enableNewGameButton();
            }
            else game.changeTurnColor("blue");
        }
        //else clicking on a Box leads to nothing
        else {
            //Show message here
            TextView textview_Message = findViewById(R.id.TextViewMessage);
            textview_Message.setText(getResources().getIdentifier("WrongBox", "string", getPackageName()));
        }
    }

    public void changeTurnColorTextviewMessage(String color){
        switch (color) {
            case "blue": {
                TextView textview_Message = findViewById(R.id.TextViewMessage);
                textview_Message.setText(getResources().getIdentifier("blue_turn", "string", getPackageName()));
                break;
            }
            case "red": {
                TextView textview_Message = findViewById(R.id.TextViewMessage);
                textview_Message.setText(getResources().getIdentifier("red_turn", "string", getPackageName()));
                break;
            }
            case "white": {
                TextView textview_Message = findViewById(R.id.TextViewMessage);
                textview_Message.setText(getResources().getIdentifier(game.showEndOfGame(), "string", getPackageName()));
                break;
            }
        }
    }

    private void skipTurn(){
        for (int i = 0; i < 5; i++) {
            imViewDices[i].clearColorFilter();
        }
        if (game.couleur.equals("red")){
            TextView textview_Message = findViewById(R.id.TextViewMessage);
            textview_Message.setText(getResources().getIdentifier("blue_turn","string", getPackageName()));
            game.changeTurnColor("blue");
        }
        else if (game.couleur.equals("blue")){
            TextView textview_Message = findViewById(R.id.TextViewMessage);
            textview_Message.setText(getResources().getIdentifier("red_turn","string", getPackageName()));
            game.changeTurnColor("red");
        }
    }

    public void onButtonSkipClicked(View v) {
        if (game.couleur.equals("blue"))
            skipTurn();
    }


    public void onButtonClickRollAllSelectedDices(View v) {
        if (game.couleur.equals("blue")) {
            if (!game.throwDices()) {
                if (game.throwNb >= game.maxThrowNb) {
                    TextView textview_Message = findViewById(R.id.TextViewMessage);
                    textview_Message.setText(getResources().getIdentifier("warningMessageThirdTry", "string", getPackageName()));
                }
                else if ((game.appelClicked)&&(game.appelRegistered.isEmpty())){
                    TextView textview_Message = findViewById(R.id.TextViewMessage);
                    textview_Message.setText(getResources().getIdentifier("selectAFigureCall", "string", getPackageName()));
                }
                else {
                    TextView textview_Message = findViewById(R.id.TextViewMessage);
                    textview_Message.setText(getResources().getIdentifier("selectAllDices", "string", getPackageName()));
                }
            } else {
                for (int i = 0; i < 5; i++)
                    updateOneDice(game.fiveDices.diceSet[i].id, game.fiveDices.diceSet[i].value);
                if (game.throwNb == 1) {
                    TextView textview_Message = findViewById(R.id.TextViewMessage);
                    textview_Message.setText(getResources().getIdentifier("FirstThrow", "string", getPackageName()));
                } else if (game.throwNb == 2) {
                    TextView textview_Message = findViewById(R.id.TextViewMessage);
                    textview_Message.setText(getResources().getIdentifier("SecondThrow", "string", getPackageName()));
                } else if (game.throwNb == game.maxThrowNb) //3ème jets
                {
                    TextView textview_Message = findViewById(R.id.TextViewMessage);
                    textview_Message.setText(getResources().getIdentifier("ThirdThrow", "string", getPackageName()));
                    if ((game.fiveDices.figureList.isEmpty()) || (game.getListFreeBoxPerFigureList(game.fiveDices.figureList).isEmpty())) //pas de figure posables ni appel
                    {
                        if (game.appelClicked) { //appel raté
                            game.fiveDices.figureList = "";
                            //Ungray appelFigTypeBox
                            ungrayAppelBoxOrFigAppelBoxToPreviousState(game.appelFigTypeBoxId);
                            game.appelRegistered = "";
                            ungrayAppelBoxOrFigAppelBoxToPreviousState(game.appelBoxId);
                            game.appelBoxId = 0;
                            game.appelRegistered = "";
                            game.appelFigTypeBoxId = 0;
                            game.appelClicked = false;
                        }
                        if (game.couleur.equals("red")) {
                            textview_Message.setText(getResources().getIdentifier("blue_turn", "string", getPackageName()));
                        } else {
                            textview_Message.setText(getResources().getIdentifier("red_turn", "string", getPackageName()));
                        }
                        game.changeTurnColor("red");
                    }
                }
            }
        }
    }

    public void updateOneDice(int aDiceId, int value) {
        Resources res = getApplicationContext().getResources();
        ImageView aDice = findViewById(aDiceId);
        switch (value) {
            case 1: {
                Drawable dice1 = ResourcesCompat.getDrawable(res, R.drawable.ic_dice1, null);
                aDice.setImageDrawable(dice1);
                break;
            }
            case 2: {
                Drawable dice2 = ResourcesCompat.getDrawable(res, R.drawable.ic_dice2, null);
                aDice.setImageDrawable(dice2);
                break;
            }
            case 3: {
                Drawable dice3 = ResourcesCompat.getDrawable(res, R.drawable.ic_dice3, null);
                aDice.setImageDrawable(dice3);
                break;
            }
            case 4: {
                Drawable dice4 = ResourcesCompat.getDrawable(res, R.drawable.ic_dice4, null);
                aDice.setImageDrawable(dice4);
                break;
            }
            case 5: {
                Drawable dice5 = ResourcesCompat.getDrawable(res, R.drawable.ic_dice5, null);
                aDice.setImageDrawable(dice5);
                break;
            }
            case 6: {
                Drawable dice6 = ResourcesCompat.getDrawable(res, R.drawable.ic_dice6, null);
                aDice.setImageDrawable(dice6);
                break;
            }
        }
    }

    public void  UI_setDiceColor(int Id, String color){
        ImageView dice = findViewById(Id);
        if (color.equals("white")){
            dice.clearColorFilter();
        }
        else if (color.equals("green")){
            dice.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }
    }

    public void onDiceSelect(View v) {
        if (game.couleur.equals("blue")){
            //Chaque fois que l'on clique sur un dé on prépare une valeur aléatoire
            game.setDiceArrayListRandomValue();
            ImageView dice = findViewById(v.getId());
            String diceName = dice.getResources().getResourceEntryName(dice.getId());
            switch (diceName) {
                case "imageViewDiceNb0": {
                    if (!game.fiveDices.diceSet[0].selected()) {
                        dice.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                    } else {
                        dice.clearColorFilter();
                    }
                    game.toggleSelectDice(0);
                }
                break;
                case "imageViewDiceNb1": {
                    if (!game.fiveDices.diceSet[1].selected()) {
                        dice.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                    } else {
                        dice.clearColorFilter();
                    }
                    game.toggleSelectDice(1);
                }
                break;
                case "imageViewDiceNb2": {
                    if (!game.fiveDices.diceSet[2].selected()) {
                        dice.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                    } else {
                        dice.clearColorFilter();
                    }
                    game.toggleSelectDice(2);
                }
                break;
                case "imageViewDiceNb3": {
                    if (!game.fiveDices.diceSet[3].selected()) {
                        dice.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));

                    } else {
                        dice.clearColorFilter();
                    }
                    game.toggleSelectDice(3);
                }
                break;
                case "imageViewDiceNb4": {
                    if (!game.fiveDices.diceSet[4].selected()) {
                        dice.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                    } else {
                        dice.clearColorFilter();
                    }
                    game.toggleSelectDice(4);
                }
                break;
            }
        }
    }
}
