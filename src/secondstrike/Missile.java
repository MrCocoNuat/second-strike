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

class Missile{ //after parting out FlyingMissile this is literally just an int, hmmmmm
    private final int type;

    Missile(int type){
	this.type = type;
    }

    int getType(){
	return type;
    }

}
