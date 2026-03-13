package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.DataFixTypes;

import java.util.Objects;

public enum ToListenSeawavesDataFixTypes {
    PRESCRIPTS(ToListenSeawavesReferences.PRESCRIPTS);

    private final DSL.TypeReference type;
    ToListenSeawavesDataFixTypes(DSL.TypeReference type) {
        this.type = type;
    }

    static int currentVersion() {
        return SharedConstants.getCurrentVersion().dataVersion().version();
    }

    public <A> Codec<A> wrapCodec(final Codec<A> codec, final DataFixer dataFixer, final int dataVersion) {
        return new Codec<A>() {
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T value) {
                return codec.encode(input, ops, value).flatMap((p_300998_) -> {
                    return ops.mergeToMap(p_300998_, ops.createString("DataVersion"), ops.createInt(ToListenSeawavesDataFixTypes.currentVersion()));
                });
            }

            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T value) {
                DataResult<T> var10000 = ops.get(value, "DataVersion");
                Objects.requireNonNull(ops);
                int i = var10000.flatMap(ops::getNumberValue).map(Number::intValue).result().orElse(dataVersion);
                Dynamic<T> dynamic = new Dynamic(ops, ops.remove(value, "DataVersion"));
                Dynamic<T> dynamic1 = ToListenSeawavesDataFixTypes.this.updateToCurrentVersion(dataFixer, dynamic, i);
                return codec.decode(dynamic1);
            }
        };
    }

    public <T> Dynamic<T> update(DataFixer fixer, Dynamic<T> input, int version, int newVersion) {
        return fixer.update(this.type, input, version, newVersion);
    }

    public <T> Dynamic<T> updateToCurrentVersion(DataFixer fixer, Dynamic<T> input, int version) {
        return this.update(fixer, input, version, currentVersion());
    }

    public CompoundTag update(DataFixer fixer, CompoundTag tag, int version, int newVersion) {
        return (CompoundTag)this.update(fixer, new Dynamic(NbtOps.INSTANCE, tag), version, newVersion).getValue();
    }

    public CompoundTag updateToCurrentVersion(DataFixer fixer, CompoundTag tag, int version) {
        return this.update(fixer, tag, version, currentVersion());
    }
}
