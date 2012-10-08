#!/bin/bash
for i in {1..100}
do
  java hoop.sim.Hoop hoop/players.list 10 100 2 12 &> analysis/turn10self100.$i 
  tail -n 2 analysis/turn10self100.$i >> analysis/turn10self100
done

for i in {1..100}
do
  java hoop.sim.Hoop hoop/players.list 10 10 2 12 &> analysis/turn10self10.$i
  tail -n 2 analysis/urn10self10.$i >> analysis/turn10self10
done
