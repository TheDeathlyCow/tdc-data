package com.github.thedeathlycow.tdcdata.server.command;

import com.github.thedeathlycow.tdcdata.server.command.argument.NbtTypesArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.argument.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.SetBlockCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class VarCommand {

    public static final Map<String, String> map = new HashMap<>();
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.setblock.failed"));
    private static final String GET_SUCCESS = "Variable %s has the value of %s";
    private static final String GET_FAIL = "Variable %s does not exist";
    private static final String DISPOSE_SUCCESS = "Variable %s has been disposed";
    private static final String DISPOSE_FAIL = "Variable %s does not exist";
    private static final String SET_SUCCESS = "Variable %s has been set to %s";
    private static final String SET_FROM_SCORE_FAIL = "Player does not have score for %s";
    private static final String OPERATE_ADD_SUCCESS = "Variable %s has been added to %s";
    private static final String OPERATE_MULTIPLY_SUCCESS = "Variable %s has been multiplied by %s";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {

        var getSubCommand = literal("get")
                .then(argument("key", StringArgumentType.string())
                        .executes(context -> {
                            return get(context.getSource(),
                                    StringArgumentType.getString(context, "key"));
                        }));

        var setSubCommand = literal("set")
                .then(argument("key", StringArgumentType.word())
                        .then(literal("value")
                                .then(argument("value", StringArgumentType.string())
                                        .executes(context -> {
                                            return set(context.getSource(),
                                                    StringArgumentType.getString(context, "key"),
                                                    StringArgumentType.getString(context, "value"));
                                        })))
                        .then(literal("from")
                                .then(literal("score")
                                        .then(argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective())
                                                .then(argument("target", ScoreHolderArgumentType.scoreHolder())
                                                        .executes(context -> {
                                                            ServerCommandSource source = context.getSource();
                                                            String key = StringArgumentType.getString(context, "key");
                                                            ScoreboardObjective objective = ScoreboardObjectiveArgumentType.getObjective(context, "objective");
                                                            String target = ScoreHolderArgumentType.getScoreHolder(context, "target");

                                                            Scoreboard scoreboard = context.getSource().getServer().getScoreboard();

                                                            if (scoreboard.playerHasObjective(target, objective)) {
                                                                int score = scoreboard.getPlayerScore(target, objective).getScore();
                                                                set(source, key, String.valueOf(score));
                                                            } else {
                                                                Text msg = Text.literal(String.format(SET_FROM_SCORE_FAIL, objective.getDisplayName()));
                                                                source.sendError(msg);
                                                            }

                                                            return 1;
                                                        }))))
                                .then(literal("block")
                                        .then(argument("target", BlockPosArgumentType.blockPos())
                                                .then(literal("name")
                                                        .executes(context -> {
                                                            ServerCommandSource source = context.getSource();
                                                            String key = StringArgumentType.getString(context, "key");
                                                            BlockPos target = BlockPosArgumentType.getBlockPos(context, "target");

                                                            String text = source.getWorld().getBlockState(target).getBlock().getName().getString();
                                                            set(source, key, text);
                                                            return 1;
                                                        }))
                                                .then(literal("id")
                                                        .executes(context -> {
                                                            ServerCommandSource source = context.getSource();
                                                            String key = StringArgumentType.getString(context, "key");
                                                            BlockPos target = BlockPosArgumentType.getBlockPos(context, "target");

                                                            Identifier id = Registry.BLOCK.getId(source.getWorld().getBlockState(target).getBlock());
                                                            String text = id.toString();
                                                            set(source, key, text);
                                                            return 1;
                                                        }))))));

        var operateSubCommand = literal("operate")
                .then(argument("key", StringArgumentType.string())
                        .then(literal("add")
                                .then(argument("key2", StringArgumentType.string())
                                        .executes(context -> {
                                            String key = StringArgumentType.getString(context, "key");
                                            String key2 = StringArgumentType.getString(context, "key2");
                                            if (!map.containsKey(key)) {
                                                Text msg = Text.literal(String.format(GET_FAIL, key));
                                                context.getSource().sendError(msg);
                                                return 0;
                                            }
                                            if (!map.containsKey(key2)) {
                                                Text msg = Text.literal(String.format(GET_FAIL, key2));
                                                context.getSource().sendError(msg);
                                                return 0;
                                            }

                                            String value = map.get(key);
                                            String value2 = map.get(key2);

                                            map.put(key, value + value2);

                                            Text msg = Text.literal(String.format(OPERATE_ADD_SUCCESS, key, key2));
                                            context.getSource().sendFeedback(msg, true);

                                            return 1;
                                        })))
                        .then(literal("multiply")
                                .then(argument("amount", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            String key = StringArgumentType.getString(context, "key");
                                            if (!map.containsKey(key)) {
                                                Text msg = Text.literal(String.format(GET_FAIL, key));
                                                context.getSource().sendError(msg);
                                                return 0;
                                            }

                                            String value = map.get(key);
                                            int amount = IntegerArgumentType.getInteger(context, "amount");

                                            map.put(key, repeat(amount, value));

                                            Text msg = Text.literal(String.format(OPERATE_MULTIPLY_SUCCESS, key, amount));
                                            context.getSource().sendFeedback(msg, true);

                                            return 1;
                                        })))
                        .then(literal("uppercase")
                                .executes(context -> {
                                    String key = StringArgumentType.getString(context, "key");
                                    if (!map.containsKey(key)) {
                                        Text msg = Text.literal(String.format(GET_FAIL, key));
                                        context.getSource().sendError(msg);
                                        return 0;
                                    }

                                    String value = map.get(key);
                                    map.put(key, value.toUpperCase());

                                    Text msg = Text.literal(String.format(SET_SUCCESS, key, value.toUpperCase()));
                                    context.getSource().sendFeedback(msg, true);

                                    return 1;
                                }))
                        .then(literal("lowercase")
                                .executes(context -> {
                                    String key = StringArgumentType.getString(context, "key");
                                    if (!map.containsKey(key)) {
                                        Text msg = Text.literal(String.format(GET_FAIL, key));
                                        context.getSource().sendError(msg);
                                        return 0;
                                    }

                                    String value = map.get(key);
                                    map.put(key, value.toLowerCase());

                                    Text msg = Text.literal(String.format(SET_SUCCESS, key, value.toLowerCase()));
                                    context.getSource().sendFeedback(msg, true);

                                    return 1;
                                })));

        var disposeSubCommand = literal("dispose")
                .then(argument("key", StringArgumentType.string())
                        .executes(context -> {
                            return dispose(context.getSource(),
                                    StringArgumentType.getString(context, "key"));
                        }));

        var functionSubCommand = literal("function")
                .then(literal("say")
                        .then(argument("targets", EntityArgumentType.players())
                                .then(argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String key = StringArgumentType.getString(context, "key");
                                            if (!map.containsKey(key)) {
                                                Text msg = Text.literal(String.format(GET_FAIL, key));
                                                context.getSource().sendError(msg);
                                                return 0;
                                            }

                                            String value = map.get(key);

                                            Text msg = Text.literal(value);
                                            for (PlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
                                                player.sendMessage(msg);
                                            }

                                            return 1;
                                        }))))
                .then(literal("block")
                        .then(argument("target", BlockPosArgumentType.blockPos())
                                .then(argument("key", StringArgumentType.string())
                                        .executes(context -> {
                                            String key = StringArgumentType.getString(context, "key");
                                            if (!map.containsKey(key)) {
                                                Text msg = Text.literal(String.format(GET_FAIL, key));
                                                context.getSource().sendError(msg);
                                                return 0;
                                            }

                                            String value = map.get(key);
                                            BlockPos target = BlockPosArgumentType.getBlockPos(context, "target");
                                            Block block = Registry.BLOCK.get(new Identifier(value));

                                            setBlock(context.getSource(), target, block, SetBlockCommand.Mode.REPLACE);

                                            return 1;
                                        }))))
                .then(literal("give")
                        .then(argument("targets", EntityArgumentType.players())
                                .then(argument("key", StringArgumentType.string())
                                        .then(argument("amount", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    String key = StringArgumentType.getString(context, "key");
                                                    Integer amount = IntegerArgumentType.getInteger(context, "amount");

                                                    if (!map.containsKey(key)) {
                                                        Text msg = Text.literal(String.format(GET_FAIL, key));
                                                        context.getSource().sendError(msg);
                                                        return 0;
                                                    }

                                                    String value = map.get(key);
                                                    Item item = Registry.ITEM.get(new Identifier(value));
                                                    for (PlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
                                                        player.getInventory().offerOrDrop(new ItemStack(item, amount));
                                                    }
                                                    return 1;
                                                })))));

        dispatcher.register(
                (literal("var").requires((src) -> src.hasPermissionLevel(2)))
                        .then(setSubCommand)
                        .then(getSubCommand)
                        .then(operateSubCommand)
                        .then(disposeSubCommand)
                        .then(functionSubCommand)
        );
    }

    private static String repeat(int count, String with) {
        return new String(new char[count]).replace("\0", with);
    }

    private static int get(final ServerCommandSource source, String key) {
        if (map.containsKey(key)) {
            Object value = map.get(key);
            Text msg = Text.literal(String.format(GET_SUCCESS, key, stringify(value)));
            source.sendFeedback(msg, true);
        } else {
            Text msg = Text.literal(String.format(GET_FAIL, key));
            source.sendError(msg);
        }

        return 0;
    }

    private static int dispose(final ServerCommandSource source, String key) {
        if (map.containsKey(key)) {
            map.remove(key);
            Text msg = Text.literal(String.format(DISPOSE_SUCCESS, key));
            source.sendFeedback(msg, true);
        } else {
            Text msg = Text.literal(String.format(DISPOSE_FAIL, key));
            source.sendError(msg);
        }

        return 0;
    }

    private static int set(final ServerCommandSource source, String key, String value) {
        map.put(key, value);

        Text msg = Text.literal(String.format(SET_SUCCESS, key, value));
        source.sendFeedback(msg, true);

        return 0;
    }

    private static String getStringType(Object obj) {
        if (obj instanceof String) {
            return "string";
        } else if (obj instanceof Byte) {
            return "byte";
        } else if (obj instanceof Short) {
            return "short";
        } else if (obj instanceof Integer) {
            return "int";
        } else if (obj instanceof Long) {
            return "long";
        } else if (obj instanceof Float) {
            return "float";
        } else if (obj instanceof Double) {
            return "double";
        } else if (obj instanceof Boolean) {
            return "boolean";
        } else {
            return "unknown";
        }
    }

    private static Object cast(Object obj, String newType) {
        if (obj instanceof String s) {
            try {
                switch (newType) {
                    case "byte":
                        return Byte.parseByte(s);
                    case "short":
                        return Short.parseShort(s);
                    case "int":
                        return Integer.parseInt(s);
                    case "long":
                        return Long.parseLong(s);
                    case "float":
                        return Float.parseFloat(s);
                    case "double":
                        return Double.parseDouble(s);
                    case "string":
                        return s;
                    case "boolean":
                        return Boolean.parseBoolean(s);
                }
            } catch (NumberFormatException e) {
                switch (newType) {
                    case "byte":
                        return (byte) 0;
                    case "short":
                        return (short) 0;
                    case "int":
                        return 0;
                    case "long":
                        return 0L;
                    case "float":
                        return 0F;
                    case "double":
                        return 0D;
                    case "boolean":
                        return false;
                }
            }
        } else if (obj instanceof Byte b) {
            switch (newType) {
                case "byte":
                    return b;
                case "short":
                    return b.shortValue();
                case "int":
                    return b.intValue();
                case "long":
                    return b.longValue();
                case "float":
                    return b.floatValue();
                case "double":
                    return b.doubleValue();
                case "string":
                    return b.toString();
                case "boolean":
                    return b.intValue() != 0;
            }
        } else if (obj instanceof Short b) {
            switch (newType) {
                case "byte":
                    return b.byteValue();
                case "short":
                    return b;
                case "int":
                    return b.intValue();
                case "long":
                    return b.longValue();
                case "float":
                    return b.floatValue();
                case "double":
                    return b.doubleValue();
                case "string":
                    return b.toString();
                case "boolean":
                    return b.intValue() != 0;
            }
        } else if (obj instanceof Integer b) {
            switch (newType) {
                case "byte":
                    return b.byteValue();
                case "short":
                    return b.shortValue();
                case "int":
                    return b;
                case "long":
                    return b.longValue();
                case "float":
                    return b.floatValue();
                case "double":
                    return b.doubleValue();
                case "string":
                    return b.toString();
                case "boolean":
                    return b != 0;
            }
        } else if (obj instanceof Long b) {
            switch (newType) {
                case "byte":
                    return b.byteValue();
                case "short":
                    return b.shortValue();
                case "int":
                    return b.intValue();
                case "long":
                    return b;
                case "float":
                    return b.floatValue();
                case "double":
                    return b.doubleValue();
                case "string":
                    return b.toString();
                case "boolean":
                    return b != 0;
            }
        } else if (obj instanceof Float b) {
            switch (newType) {
                case "byte":
                    return b.byteValue();
                case "short":
                    return b.shortValue();
                case "int":
                    return b.intValue();
                case "long":
                    return b.longValue();
                case "float":
                    return b;
                case "double":
                    return b.doubleValue();
                case "string":
                    return b.toString();
                case "boolean":
                    return b != 0;
            }
        } else if (obj instanceof Double b) {
            switch (newType) {
                case "byte":
                    return b.byteValue();
                case "short":
                    return b.shortValue();
                case "int":
                    return b.intValue();
                case "long":
                    return b.longValue();
                case "float":
                    return b.floatValue();
                case "double":
                    return b;
                case "string":
                    return b.toString();
                case "boolean":
                    return b != 0;
            }
        } else if (obj instanceof Boolean b) {
            switch (newType) {
                case "byte":
                    return b ? (byte) 1 : (byte) 0;
                case "short":
                    return b ? (short) 1 : (short) 0;
                case "int":
                    return b ? 1 : 0;
                case "long":
                    return b ? 1L : 0L;
                case "float":
                    return b ? 1F : 0F;
                case "double":
                    return b ? 1D : 0D;
                case "string":
                    return b.toString();
                case "boolean":
                    return b;
            }
        }

        return String.valueOf(obj);
    }

    private static String stringify(Object obj) {
        if (obj instanceof String s) {
            return "\"" + s + "\"";
        } else if (obj instanceof Byte b) {
            return b + "b";
        } else if (obj instanceof Short b) {
            return b + "s";
        } else if (obj instanceof Integer b) {
            return String.valueOf(b);
        } else if (obj instanceof Long b) {
            return b + "l";
        } else if (obj instanceof Float b) {
            return b + "f";
        } else if (obj instanceof Double b) {
            return b + "d";
        } else if (obj instanceof Byte[] arr) {
            StringBuilder sb = new StringBuilder("[B;");

            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i]).append('b');
                if (i < arr.length - 1) {
                    sb.append(',');
                }
            }

            sb.append(']');
            return sb.toString();
        } else if (obj instanceof Integer[] arr) {
            StringBuilder sb = new StringBuilder("[I;");

            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i]);
                if (i < arr.length - 1) {
                    sb.append(',');
                }
            }

            sb.append(']');
            return sb.toString();
        } else if (obj instanceof Long[] arr) {
            StringBuilder sb = new StringBuilder("[L;");

            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i]).append('l');
                if (i < arr.length - 1) {
                    sb.append(',');
                }
            }

            sb.append(']');
            return sb.toString();
        } else if (obj instanceof Object[] arr) {
            StringBuilder sb = new StringBuilder("[");

            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i]);
                if (i < arr.length - 1) {
                    sb.append(',');
                }
            }

            sb.append(']');
            return sb.toString();
        } else if (obj instanceof Boolean b) {
            return b ? "true" : "false";
        } else {
            return String.valueOf(obj);
        }
    }

    private static int setBlock(ServerCommandSource source, BlockPos pos, Block block, SetBlockCommand.Mode mode) throws CommandSyntaxException {
        ServerWorld serverWorld = source.getWorld();
        boolean bl;
        if (mode == SetBlockCommand.Mode.DESTROY) {
            serverWorld.breakBlock(pos, true);
            bl = !block.getDefaultState().isAir() || !serverWorld.getBlockState(pos).isAir();
        } else {
            BlockEntity blockEntity = serverWorld.getBlockEntity(pos);
            Clearable.clear(blockEntity);
            bl = true;
        }

        if (bl && !serverWorld.setBlockState(pos, block.getDefaultState(), 2)) {
            throw FAILED_EXCEPTION.create();
        } else {
            serverWorld.updateNeighbors(pos, block);
            source.sendFeedback(Text.translatable("commands.setblock.success", pos.getX(), pos.getY(), pos.getZ()), true);
            return 1;
        }
    }
}