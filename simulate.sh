#!/bin/bash


folder="analysis"
echo "drop table if exists games;" > $folder/turn.sql
echo "create table games (game_id integer, score integer, team varchar(255), self_games integer);" >> $folder/turn.sql

for self in 10
do
  rm -f $folder/turn10self$self.sql
  for i in {1..2}
  do
    java hoop.sim.Hoop hoop/players.list 10 $self 2 12 &> $folder/turn10self$self.$i
    tail -n 2 $folder/turn10self$self.$i | 
      awk -v gameid="$i" -v self="$self" '{ print "INSERT INTO games (game_id, score, team, self_games) VALUES (" gameid "," $1 ",'\''" $2 $3 $4 $5 $6 "'\''," self ");"}' >> $folder/turn10self$self.sql
  done
  cat analysis/turn10self$self.sql >> $folder/turn.sql
done
