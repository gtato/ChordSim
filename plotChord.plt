#!/usr/bin/gnuplot

if (!exists("filename")) filename='graph.dat'

unset key
set size square
set title "Chord topology" 



# this is for plotting the logical topology (using LinkObserver)
plot filename with lines lc rgb "#0091ea" ,\
     '' u ($1):($2):($3) with labels point pt 7 offset char 0,0.5 lc rgb "black"


pause mouse close