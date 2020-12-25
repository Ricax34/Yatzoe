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
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity {

    boolean flagCheat = false;
    boolean blueFlagCheat = false;
    boolean redFlagCheat = false;

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

    public void quitGame (View v){
        this.finish();
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
        game.fiveDices = new Figure(
                imViewDices[0].getId(),
                imViewDices[1].getId(),
                imViewDices[2].getId(),
                imViewDices[3].getId(),
                imViewDices[4].getId()
        );
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
        switch (game.findBoxById(v.getId()).color) {
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
        ImageView appelOrFigBoxView = findViewById(appelOrFigBoxId);
        appelOrFigBoxView.setBackgroundResource(0);
        switch (game.findBoxById(appelOrFigBoxId).color) {
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

    public void onBoxAppelClicked(View v){
        //It is an appel toggled ON or OFF-using-the-same-Box
        //game.appelClicked Devrait être 0 depuis machienPlayTask
        if ((!game.appelClicked) || (game.appelBoxId == v.getId())) {//if game.appelBoxId==v.getId() then game.appelClicked is true
            if (!game.appelClicked) {
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
        if ((game.findBoxById(v.getId()).figType.matches(".*(Full|Suite|Carre|Sec|Yam|Small).*"))) {
            if (game.appelRegistered.isEmpty()) {
                game.appelRegistered = game.findBoxById(v.getId()).figType;
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
                    game.appelRegistered = game.findBoxById(v.getId()).figType;
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

    public void onBoxClicked(View v) {
        if (flagCheat){
            Resources res = getApplicationContext().getResources();
            Box clickedBox = game.findBoxById(v.getId());
            ImageView uneCase = findViewById(v.getId());

            if (blueFlagCheat){
                Drawable pionBleu = ResourcesCompat.getDrawable(res, R.drawable.ic_pion_bleu_contour_noir, null);
                uneCase.setBackground(pionBleu);
                clickedBox.color = "blue";
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
                clickedBox.color = "red";
                game.redMarkers--;
                TextView redMarkerTV = findViewById(R.id.redMarkerTextView);
                redMarkerTV.setText(String.format("%s", game.redMarkers));
                game.redPoints+=game.countLine(3, "red", v.getId());
                TextView redPointsTV = findViewById(R.id.redPoints);
                redPointsTV.setText(String.format("%s", game.redPoints));
            }
        }
        else if (game.couleur.equals("blue")) {
            Box clickedBox = game.findBoxById(v.getId());
            //Check whether it is an appel called or un-called + gray or un-gray Boxes
            if (game.throwNb == 1) {
                if (game.findBoxById(v.getId()).figType.equals("Appel")) {
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
                } else if ((game.fiveDices.figureList.contains(clickedBox.figType)) && (clickedBox.color.equals("white"))) {//If it is not an appel
                    placeMarker(v);
                }
                else {
                    TextView textview_Message = findViewById(R.id.TextViewMessage);
                    textview_Message.setText(getResources().getIdentifier("wrongBox", "string", getPackageName()));
                }
            }
            if (game.throwNb > 1) {
                if ((game.appelClicked) && (game.findBoxById(v.getId()).figType.equals("Appel"))) {//We've asked an appel  + we clicked on an appel Box
                    if (game.fiveDices.figureList.equals("Appel")) {//Appel is OK
                        //the current Box is an appel one, ungray the one that was grayed on turn 1 and the appel figType Box as well
                        //and reset the clicked Boxes to their previous state
                        if (clickedBox.color.equals("white")) {
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
                } else if ((game.appelClicked) && (!game.findBoxById(v.getId()).figType.equals("Appel"))) {
                    TextView textview_Message = findViewById(R.id.TextViewMessage);
                    textview_Message.setText(getResources().getIdentifier("wrongBox", "string", getPackageName()));
                } else {
                    if ((!game.appelClicked) && (game.fiveDices.figureList.contains(clickedBox.figType)) && (clickedBox.color.equals("white"))) {
                        placeMarker(v);
                    } else {
                        TextView textview_Message = findViewById(R.id.TextViewMessage);
                        textview_Message.setText(getResources().getIdentifier("wrongBox", "string", getPackageName()));
                    }
                }
            }
        }
    }

    private int cheat(int v, int h, String color){
        if (color.equals("blue")){
            game.checkerBox[v][h].color="blue";
            game.blueMarkers--;
            return game.countLine(3, "blue", game.checkerBox[v][h].id);
        }
        else if (color.equals("red")){
            game.checkerBox[v][h].color="red";
            game.redMarkers--;
            return game.countLine(3, "red", game.checkerBox[v][h].id);
        }
        return 0;
    }

    public void onButtonCheatClicked(View v){
        //     game.fiveDices.figureList="SmallSuiteSecFullCarreAppelYam123456";
        //game.fiveDices.figureList="";
        //    int bPoints = 0;
        //   int rPoints = 0 ;

 /*       game.checkerBox[0][0].color="red";
        rPoints += game.countLine(3, "red", game.checkerBox[0][0].id);
        game.redMarkers--;
        game.checkerBox[0][2].color="blue";
        bPoints += game.countLine(3, "blue", game.checkerBox[0][3].id);
        game.blueMarkers--;
        game.checkerBox[0][4].color="blue";
        bPoints += game.countLine(3, "blue", game.checkerBox[0][4].id);
        game.blueMarkers--;
        game.checkerBox[3][2].color="red";
        rPoints += game.countLine(3, "red", game.checkerBox[3][2].id);
        game.redMarkers--;
        game.checkerBox[3][3].color="red";
        rPoints += game.countLine(3, "red", game.checkerBox[3][3].id);
        game.redMarkers--;
        game.checkerBox[4][1].color="red";
        rPoints += game.countLine(3, "red", game.checkerBox[4][1].id);
        game.redMarkers--;
        game.checkerBox[4][2].color="red";
        rPoints += game.countLine(3, "red", game.checkerBox[4][2].id);
        game.redMarkers--;
        game.checkerBox[4][3].color="red";
        rPoints += game.countLine(3, "red", game.checkerBox[4][3].id);
        game.redMarkers--;
        game.checkerBox[4][4].color="red";
        rPoints += game.countLine(3, "red", game.checkerBox[4][4].id);
        game.redMarkers--;
*/
  /*
        rPoints+= cheat(0, 0, "red");
        rPoints+= cheat(0, 2, "red");
        rPoints+= cheat(0, 3, "red");
        rPoints+= cheat(0, 4, "red");
        rPoints+= cheat(2, 0, "red");
        rPoints+= cheat(3, 0, "red");
        rPoints+= cheat(4, 1, "red");
        rPoints+= cheat(4, 2, "red");
        bPoints+= cheat(1, 0, "blue");
        bPoints+= cheat(1, 1, "blue");
        bPoints+= cheat(1, 2, "blue");
        bPoints+= cheat(2, 2, "blue");
        bPoints+= cheat(2, 3, "blue");
        bPoints+= cheat(3, 1, "blue");
      //  bPoints+= cheat(3, 3, "blue");
        bPoints+= cheat(3, 4, "blue");
        bPoints+= cheat(4, 3, "blue");

        game.bluePoints+=bPoints;
        game.redPoints+=rPoints;

        Resources res = getApplicationContext().getResources();

        Drawable pionBleu = ResourcesCompat.getDrawable(res, R.drawable.ic_pion_bleu_contour_noir, null);
        TextView bluePointsTV = findViewById(R.id.bluePoints);
        bluePointsTV.setText(String.format("%s", game.bluePoints));
        TextView blueMarkerTV = findViewById(R.id.blueMarkerTextView);
        blueMarkerTV.setText(String.format("%s", game.blueMarkers));

        Drawable pionRouge = ResourcesCompat.getDrawable(res, R.drawable.ic_pion_rouge_contour_noir, null);
        TextView redPointsTV = findViewById(R.id.redPoints);
        redPointsTV.setText(String.format("%s", game.redPoints));
        TextView redMarkerTV = findViewById(R.id.redMarkerTextView);
        redMarkerTV.setText(String.format("%s", game.redMarkers));

        for (int i =0; i<5; i++) {
            for (int j = 0; j < 5; j++) {
                if (game.checkerBox[i][j].color.equals("blue")) {
                    ImageView uneCase = findViewById(game.checkerBox[i][j].id);
                    uneCase.setBackground(pionBleu);
                } else if (game.checkerBox[i][j].color.equals("red")) {
                    ImageView uneCase = findViewById(game.checkerBox[i][j].id);
                    uneCase.setBackground(pionRouge);
                }
            }
        }
        */
        System.out.println("onButtonCheatClicked1 flagCheat:"+flagCheat);
        if (!flagCheat) {
            flagCheat=true;
            TextView blueMarkersTextview = findViewById(R.id.blueMarkerTextView);
            blueMarkersTextview.setClickable(true);
            TextView redMarkersTextview = findViewById(R.id.redMarkerTextView);
            redMarkersTextview.setClickable(true);
        }
        else{
            flagCheat=false;
            TextView blueMarkersTextview = findViewById(R.id.blueMarkerTextView);
            blueMarkersTextview.setClickable(false);
            blueFlagCheat=false;
            TextView redMarkersTextview = findViewById(R.id.redMarkerTextView);
            redMarkersTextview.setClickable(false);
            redFlagCheat=false;
        }
        System.out.println("onButtonCheatClicked2 flagCheat:"+flagCheat);
    }

    public void onColorMarkersNbTVClicked(View v){
        if (flagCheat){
            System.out.println("flagCheat: "+flagCheat);
            if (v.getTag().toString().equals("blueMarkerTextView")){
                blueFlagCheat=true;
                redFlagCheat=false;
                System.out.println("blueFlagCheat: "+blueFlagCheat);
                System.out.println("redFlagCheat: "+redFlagCheat);
            }
            else if (v.getTag().toString().equals("redMarkerTextView")){
                redFlagCheat=true;
                blueFlagCheat=false;
                System.out.println("blueFlagCheat: "+blueFlagCheat);
                System.out.println("redFlagCheat: "+redFlagCheat);
            }
        }
        else
            System.out.println("No cheats!");
    }

    public void placeMarkerById(int id){
        placeMarker(findViewById(id));
    }

    private void placeMarker(View v) {
        Resources res = getApplicationContext().getResources();
        Box clickedBox = game.findBoxById(v.getId());
        //Gerer le cas ou c'est un appel reussi
        if (game.findBoxById(v.getId()).figType.equals("Appel")){
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
            clickedBox.color = "blue";
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
                //TODO faire apparaitre la fenetre de menu a la place de enableNewGameButton()
            }
            else game.changeTurnColor("red");
        }
        else if (game.couleur.equals("red")) { //Check if it is this player's turn to play
            //Check if it's ok to place the marker in this particular Box
            Drawable pionRouge = ResourcesCompat.getDrawable(res, R.drawable.ic_pion_rouge_contour_noir, null);
            ImageView uneCase = findViewById(v.getId());
            uneCase.setBackground(pionRouge);
            clickedBox.color = "red";
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
                //TODO faire apparaitre la fenetre de menu a la place de enableNewGameButton()
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
                    if ((game.fiveDices.figureList.isEmpty()) || (game.getListFreeBoxesPerFigureList(game.fiveDices.figureList).isEmpty())) //pas de figure posables ni appel
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
//TODO ajouter une animation qd on change de joueur et qd un évènement survient (point, appel, victoire, Yam)
//TODO ajouter YAAAATZOOOE en cas de victoire (fichier son)
//TODO voir jeu réseau
