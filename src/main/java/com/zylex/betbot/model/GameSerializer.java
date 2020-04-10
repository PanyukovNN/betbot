package com.zylex.betbot.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class GameSerializer extends StdSerializer<Game> {

    public GameSerializer() {
        this(null);
    }

    public GameSerializer(Class<Game> t) {
        super(t);
    }

    @Override
    public void serialize(Game game, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeNumberField("id", game.getId());
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        jgen.writeObjectField("dateTime", DATE_TIME_FORMATTER.format(game.getDateTime()));
        jgen.writeStringField("league", game.getLeague().getName());
        jgen.writeStringField("firstTeam", game.getFirstTeam());
        jgen.writeStringField("secondTeam", game.getSecondTeam());
        jgen.writeStringField("rules", game.getRules().toString());
        jgen.writeStringField("result", game.getResult());
        jgen.writeStringField("bets", game.getBets().toString());
        jgen.writeStringField("gameInfo", game.getGameInfo().toString());
        jgen.writeEndObject();
    }
}
