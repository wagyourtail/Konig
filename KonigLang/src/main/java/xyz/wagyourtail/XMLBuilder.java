package xyz.wagyourtail;

import java.util.*;

public class XMLBuilder {
    public static final int INLINE = 0b1;
    public static final int START_NEW_LINE = 0b10;
    public static final int SELF_CLOSING = 0b100;
    public final Map<String, String> options = new LinkedHashMap<>();
    public final List<Object> children = new ArrayList<>();
    public final String type;
    public boolean inline;
    public boolean startNewLine;
    public boolean selfClosing;

    public XMLBuilder(String type) {
        this.type = type;
        this.inline = false;
        this.startNewLine = true;
    }

    public XMLBuilder(String type, int flags) {
        this.type = type;
        this.inline = (flags & INLINE) != 0;
        this.startNewLine = !inline || (flags & START_NEW_LINE) != 0;
        this.selfClosing = (flags & SELF_CLOSING) != 0;
    }


    public XMLBuilder addOption(String key, String option) {
        options.put(key, option);
        return this;
    }

    public XMLBuilder addKeyOption(String option) {
        options.put(option, null);
        return this;
    }

    public XMLBuilder setId(String id) {
        return addStringOption("id", id);
    }

    public XMLBuilder addStringOption(String key, String option) {
        options.put(key, "\"" + option + "\"");
        return this;
    }

    public XMLBuilder setClass(String clazz) {
        return addStringOption("class", clazz);
    }

    public XMLBuilder append(Object... children) {
        this.children.addAll(Arrays.asList(children));
        return this;
    }

    public Object pop(int index) {
        return children.remove(index);
    }

    public Object pop() {
        return children.remove(children.size() - 1);
    }

    public String toString() {
        final StringBuilder builder = new StringBuilder("<").append(type);
        for (Map.Entry<String, String> option : options.entrySet()) {
            builder.append(" ").append(option.getKey());
            if (option.getValue() != null) {
                builder.append("=").append(option.getValue());
            }
        }
        if (children.isEmpty()) {
            if (selfClosing) {
                builder.append("/>");
            } else {
                builder.append(">");
                builder.append("</").append(type).append(">");
            }
        } else {
            builder.append(">");
            boolean inline = this.inline;
            for (Object rawChild : children) {
                if (rawChild instanceof XMLBuilder) {
                    XMLBuilder child = (XMLBuilder) rawChild;
                    builder.append((inline || child.inline) && !child.startNewLine ? "" : "\n    ")
                        .append(tabIn(child.toString(), child.inline));
                    inline = child.inline;
                } else if (rawChild != null) {
                    builder.append(inline ? "" : "\n    ").append(tabIn(rawChild.toString(), inline));
                    inline = this.inline;
                }
            }
            builder.append(this.inline ? "" : "\n").append("</").append(type).append(">");
        }
        return builder.toString();
    }

    private String tabIn(String string, boolean inline) {
        if (inline) {
            return string;
        }
        return string.replaceAll("\n", "\n    ");
    }

}