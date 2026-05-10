package me.aidan.sydney.gui;

import lombok.Getter;
import lombok.Setter;
import me.aidan.sydney.Sydney;
import me.aidan.sydney.gui.api.DescriptionFrame;
import me.aidan.sydney.modules.Module;
import me.aidan.sydney.modules.impl.core.ClickGuiModule;
import me.aidan.sydney.gui.api.Button;
import me.aidan.sydney.gui.api.Frame;
import me.aidan.sydney.modules.impl.core.ColorModule;
import me.aidan.sydney.utils.color.ColorUtils;
import me.aidan.sydney.utils.graphics.Renderer2D;
import me.aidan.sydney.utils.system.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;

@Getter @Setter
public class ClickGuiScreen extends Screen {
    private final ArrayList<Frame> frames = new ArrayList<>();
    private final ArrayList<Button> buttons = new ArrayList<>();
    private final DescriptionFrame descriptionFrame;

    private final Timer lineTimer = new Timer();
    private boolean showLine = false;
    private Color colorClipboard = null;
    private String moduleSearchQuery = "";
    private boolean searchingModules = false;
    private boolean selectingModuleSearch = false;
    private int moduleSearchCursor = 0;

    public ClickGuiScreen() {
        super(Text.literal(Sydney.MOD_ID + "-click-gui"));

        int x = 6;
        for(Module.Category category : Module.Category.values()) {
            frames.add(new Frame(category, x, 3, 100, 13));
            x += 104;
        }

        this.descriptionFrame = new DescriptionFrame(x, 3, 200, 13);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (lineTimer.hasTimeElapsed(400L)){
            showLine = !showLine;
            lineTimer.reset();
        }

        descriptionFrame.setDescription("");
        for(Frame frame : frames) frame.render(context, mouseX, mouseY, delta);

        descriptionFrame.render(context, mouseX, mouseY, delta);
        renderModuleSearch(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for(Frame frame : frames) frame.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isHoveringModuleSearch(mouseX, mouseY)) {
                searchingModules = true;
                selectingModuleSearch = !moduleSearchQuery.isEmpty();
                moduleSearchCursor = moduleSearchQuery.length();
            } else {
                selectingModuleSearch = false;
                searchingModules = false;
            }
        }

        for (Frame frame : frames) {
            frame.mouseClicked(mouseX, mouseY, button);
        }

        descriptionFrame.mouseClicked(mouseX, mouseY, button);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY, button);
        }

        descriptionFrame.mouseReleased(mouseX, mouseY, button);

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Frame frame : frames) {
            frame.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        return this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        long handle = client.getWindow().getHandle();
        boolean ctrl = InputUtil.isKeyPressed(handle, MinecraftClient.IS_SYSTEM_MAC ? GLFW.GLFW_KEY_LEFT_SUPER : GLFW.GLFW_KEY_LEFT_CONTROL);

        if (ctrl && keyCode == GLFW.GLFW_KEY_F) {
            searchingModules = true;
            selectingModuleSearch = !moduleSearchQuery.isEmpty();
            moduleSearchCursor = moduleSearchQuery.length();
            return true;
        }

        if (searchingModules) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                moduleSearchQuery = "";
                moduleSearchCursor = 0;
                selectingModuleSearch = false;
                searchingModules = false;
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                selectingModuleSearch = false;
                searchingModules = false;
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (selectingModuleSearch) {
                    moduleSearchQuery = "";
                    moduleSearchCursor = 0;
                    selectingModuleSearch = false;
                } else if (moduleSearchCursor > 0) {
                    moduleSearchQuery = moduleSearchQuery.substring(0, moduleSearchCursor - 1) + moduleSearchQuery.substring(moduleSearchCursor);
                    moduleSearchCursor--;
                }

                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                if (selectingModuleSearch) {
                    moduleSearchQuery = "";
                    moduleSearchCursor = 0;
                    selectingModuleSearch = false;
                } else if (moduleSearchCursor < moduleSearchQuery.length()) {
                    moduleSearchQuery = moduleSearchQuery.substring(0, moduleSearchCursor) + moduleSearchQuery.substring(moduleSearchCursor + 1);
                }

                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                if (selectingModuleSearch) {
                    selectingModuleSearch = false;
                    moduleSearchCursor = 0;
                } else if (moduleSearchCursor > 0) {
                    moduleSearchCursor--;
                }

                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                if (selectingModuleSearch) {
                    selectingModuleSearch = false;
                    moduleSearchCursor = moduleSearchQuery.length();
                } else if (moduleSearchCursor < moduleSearchQuery.length()) {
                    moduleSearchCursor++;
                }

                return true;
            }

            if (ctrl) {
                if (keyCode == GLFW.GLFW_KEY_V) {
                    try {
                        String clipboard = client.keyboard.getClipboard();
                        if (clipboard != null) {
                            if (selectingModuleSearch) {
                                moduleSearchQuery = clipboard;
                                moduleSearchCursor = moduleSearchQuery.length();
                                selectingModuleSearch = false;
                            } else {
                                moduleSearchQuery = moduleSearchQuery.substring(0, moduleSearchCursor) + clipboard + moduleSearchQuery.substring(moduleSearchCursor);
                                moduleSearchCursor += clipboard.length();
                            }
                        }
                    } catch (Exception exception) {
                        Sydney.LOGGER.error("{}: Failed to process clipboard paste", exception.getClass().getName(), exception);
                    }

                    return true;
                }

                if (keyCode == GLFW.GLFW_KEY_C && selectingModuleSearch) {
                    try {
                        client.keyboard.setClipboard(moduleSearchQuery);
                    } catch (Exception exception) {
                        Sydney.LOGGER.error("{}: Failed to process clipboard change", exception.getClass().getName(), exception);
                    }

                    return true;
                }

                if (keyCode == GLFW.GLFW_KEY_A) {
                    if (!moduleSearchQuery.isEmpty()) {
                        selectingModuleSearch = true;
                        moduleSearchCursor = moduleSearchQuery.length();
                    }

                    return true;
                }
            }
        }

        for (Frame frame : frames) {
            frame.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchingModules && !Character.isISOControl(chr)) {
            if (selectingModuleSearch) {
                moduleSearchQuery = String.valueOf(chr);
                moduleSearchCursor = 1;
                selectingModuleSearch = false;
            } else {
                moduleSearchQuery = moduleSearchQuery.substring(0, moduleSearchCursor) + chr + moduleSearchQuery.substring(moduleSearchCursor);
                moduleSearchCursor++;
            }

            return true;
        }

        for (Frame frame : frames) {
            frame.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if(Sydney.MODULE_MANAGER.getModule(ClickGuiModule.class).blur.getValue()) applyBlur();
        Renderer2D.renderQuad(context.getMatrices(), 0, 0, this.width, this.height, new Color(0, 0, 0, 100));
    }

    @Override
    public void close() {
        super.close();
        moduleSearchQuery = "";
        moduleSearchCursor = 0;
        selectingModuleSearch = false;
        searchingModules = false;
        Sydney.MODULE_MANAGER.getModule(ClickGuiModule.class).setToggled(false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public static Color getButtonColor(int index, int alpha) {
        Color color = Sydney.MODULE_MANAGER.getModule(ClickGuiModule.class).isRainbow() ? ColorUtils.getOffsetRainbow(index*10L) : Sydney.MODULE_MANAGER.getModule(ClickGuiModule.class).color.getColor();
        return ColorUtils.getColor(color, alpha);
    }

    public boolean hasModuleSearchQuery() {
        return !moduleSearchQuery.isBlank();
    }

    public boolean matchesModuleSearch(Module module) {
        if (!hasModuleSearchQuery()) return true;

        String searchable = (module.getName() + " " + module.getDescription()).toLowerCase();
        for (String word : moduleSearchQuery.toLowerCase().trim().split("\\s+")) {
            if (!searchable.contains(word)) return false;
        }

        return true;
    }

    private void renderModuleSearch(DrawContext context, int mouseX, int mouseY) {
        int searchWidth = 180;
        int searchHeight = 13;
        int searchX = 6;
        int searchY = height - searchHeight - 6;

        Renderer2D.renderQuad(context.getMatrices(), searchX, searchY, searchX + searchWidth, searchY + searchHeight, getButtonColor(searchY, 100));

        String text;
        if (searchingModules) {
            if (selectingModuleSearch) {
                text = "Search: " + moduleSearchQuery;
            } else {
                String cursor = showLine ? "|" : " ";
                int index = Math.clamp(moduleSearchCursor, 0, moduleSearchQuery.length());
                text = "Search: " + moduleSearchQuery.substring(0, index) + cursor + moduleSearchQuery.substring(index);
            }
        } else if (moduleSearchQuery.isEmpty()) {
            text = Formatting.GRAY + "Ctrl+F Search Modules";
        } else {
            text = "Search: " + Formatting.GRAY + moduleSearchQuery;
        }

        Sydney.FONT_MANAGER.drawTextWithShadow(context, text, searchX + 4, searchY + 2, isHoveringModuleSearch(mouseX, mouseY) || searchingModules ? Color.WHITE : Color.LIGHT_GRAY);
    }

    private boolean isHoveringModuleSearch(double mouseX, double mouseY) {
        int searchWidth = 180;
        int searchHeight = 13;
        int searchX = 6;
        int searchY = height - searchHeight - 6;
        return searchX <= mouseX && searchY <= mouseY && searchX + searchWidth > mouseX && searchY + searchHeight > mouseY;
    }
}
