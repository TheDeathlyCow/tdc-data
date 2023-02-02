package com.github.thedeathlycow.tdcdata.server.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class RawVec3Argument implements PosArgument {

    private final double x;
    private final double y;
    private final double z;

    public RawVec3Argument(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static RawVec3Argument parse(StringReader reader) throws CommandSyntaxException {
        int startingCursorPos = reader.getCursor();
        double x = readCoordinate(reader);
        if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            double y = readCoordinate(reader);
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                double z = readCoordinate(reader);
                return new RawVec3Argument(x, y, z);
            } else {
                reader.setCursor(startingCursorPos);
                throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
            }
        } else {
            reader.setCursor(startingCursorPos);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
        }
    }

    @Override
    public Vec3d toAbsolutePos(ServerCommandSource source) {
        return new Vec3d(this.x, this.y, this.z);
    }

    @Override
    public Vec2f toAbsoluteRotation(ServerCommandSource source) {
        return Vec2f.ZERO;
    }

    @Override
    public boolean isXRelative() {
        return false;
    }

    @Override
    public boolean isYRelative() {
        return false;
    }

    @Override
    public boolean isZRelative() {
        return false;
    }

    private static double readCoordinate(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw CoordinateArgument.MISSING_COORDINATE.createWithContext(reader);
        } else {
            reader.skip();
            return reader.canRead() && reader.peek() != ' ' ? reader.readDouble() : 0.0;
        }
    }
}
