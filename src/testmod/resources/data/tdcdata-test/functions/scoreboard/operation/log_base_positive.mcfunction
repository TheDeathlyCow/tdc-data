# as at command block

function tdcdata-test:scoreboard/operation/before_each

scoreboard players set LogBPositive TestOperationObjective 9

scoreboard players operation LogBPositive TestOperationObjective log_b #3 TestConsts

execute if score LogBPositive TestOperationObjective matches 2 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/operation/after_each