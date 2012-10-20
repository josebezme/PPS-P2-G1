select 
	CONCAT("Group " , group_id) as 'Group', 
	pre_season as 'Pre-Seasons',
	avg(rank) as 'Average Rank'
from result r join game g on g.id = r.game_id
group by group_id, g.pre_season;

select 
	CONCAT("Group " , group_id) as 'Group', 
	seasons as 'Seasons',
	avg(rank) as 'Average Rank'
from result r join game g on g.id = r.game_id
group by group_id, g.seasons