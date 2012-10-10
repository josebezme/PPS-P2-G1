#!/bin/bash

table=$1
folder="analysis"
echo "drop table if exists ${table};" > $folder/${table}.sql
echo "create table ${table} (game_id integer, score integer, team varchar(255), self_games integer);" >> $folder/${table}.sql

for self in 100
do
  echo "doing $1 with self: $self"
  rm -f $folder/$table_turn10self$self.sql
  for i in {1..25}
  do
    echo $i
    java hoop.sim.Hoop hoop/players.list 10 $self 100 12 &> $folder/${table}_turn10self$self.$i
    tail -n 2 $folder/${table}_turn10self$self.$i | 
      awk -v gameid="$i" -v self="$self" -v table="$1" '{ print "INSERT INTO " table " (game_id, score, team, self_games) VALUES (" gameid "," $1 ",'\''" $2 $3 $4 $5 $6 "'\''," self ");"}' >> $folder/$table_turn10self$self.sql
  done
  cat analysis/$table_turn10self$self.sql >> $folder/$table.sql
done
