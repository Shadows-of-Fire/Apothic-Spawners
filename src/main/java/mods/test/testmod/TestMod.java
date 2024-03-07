package mods.test.testmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(TestMod.MODID)
public class TestMod {

    public static final String MODID = "modid";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public TestMod(IEventBus bus) {
        LOGGER.info("Hello World");
    }

}
