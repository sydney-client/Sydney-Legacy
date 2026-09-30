package me.aidan.sydney;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.aidan.sydney.commands.CommandManager;
import me.aidan.sydney.events.EventHandler;
import me.aidan.sydney.gui.ClickGuiScreen;
import me.aidan.sydney.managers.*;
import me.aidan.sydney.modules.ModuleManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sydney implements ModInitializer {
	public static final String MOD_NAME = BuildConstants.MOD_NAME;
	public static final String MOD_ID = BuildConstants.MOD_ID;
	public static final String MOD_VERSION = BuildConstants.MOD_VERSION;
	public static final String MINECRAFT_VERSION = BuildConstants.MINECRAFT_VERSION;
	public static final String GIT_HASH = BuildConstants.GIT_HASH;
	public static final String GIT_REVISION = BuildConstants.GIT_REVISION;
	public static final long UPTIME = System.currentTimeMillis();

	public static final EventHandler EVENT_HANDLER = new EventHandler();
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	private static final ExecutorService UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();
	public static String UPDATE_STATUS = "none";

	private static final String VERSION_URL = "";
	private static final String SECRET_KEY = "";

	public static ChatManager CHAT_MANAGER;
	public static FontManager FONT_MANAGER;
	public static FriendManager FRIEND_MANAGER;
	public static WorldManager WORLD_MANAGER;
	public static PositionManager POSITION_MANAGER;
	public static RotationManager ROTATION_MANAGER;
	public static ServerManager SERVER_MANAGER;
	public static RenderManager RENDER_MANAGER;
	public static TargetManager TARGET_MANAGER;
	public static MacroManager MACRO_MANAGER;
	public static TaskManager TASK_MANAGER;
	public static WaypointManager WAYPOINT_MANAGER;

	public static ModuleManager MODULE_MANAGER;
	public static CommandManager COMMAND_MANAGER;

	public static ConfigManager CONFIG_MANAGER;

	public static ClickGuiScreen CLICK_GUI;

	@Override
	public void onInitialize() {
		CHAT_MANAGER = new ChatManager();
		FONT_MANAGER = new FontManager();
		FRIEND_MANAGER = new FriendManager();
		WORLD_MANAGER = new WorldManager();
		POSITION_MANAGER = new PositionManager();
		ROTATION_MANAGER = new RotationManager();
		SERVER_MANAGER = new ServerManager();
		RENDER_MANAGER = new RenderManager();
		TARGET_MANAGER = new TargetManager();
		MACRO_MANAGER = new MacroManager();
		TASK_MANAGER = new TaskManager();
		WAYPOINT_MANAGER = new WaypointManager();

		MODULE_MANAGER = new ModuleManager();
		COMMAND_MANAGER = new CommandManager();
	}

	public static void onPostInitialize() {
		CONFIG_MANAGER = new ConfigManager();

		CLICK_GUI = new ClickGuiScreen();

		LOGGER.info("{} {} has been initialized.", MOD_NAME, MOD_VERSION);
	}

	public static void checkForUpdates() {
		UPDATE_EXECUTOR.submit(() -> {
			if (FabricLoader.getInstance().isDevelopmentEnvironment()) return;
			if (!FabricLoader.getInstance().isModLoaded(MOD_ID + "-updater")) return;

			try {
				HttpURLConnection versionConnection = (HttpURLConnection) new URL(VERSION_URL + SECRET_KEY).openConnection();
				versionConnection.setRequestMethod("GET");
				versionConnection.connect();

				if (versionConnection.getResponseCode() == 200) {
					InputStreamReader reader = new InputStreamReader(versionConnection.getInputStream());

					JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
					if (!jsonObject.has("version")) return;

					if (!MOD_VERSION.equalsIgnoreCase(jsonObject.get("version").getAsString())) {
						UPDATE_STATUS = "update-available";
					}
				} else {
					UPDATE_STATUS = "failed-connection";
				}
			} catch (IOException exception) {
				UPDATE_STATUS = "failed";
			}

			if (UPDATE_STATUS.equalsIgnoreCase("none")) UPDATE_STATUS = "up-to-date";
		});
	}
}