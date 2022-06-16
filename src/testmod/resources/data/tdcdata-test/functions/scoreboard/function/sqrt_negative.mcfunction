# as at command block

function tdcdata-test:scoreboard/function/before_each

scoreboard players set SqrtNegativeFuncPlayer TestFunctionObjective -16

scoreboard players tdcdata.function sqrt SqrtNegativeFuncPlayer TestFunctionObjective

execute if score SqrtNegativeFuncPlayer TestFunctionObjective matches -16 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/function/after_each