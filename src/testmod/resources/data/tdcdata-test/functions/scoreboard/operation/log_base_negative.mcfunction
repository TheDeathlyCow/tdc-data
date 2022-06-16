# as at command block

function tdcdata-test:scoreboard/operation/before_each

scoreboard players set LogBNegativePlayer TestOperationObjective -9

scoreboard players operation LogBNegativePlayer TestOperationObjective log_b #3 TestConsts

# Checking that the operation did not execute because input is negative
execute if score LogBNegativePlayer TestOperationObjective matches -9 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/operation/after_each