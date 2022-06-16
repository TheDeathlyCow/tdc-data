# as nearest villager at command block

function tdcdata-test:scoreboard/operation/before_each

scoreboard players set RShiftPositivePlayer TestObjective 100

scoreboard players operation RShiftPositivePlayer TestObjective >>= #2 TestConsts

execute if score RShiftPositivePlayer TestObjective matches 25 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/operation/after_each