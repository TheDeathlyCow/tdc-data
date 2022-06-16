# as at command block

function tdcdata-test:scoreboard/function/before_each

scoreboard players set SqrtZeroFuncPlayer TestFunctionObjective 0

scoreboard players tdcdata.function sqrt SqrtZeroFuncPlayer TestFunctionObjective

execute if score SqrtZeroFuncPlayer TestFunctionObjective matches 0 run setblock ~ ~1 ~ minecraft:lime_wool

function tdcdata-test:scoreboard/function/after_each