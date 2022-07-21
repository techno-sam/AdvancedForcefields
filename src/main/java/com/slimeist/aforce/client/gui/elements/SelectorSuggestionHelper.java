package com.slimeist.aforce.client.gui.elements;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class SelectorSuggestionHelper {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    private static final Style UNPARSED_STYLE = Style.EMPTY.withColor(TextFormatting.RED);
    private static final Style LITERAL_STYLE = Style.EMPTY.withColor(TextFormatting.GRAY);
    private static final List<Style> ARGUMENT_STYLES = Stream.of(TextFormatting.AQUA, TextFormatting.YELLOW, TextFormatting.GREEN, TextFormatting.LIGHT_PURPLE, TextFormatting.GOLD).map(Style.EMPTY::withColor).collect(ImmutableList.toImmutableList());
    private final Minecraft minecraft;
    private final Screen screen;
    private final TextFieldWidget input;
    private final FontRenderer font;
    private final int lineStartOffset;
    private final int suggestionLineLimit;
    private final boolean anchorToBottom;
    private final int fillColor;
    private final List<IReorderingProcessor> commandUsage = Lists.newArrayList();
    private int commandUsagePosition;
    private int commandUsageWidth;
    //private ParseResults<ISuggestionProvider> currentParse;
    private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> pendingSuggestions;
    private SelectorSuggestionHelper.Suggestions suggestions;
    private boolean allowSuggestions;
    private boolean keepSuggestions;
    private int yOffset = 0;

    public SelectorSuggestionHelper(Minecraft minecraft, Screen gui, TextFieldWidget input, FontRenderer font, int lineStartOffset, int suggestLineLimit, boolean anchorToBottom, int fillColor) {
        this.minecraft = minecraft;
        this.screen = gui;
        this.input = input;
        this.font = font;
        this.lineStartOffset = lineStartOffset;
        this.suggestionLineLimit = suggestLineLimit;
        this.anchorToBottom = anchorToBottom;
        this.fillColor = fillColor;
    }

    public void setYOffset(int y) {
        this.yOffset = y;
    }

    public int getYOffset() {
        return this.yOffset;
    }

    public void setAllowSuggestions(boolean allow) {
        this.allowSuggestions = allow;
        if (!allow) {
            this.suggestions = null;
        }

    }

    public boolean keyPressed(int key, int scancode, int p_228115_3_) {
        if (this.suggestions != null && this.suggestions.keyPressed(key, scancode, p_228115_3_)) {
            return true;
        } else if (this.screen.getFocused() == this.input && key == 258) {
            this.showSuggestions(true);
            return true;
        } else {
            return false;
        }
    }

    public boolean mouseScrolled(double amt) {
        return this.suggestions != null && this.suggestions.mouseScrolled(MathHelper.clamp(amt, -1.0D, 1.0D));
    }

    public boolean mouseClicked(double x, double y, int p_228113_5_) {
        return this.suggestions != null && this.suggestions.mouseClicked((int)x, (int)y, p_228113_5_);
    }

    private static final String[] banned_suggestions = new String[] {"distance=", "dx=", "dy=", "dz=", "@s"};

    private boolean shouldIgnoreSuggestion(Suggestion suggestion) {
        return Arrays.stream(banned_suggestions).anyMatch((string) -> string.equals(suggestion.getText()));
    }

    private boolean shouldIgnoreAllSuggestions(com.mojang.brigadier.suggestion.Suggestions suggestions) {
        for (Suggestion suggestion : suggestions.getList()) {
            if (!shouldIgnoreSuggestion(suggestion)) {
                return false;
            }
        }
        return true;
    }

    public void showSuggestions(boolean narrator_bool) {
        if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
            com.mojang.brigadier.suggestion.Suggestions suggestions = this.pendingSuggestions.join();
            if (!suggestions.isEmpty()) {
                if (shouldIgnoreAllSuggestions(suggestions))
                    return;
                int width = 0;

                for(Suggestion suggestion : suggestions.getList()) {
                    if (shouldIgnoreSuggestion(suggestion))
                        continue;
                    width = Math.max(width, this.font.width(suggestion.getText()));
                }

                int x = MathHelper.clamp(this.input.getScreenX(suggestions.getRange().getStart()), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - width);
                int y = this.anchorToBottom ? this.screen.height - 12 + this.yOffset : 72 + this.yOffset;
                this.suggestions = new SelectorSuggestionHelper.Suggestions(x, y, width, this.sortSuggestions(suggestions), narrator_bool);
            }
        }

    }

    private List<Suggestion> sortSuggestions(com.mojang.brigadier.suggestion.Suggestions p_241575_1_) {
        String s = this.input.getValue().substring(0, this.input.getCursorPosition());
        int i = getLastWordIndex(s);
        String s1 = s.substring(i).toLowerCase(Locale.ROOT);
        List<Suggestion> list = Lists.newArrayList();
        List<Suggestion> list1 = Lists.newArrayList();

        for(Suggestion suggestion : p_241575_1_.getList()) {
            if (shouldIgnoreSuggestion(suggestion))
                continue;
            if (!suggestion.getText().startsWith(s1) && !suggestion.getText().startsWith("minecraft:" + s1)) {
                list1.add(suggestion);
            } else {
                list.add(suggestion);
            }
        }

        list.addAll(list1);
        return list;
    }

    public void updateCommandInfo() {
        String s = this.input.getValue();
        /*if (this.currentParse != null && !this.currentParse.getReader().getString().equals(s)) {
            this.currentParse = null;
        }*/

        if (!this.keepSuggestions) {
            this.input.setSuggestion((String)null);
            this.suggestions = null;
        }

        this.commandUsage.clear();
        /*StringReader stringreader = new StringReader(s);
        boolean flag = stringreader.canRead() && stringreader.peek() == '/';
        if (flag) {
            stringreader.skip();
        }*/

        //boolean flag1 = this.commandsOnly || flag;
        int i = this.input.getCursorPosition();
        //CommandDispatcher<ISuggestionProvider> commanddispatcher = this.minecraft.player.connection.getCommands();
            /*if (this.currentParse == null) {
                this.currentParse = commanddispatcher.parse(stringreader, this.minecraft.player.connection.getSuggestionsProvider());
            }*/

        int j = 1;
        if (i >= j && (this.suggestions == null || !this.keepSuggestions)) {
            this.pendingSuggestions = MiscUtil.suggestionsFromSelector(s.substring(0, i), i).orElse(com.mojang.brigadier.suggestion.Suggestions.empty());//commanddispatcher.getCompletionSuggestions(this.currentParse, i);
            this.pendingSuggestions.thenRun(() -> {
                if (this.pendingSuggestions.isDone()) {
                    this.updateUsageInfo();
                }
            });
        }

    }

    private static int getLastWordIndex(String p_228121_0_) {
        if (Strings.isNullOrEmpty(p_228121_0_)) {
            return 0;
        } else {
            int i = 0;

            for(Matcher matcher = WHITESPACE_PATTERN.matcher(p_228121_0_); matcher.find(); i = matcher.end()) {
            }

            return i;
        }
    }

    private static IReorderingProcessor getExceptionMessage(CommandSyntaxException p_243255_0_) {
        ITextComponent itextcomponent = TextComponentUtils.fromMessage(p_243255_0_.getRawMessage());
        String s = p_243255_0_.getContext();
        return s == null ? itextcomponent.getVisualOrderText() : (new TranslationTextComponent("command.context.parse_error", itextcomponent, p_243255_0_.getCursor(), s)).getVisualOrderText();
    }

    private void updateUsageInfo() {

        this.commandUsagePosition = 0;
        this.commandUsageWidth = this.screen.width;

        this.suggestions = null;
        if (this.allowSuggestions && this.minecraft.options.autoSuggestions) {
            this.showSuggestions(false);
        }

    }

    @Nullable
    private static String calculateSuggestionSuffix(String p_228127_0_, String p_228127_1_) {
        return p_228127_1_.startsWith(p_228127_0_) ? p_228127_1_.substring(p_228127_0_.length()) : null;
    }

    private static IReorderingProcessor formatText(ParseResults<ISuggestionProvider> p_228116_0_, String p_228116_1_, int p_228116_2_) {
        List<IReorderingProcessor> list = Lists.newArrayList();
        int i = 0;
        int j = -1;
        CommandContextBuilder<ISuggestionProvider> commandcontextbuilder = p_228116_0_.getContext().getLastChild();

        for(ParsedArgument<ISuggestionProvider, ?> parsedargument : commandcontextbuilder.getArguments().values()) {
            ++j;
            if (j >= ARGUMENT_STYLES.size()) {
                j = 0;
            }

            int k = Math.max(parsedargument.getRange().getStart() - p_228116_2_, 0);
            if (k >= p_228116_1_.length()) {
                break;
            }

            int l = Math.min(parsedargument.getRange().getEnd() - p_228116_2_, p_228116_1_.length());
            if (l > 0) {
                list.add(IReorderingProcessor.forward(p_228116_1_.substring(i, k), LITERAL_STYLE));
                list.add(IReorderingProcessor.forward(p_228116_1_.substring(k, l), ARGUMENT_STYLES.get(j)));
                i = l;
            }
        }

        if (p_228116_0_.getReader().canRead()) {
            int i1 = Math.max(p_228116_0_.getReader().getCursor() - p_228116_2_, 0);
            if (i1 < p_228116_1_.length()) {
                int j1 = Math.min(i1 + p_228116_0_.getReader().getRemainingLength(), p_228116_1_.length());
                list.add(IReorderingProcessor.forward(p_228116_1_.substring(i, i1), LITERAL_STYLE));
                list.add(IReorderingProcessor.forward(p_228116_1_.substring(i1, j1), UNPARSED_STYLE));
                i = j1;
            }
        }

        list.add(IReorderingProcessor.forward(p_228116_1_.substring(i), LITERAL_STYLE));
        return IReorderingProcessor.composite(list);
    }

    public void render(MatrixStack p_238500_1_, int p_238500_2_, int p_238500_3_) {
        if (this.suggestions != null) {
            this.suggestions.render(p_238500_1_, p_238500_2_, p_238500_3_);
        } else {
            int i = 0;

            for(IReorderingProcessor ireorderingprocessor : this.commandUsage) {
                int j = this.anchorToBottom ? this.screen.height - 14 - 13 + this.yOffset - 12 * i : 72 + this.yOffset + 12 * i;
                AbstractGui.fill(p_238500_1_, this.commandUsagePosition - 1, j, this.commandUsagePosition + this.commandUsageWidth + 1, j + 12, this.fillColor);
                this.font.drawShadow(p_238500_1_, ireorderingprocessor, (float)this.commandUsagePosition, (float)(j + 2), -1);
                ++i;
            }
        }

    }

    public String getNarrationMessage() {
        return this.suggestions != null ? "\n" + this.suggestions.getNarrationMessage() : "";
    }

    @OnlyIn(Dist.CLIENT)
    public class Suggestions {
        private final Rectangle2d rect;
        private final String originalContents;
        private final List<Suggestion> suggestionList;
        private int offset;
        private int current;
        private Vector2f lastMouse = Vector2f.ZERO;
        private boolean tabCycles;
        private int lastNarratedEntry;

        private Suggestions(int x, int y, int width, List<Suggestion> suggestions, boolean narrated) {
            int i = x - 1;
            int j = SelectorSuggestionHelper.this.anchorToBottom ? y - 3 - Math.min(suggestions.size(), SelectorSuggestionHelper.this.suggestionLineLimit) * 12 : y;
            this.rect = new Rectangle2d(i, j, width + 1, Math.min(suggestions.size(), SelectorSuggestionHelper.this.suggestionLineLimit) * 12);
            this.originalContents = SelectorSuggestionHelper.this.input.getValue();
            this.lastNarratedEntry = narrated ? -1 : 0;
            this.suggestionList = suggestions;
            this.select(0);
        }

        public void render(MatrixStack transform, int mx, int my) {
            int num_lines = Math.min(this.suggestionList.size(), SelectorSuggestionHelper.this.suggestionLineLimit);
            int j = -5592406;
            boolean has_offset = this.offset > 0;
            boolean overflow = this.suggestionList.size() > this.offset + num_lines;
            boolean offset_or_overflow = has_offset || overflow;
            boolean mouse_moved = this.lastMouse.x != (float)mx || this.lastMouse.y != (float)my;
            if (mouse_moved) {
                this.lastMouse = new Vector2f((float)mx, (float)my);
            }

            if (offset_or_overflow) { //draw dotted 'extra' lines
                AbstractGui.fill(transform, this.rect.getX(), this.rect.getY() - 1, this.rect.getX() + this.rect.getWidth(), this.rect.getY(), SelectorSuggestionHelper.this.fillColor);
                AbstractGui.fill(transform, this.rect.getX(), this.rect.getY() + this.rect.getHeight(), this.rect.getX() + this.rect.getWidth(), this.rect.getY() + this.rect.getHeight() + 1, SelectorSuggestionHelper.this.fillColor);
                if (has_offset) {
                    for(int k = 0; k < this.rect.getWidth(); ++k) {
                        if (k % 2 == 0) {
                            AbstractGui.fill(transform, this.rect.getX() + k, this.rect.getY() - 1, this.rect.getX() + k + 1, this.rect.getY(), -1);
                        }
                    }
                }

                if (overflow) {
                    for(int i1 = 0; i1 < this.rect.getWidth(); ++i1) {
                        if (i1 % 2 == 0) {
                            AbstractGui.fill(transform, this.rect.getX() + i1, this.rect.getY() + this.rect.getHeight(), this.rect.getX() + i1 + 1, this.rect.getY() + this.rect.getHeight() + 1, -1);
                        }
                    }
                }
            }

            boolean selected = false;

            for(int line = 0; line < num_lines; ++line) {
                Suggestion suggestion = this.suggestionList.get(line + this.offset);
                AbstractGui.fill(transform, this.rect.getX(), this.rect.getY() + 12 * line, this.rect.getX() + this.rect.getWidth(), this.rect.getY() + 12 * line + 12, SelectorSuggestionHelper.this.fillColor);
                if (mx > this.rect.getX() && mx < this.rect.getX() + this.rect.getWidth() && my > this.rect.getY() + 12 * line && my < this.rect.getY() + 12 * line + 12) {
                    if (mouse_moved) {
                        this.select(line + this.offset);
                    }

                    selected = true;
                }

                SelectorSuggestionHelper.this.font.drawShadow(transform, suggestion.getText(), (float)(this.rect.getX() + 1), (float)(this.rect.getY() + 2 + 12 * line), line + this.offset == this.current ? -256 : -5592406);
            }

            if (selected) {
                Message message = this.suggestionList.get(this.current).getTooltip();
                if (message != null) {
                    SelectorSuggestionHelper.this.screen.renderTooltip(transform, TextComponentUtils.fromMessage(message), mx, my);
                }
            }

        }

        public boolean mouseClicked(int p_228150_1_, int p_228150_2_, int p_228150_3_) {
            if (!this.rect.contains(p_228150_1_, p_228150_2_)) {
                return false;
            } else {
                int i = (p_228150_2_ - this.rect.getY()) / 12 + this.offset;
                if (i >= 0 && i < this.suggestionList.size()) {
                    this.select(i);
                    this.useSuggestion();
                }

                return true;
            }
        }

        public boolean mouseScrolled(double p_228147_1_) {
            int i = (int)(SelectorSuggestionHelper.this.minecraft.mouseHandler.xpos() * (double) SelectorSuggestionHelper.this.minecraft.getWindow().getGuiScaledWidth() / (double) SelectorSuggestionHelper.this.minecraft.getWindow().getScreenWidth());
            int j = (int)(SelectorSuggestionHelper.this.minecraft.mouseHandler.ypos() * (double) SelectorSuggestionHelper.this.minecraft.getWindow().getGuiScaledHeight() / (double) SelectorSuggestionHelper.this.minecraft.getWindow().getScreenHeight());
            if (this.rect.contains(i, j)) {
                this.offset = MathHelper.clamp((int)((double)this.offset - p_228147_1_), 0, Math.max(this.suggestionList.size() - SelectorSuggestionHelper.this.suggestionLineLimit, 0));
                return true;
            } else {
                return false;
            }
        }

        public boolean keyPressed(int p_228154_1_, int p_228154_2_, int p_228154_3_) {
            if (p_228154_1_ == 265) {
                this.cycle(-1);
                this.tabCycles = false;
                return true;
            } else if (p_228154_1_ == 264) {
                this.cycle(1);
                this.tabCycles = false;
                return true;
            } else if (p_228154_1_ == 258) {
                if (this.tabCycles) {
                    this.cycle(Screen.hasShiftDown() ? -1 : 1);
                }

                this.useSuggestion();
                return true;
            } else if (p_228154_1_ == 256) {
                this.hide();
                return true;
            } else {
                return false;
            }
        }

        public void cycle(int p_228148_1_) {
            this.select(this.current + p_228148_1_);
            int i = this.offset;
            int j = this.offset + SelectorSuggestionHelper.this.suggestionLineLimit - 1;
            if (this.current < i) {
                this.offset = MathHelper.clamp(this.current, 0, Math.max(this.suggestionList.size() - SelectorSuggestionHelper.this.suggestionLineLimit, 0));
            } else if (this.current > j) {
                this.offset = MathHelper.clamp(this.current + SelectorSuggestionHelper.this.lineStartOffset - SelectorSuggestionHelper.this.suggestionLineLimit, 0, Math.max(this.suggestionList.size() - SelectorSuggestionHelper.this.suggestionLineLimit, 0));
            }

        }

        public void select(int p_228153_1_) {
            this.current = p_228153_1_;
            if (this.current < 0) {
                this.current += this.suggestionList.size();
            }

            if (this.current >= this.suggestionList.size()) {
                this.current -= this.suggestionList.size();
            }

            Suggestion suggestion = this.suggestionList.get(this.current);
            SelectorSuggestionHelper.this.input.setSuggestion(SelectorSuggestionHelper.calculateSuggestionSuffix(SelectorSuggestionHelper.this.input.getValue(), suggestion.apply(this.originalContents)));
            if (NarratorChatListener.INSTANCE.isActive() && this.lastNarratedEntry != this.current) {
                NarratorChatListener.INSTANCE.sayNow(this.getNarrationMessage());
            }

        }

        public void useSuggestion() {
            Suggestion suggestion = this.suggestionList.get(this.current);
            SelectorSuggestionHelper.this.keepSuggestions = true;
            SelectorSuggestionHelper.this.input.setValue(suggestion.apply(this.originalContents));
            int i = suggestion.getRange().getStart() + suggestion.getText().length();
            SelectorSuggestionHelper.this.input.setCursorPosition(i);
            SelectorSuggestionHelper.this.input.setHighlightPos(i);
            this.select(this.current);
            SelectorSuggestionHelper.this.keepSuggestions = false;
            this.tabCycles = true;
        }

        private String getNarrationMessage() {
            this.lastNarratedEntry = this.current;
            Suggestion suggestion = this.suggestionList.get(this.current);
            Message message = suggestion.getTooltip();
            return message != null ? I18n.get("narration.suggestion.tooltip", this.current + 1, this.suggestionList.size(), suggestion.getText(), message.getString()) : I18n.get("narration.suggestion", this.current + 1, this.suggestionList.size(), suggestion.getText());
        }

        public void hide() {
            SelectorSuggestionHelper.this.suggestions = null;
        }
    }
}
