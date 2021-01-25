# second-strike

This software is no longer maintained. It was mostly a school project anyway.

### General Instructions

To run this software:

Make sure you have a sufficiently modern version of Java installed, anything
11 (which I used to compile) and up is fine I think. Then, open the .jar file 
you want to run; if that doesn't work for whatever reason you can also use

 java -jar NAME.jar

in the command line, after moving to the directory this file is in.

### Second Strike (2P):

The year is 20XX and it's time for a Flame Deluge! Of course, this will only be
fun if you're the one left alive in the end. Nuke your opponent until their
territory is nothing but a wasteland to win.

This game is inspired by First Strike: Final Hour, and is also an extension of
an earlier project of mine.

#### Iinterface:

One window will appear when you run this game, the map.

*DO NOT CLOSE THE WINDOW WHILE THE GAME IS IN PROGRESS* or you will be unable
to continue playing, and the game will still be open in the background! At this
point, you should kill the process and start a new game if you want. Of course,
if you intend to quit the game, go ahead and kill the process. The window will
automatically close. 

The map window also has some extra stuff: in the bottom left, the action
counter is displayed. The bottom hosts the action bar, which is used to do
things. On the right is the research tracker.

#### Gameplay:

Player 1 goes first: player 1 will only have 1 action the first turn, but every
subsequent turn, each player gets 2 actions and up to 6 more, 1 for every 6
tiles controlled. When counting time in turns, both players taking a turn each
counts as two turns elapsing!

Actions can be spent on the action bar, which has 7 buttons. Any action can be
canceled using the cancel button, which the seventh button will act as once an
action is selected.

 1 Action:
 - Build Missile: Select a tile that you own that has not reached missile
   capacity, then select a missile type to build.
 - Scrap Missile: Select a tile that you own that has missiles, then select a
   missile to scrap.
 - Expand: Select an uncontrolled tile orthogonally adjacent to one you own and
   take control of it. You can expand to destroyed tiles, but irradiated tiles
   are off limits.
 - Research: Select a subject in the research tracker to add a research point.
   Subjects can be researched in any order. You cannot select already completed
   subjects.
 0 Actions:
 - Attack: Select a tile that you own that has missiles, then select a missile,
   then select a tile that you do not own to launch an attack.
 - Defend: Select a tile that you own that has a cruise missile, then select a
   tile that is being attacked. The intercepted missile is the one that will
   hit its target earliest, and dirty bombs will be prioritized if there is
   more than one missile that will strike on the same turn. You can defend your
   opponent's tiles if you feel like it.
 N Actions:
 - Nothing: Ends your turn. Don't press this by accident! This is the only
   possible move when you run out of actions and opportunities to attack or
   defend. As stated, if you are in the middle of performing an action, this
   will cancel that instead.

The different missiles function as such: 

 - Cruise Missile (blue): Fast and light, this takes only 3 turns to reach its
   destination. This missile can also intercept others; the target is the tile
   this missile will protect, which is reached in 0 turns. Either way, a tiny
   fuel tank means a range of only 2 tiles, taxicab metric. 0 research points,
   so this is available to build immediately. Blue.
 - ICBM (red): This super long-range missile can reach the entire map. It is slower
   though, taking 5 turns to destroy its target. 12 research points. RED.
 - Trident (yellow): A compound warhead allows this missile to hit 3 tiles after 7 turns
   simultaneously, but the huge payload restricts it to a 4 tile range. 24
   research points. Yellow.
 - Dirty Bomb(green): This missile is designed to ravage the land it hits; after 9
   turns of flight, this missile's target, which must be within 6 tiles, is
   irradiated. Dead-simple construction and thick depleted uranium plates also
   mean three cruise missiles are needed to destroy it. If this missile hits a
   silo, only the silo is irradiated, not its surroundings. 24 research points.
   Green.

Each player starts out with a nation composed of 2 randomly placed contiguous
tiles, one of which is a capital. Each nation will also have a few missiles to
start. A number of missile silos will appear on the map, which are guaranteed
to not be part of a nation at the start. Some tiles will also be initially
irradiated; these cannot also be silos.

A normal tile can hold 2 missiles. If it is destroyed and reinhabited, this
capacity drops to 1. 

The special tiles have the following effects:
 
 - Capital: While you have a capital, any research action gives 2 research
   points to the subject instead of 1. This is very important in the early
   game!
 - Silo: A silo can carry 4 missiles instead of 2, and every missile launched
   from it travels 2 turns faster. This impressive striking power comes at a
   cost: if a silo is destroyed, the 4 adjacent tiles will also be destroyed.
   Beware of chain detonations!

If a silo is destroyed, its specialness is gone forever, but a capital can be
recaptured, even by the other player, unless it is irradiated. Controlling 2 
capitals has no further effect.

The game ends when a player ends their turn and one player has no tiles. Even
if after the winner's turn they are completely doomed, they are still the last
one alive. After this, the game will automatically kill itself in 30 seconds,
so you can safely close the window. If you want to replay, just re-open the
.jar.

How will you do in a game where the only winning move is not to play?
