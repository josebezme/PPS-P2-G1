select CONCAT("Group ", ps.group_id) as group_id, 
	max(ps.rank) - min(ps.rank) as ps_diff,
	max(pl.rank) - min(pl.rank) as pl_diff,
	max(s.rank) - min(s.rank) as s_diff,
	max(same.rank) - min(same.rank) as same
from 
(
	select r.group_id, avg(r.rank) as rank from game g join result r on g.id = r.game_id
	group by group_id, g.pre_season
) as ps,
(
	select r.group_id, avg(r.rank) as rank from game g join result r on g.id = r.game_id
	group by group_id, g.players
) as pl,
(
	select r.group_id, avg(r.rank) as rank from game g join result r on g.id = r.game_id
	group by group_id, g.seasons
) as s,
(
	select r.group_id, avg(r.rank) as rank from game g join result r on g.id = r.game_id
	group by group_id, g.same
) as same

where ps.group_id = pl.group_id
and pl.group_id = s.group_id
and s.group_id = same.group_id
group by group_id;


select CONCAT("Group ", ps.group_id) as group_id, 
	avg(ps.rank) as 'Pre-Seasons',
	avg(pl.rank) as 'Players',
	avg(s.rank) as 'Seasons',
	avg(same.rank) as 'Same Players'
from 
(
	select r.group_id, avg(r.rank) as rank from game g join result r on g.id = r.game_id
	group by group_id, g.pre_season
) as ps,
(
	select r.group_id, avg(r.rank) as rank from game g join result r on g.id = r.game_id
	group by group_id, g.players
) as pl,
(
	select r.group_id, avg(r.rank) as rank from game g join result r on g.id = r.game_id
	group by group_id, g.seasons
) as s,
(
	select r.group_id, avg(r.rank) as rank from game g join result r on g.id = r.game_id
	group by group_id, g.same
) as same

where ps.group_id = pl.group_id
and pl.group_id = s.group_id
and s.group_id = same.group_id
group by group_id