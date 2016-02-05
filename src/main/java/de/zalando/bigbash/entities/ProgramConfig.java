package de.zalando.bigbash.entities;

import org.aeonbits.owner.Config;

/**
 * Created by bvonloesch on 5/15/15.
 */
@Config.Sources({"file:./bigbash.conf", "file:~/.config/bigbash.conf", "file:/etc/bigbash.conf"})
public interface ProgramConfig extends Config {

    @DefaultValue("sort")
    String sort();

    @DefaultValue("awk")
    String awk();

    @DefaultValue("cut")
    String cut();

    @DefaultValue("sed")
    String sed();

    @DefaultValue("head")
    String head();

    @DefaultValue("tail")
    String tail();

    @DefaultValue("join")
    String join();

    @DefaultValue("gzip -dc")
    String zcat();

    @DefaultValue("cat")
    String cat();

    @DefaultValue("sqlite3")
    String sqlite3();

    @DefaultValue("true")
    @Key("remove_unused_columns")
    boolean isRemovingUnusedColumns();

    @DefaultValue("true")
    @Key("optimize_joins")
    boolean isOptimizingJoins();

    @DefaultValue("tr")
    String tr();

    @DefaultValue("find")
    String find();

    @DefaultValue("xargs")
    String xargs();

    @DefaultValue("sh -c")
    @Key("execute_shell")
    String getExecuteShellCommand();
}
