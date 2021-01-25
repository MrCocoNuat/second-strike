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

class FlyingMissile implements Comparable<FlyingMissile>{
    private final int type;
    private final int[] source;
    private final int[] target;
    private int health;
    private int turnsLeft;
    private final int[][] deviation = {{(int) (60 * Math.random()) - 30, (int) (60 * Math.random()) - 30},{(int) (60 * Math.random()) - 30,(int) (60 * Math.random()) - 30}};

    FlyingMissile(int typ, int[] sour, int[] targ, int turn){
	type = typ;
	source = sour;
	target = targ;
	health = (type == 3)? 3 : 1;
	turnsLeft = turn;
    }

    int getType(){
	return type;
    }
    int[] getSource(){
	return source;
    }

    int[] getTarget(){
	return target;
    }

    int getHealth(){
	return health;
    }

    void intercept(){
	health--;
    }
    
    int getTurns(){
	return turnsLeft;
    }
    
    void travel(){
	turnsLeft--;
    }
    
    int[][] getDeviation(){
	return deviation;
    }

    @Override
    public int compareTo(FlyingMissile other){
	return ((turnsLeft == other.turnsLeft)? ((type == other.type)? health - other.health : other.type - type) : turnsLeft - other.turnsLeft);

	
    }
}
