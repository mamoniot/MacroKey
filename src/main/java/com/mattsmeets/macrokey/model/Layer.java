package com.mattsmeets.macrokey.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Layer {

    public UUID ulid;
    public String displayName = "";
    public HashSet<UUID> macros = new HashSet<UUID>();

    public Layer() {
        this.ulid = UUID.randomUUID();
    }

    public UUID getULID() {
        return ulid;
    }

    @Override
    public boolean equals(Object obj) {
        return
                obj instanceof Layer &&
                        this.ulid.equals(((Layer) obj).ulid);
    }

}
