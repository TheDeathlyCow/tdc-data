# as nearest villager at command block

function tdcdata-test:scoreboard/operation/before_each

scoreboard players set RShiftPositivePlayer TestOperationObjective 100

scoreboard players operation RShiftPositivePlayer TestOperationObjective >>= #2 TestConsts

execute if score RShiftPositivePlayer TestOperationObjective matches 25 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/operation/after_each