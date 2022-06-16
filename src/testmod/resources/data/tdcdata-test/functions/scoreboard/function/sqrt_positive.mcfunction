# as at command block

function tdcdata-test:scoreboard/function/before_each

scoreboard players set SqrtPositivePlayer TestFunctionObjective 16

scoreboard players tdcdata.function sqrt SqrtPositivePlayer TestFunctionObjective

execute if score SqrtPositivePlayer TestFunctionObjective matches 4 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/function/after_each