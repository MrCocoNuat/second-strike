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

import java.util.ArrayList;

class MapTile{
    private ArrayList<Missile> arsenal = new ArrayList<Missile>(4);
    private int owner;
    private int type;
    private boolean damaged;
    private boolean dirty;

    MapTile(){
	owner = -1;
	type = 0;
	damaged = false;
	dirty = false;
    }
    
    void setType(int t){
	type = t;
    }
    int getType(){
	return type;
    }
    ArrayList<Missile> getArsenal(){
	return arsenal;
    }
    int getSize(){
	return arsenal.size();
    }
    void build(int m){
	arsenal.add(new Missile(m));
    }
    void debuild(int i){
	arsenal.remove(i);
    }
    boolean isDamaged(){
	return damaged;
    }
    void destroy(){
	damaged = true;
	owner = -1;
	arsenal.clear();
    }
    boolean isDirty(){
	return dirty;
    }
    void dirty(){
	dirty = true;
    }
    void setOwner(int o){
	owner = o;
    }

    int getOwner(){
	return owner;
    }
}
