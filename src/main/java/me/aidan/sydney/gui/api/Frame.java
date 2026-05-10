package me.aidan.sydney.gui.api;

import lombok.Getter;
import lombok.Setter;
import me.aidan.sydney.Sydney;
import me.aidan.sydney.gui.ClickGuiScreen;
import me.aidan.sydney.gui.impl.WhitelistButton;
import me.aidan.sydney.modules.Module;
import me.aidan.sydney.gui.impl.ModuleButton;
import me.aidan.sydney.modules.impl.core.ClickGuiModule;
import me.aidan.sydney.utils.graphics.Renderer2D;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Frame {
    private final Module.Category category;
    private int x, y, width, height, totalHeight, dragX = 0, dragY = 0, textPadding = 3;
    public boolean open = true, dragging = false;
    private final ArrayList<Button> buttons = new ArrayList<>();

    public Frame(Module.Category category, int x, int y, int width, int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        for(Module module : Sydney.MODULE_MANAGER.getModules(category)) buttons.add(new ModuleButton(module, this, height));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if(dragging) {
            setX(mouseX - dragX);
            setY(mouseY - dragY);
        }

        this.totalHeight = height;
        boolean showButtons = open || Sydney.CLICK_GUI.hasModuleSearchQuery();

        if(showButtons) {
            totalHeight += 1;
            for(Button button : buttons) {
                if (button instanceof ModuleButton moduleButton) {
                    button.setVisible(Sydney.CLICK_GUI.matchesModuleSearch(moduleButton.getModule()));
                }

                if(!button.isVisible()) continue;
                button.setX(x);
                button.setY(y + totalHeight);
                totalHeight += button.getHeight();

                if(button instanceof ModuleButton moduleButton && moduleButton.isOpen()) {
                    for(Button b : moduleButton.getButtons()) {
                        b.getSetting().getVisibility().update();
                        b.setVisible(b.getSetting().getVisibility().isVisible());
                        if(!b.isVisible()) continue;

                        b.setX(x);
                        b.setY(y + totalHeight);
                        totalHeight += b.getHeight();
                    }
                }
            }
        }

        Renderer2D.renderQuad(context.getMatrices(), x, y, x + width, y + height, ClickGuiScreen.getButtonColor(y, 100));
        Sydney.FONT_MANAGER.drawTextWithShadow(context, category.getName(), x + textPadding, y + 2, Color.WHITE);

        if(showButtons) {
            Color color = Sydney.MODULE_MANAGER.getModule(ClickGuiModule.class).color.getColor();
            Renderer2D.renderQuad(context.getMatrices(), x, y + height, x + width, y + totalHeight + 1, Sydney.MODULE_MANAGER.getModule(ClickGuiModule.class).isRainbow() ? new Color(0, 0, 0, 100) : new Color((int) (color.getRed()*0.3), (int) (color.getGreen()*0.3), (int) (color.getBlue()*0.3), 100));
            for(Button button : buttons) {
                if(!button.isVisible()) continue;
                button.render(context, mouseX, mouseY, delta);
            }
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if(isHovering(mouseX, mouseY)) {
            if(button == 0) {
                dragging = true;
                dragX = (int) (mouseX - getX());
                dragY = (int) (mouseY - getY());
            } else if(button == 1) {
                open = !open;
            }
        }

        if(open || Sydney.CLICK_GUI.hasModuleSearchQuery()) {
            for(Button b : buttons) {
                if(!b.isVisible()) continue;
                b.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }

        for(Button b : buttons) {
            if(!b.isVisible()) continue;
            b.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (x <= mouseX && x + width > mouseX) {
            boolean whitelistHandling = false;
            for (Button b : buttons) {
                if (b instanceof ModuleButton moduleButton && moduleButton.isOpen()) {
                    List<Button> wbButtons = moduleButton.getButtons().stream().filter(button -> button instanceof WhitelistButton).toList();
                    for (Button whitelistButton : wbButtons) {
                        if (!moduleButton.isVisible()) continue;
                        if (whitelistButton instanceof WhitelistButton wb) {
                            if (wb.isHandlingScroll(mouseX, mouseY)) {
                                wb.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
                                whitelistHandling = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (!whitelistHandling) {
                if (verticalAmount < 0) {
                    setY(getY() - Sydney.MODULE_MANAGER.getModule(ClickGuiModule.class).scrollSpeed.getValue().intValue());
                } else if (verticalAmount > 0) {
                    setY(getY() + Sydney.MODULE_MANAGER.getModule(ClickGuiModule.class).scrollSpeed.getValue().intValue());
                }
            }
        }
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Button b : buttons) {
            if(!b.isVisible()) continue;
            b.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (open || Sydney.CLICK_GUI.hasModuleSearchQuery()) {
            for (Button button : buttons) {
                if(!button.isVisible()) continue;
                button.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    public void charTyped(char chr, int modifiers) {
        if (open || Sydney.CLICK_GUI.hasModuleSearchQuery()) {
            for (Button button : buttons) {
                if(!button.isVisible()) continue;
                button.charTyped(chr, modifiers);
            }
        }
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return x <= mouseX && y <= mouseY && x + width > mouseX && y + height > mouseY;
    }
}
