# as at command block

function tdcdata-test:scoreboard/operation/before_each

scoreboard players set ExponentPlayer TestOperationObjective 100

scoreboard players operation ExponentPlayer TestOperationObjective **= #2 TestConsts

execute if score ExponentPlayer TestOperationObjective matches 10000 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/operation/after_each