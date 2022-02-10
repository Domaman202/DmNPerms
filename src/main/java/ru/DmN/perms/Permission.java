package ru.DmN.perms;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public class Permission implements Serializable {
    public final Set<String> players, commands;
    public String name, parent;

    public Permission(String name, String parent) {
        this.name = name;
        this.parent = parent;
        this.players = new LinkedHashSet<>();
        this.commands = new LinkedHashSet<>();
    }
}