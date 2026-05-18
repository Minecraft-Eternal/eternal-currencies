package com.mceternal.eternalcurrencies.data.shop.requirement;

import com.mceternal.eternalcurrencies.api.shop.ShopRequirement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.level.ServerPlayer;

public class FTBQuestRequirement extends ShopRequirement<Long> {

    public static final Codec<Long> CONTENT_CODEC = Codec.STRING.comapFlatMap(FTBQuestRequirement::parseHexString, FTBQuestRequirement::writeHexLong);

    public static final MapCodec<FTBQuestRequirement> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            CONTENT_CODEC.fieldOf("quest").forGetter(e -> e.target)
    ).apply(inst, FTBQuestRequirement::new));

    private static String writeHexLong(Long aLong) {
        return Long.toHexString(aLong);
    }

    private static DataResult<Long> parseHexString(String s) {
        try {
            Long aLong = Long.parseLong(s.toLowerCase(), 16);
            //EternalCurrencies.LOGGER.info("parsed long of {} from {}", aLong, s);
            return DataResult.success(aLong);
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Failed to Parse "+ s +" to Long.");
        }
    }

    public FTBQuestRequirement(Long questid) {
        super(questid);
    }

    @Override
    public Codec contentCodec() {
        return CONTENT_CODEC;
    }

    @Override
    public MapCodec<? extends ShopRequirement> codec() {
        return CODEC;
    }

    @Override
    public boolean meetsRequirement(ServerPlayer player) {
        Quest quest = FTBQuestsAPI.api().getQuestFile(false).getQuest(this.target);
        return quest != null
                && quest.isCompletedRaw(TeamData.get(player));
    }
}
