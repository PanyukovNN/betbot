package com.zylex.betbot.service.rule;

import com.zylex.betbot.exception.RuleException;
import com.zylex.betbot.model.Game;

import java.io.*;

class RuleUtil {

    static Game clone(Game game) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream ous = new ObjectOutputStream(baos)) {
            ous.writeObject(game);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (Game) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuleException(e.getMessage(), e);
        }
    }
}
