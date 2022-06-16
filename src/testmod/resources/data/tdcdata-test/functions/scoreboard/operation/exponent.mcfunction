# as at command block

function tdcdata-test:scoreboard/operation/before_each

scoreboard players set ExponentPlayer TestObjective 100

scoreboard players operation ExponentPlayer TestObjective **= #2 TestConsts

execute if score ExponentPlayer TestObjective matches 10000 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/operation/after_each