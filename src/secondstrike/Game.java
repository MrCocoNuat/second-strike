/*
 * secondstrike
 * Copyright (C) 2020-2021 Aaron Wang
 *
 * This file is part of secondstrike.jar.
 *
 * secondstrike.jar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * secondstrike.jar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this secondstrike.jar.  If not, see <http://www.gnu.org/licenses/>
 */

package secondstrike;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.Font;

class Game implements DrawListener{

    Draw gui = new Draw();
    MapTile[][] map = new MapTile[9][9];
    int actions = 1;

    int turn = 0;
    int endState = -1;
    boolean war = false;

    Font bigFont = new Font("SANS_SERIF",0,80);
    
    boolean[] capitals = {true,true};
    int[][] researchPoints = new int[2][4];
    int[][] researchTargets = {{0,12,24,24},{0,12,24,24}};
    boolean[][] researches = {{true,false,false,false},{true,false,false,false}};

    int expectedInput = -1; //null, board, button, research

    volatile boolean choosing;
    int[] choice = new int[2];

    int[] ranges = {2,100,4,6};
    int[] times = {3,5,7,9};
    ArrayList<FlyingMissile> transitors = new ArrayList<FlyingMissile>();


    public static void main(String[] args){
	Game thing = new Game();
    }
    
    Game(){
	
	initMap();
	gui.enableDoubleBuffering();
	gui.addListener(this);
	gui.setCanvasSize(1150,1000);
	gui.setXscale(0,1150);
	gui.setYscale(0,1000);
	gui.clear(Draw.BLACK);
	
	int player = 0;
	while (endState == -1){
	    gameTurn(player);
	    turn++;
	    player = 1 - player;
	}
	gui.setFont(bigFont);
	switch (endState){
	    
	case 0:
	    gui.setPenColor(Draw.RED);
	    gui.text(450,50,"PLAYER 1 WINS?");
	    break;
	case 1:
	    gui.setPenColor(Draw.BLUE);
	    gui.text(450,50,"PLAYER 2 WINS?");
	    break;
	case 2:
	    gui.setPenColor(Draw.WHITE);
	    gui.text(450,50,"EVERYBODY WINS!");
	    break;
	}

	guiResearch();
	gui.show();
	gui.pause(30000);
	System.exit(0);
    }

    
    void initMap(){
	for(int i = 0; i < 9; i++){
	    for(int j = 0; j < 9; j++){
		map[i][j] = new MapTile();
	    }
	}
		
	int[][] capitals = new int[2][2];
	boolean valid = false;
	do {
	    for(int i = 0; i < 2; i++){
		for(int j = 0; j < 2; j++){
		    capitals[i][j] = (int) (9 * Math.random());
		}
	    }
	    valid = (Math.abs(capitals[0][0] - capitals[1][0]) > 2) || (Math.abs(capitals[0][1] - capitals[1][1]) > 2);
	}while (!valid);

	for(int i = 0; i < 2; i++){
	    MapTile active = map[capitals[i][0]][capitals[i][1]];
	    active.setOwner(i);
	    active.setType(1);
	    active.build((Math.random() < 0.75)? 0 : 1);

	    int ext = 0;
	    do {
		ext = (int)(4 * Math.random());
		valid = !((ext == 0 && capitals[i][0] == 8) || (ext == 1 && capitals[i][1] == 8) || (ext == 2 && capitals[i][0] == 0) || (ext == 3 && capitals[i][1] == 0));
	    }while (!valid);
	    active = map[capitals[i][0] + ((ext == 0)? 1: ((ext == 2)? -1 : 0))][capitals[i][1] + ((ext == 1)? 1: ((ext == 3)? -1 : 0))];
	    active.setOwner(i);
	    active.build((Math.random() < 0.75)? 1 : 0);
	}

	for(int i = 0; i < 9; i++){
	    for(int j = 0; j < 9; j++){
		if (map[i][j].getOwner() == -1){
		    if (Math.random() < 0.1){
			map[i][j].setType(2);
			map[i][j].build(1);
			map[i][j].build((Math.random() < 0.5)? 0 : (Math.random() < 0.5)? 2 : 3);
		    }
		    else if (Math.random() < 0.1){
			map[i][j].dirty();
		    }
		}
	    }
	}
    }

    void destroy(int x, int y){
	MapTile active = map[x][y];
	active.destroy();
	if (active.getType() == 2){
	    active.setType(0);
	    if (x < 8) destroy(x+1,y);
	    if (x > 0) destroy(x-1,y);
	    if (y < 8) destroy(x,y+1);
	    if (y > 0) destroy(x,y-1);
	}
    }
    
    int tileCount(int player){
	int ans = 0;
	for(int i = 0; i < 9; i++){
	    for(int j = 0; j < 9; j++){
		if (map[i][j].getOwner() == player) ans++;
	    }
	}
	return ans;
    }



    void tickMissiles(){
	for(FlyingMissile missile : transitors){
	    missile.travel();
	    if (missile.getTurns() == -1){
		destroy(missile.getTarget()[0],missile.getTarget()[1]);
		if (missile.getType() == 3){
		    map[missile.getTarget()[0]][missile.getTarget()[1]].dirty();
		}
	    }
	}
	for(int i = 0; i < transitors.size(); i++){//nonconcurrent modification
	    if (transitors.get(i).getTurns() == -1) {
		transitors.remove(i);
		i--;
	    }
	}
    }
    void interceptMissiles(){
	for(int i = 0; i < transitors.size(); i++){
	    if (transitors.get(i).getHealth() == 0) {
		transitors.remove(i);
		i--;
	    }
	}
    }
    void sortMissiles(){
	Collections.sort(transitors);
    }
    
    void gameTurn(int player){
	actions = 2 + Math.min(6,tileCount(player)/6);
	if (turn == 0) actions = 1;

	tickMissiles();

	
	while(actions != -1){
	    interceptMissiles();
	    sortMissiles();

	    capitals[0] = false;
	    capitals[1] = false;
	    for(int i = 0; i < 2; i++){
		for(int j = 0; j < 9; j++){
		    for(int k = 0; k < 9; k++){
			capitals[i] = capitals[i] || (map[j][k].getOwner() == i && map[j][k].getType() == 1);
		    }
		}
	    }

	    
	    gui.clear(Draw.BLACK);
	    for(int i = 0; i < 9; i++){
		for(int j = 0; j < 9; j++){
		    guiTile(i,j);
		}
	    }
	    
	    for(int i = 0; i < 2; i++){
		if (tileCount(1 - i) == 0) {
		    endState = i;
		    break;
		}
	    }
	    if (endState != -1) break;
	    
	    int missileCount = 0;
	    for(int i = 0; i < 9; i++){//easter egg: just had to look, you cheeky bastard :)
		for(int j = 0; j < 9; j++){
		    if (map[i][j].getOwner() != -1) missileCount += map[i][j].getSize();
		}
	    }
	    if (!war && missileCount == 0){
		endState = 2;
		break;
	    }
	    




	    //process internal increments like guiMissiles,

	    
	    guiActions(actions,player);
	    guiButtons(0,-1,-1);
	    guiResearch();
	    guiMissile();
	    gui.show();
	    
	    expectedInput = 1;
	    gui.setPenColor(Draw.WHITE);
	    gui.rectangle(550,50,345,45);
	    gui.show();
	    choosing = true;
	    
	    while(choosing) gui.pause(10);
	    
	    gui.setPenColor(Draw.BLACK);
	    gui.rectangle(550,50,345,45);
	    gui.show();
	    
	    int buttonInput = choice[0];
	    
	    if (actions > 0 && buttonInput == 0){ //build
		gui.setPenColor(Draw.CYAN);
		gui.square(250,50,40);
		gui.show();

		boolean valid = false;
		boolean canceled = false;
		while(!valid){

		    expectedInput = 0;
		    gui.setPenColor(Draw.WHITE);
		    gui.square(450,550,447);
		    gui.show();
		    choosing = true;
		    while(choosing) gui.pause(10);

		    if (choice [0] == -1){
			canceled = true;
			break;
		    }
		    gui.setPenColor(Draw.BLACK);
		    gui.square(450,550,447);
		    gui.show();

		    int cx = choice[0];
		    int cy = choice[1];
		    MapTile active = map[cx][cy];
		    if (active.getOwner() == player && (active.getSize() < 2 - ((active.isDamaged())? 1 : 0) || active.getSize() < 4 && active.getType() == 2)) valid = true; 
		}

		gui.setPenColor(Draw.BLACK);
		gui.square(250,50,40);
		gui.show();
		
		if(!canceled){
		    valid = false;
		    int cx = choice[0];
		    int cy = choice[1];
		    gui.setPenColor((player == 0)? Draw.RED : Draw.BLUE);
		    gui.square(50 + 100 * cx, 950 - 100 * cy, 50);
		    
		    while(!valid){
			expectedInput = 1;
			gui.setPenColor(Draw.WHITE);
			gui.rectangle(550,50,345,45);
			gui.show();
			guiButtons(2,-1,-1);
			gui.show();
			choosing = true;
			while (choosing) gui.pause(10);

			gui.setPenColor(Draw.BLACK);
			gui.rectangle(550,50,345,45);
			gui.show();
			
			if (choice[0] == 6){
			    canceled = true;
			    gui.setPenColor(Draw.BLACK);
			    gui.square(250,50,40);
			    gui.show();
			    break;
			}

			if (choice[0] < 4){
			    if (researches[player][choice[0]]){
				map[cx][cy].build(choice[0]);
				valid = true;
				actions--;
			    }
			}
		    }
		    
		}
	    }
	    
	    if (actions > 0 && buttonInput == 1){ //debuild
		gui.setPenColor(Draw.CYAN);
		gui.square(350,50,40);
		gui.show();

		boolean valid = false;
		boolean canceled = false;
		while(!valid){

		    expectedInput = 0;
		    gui.setPenColor(Draw.WHITE);
		    gui.square(450,550,447);
		    choosing = true;
		    while(choosing) gui.pause(10);

		    if (choice [0] == -1){
			canceled = true;
			break;
		    }
		    gui.setPenColor(Draw.BLACK);
		    gui.square(450,550,447);
		    gui.show();
		    
		    int cx = choice[0];
		    int cy = choice[1];
		    MapTile active = map[cx][cy];
		    if (active.getOwner() == player && active.getSize() > 0) valid = true; 
		}

		gui.setPenColor(Draw.BLACK);
		gui.square(350,50,40);
		gui.show();

		if(!canceled){
		    valid = false;
		    int cx = choice[0];
		    int cy = choice[1];
		    gui.setPenColor((player == 0)? Draw.RED : Draw.BLUE);
		    gui.square(50 + 100 * cx, 950 - 100 * cy, 50);
		    
		    while(!valid){
			expectedInput = 1;
			gui.setPenColor(Draw.WHITE);
			gui.rectangle(550,50,345,45);
			gui.show();
			guiButtons(1,cx,cy);
			gui.show();
			choosing = true;
			while (choosing) gui.pause(10);

			gui.setPenColor(Draw.BLACK);
			gui.rectangle(550,50,345,45);
			gui.show();
			
			if (choice[0] == 6){
			    canceled = true;
			    gui.setPenColor(Draw.BLACK);
			    gui.square(250,50,40);
			    gui.show();
			    break;
			}

			if (choice[0] < map[cx][cy].getSize()){
			    map[cx][cy].debuild(choice[0]);
			    valid = true;
			    actions--;
			    
			}
		    }
		    
		}
	    }
	    
	    if (actions > 0 && buttonInput == 2){ //expand
		gui.setPenColor(Draw.CYAN);
		gui.square(450,50,40);
		gui.show();
		
		boolean valid = false;
		boolean canceled = false;
		while (!valid){
		    
		    expectedInput = 0;
		    gui.setPenColor(Draw.WHITE);
		    gui.square(450,550,447);
		    gui.show();
		    choosing = true;
		    while (choosing) gui.pause(10);

		    if (choice[0] == -1){
			canceled = true;
			break;
		    }
		    gui.setPenColor(Draw.BLACK);
		    gui.square(450,550,447);
		    gui.show();
		    
		    int cx = choice[0];
		    int cy = choice[1];
		    if (cx < 8 && map[cx + 1][cy].getOwner() == player) valid = true;
		    if (cy < 8 && map[cx][cy + 1].getOwner() == player) valid = true;
		    if (cx > 0 && map[cx - 1][cy].getOwner() == player) valid = true;
		    if (cy > 0 && map[cx][cy - 1].getOwner() == player) valid = true;
		    if (map[cx][cy].getOwner() != -1 || map[cx][cy].isDirty()) valid = false;
		}
		if (!canceled){
		    map[choice[0]][choice[1]].setOwner(player);
		    actions--;
		}

		gui.setPenColor(Draw.BLACK);
		gui.square(450,50,40);
		gui.show();
	    }
	    
	    if (actions > 0 && buttonInput == 3){ //research
		gui.setPenColor(Draw.CYAN);
		gui.square(550,50,40);
		gui.show();
		
		boolean valid = false;
		boolean canceled = false;
		while(! valid){

		    expectedInput = 2;
		    gui.setPenColor(Draw.WHITE);
		    gui.rectangle(1025,500,120,495);
		    gui.show();
		    choosing = true;
		    while(choosing) gui.pause(10);

		    gui.setPenColor(Draw.BLACK);
		    gui.rectangle(1025,500,120,495);
		    gui.show();
	    
		    if (choice[0] == -1){
			canceled = true;
			break;
		    }
		    if (!researches[player][choice[0]]) valid = true; 
		}

		if (!canceled){
		    if (capitals[player]) researchPoints[player][choice[0]]++;
		    researchPoints[player][choice[0]]++;
		    if (researchPoints[player][choice[0]] >= researchTargets[player][choice[0]]) researches[player][choice[0]] = true;
		    actions--;
		}
		
		gui.setPenColor(Draw.BLACK);
		gui.square(550,50,40);
		gui.show();
		
	    }
	    
	    if (actions > -1 && buttonInput == 4){ // attack
		gui.setPenColor(Draw.ORANGE);
		gui.square(650,50,40);
		gui.show();
		
		boolean valid = false;
		boolean canceled = false;
		while(!valid){
		    
		    expectedInput = 0;
		    gui.setPenColor(Draw.WHITE);
		    gui.square(450,550,447);
		    gui.show();
		    choosing = true;
		    while(choosing) gui.pause(10);
		    
		    if (choice [0] == -1){
			canceled = true;
			break;
		    }
		    gui.setPenColor(Draw.BLACK);
		    gui.square(450,550,447);
		    gui.show();
		    
		    int cx = choice[0];
		    int cy = choice[1];
		    MapTile active = map[cx][cy];
		    if (active.getOwner() == player && active.getSize() > 0) valid = true; 
		}
		
		gui.setPenColor(Draw.BLACK);
		gui.square(650,50,40);
		gui.show();
		
		if (!canceled){
		    int sx = choice[0];
		    int sy = choice[1];
		    
		    valid = false;
		    gui.setPenColor((player == 0)? Draw.RED : Draw.BLUE);
		    gui.square(50 + 100 * sx, 950 - 100 * sy, 50);
		    
		    while(!valid){
			expectedInput = 1;
			gui.setPenColor(Draw.WHITE);
			gui.rectangle(550,50,345,45);
			gui.show();
			guiButtons(1,sx,sy);
			gui.show();
			choosing = true;
			while (choosing) gui.pause(10);

			gui.setPenColor(Draw.BLACK);
			gui.rectangle(550,50,345,45);
			gui.show();
			
			if (choice[0] == 6){
			    canceled = true;
			    break;
			}
			
			if (choice[0] < map[sx][sy].getSize()){
			    gui.setPenColor(Draw.CYAN);
			    gui.square(250 + 100 * choice[0],50,40);
			    gui.show();
			    valid = true; 
			}
		    }
		    
		    
		    if (!canceled){
			int missileNumber = choice[0];
			int missileType = map[sx][sy].getArsenal().get(missileNumber).getType();

			gui.setPenColor(Draw.MAGENTA);
			gui.line(50 + 100 * (sx - ranges[missileType]) , 950 - 100 * sy, 50 + 100 * sx , 950 - 100 * (sy + ranges[missileType]));
			gui.line(50 + 100 * sx , 950 - 100 * (sy + ranges[missileType]), 50 + 100 * (sx + ranges[missileType]) , 950 - 100 * sy);
			gui.line(50 + 100 * (sx + ranges[missileType]) , 950 - 100 * sy, 50 + 100 * sx , 950 - 100 * (sy - ranges[missileType]));
			gui.line(50 + 100 * sx , 950 - 100 * (sy - ranges[missileType]), 50 + 100 * (sx - ranges[missileType]) , 950 - 100 * sy);
			gui.setPenColor(Draw.BLACK); //redraw instead of culling because i am lazy
			gui.filledRectangle(1025, 500, 125, 500);
			guiResearch();
			gui.setPenColor(Draw.BLACK);
			gui.filledRectangle(450,50,450,50);
			guiActions(actions, player);
			guiButtons(1,sx,sy);
			gui.setPenColor(Draw.CYAN);
			gui.square(250 + 100 * missileNumber, 50, 40);
			gui.show();
			
			int[] tritx = new int[3]; //yeah this is super duper shoehorned but whatever it works
			int[] trity = new int[3]; //don't do this kids
			
			targetSelection:
			for(int i = 0; i < ((missileType == 2)? 3 : 1); i++){
			    valid = false;
			    while(!valid){
				
				expectedInput = 0;
				gui.setPenColor(Draw.WHITE);
				gui.square(450,550,447);
				gui.show();
				choosing = true;
				while(choosing) gui.pause(10);
			    
				if (choice [0] == -1){
				    canceled = true;
				    break targetSelection;
				}
				gui.setPenColor(Draw.BLACK);
				gui.square(450,550,447);
				gui.show();
		    
				int cx = choice[0];
				int cy = choice[1];
				
				MapTile active = map[cx][cy];
				if (active.getOwner() != player && Math.abs(cx - sx) + Math.abs(cy - sy) <= ranges[missileType]) valid = true; 
			    
			    }
			    
			    if (missileType == 2){
				tritx[i] = choice[0];
				trity[i] = choice[1];
				gui.setPenColor(Draw.YELLOW);
				gui.square(50 + choice[0] * 100, 950 - choice[1] * 100, 50);
				gui.show();
			    }
			    
			}
			
			if (!canceled){
			    for(int i = 0; i < ((missileType == 2)? 3 : 1); i++){
				int tx, ty = -1;
				if (missileType == 2){
				    tx = tritx[i];
				    ty = trity[i];
				}
				else {
				    tx = choice[0];
				    ty = choice[1];
				}
				int[] source = {sx,sy};
				int[] target = {tx,ty};
				transitors.add(new FlyingMissile(missileType, source, target, times[missileType] - ((map[sx][sy].getType() == 2)? 2 : 0)));
				war = true;
			    }
			    map[sx][sy].debuild(missileNumber);
			}
			gui.setPenColor(Draw.BLACK);
			gui.square(250 + 100 * missileNumber,50,40);
			gui.show();
		    }
		    gui.setPenColor(Draw.BLACK);
		    gui.square(50 + 100 * sx, 950 - 100 * sy, 50);
		    gui.show();
		}
	    }
	    
	    if (actions > -1 && buttonInput == 5){ // defend
		gui.setPenColor(Draw.ORANGE);
		gui.square(750,50,40);
		gui.show();
		
		boolean valid = false;
		boolean canceled = false;
		while(!valid){ //boy i really should make all of this a separate function to save 10kB...
		    
		    expectedInput = 0;
		    gui.setPenColor(Draw.WHITE);
		    gui.square(450,550,447);
		    gui.show();
		    choosing = true;
		    while(choosing) gui.pause(10);
		    
		    if (choice [0] == -1){
			canceled = true;
			break;
		    }
		    gui.setPenColor(Draw.BLACK);
		    gui.square(450,550,447);
		    gui.show();
		    
		    int cx = choice[0];
		    int cy = choice[1];
		    MapTile active = map[cx][cy];
		    for (Missile missile: map[cx][cy].getArsenal()){
			valid = valid || (missile.getType() == 0);
		    }
		    if (active.getOwner() != player) valid = false; 
		}
		
		gui.setPenColor(Draw.BLACK);
		gui.square(750,50,40);
		gui.show();

		if (!canceled){
		    int sx = choice[0];
		    int sy = choice[1];
		    
		    gui.setPenColor((player == 0)? Draw.RED : Draw.BLUE);
		    gui.square(50 + 100 * sx, 950 - 100 * sy, 50);
		    gui.show();

		    gui.setPenColor(Draw.MAGENTA);
		    gui.line(50 + 100 * (sx - 2) , 950 - 100 * sy, 50 + 100 * sx , 950 - 100 * (sy + 2));
		    gui.line(50 + 100 * sx , 950 - 100 * (sy + 2), 50 + 100 * (sx + 2) , 950 - 100 * sy);
		    gui.line(50 + 100 * (sx + 2) , 950 - 100 * sy, 50 + 100 * sx , 950 - 100 * (sy - 2));
		    gui.line(50 + 100 * sx , 950 - 100 * (sy - 2), 50 + 100 * (sx - 2) , 950 - 100 * sy);
		    gui.setPenColor(Draw.BLACK);
		    gui.filledRectangle(1025, 500, 125, 500);
		    guiResearch();
		    gui.setPenColor(Draw.BLACK);
		    gui.filledRectangle(450,50,450,50);
		    guiActions(actions, player);
		    guiButtons(0,-1,-1);
		    gui.setPenColor(Draw.ORANGE);
		    gui.square(750,50,40);
		    gui.show();

		    valid = false;
		    while(!valid){
			
			expectedInput = 0;
			gui.setPenColor(Draw.WHITE);
			gui.square(450,550,447);
			gui.show();
			choosing = true;
			while(choosing) gui.pause(10);
			
			if (choice [0] == -1){
			    canceled = true;
			    break;
			}
			gui.setPenColor(Draw.BLACK);
			gui.square(450,550,447);
			gui.show();
		    
			int cx = choice[0];
			int cy = choice[1];
			MapTile active = map[cx][cy];
			for (FlyingMissile missile: transitors){
			    valid = valid || (missile.getTarget()[0] == cx && missile.getTarget()[1] == cy);
			}
			if (Math.abs(cx - sx) + Math.abs(sx - cx) > 2) valid = false;
			
		    }

		    if (!canceled){
			int tx = choice[0];
			int ty = choice[1];

			FlyingMissile interception = null;
			for(FlyingMissile missile : transitors){
			    if (missile.getTarget()[0] == tx && missile.getTarget()[1] == ty){
				missile.intercept();
				break;
			    }
			}

			for(int i = 0; i < map[sx][sy].getArsenal().size(); i++){
			    if (map[sx][sy].getArsenal().get(i).getType() == 0){
				map[sx][sy].debuild(i);
				break;
			    }
			}
			
		    }
		    
		    
		    gui.setPenColor(Draw.BLACK);
		    gui.square(50 + 100 * sx, 950 - 100 * sy, 50);
		    gui.show();
		}

		
	    }
	    
	    if (buttonInput == 6){ //nothing
		actions = -1;

		
	    }
	}
	
	turn++;
    }
    
    void guiTile(int x, int y){
	MapTile active = map[x][y];
	Color hue = active.isDirty()?
	    new Color(0,200,0)
	    : (active.isDamaged()?
	       ((active.getOwner() == -1)?
		new Color(50,50,50)
		: ((active.getOwner() == 0)?
		   new Color(100,0,0)
		   : new Color(0,0,100)))
	       : ((active.getOwner() == -1)?
		  new Color(200,200,200)
		  : ((active.getOwner() == 0)?
		     new Color(200,0,0)
		     : new Color(0,0,200))));

	gui.setPenColor(hue);
	gui.square(100 * x  + 50, 950 - 100 * y, 45);

	if (active.getType() == 1) gui.picture(100 * x + 51, 960 - 100 * y, "capital.png");
	if (active.getType() == 2) gui.picture(100 * x + 51, 960 - 100 * y, "silo.png");

	ArrayList<Missile> missiles = active.getArsenal();
	for(int i = 0; i < missiles.size(); i++){
	    Missile missile = missiles.get(i);
	    switch (missile.getType()){
	    case 0: gui.setPenColor(Draw.BLUE);
		break;
	    case 1: gui.setPenColor(Draw.RED);
		break;
	    case 2: gui.setPenColor(Draw.YELLOW);
		break;
	    case 3: gui.setPenColor(Draw.GREEN);
		break;
	    }
	    gui.filledRectangle(100 * x + 35 + 10 * i, 920 - 100 * y, 2, 10);
	}
    }

    void guiActions(int actions, int player){
	gui.setPenColor((player == 0)? Draw.RED : Draw.BLUE);
	for(int i = 0; i < 4; i++){
	    if (i < actions) gui.filledCircle(25 + 50 * i, 75, 20);
	    else gui.circle(25 + 50 * i, 75, 20);
	}
	for(int i = 4; i < 8; i++){
	    if (i < actions) gui.filledCircle(-175 + 50 * i, 25, 20);
	    else gui.circle(-175 + 50 * i, 25, 20);
	}
    }

    void guiButtons(int mode, int x, int y){
	if (mode == 0){
	    gui.setPenColor(Draw.CYAN);
	    for(int i = 0; i < 4; i++){
		gui.square(250 + 100 * i, 50, 37);
	    }
	    gui.setPenColor(Draw.ORANGE);
	    for(int i = 4; i < 6; i++){
		gui.square(250 + 100 * i, 50, 37);
	    }
	    gui.setPenColor(Draw.MAGENTA);
	    gui.square(850, 50, 37);
	    
	    gui.picture(250,50,"buttonAdd.png");
	    gui.picture(350,50,"buttonScrap.png");
	    gui.picture(450,50,"buttonExpand.png");
	    gui.picture(550,50,"buttonResearch.png");
	    gui.picture(650,50,"buttonAttack.png");
	    gui.picture(750,50,"buttonDefend.png");
	    gui.picture(850,50,"buttonNothing.png");
	} //main mode
	
	if (mode == 1){
	    MapTile active = map[x][y];
	    gui.setPenColor(Draw.BLACK);
	    for(int i = 0; i < 6; i++){
		gui.filledSquare(250 + 100 * i,50,35);
	    }
	    for(int i = 0; i < active.getSize(); i++){
		Missile current = active.getArsenal().get(i);
		if (current.getType() == 0) gui.picture(250 + 100 * i,50,"buttonCruise.png");
		if (current.getType() == 1) gui.picture(250 + 100 * i,50,"buttonICBM.png");
		if (current.getType() == 2) gui.picture(250 + 100 * i,50,"buttonTrident.png");
		if (current.getType() == 3) gui.picture(250 + 100 * i,50,"buttonDirty.png");
	    }
	} //arsenal mode

	if (mode == 2){
	    gui.picture(250,50,"buttonCruise.png");
	    gui.picture(350,50,"buttonICBM.png");
	    gui.picture(450,50,"buttonTrident.png");
	    gui.picture(550,50,"buttonDirty.png");
	    gui.setPenColor(Draw.BLACK);
	    gui.filledSquare(650,50,35);
	    gui.filledSquare(750,50,35);
	} //catalog mode
    }

    void guiResearch(){
	gui.setPenColor(Draw.BLUE);
	gui.square(1025, 140, 100);
	gui.picture(1025,140,"researchCruise.png");
	gui.setPenColor(Draw.RED);
	gui.square(1025, 390, 100);
	gui.picture(1025,390,"researchICBM.png");
	gui.setPenColor(Draw.YELLOW);
	gui.square(1025, 640, 100);
	gui.picture(1025,640,"researchTrident.png");
	gui.setPenColor(Draw.GREEN);
	gui.square(1025, 890, 100);
	gui.picture(1025,890,"researchDirty.png");
	
	gui.setPenColor(Draw.RED);
	for(int i = 0; i < 6; i++){
	    if (researchPoints[0][1] > i) gui.filledCircle(915 + 20 * i, 275, 10);
	    else gui.circle(915 + 20 * i, 275, 10);
	}
	for(int i = 6; i < 12; i++){
	    if (researchPoints[0][1] > i) gui.filledCircle(915 - 120 + 20 * i, 255, 10);
	    else gui.circle(915 - 120 + 20 * i, 255, 10);
	}
	for(int i = 0; i < 8; i++){
	    if (researchPoints[0][2] > i) gui.filledCircle(920 + 14 * i, 530, 7);
	    else gui.circle(920 + 14 * i, 530, 7);
	}
	for(int i = 8; i < 16; i++){
	    if (researchPoints[0][2] > i) gui.filledCircle(920 - 112 + 14 * i, 516, 7);
	    else gui.circle(920 - 112 + 14 * i, 516, 7);
	}
	for(int i = 16; i < 24; i++){
	    if (researchPoints[0][2] > i) gui.filledCircle(920 - 224 + 14 * i, 502, 7);
	    else gui.circle(920 - 224 + 14 * i, 502, 7);
	}
	for(int i = 0; i < 8; i++){
	    if (researchPoints[0][3] > i) gui.filledCircle(920 + 14 * i, 780, 7);
	    else gui.circle(920 + 14 * i, 780, 7);
	}
	for(int i = 8; i < 16; i++){
	    if (researchPoints[0][3] > i) gui.filledCircle(920 - 112 + 14 * i, 766, 7);
	    else gui.circle(920 - 112 + 14 * i, 766, 7);
	}
	for(int i = 16; i < 24; i++){
	    if (researchPoints[0][3] > i) gui.filledCircle(920 - 224 + 14 * i, 752, 7);
	    else gui.circle(920 - 224 + 14 * i, 752, 7);
	}
	
	gui.setPenColor(Draw.BLUE);
	for(int i = 0; i < 6; i++){
	    if (researchPoints[1][1] > i) gui.filledCircle(1035 + 20 * i, 275, 10);
	    else gui.circle(1035 + 20 * i, 275, 10);
	}
	for(int i = 6; i < 12; i++){
	    if (researchPoints[1][1] > i) gui.filledCircle(1035 - 120 + 20 * i, 255, 10);
	    else gui.circle(1035 - 120 + 20 * i, 255, 10);
	}
	for(int i = 0; i < 8; i++){
	    if (researchPoints[1][2] > i) gui.filledCircle(1032 + 14 * i, 530, 7);
	    else gui.circle(1032 + 14 * i, 530, 7);
	}
	for(int i = 8; i < 16; i++){
	    if (researchPoints[1][2] > i) gui.filledCircle(1032 - 112 + 14 * i, 516, 7);
	    else gui.circle(1032 - 112 + 14 * i, 516, 7);
	}
	for(int i = 16; i < 24; i++){
	    if (researchPoints[1][2] > i) gui.filledCircle(1032 - 224 + 14 * i, 502, 7);
	    else gui.circle(1032 - 224 + 14 * i, 502, 7);
	}
		for(int i = 0; i < 8; i++){
	    if (researchPoints[1][3] > i) gui.filledCircle(1032 + 14 * i, 780, 7);
	    else gui.circle(1032 + 14 * i, 780, 7);
	}
	for(int i = 8; i < 16; i++){
	    if (researchPoints[1][3] > i) gui.filledCircle(1032 - 112 + 14 * i, 766, 7);
	    else gui.circle(1032 - 112 + 14 * i, 766, 7);
	}
	for(int i = 16; i < 24; i++){
	    if (researchPoints[1][3] > i) gui.filledCircle(1032 - 224 + 14 * i, 752, 7);
	    else gui.circle(1032 - 224 + 14 * i, 752, 7);
	}
    }

    void guiMissile(){
	for(FlyingMissile missile: transitors){
	    int sx = missile.getSource()[0];
	    int sy = missile.getSource()[1];
	    int tx = missile.getTarget()[0];
	    int ty = missile.getTarget()[1];
	    int[] sd = missile.getDeviation()[0];
	    int[] td = missile.getDeviation()[1];
	    int type = missile.getType();
	    int turn = missile.getTurns();
	    int time = times[type];

	    int csx = 50 + 100 * sx + sd[0]; //coordinate
	    int csy = 950 - 100 * sy + sd[1];
	    int ctx = 50 + 100 * tx + td[0];
	    int cty = 950 - 100 * ty + td[1];
	    gui.setPenColor((type == 0)? Draw.BLUE : ((type == 1)? Draw.RED : ((type == 2)? Draw.YELLOW : Draw.GREEN)));
	    gui.line(csx, csy, ctx, cty);
	    gui.line(ctx - 20, cty, ctx + 20, cty);
	    gui.line(ctx, cty - 20, ctx, cty + 20);
	    gui.circle(ctx, cty, 16);
	   
	    double mx = ctx - (1.0 * turn / time) * (ctx - csx);
	    double my = cty - (1.0 * turn / time) * (cty - csy);
	    gui.filledCircle(mx, my, 10);
	    for(int i = 2; i < 4; i++){
		if (missile.getHealth() >= i) gui.circle(mx, my, 6 + 4 * i);
	    }
	    
	}
    }
    
    public void mousePressed(double x, double y){ 
	if (expectedInput == -1) return;
	if (expectedInput == 0){ //board
	    if (x > 900) {
		return;
	    }
	    if (y < 100){
		if (x > 813 && x < 878 && y > 13 && y < 87){ //cancel
		    choice[0] = -1;
		    choosing = false;
		    return;
		}
		else {
		    return;
		}
	    }
	    else{
		choice[0] = (int) (x / 100);
		choice[1] = 9 - (int) (y / 100);
		choosing = false;
		return;
	    }
	}
	if (expectedInput == 1){ //bar
	    if (y < 13 || y > 87){
		return;
	    }
	    for(int i = 0; i < 7; i++){
		if (x > 213 + 100 * i  && x < 287 + 100 * i){
		    choice[0] = (int) (x / 100) - 2;
		    choosing = false;
		    return;
		}
	    }
	    return;
	} 
	if (expectedInput == 2){ //research
	    if (x < 900){
		if (x > 813 && x < 878 && y > 13 && y < 87){ //cancel
		    choice[0] = -1;
		    choosing = false;
		    return;
		}
	    }
	    else{
		for(int i = 0; i < 4; i++){
		    if (x > 925 && x < 1125 && y > 40 + 250 * i && y < 240 + 250 * i){
			choice[0] = (int) (y / 250);
			choosing = false;
			return;
		    }
		}
	    }
	    return;
	}
    }


    
    public void mouseDragged(double x, double y){}
    public void mouseReleased(double x, double y){}
    public void mouseClicked(double x, double y){}
    public void keyTyped(char c){}
    public void keyPressed(int keycode){}
    public void keyReleased(int keycode){}
}
