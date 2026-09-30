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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

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

    public ClickGuiScreen() {
        super(Text.literal(Sydney.MOD_ID + "-click-gui"));

        int x = 6;
        for(Module.Category category : new Module.Category[]{Module.Category.COMBAT, Module.Category.CORE}) {
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
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for(Frame frame : frames) frame.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
        for (Frame frame : frames) {
            frame.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (Frame frame : frames) {
            frame.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if(Sydney.MODULE_MANAGER.getModule(ClickGuiModule.class).blur.getValue()) applyBlur(delta);
        Renderer2D.renderQuad(context.getMatrices(), 0, 0, this.width, this.height, new Color(0, 0, 0, 100));
    }

    @Override
    public void close() {
        super.close();
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
}
