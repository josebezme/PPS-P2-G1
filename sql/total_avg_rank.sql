select CONCAT("Group " , group_id) as 'Group', avg(rank) as 'Average Rank'
from result
group by group_id